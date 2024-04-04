package com.example.learnMongoRedis.service;

import com.example.learnMongoRedis.domain.StateModel.MatchResultState;
import com.example.learnMongoRedis.domain.model.PlayerOfMonthly;
import com.example.learnMongoRedis.domain.StateModel.SimulationData;
import com.example.learnMongoRedis.domain.StateModel.UpdateMatchOutcome;
import com.example.learnMongoRedis.domain.model.Player;
import com.example.learnMongoRedis.domain.model.SeasonInTeam;
import com.example.learnMongoRedis.domain.model.Player;
import com.example.learnMongoRedis.domain.model.PlayerInTeam;
import com.example.learnMongoRedis.domain.model.match.*;
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
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Stream;

@Service
@Log4j2
public class SimulationMatch {

    private final TeamRepository teamRepository;
    private final PlayerRepository playerRepository;
    private final SeasonRepository seasonRepository;
    @Autowired
    private final MongoTemplate mongoTemplate;
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
        Random random = new Random();
        List<Match> matches = new ArrayList<>();
        List<Team> teams = simulationData.getTeams();
        Season season = simulationData.getSeason();

        int totalTeams = teams.size();  // 총 팀수
        int matchesPerRound = totalTeams / 2; // 라운드 당 경기 수
        int roundCount = season.getRoundCount();



        // 홈 팀, 어웨이 팀
        double Advantage [] = {0.9,1.1};

        //팀 정보에서 선수들 오버롤 들을 더해서 평균값 내기(팀 오버롤)

