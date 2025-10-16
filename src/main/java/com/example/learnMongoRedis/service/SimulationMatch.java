package com.example.learnMongoRedis.service;

import com.example.learnMongoRedis.domain.StateModel.MatchResultState;
import com.example.learnMongoRedis.domain.StateModel.SimulationData;
import com.example.learnMongoRedis.domain.StateModel.UpdateMatchOutcome;
import com.example.learnMongoRedis.domain.model.*;
import com.example.learnMongoRedis.domain.model.match.Round;
import com.example.learnMongoRedis.domain.model.match.*;
import com.example.learnMongoRedis.global.error_handler.AppError;
import com.example.learnMongoRedis.repository.SeasonRepository;
import com.example.learnMongoRedis.repository.TeamRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Stream;

@Service
@Log4j2
public class SimulationMatch {

    // 시뮬레이션 스탯 확률 상수
    private static final double HOME_ADVANTAGE = 0.9;
    private static final double AWAY_ADVANTAGE = 1.1;
    private static final double CORNER_KICK_PROBABILITY = 0.10;
    private static final double SHOOTING_PROBABILITY = 0.20;
    private static final double EFFECTIVE_SHOT_PROBABILITY = 0.30;
    private static final double GOAL_PROBABILITY = 0.40;
    private static final double STRIKER_GOAL_PROBABILITY = 0.55;
    private static final double MIDFIELDER_GOAL_PROBABILITY = 0.90;
    private static final int MATCH_DURATION_MINUTES = 90;
    private static final int MAX_POSSESSION_PERCENTAGE = 100;

    // 포지션 상수
    private static final int POSITION_STRIKER = 1;
    private static final int POSITION_MIDFIELDER = 2;
    private static final int POSITION_DEFENDER = 3;

    private final TeamRepository teamRepository;
    private final SeasonRepository seasonRepository;
    private final MongoTemplate mongoTemplate;

    @Autowired
    public SimulationMatch(TeamRepository teamRepository, SeasonRepository seasonRepository, MongoTemplate mongoTemplate) {
        this.teamRepository = teamRepository;
        this.seasonRepository = seasonRepository;
        this.mongoTemplate = mongoTemplate;
    }

    @Transactional(readOnly = true)
    public SimulationData fetchSimulationData() {
        List<Team> teams = teamRepository.findAll();
        Season season = getCurrentSeason();
        return new SimulationData(teams, season);
    }

    @Scheduled(fixedRate = 5000)
    public void runSimulation() {
        SimulationData simulationData = fetchSimulationData();
        List<Team> teams = simulationData.getTeams();
        Season season = simulationData.getSeason();

        List<Match> matches = simulateRoundMatches(teams, season);
        Round round = createRound(season.getRoundCount() + 1, matches);
        saveSimulationResults(season, round, teams);
    }

    /**
     * 라운드의 모든 경기를 시뮬레이션
     */
    private List<Match> simulateRoundMatches(List<Team> teams, Season season) {
        List<Match> matches = new ArrayList<>();
        int totalTeams = teams.size();
        int matchesPerRound = totalTeams / 2;
        int roundCount = season.getRoundCount();

        for (int matchIndex = 0; matchIndex < matchesPerRound; matchIndex++) {
            TeamPair teamPair = determineMatchTeams(teams, totalTeams, roundCount, matchIndex);
            Match match = simulateSingleMatch(teamPair.home, teamPair.away);
            matches.add(match);
        }

        return matches;
    }

    /**
     * 라운드 로빈 방식으로 매치할 팀을 결정
     */
    private TeamPair determineMatchTeams(List<Team> teams, int totalTeams, int roundCount, int matchIndex) {
        int homeTeamIndex = (roundCount % (totalTeams - 1) + matchIndex) % (totalTeams - 1);
        int awayTeamIndex = ((totalTeams - 1) - matchIndex + roundCount % (totalTeams - 1)) % (totalTeams - 1);

        // 한 팀은 고정
        if (matchIndex == 0) {
            awayTeamIndex = totalTeams - 1;
        }

        // 후반 라운드에서는 홈/어웨이 전환
        if (roundCount >= totalTeams - 1) {
            int temp = homeTeamIndex;
            homeTeamIndex = awayTeamIndex;
            awayTeamIndex = temp;
        }

        return new TeamPair(teams.get(homeTeamIndex), teams.get(awayTeamIndex));
    }

