package com.example.learnMongoRedis.service;

import com.example.learnMongoRedis.domain.StateModel.MatchResultState;
import com.example.learnMongoRedis.domain.model.PlayerOfMonthly;
import com.example.learnMongoRedis.domain.StateModel.SimulationData;
import com.example.learnMongoRedis.domain.StateModel.UpdateMatchOutcome;
import com.example.learnMongoRedis.domain.model.Player;
import com.example.learnMongoRedis.domain.model.SeasonInTeam;
import com.example.learnMongoRedis.domain.model.Team;
import com.example.learnMongoRedis.domain.model.match.*;
import com.example.learnMongoRedis.global.DummyMatchUtils;
import com.example.learnMongoRedis.global.error_handler.AppError;
import com.example.learnMongoRedis.repository.PlayerRepository;
import com.example.learnMongoRedis.repository.SeasonRepository;
import com.example.learnMongoRedis.repository.TeamRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@Service
@Log4j2
public class SimulationMatch {

    private final TeamRepository teamRepository;
    private final PlayerRepository playerRepository;
    private final SeasonRepository seasonRepository;
    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    public SimulationMatch(TeamRepository teamRepository, PlayerRepository playerRepository, SeasonRepository seasonRepository, MongoTemplate mongoTemplate) {
        this.teamRepository = teamRepository;
        this.playerRepository = playerRepository;
        this.seasonRepository = seasonRepository;
        this.mongoTemplate = mongoTemplate;
    }

    @Transactional(readOnly = true)
    public SimulationData fetchSimulationData() {
        List<Team> teams = teamRepository.findAll();
        Season season = getCurrentSeason();
        return new SimulationData(teams, season);
    }


    public void runSimulation() {
        SimulationData simulationData = fetchSimulationData();
        List<Match> matches = new ArrayList<>();
        List<Team> teams = simulationData.getTeams();
        Season season = simulationData.getSeason();

        int totalTeams = teams.size();  // 총 팀수
        int matchesPerRound = totalTeams / 2; // 라운드 당 경기 수
        int roundCount = season.getRoundCount();

        for (int match = 0; match < matchesPerRound; match++) {
            // 매치 팀 인덱스 설정
            int homeTeamIndex = (roundCount % (totalTeams - 1) + match) % (totalTeams - 1);
            int awayTeamIndex = ((totalTeams - 1) - match + roundCount % (totalTeams - 1)) % (totalTeams - 1);

            // 한 팀(맨 끝 인덱스인 팀 고정)
            if (match == 0) {
                awayTeamIndex = totalTeams - 1;
            }

            // 절반 라운드가 진행되면 1라운드의 홈,어웨이만 바꿔서 같은 방식으로 진행
            if (roundCount >= totalTeams - 1) {
                int temp = homeTeamIndex;
                homeTeamIndex = awayTeamIndex;
                awayTeamIndex = temp;
            }

            Team homeTeam = teams.get(homeTeamIndex);
            Team awayTeam = teams.get(awayTeamIndex);

            TeamStat homeStat = DummyMatchUtils.generateTeamStat(homeTeam.getId(), homeTeam.getClubName());
            TeamStat awayStat = DummyMatchUtils.generateTeamStat(awayTeam.getId(), awayTeam.getClubName());

            matches.add(createMatch(
                    homeStat,
                    awayStat,
                    homeTeam.getHomeStadium()
            ));
        }

        Round round = createRound(roundCount + 1, matches);
        saveSimulationResults(season, round, teams);
    }

    @Transactional
    public void saveSimulationResults(Season season, Round round, List<Team> teams) {
        addRoundInSeason(season.getId(), round);
        updateSeasonInTeam(teams, round, season.getSeason());
    }

    private void addRoundInSeason(String seasonId, Round round) {
        Query query = new Query(Criteria.where("id").is(seasonId));
        Update update = new Update().inc("roundCount", 1).push("roundList", round);
        mongoTemplate.findAndModify(query, update, Season.class);
    }