        for (int match = 0; match < matchesPerRound; match++) {

            int[] shots = {0,0};
            int[] effectiveShots = {0,0};
            int[] goals = {0,0};
            int[] connerKicks = {0,0};
            ArrayList<Goal> homeGoals = new ArrayList<>();
            ArrayList<Goal> awayGoals = new ArrayList<>();
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

            List<PlayerInTeam> homeTeamPlayers = homeTeam.getPlayers();
            log.error("192837491827349182734");
            Map<Integer,List<PlayerInTeam>> homePositionPlayers = chunkPlayersToPosition(homeTeamPlayers);
            log.error("*************9898989*******************************");
            log.error(homePositionPlayers.toString());
            log.error("*********9898989***********************************");

            List<PlayerInTeam> awayTeamPlayers = awayTeam.getPlayers();
            Map<Integer,List<PlayerInTeam>> awayPositionPlayers = chunkPlayersToPosition(awayTeamPlayers);

            double overall []  = {homeTeam.teamOverallAvg(),awayTeam.teamOverallAvg()};

            // 팀 배분 여기서 끝
            // 여기서 90분 돌려서 가상의 결과 데이터 goalSimulation => Match
            // Match class 만든다

            // 슛->골
            for(int i=0;i<2;i++){ // 홈팀 어웨이팀 2번
                double ratio = Advantage[i] * (-0.002*overall[i]+1.2);
                for (int minute = 0; minute < 90; minute++) {
                    if (random.nextDouble()<0.10) connerKicks[i]++;
                    if (random.nextDouble() * ratio < 0.20) { // 20% 확률로 슈팅
                        shots[i]++;
                        if (random.nextDouble() * ratio < 0.30) { // 슈팅 중 30%은 유효슈팅
                            effectiveShots[i]++;
                            if (random.nextDouble() * ratio < 0.40) { // 유효슈팅 중 40%은 골
                                goals[i]++;
                                double scorer = random.nextDouble();
                                if (scorer < 0.55) { // 55% 확률로 공격수가 골
                                    // 골 넣은 선수 및 어시스트 선수 선정하기
                                    // 골은 해당 포지션의 선수들 중 하나, 어시스트는 골 넣은 선수 제외한 나머지
                                    if(i==0){ // 홈 팀
                                        int goalIndex = random.nextInt(homePositionPlayers.get(1).size());
                                        int assistIndex = random.nextInt(homePositionPlayers.get(1).size());
                                        List<PlayerInTeam> attackPlayers = homePositionPlayers.get(1);
                                        PlayerInTeam goalPlayer = attackPlayers.get(goalIndex);
                                        PlayerInTeam assistPlayer = attackPlayers.get(assistIndex);
                                        log.error("********************************************");
                                        log.error(attackPlayers.toString());
                                        log.error("********************************************");
                                        homeGoals.add(new Goal(
                                                minute,
                                                goalPlayer.get_id(),
                                                goalPlayer.getName(),
                                                assistPlayer.get_id(),
                                                assistPlayer.getName()
                                        ));
                                    }else{ // 어웨이 팀
                                        int goalIndex = random.nextInt(awayPositionPlayers.get(1).size());
                                        int assistIndex = random.nextInt(awayPositionPlayers.get(1).size());
                                        List<PlayerInTeam> attackPlayers = awayPositionPlayers.get(1);
                                        PlayerInTeam goalPlayer = attackPlayers.get(goalIndex);
                                        PlayerInTeam assistPlayer = attackPlayers.get(assistIndex);
                                        awayGoals.add(new Goal(
                                                minute,
                                                goalPlayer.get_id(),
                                                goalPlayer.getName(),
                                                assistPlayer.get_id(),
                                                assistPlayer.getName()
                                        ));
                                    }
                                } else if (scorer < 0.90) { // 추가 35% 확률로 미드필더가 골
                                    // 골 넣은 선수 및 어시스트 선수 선정하기
                                    // 팀에 해당하는 선수(팀 아이디를 불러오기)
                                    // 골은 해당 포지션의 선수들 중 하나, 어시스트는 골 넣은 선수 제외한 나머지
                                    if(i==0){ // 홈 팀
                                        int goalIndex = random.nextInt(homePositionPlayers.get(2).size());
                                        int assistIndex = random.nextInt(homePositionPlayers.get(2).size());
                                        List<PlayerInTeam> midPlayers = homePositionPlayers.get(2);
                                        PlayerInTeam goalPlayer = midPlayers.get(goalIndex);
                                        PlayerInTeam assistPlayer = midPlayers.get(assistIndex);
                                        homeGoals.add(new Goal(
                                                minute,
                                                goalPlayer.get_id(),
                                                goalPlayer.getName(),
                                                assistPlayer.get_id(),
                                                assistPlayer.getName()
                                        ));
                                    }else{ // 어웨이 팀
                                        int goalIndex = random.nextInt(awayPositionPlayers.get(2).size());
                                        int assistIndex = random.nextInt(awayPositionPlayers.get(2).size());
                                        List<PlayerInTeam> midPlayers = awayPositionPlayers.get(2);
                                        PlayerInTeam goalPlayer = midPlayers.get(goalIndex);
                                        PlayerInTeam assistPlayer = midPlayers.get(assistIndex);
                                        awayGoals.add(new Goal(
                                                minute,
                                                goalPlayer.get_id(),
                                                goalPlayer.getName(),
                                                assistPlayer.get_id(),
                                                assistPlayer.getName()
                                        ));
                                    }
                                } else {
                                    // 나머지 확률로 수비수가 골
                                    // 골은 해당 포지션의 선수들 중 하나, 어시스트는 골 넣은 선수 제외한 나머지
                                    if(i==0){ // 홈 팀
                                        int goalIndex = random.nextInt(homePositionPlayers.get(3).size());
                                        int assistIndex = random.nextInt(homePositionPlayers.get(3).size());
                                        List<PlayerInTeam> defendPlayers = homePositionPlayers.get(3);
                                        PlayerInTeam goalPlayer = defendPlayers.get(goalIndex);
                                        PlayerInTeam assistPlayer = defendPlayers.get(assistIndex);
                                        homeGoals.add(new Goal(
                                                minute,
                                                goalPlayer.get_id(),
                                                goalPlayer.getName(),
                                                assistPlayer.get_id(),
                                                assistPlayer.getName()
                                        ));
                                    }else{ // 어웨이 팀
                                        int goalIndex = random.nextInt(awayPositionPlayers.get(3).size());
                                        int assistIndex = random.nextInt(awayPositionPlayers.get(3).size());
                                        List<PlayerInTeam> defendPlayers = awayPositionPlayers.get(3);
                                        PlayerInTeam goalPlayer = defendPlayers.get(goalIndex);
                                        PlayerInTeam assistPlayer = defendPlayers.get(assistIndex);
                                        awayGoals.add(new Goal(
                                                minute,
                                                goalPlayer.get_id(),
                                                goalPlayer.getName(),
                                                assistPlayer.get_id(),
                                                assistPlayer.getName()
                                        ));
                                    }
                                }
                            }
                        }
                    }
                }
            }
            // 아래가 가짜 데이터 이거를 진짜로 바꿔야 함(0->home, 1->away)
            TeamStat homeStat = new TeamStat(
                    homeTeam.getId(),
                    homeTeam.getClubName(),
                    homeGoals,
                    shots[0],effectiveShots[0],connerKicks[0]
                    );
            TeamStat awayStat = new TeamStat(
                    awayTeam.getId(),
                    awayTeam.getClubName(),
                    awayGoals,
                    shots[1],effectiveShots[1],connerKicks[1]
                    );

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

    private Map<Integer,List<PlayerInTeam>> chunkPlayersToPosition(List<PlayerInTeam> players) {
        Map<Integer,List<PlayerInTeam>> divisionToPosition = new HashMap<>();

        for(int i=0;i<players.size();i++){
            switch (players.get(i).getPosition()) {
                case 1 -> {
                    divisionToPosition.computeIfAbsent(1, k -> new ArrayList<PlayerInTeam>());
                    divisionToPosition.get(1).add(players.get(i));
                }
                case 2 -> {
                    divisionToPosition.computeIfAbsent(2, k -> new ArrayList<PlayerInTeam>());
                    divisionToPosition.get(2).add(players.get(i));
                }
                case 3 -> {
                    divisionToPosition.computeIfAbsent(3, k -> new ArrayList<PlayerInTeam>());
                    divisionToPosition.get(3).add(players.get(i));
                }
                default -> {
                    divisionToPosition.computeIfAbsent(4, k -> new ArrayList<PlayerInTeam>());
                    divisionToPosition.get(4).add(players.get(i));
                }
            }
        }
        return divisionToPosition;
    }

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