    /**
     * 단일 경기를 시뮬레이션
     */
    private Match simulateSingleMatch(Team homeTeam, Team awayTeam) {
        Map<Integer, List<PlayerInTeam>> homePositionPlayers = chunkPlayersToPosition(homeTeam.getPlayers());
        Map<Integer, List<PlayerInTeam>> awayPositionPlayers = chunkPlayersToPosition(awayTeam.getPlayers());

        MatchStats homeStats = simulateTeamPerformance(homeTeam, homePositionPlayers, HOME_ADVANTAGE);
        MatchStats awayStats = simulateTeamPerformance(awayTeam, awayPositionPlayers, AWAY_ADVANTAGE);

        int[] possessions = calculatePossessions(homeTeam.teamOverallAvg(), awayTeam.teamOverallAvg());

        TeamStat homeStat = createTeamStat(homeTeam, homeStats, possessions[0]);
        TeamStat awayStat = createTeamStat(awayTeam, awayStats, possessions[1]);

        return createMatch(homeStat, awayStat, homeTeam.getHomeStadium());
    }

    /**
     * 팀의 경기 통계를 시뮬레이션
     */
    private MatchStats simulateTeamPerformance(Team team, Map<Integer, List<PlayerInTeam>> positionPlayers, double advantage) {
        Random random = new Random();
        MatchStats stats = new MatchStats();
        int overall = team.teamOverallAvg();
        double ratio = advantage * (-0.002 * overall + 1.2); // 팀 오버롤에 따른 보정 비율

        for (int minute = 0; minute < MATCH_DURATION_MINUTES; minute++) {
            // 코너킥 시뮬레이션
            if (random.nextDouble() < CORNER_KICK_PROBABILITY) {
                stats.cornerKicks++;
            }

            // 슈팅 시뮬레이션
            if (random.nextDouble() * ratio < SHOOTING_PROBABILITY) {
                stats.shots++;

                // 유효 슈팅 시뮬레이션
                if (random.nextDouble() * ratio < EFFECTIVE_SHOT_PROBABILITY) {
                    stats.effectiveShots++;

                    // 골 시뮬레이션
                    if (random.nextDouble() * ratio < GOAL_PROBABILITY) {
                        Goal goal = generateGoal(random, positionPlayers, minute);
                        stats.goals.add(goal);
                    }
                }
            }
        }

        return stats;
    }

    /**
     * 골을 생성
     */
    private Goal generateGoal(Random random, Map<Integer, List<PlayerInTeam>> positionPlayers, int minute) {
        double scorer = random.nextDouble();

        if (scorer < STRIKER_GOAL_PROBABILITY) {
            return createGoalFromPosition(random, positionPlayers.get(POSITION_STRIKER), minute);
        } else if (scorer < MIDFIELDER_GOAL_PROBABILITY) {
            return createGoalFromPosition(random, positionPlayers.get(POSITION_MIDFIELDER), minute);
        } else {
            return createGoalFromPosition(random, positionPlayers.get(POSITION_DEFENDER), minute);
        }
    }

    /**
     * 특정 포지션의 선수로부터 골을 생성
     */
    private Goal createGoalFromPosition(Random random, List<PlayerInTeam> players, int minute) {
        if (players == null || players.isEmpty()) {
            throw new AppError.Unexpected.NullPointerException("해당 포지션에 선수가 없습니다.");
        }

        int goalIndex = random.nextInt(players.size());
        int assistIndex = random.nextInt(players.size());

        PlayerInTeam goalPlayer = players.get(goalIndex);
        PlayerInTeam assistPlayer = players.get(assistIndex);

        return new Goal(
            minute,
            goalPlayer.get_id(),
            goalPlayer.getName(),
            assistPlayer.get_id(),
            assistPlayer.getName()
        );
    }

    /**
     * 팀 오버롤을 기반으로 점유율을 계산
     */
    private int[] calculatePossessions(int homeOverall, int awayOverall) {
        Random random = new Random();
        int home = 0;
        int away = 0;
        int totalRandomizations = homeOverall + awayOverall;

        for (int i = 0; i < MATCH_DURATION_MINUTES; i++) {
            int randomInt = random.nextInt(totalRandomizations);
            if (homeOverall >= randomInt) {
                home++;
            } else {
                away++;
            }
        }

        double totalPercentage = home + away;
        if (totalPercentage > MAX_POSSESSION_PERCENTAGE) {
            double ratio = MAX_POSSESSION_PERCENTAGE / totalPercentage;
            home = (int) Math.ceil(home * ratio);
            away = (int) (away * ratio);
        }

        return new int[]{home, away};
    }

    /**
     * TeamStat 객체를 생성
     */
    private TeamStat createTeamStat(Team team, MatchStats stats, int possession) {
        return new TeamStat(
            team.getId(),
            team.getClubName(),
            stats.goals,
            stats.shots,
            stats.effectiveShots,
            stats.cornerKicks,
            possession
        );
    }