    private Season getCurrentSeason() {
        Season currentSeason = seasonRepository.findTopByOrderByIdDesc().orElseGet(() -> createSeason(2024));
        if (currentSeason.getRoundCount() >= 29) {
            return createSeason(currentSeason.getSeason() + 1);
        } else {
            return currentSeason;
        }
    }

    private Season createSeason(int seasonNumber) {
        Season season = Season.builder()
                .season(seasonNumber)
                .roundList(List.of())
                .roundCount(0)
                .build();

        seasonRepository.save(season);
        return season;
    }

    private Match createMatch(TeamStat home, TeamStat away, String stadium) {
        return new Match(
                LocalDateTime.now().toString(),
                stadium,
                home,
                away
        );
    }

    private Round createRound(int round, List<Match> matches) {
        return Round.builder()
                .round(round)
                .matches(matches)
                .build();
    }

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

            if ((round.getRound() + 1) % 4 == 0) {
                saveTopPlayersMonthlyScore();
            }
        }));
    }

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

    public void addSeasonToTeam(String teamId, SeasonInTeam newSeason) {
        Query query = new Query(Criteria.where("id").is(teamId));
        Update update = new Update().push("seasons", newSeason);
        mongoTemplate.updateFirst(query, update, Team.class);
    }

    public UpdateMatchOutcome makeUpdateMatchOutcome(TeamStat home, TeamStat away, boolean isHome) {

        int homeGoals = home.goals.size();
        int awayGoals = away.goals.size();
        if (isHome) {
            return new UpdateMatchOutcome(homeGoals, awayGoals, MatchResultState.convertMatchResultState(homeGoals, awayGoals));
        } else {
            return new UpdateMatchOutcome(awayGoals, homeGoals, MatchResultState.convertMatchResultState(awayGoals, homeGoals));
        }
    }

    public void updateMatchResultOutcome(Team team, int seasonIdx, UpdateMatchOutcome updateMatchOutcome) {
        Query query = new Query(Criteria.where("_id").is(team.getId()));
        Update update = new Update();
        String baseUpdatePath = "seasons." + seasonIdx + ".";
        update.inc(baseUpdatePath + "totalGoal", updateMatchOutcome.getGoal());
        update.inc(baseUpdatePath + "totalConceded", updateMatchOutcome.getConceded());

        String fieldToUpdate = baseUpdatePath + "wins"; // Default to wins
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

    // 추가해야할 로직 이달의 선수 이달의 팀 선수 업데이트
    private void updatePlayerStatsForMatch(TeamStat home, TeamStat away) {
        List<Goal> goals = Stream.concat(home.getGoals().stream(), away.getGoals().stream()).toList();
        log.error("#@#@$@#$#@$@#$@#$@#$@#$@#$@#$");
        log.error(goals.size());
        log.error(goals.toString());
        goals.forEach(this::updatePlayerStatsForGoal);
    }

    private void updatePlayerStatsForGoal(Goal goal) {
        if (goal.getGoalPlayerId() != null && !goal.getGoalPlayerId().isEmpty()) {
            // 득점자의 통계 업데이트
            Query queryGoalPlayer = new Query(Criteria.where("id").is(goal.getGoalPlayerId()));
            Update updateGoalPlayer = new Update()
                    .inc("totalGoalsScored", 1)
                    .inc("monthlyGoal", 1)
                    .inc("goal", 1);
            mongoTemplate.updateFirst(queryGoalPlayer, updateGoalPlayer, Player.class);
        }

        if (goal.getAssistPlayerId() != null && !goal.getAssistPlayerId().isEmpty()) {
            // 어시스트 제공자의 통계 업데이트
            Query queryAssistPlayer = new Query(Criteria.where("id").is(goal.getAssistPlayerId()));
            Update updateAssistPlayer = new Update()
                    .inc("totalAssists", 1)
                    .inc("monthlyAssists", 1)
                    .inc("assist", 1);
            mongoTemplate.updateFirst(queryAssistPlayer, updateAssistPlayer, Player.class);
        }
    }

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

    public void resetMonthlyStats() {
        Update update = new Update().set("monthlyGoal", 0).set("monthlyAssists", 0);
        mongoTemplate.updateMulti(new Query(), update, Player.class);
    }

}