    /**
     * 경기 통계를 담는 내부 클래스
     */
    private static class MatchStats {
        int shots = 0;
        int effectiveShots = 0;
        int cornerKicks = 0;
        List<Goal> goals = new ArrayList<>();
    }

    /**
     * 홈팀과 어웨이팀 쌍을 담는 record
     */
    private record TeamPair(Team home, Team away) {}

    @Transactional
    public void saveSimulationResults(Season season, Round round, List<Team> teams) {
        addRoundInSeason(season.getId(), round);
        updateSeasonInTeam(teams, round, season.getSeason());
        if ((round.getRound()) % 4 == 0) saveTopPlayersMonthlyScore();
        if ((round.getRound()) % 30 == 0) resetSeasonsStats();
    }

    /**
     * 포지션별로 선수들을 분류
     */
    private Map<Integer, List<PlayerInTeam>> chunkPlayersToPosition(List<PlayerInTeam> players) {
        Map<Integer, List<PlayerInTeam>> divisionToPosition = new HashMap<>();

        for (PlayerInTeam player : players) {
            divisionToPosition.computeIfAbsent(player.getPosition(), k -> new ArrayList<>()).add(player);
        }

        return divisionToPosition;
    }

    /**
     * 시즌에 라운드를 추가
     */
    private void addRoundInSeason(String seasonId, Round round) {
        Query query = new Query(Criteria.where("id").is(seasonId));
        Update update = new Update().inc("roundCount", 1).push("roundList", round);
        mongoTemplate.findAndModify(query, update, Season.class);
    }

    private Season getCurrentSeason() {
        Season currentSeason = seasonRepository.findTopByOrderByIdDesc().orElseGet(() -> createSeason(2024));
        if (currentSeason.getRoundCount() >= 30) {
            return createSeason(currentSeason.getSeason() + 1);
        } else {
            return currentSeason;
        }
    }

    /**
     * 새로운 시즌 생성
     */
    private Season createSeason(int seasonNumber) {
        Season season = Season.builder()
                .season(seasonNumber)
                .roundList(List.of())
                .roundCount(0)
                .build();

        seasonRepository.save(season);
        return season;
    }

    /**
     * 단일 매치 객체 생성
     */
    private Match createMatch(TeamStat home, TeamStat away, String stadium) {
        return new Match(
                LocalDateTime.now().toString(),
                stadium,
                home,
                away
        );
    }

    /**
     * 단일 라운드 객체 생성
     */
    private Round createRound(int round, List<Match> matches) {
        return Round.builder()
                .round(round)
                .matches(matches)
                .build();
    }

    /**
     * 팀의 시즌 기록 업데이트
     */
    private void updateSeasonInTeam(List<Team> teamList, Round round, int season) {
        round.getMatches().forEach((match -> {
            TeamStat homeStat = match.homeTeam;
            TeamStat awayStat = match.awayTeam;
            Team homeTeam = null;
            Team awayTeam = null;

            for (Team team : teamList) {
                if (homeStat.teamId.equals(team.getId())) homeTeam = team;
                if (awayStat.teamId.equals(team.getId())) awayTeam = team;
            }

            if (homeTeam == null || awayTeam == null)
                throw new AppError.Unexpected.NullPointerException("팀을 찾을 수 없습니다.");

            List<SeasonInTeam> homeSeasons = homeTeam.getSeasons();
            List<SeasonInTeam> awaySeasons = awayTeam.getSeasons();

            if (homeSeasons.isEmpty() || homeSeasons.get(homeSeasons.size() - 1).getSeason() != season) {
                homeSeasons.add(createSeasonInTeam(homeTeam.getId(), season));
            }
            if (awaySeasons.isEmpty() || awaySeasons.get(awaySeasons.size() - 1).getSeason() != season) {
                awaySeasons.add(createSeasonInTeam(awayTeam.getId(), season));
            }

            updateMatchResultOutcome(homeTeam, homeSeasons.size() - 1, makeUpdateMatchOutcome(homeStat, awayStat, true));
            updateMatchResultOutcome(awayTeam, awaySeasons.size() - 1, makeUpdateMatchOutcome(homeStat, awayStat, false));
            updatePlayerStatsForMatch(homeStat, awayStat);
        }));
    }

    /**
     * 팀에 새로운 시즌 기록 생성 및 추가
     */
    private SeasonInTeam createSeasonInTeam(String teamId, int season) {
        SeasonInTeam seasonInTeam = SeasonInTeam
                .builder()
                .season(season)
                .wins(0)
                .draws(0)
                .lose(0)
                .totalGoal(0)
                .totalConceded(0)
                .build();
        addSeasonToTeam(teamId, seasonInTeam);
        return seasonInTeam;
    }

    /**
     * 팀에 시즌 기록 추가
     */
    public void addSeasonToTeam(String teamId, SeasonInTeam newSeason) {
        Query query = new Query(Criteria.where("id").is(teamId));
        Update update = new Update().push("seasons", newSeason);
        mongoTemplate.updateFirst(query, update, Team.class);
    }

    /**
     * 매치 결과를 기반으로 UpdateMatchOutcome 객체 생성
     */
    public UpdateMatchOutcome makeUpdateMatchOutcome(TeamStat home, TeamStat away, boolean isHome) {
        int homeGoals = home.goals.size();
        int awayGoals = away.goals.size();
        if (isHome) {
            return new UpdateMatchOutcome(homeGoals, awayGoals, MatchResultState.convertMatchResultState(homeGoals, awayGoals));
        } else {
            return new UpdateMatchOutcome(awayGoals, homeGoals, MatchResultState.convertMatchResultState(awayGoals, homeGoals));
        }
    }

    /**
     * 팀의 시즌 기록 업데이트
     */
    public void updateMatchResultOutcome(Team team, int seasonIdx, UpdateMatchOutcome updateMatchOutcome) {
        Query query = new Query(Criteria.where("_id").is(team.getId()));
        Update update = new Update();
        String baseUpdatePath = "seasons." + seasonIdx + ".";
        update.inc(baseUpdatePath + "totalGoal", updateMatchOutcome.getGoal());
        update.inc(baseUpdatePath + "totalConceded", updateMatchOutcome.getConceded());

        String fieldToUpdate = baseUpdatePath + "wins";
        switch (updateMatchOutcome.getMatchResultState()) {
            case DRAW:
                fieldToUpdate = baseUpdatePath + "draws";
                break;
            case LOSE:
                fieldToUpdate = baseUpdatePath + "lose";
                break;
            default:
                break;
        }

        update.inc(fieldToUpdate, 1);
        mongoTemplate.updateFirst(query, update, Team.class);
    }

    /**
     * 매치에서 발생한 골을 기반으로 선수 기록 업데이트
     */
    private void updatePlayerStatsForMatch(TeamStat home, TeamStat away) {
        List<Goal> goals = Stream.concat(home.getGoals().stream(), away.getGoals().stream()).toList();
        goals.forEach(this::updatePlayerStatsForGoal);
    }

    /**
     * 단일 골에 대해 선수 기록 업데이트
     */
    private void updatePlayerStatsForGoal(Goal goal) {
        if (goal.getGoalPlayerId() != null && !goal.getGoalPlayerId().isEmpty()) {
            Query queryGoalPlayer = new Query(Criteria.where("id").is(goal.getGoalPlayerId()));
            Update updateGoalPlayer = new Update()
                    .inc("totalGoalsScored", 1)
                    .inc("monthlyGoal", 1)
                    .inc("goal", 1);
            mongoTemplate.updateFirst(queryGoalPlayer, updateGoalPlayer, Player.class);
        }

        if (goal.getAssistPlayerId() != null && !goal.getAssistPlayerId().isEmpty()) {
            Query queryAssistPlayer = new Query(Criteria.where("id").is(goal.getAssistPlayerId()));
            Update updateAssistPlayer = new Update()
                    .inc("totalAssists", 1)
                    .inc("monthlyAssists", 1)
                    .inc("assist", 1);
            mongoTemplate.updateFirst(queryAssistPlayer, updateAssistPlayer, Player.class);
        }
    }

    /**
     * 월간 최고 선수 점수를 계산하고 저장
     */
    public void saveTopPlayersMonthlyScore() {
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.project("id", "name", "age", "monthlyGoal", "monthlyAssists", "totalGoalsScored", "totalAssists", "teamId", "overall")
                        .andExpression("monthlyGoal * 10 + monthlyAssists * 5").as("monthlyScore"),
                Aggregation.sort(Sort.Direction.DESC, "monthlyScore"),
                Aggregation.limit(11),
                Aggregation.out("playerOfMonthly")
        );

        mongoTemplate.aggregate(aggregation, "players", PlayerOfMonthly.class);
        resetMonthlyStats();
    }

    /**
     * 모든 선수의 월간 기록 초기화
     */
    public void resetMonthlyStats() {
        Update update = new Update().set("monthlyGoal", 0).set("monthlyAssists", 0);
        mongoTemplate.updateMulti(new Query(), update, Player.class);
    }

    /**
     * 모든 선수의 시즌 기록 초기화
     */
    public void resetSeasonsStats() {
        Update update = new Update().set("goal", 0).set("assist", 0);
        mongoTemplate.updateMulti(new Query(), update, Player.class);
    }
}

