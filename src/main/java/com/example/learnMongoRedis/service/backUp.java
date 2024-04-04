//package com.example.learnMongoRedis.service;
//
//import com.example.learnMongoRedis.domain.model.Player;
//import com.example.learnMongoRedis.domain.model.PlayerInTeam;
//import com.example.learnMongoRedis.domain.model.match.*;
//import com.example.learnMongoRedis.domain.model.Team;
//import com.example.learnMongoRedis.global.error_handler.AppError;
//import com.example.learnMongoRedis.repository.PlayerRepository;
//import com.example.learnMongoRedis.repository.SeasonRepository;
//import com.example.learnMongoRedis.repository.TeamRepository;
//import lombok.extern.log4j.Log4j2;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.data.mongodb.core.MongoTemplate;
//import org.springframework.data.mongodb.core.query.Criteria;
//import org.springframework.data.mongodb.core.query.Query;
//import org.springframework.data.mongodb.core.query.Update;
//import org.springframework.data.redis.core.StringRedisTemplate;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.time.LocalDateTime;
//import java.util.*;
//import java.util.concurrent.ThreadLocalRandom;
//
//@Service
//@Log4j2
//public class SimulationMatch {
//
//    private final TeamRepository teamRepository;
//    private final PlayerRepository playerRepository;
//    private final SeasonRepository seasonRepository;
//    @Autowired
//    private MongoTemplate mongoTemplate;
//    @Autowired
//    private StringRedisTemplate stringRedisTemplate;
//
//    @Autowired
//    public SimulationMatch(TeamRepository teamRepository, PlayerRepository playerRepository, SeasonRepository seasonRepository, MongoTemplate mongoTemplate) {
//        this.teamRepository = teamRepository;
//        this.playerRepository = playerRepository;
//        this.seasonRepository = seasonRepository;
//        this.mongoTemplate = mongoTemplate;
//    }
//
//    private Season getCurrentSeason() {
//
//        Optional<Season> getSeason = seasonRepository.findTopByOrderByIdDesc();
//        Season currentSeason = null;
//        if(getSeason.isPresent()) {
//            currentSeason = getSeason.get();
//        } else {
//            currentSeason = createSeason(2024);
//        }
//        log.error("&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&");
//        log.error(currentSeason.toString());
//        log.error("&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&");
//
//        if (currentSeason.getRoundCount() >= 29) {
//            // 한 시즌의 라운드가 끝났을 때 시즌에 1더함
//            log.error("++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
//            return createSeason(currentSeason.getSeason() + 1);
//        } else {
//            return currentSeason;
//        }
//    }
//
//    // 만약 시즌이 없거나 시즌의 라운드가 30인경우 새로 생성
//    @Transactional
//    private Season createSeason(int seasonNumber) {
//        Season season = Season.builder()
//                .season(seasonNumber)
//                .roundList(List.of())
//                .roundCount(0)
//                .build();
//
//        seasonRepository.save(season);
//        return season;
//    }
//
//    private Match createMatch(TeamStat home, TeamStat away, String stadium) {
//        return new Match(
//                null,
//                LocalDateTime.now().toString(),
//                stadium,
//                home,
//                away
//        );
//    }
//
//    private Round createRound(int round, List<Match> matches) {
//        return Round.builder()
//                .round(round)
//                .matches(matches)
//                .build();
//    }
//
//    private void addRoundInSeason(String seasonId, Round round) {
//        Query query = new Query(Criteria.where("id").is(seasonId));
//        Update update = new Update().inc("roundCount", 1).push("roundList", round);
//        mongoTemplate.findAndModify(query, update, Season.class);
//    }
//
//    public void runSimulation() {
//        Random random = new Random();
//        List<Team> teams = teamRepository.findAll();
//        List<Match> matches = new ArrayList<>();
//        Season season = getCurrentSeason();
//        log.error("********************************************");
//        log.error(season.toString());
//        log.error("********************************************");
//        int totalTeams = teams.size();  // 총 팀수
//        int matchesPerRound = totalTeams / 2; // 라운드 당 경기 수
//        int roundCount = season.getRoundCount();
//
//        int shots []= {0,0};
//        int effectiveShots []= {0,0};
//        int goals []= {0,0};
//        int connerKicks []= {0,0};
//        ArrayList<Goal> homeGoals = new ArrayList<>();
//        ArrayList<Goal> awayGoals = new ArrayList<>();
//
//        // 홈 팀, 어웨이 팀
//        double Advantage [] = {0.9,1.1};
//
//        //팀 정보에서 선수들 오버롤 들을 더해서 평균값 내기(팀 오버롤)
//
//        for (int match = 0; match < matchesPerRound; match++) {
//            // 매치 팀 인덱스 설정
//            int homeTeamIndex = (roundCount % (totalTeams - 1) + match) % (totalTeams - 1);
//            int awayTeamIndex = ((totalTeams - 1) - match + roundCount % (totalTeams - 1)) % (totalTeams - 1);
//
//            // 한 팀(맨 끝 인덱스인 팀 고정)
//            if (match == 0) {
//                awayTeamIndex = totalTeams - 1;
//            }
//
//            // 절반 라운드가 진행되면 1라운드의 홈,어웨이만 바꿔서 같은 방식으로 진행
//            if (roundCount >= totalTeams - 1) {
//                int temp = homeTeamIndex;
//                homeTeamIndex = awayTeamIndex;
//                awayTeamIndex = temp;
//            }
//
//            Team homeTeam = teams.get(homeTeamIndex);
//            Team awayTeam = teams.get(awayTeamIndex);
//
//            List<PlayerInTeam> homeTeamPlayers = homeTeam.getPlayers();
//            Map<Integer,List<PlayerInTeam>> homePositionPlayers = chunkPlayersToPosition(homeTeamPlayers);
//
//            List<PlayerInTeam> awayTeamPlayers = awayTeam.getPlayers();
//            Map<Integer,List<PlayerInTeam>> awayPositionPlayers = chunkPlayersToPosition(awayTeamPlayers);
//
//            double overall []  = {homeTeam.teamOverallAvg(),awayTeam.teamOverallAvg()};
//
//            // 팀 배분 여기서 끝
//            // 여기서 90분 돌려서 가상의 결과 데이터 goalSimulation => Match
//            // Match class 만든다
//
//            // 슛->골
//            for(int i=0;i<2;i++){ // 홈팀 어웨이팀 2번
//                double ratio = Advantage[i] * (-0.002*overall[i]+1.2);
//                for (int minute = 0; minute < 90; minute++) {
//                    if (random.nextDouble()<0.10) connerKicks[i]++;
//                    if (random.nextDouble() * ratio < 0.20) { // 20% 확률로 슈팅
//                        shots[i]++;
//                        if (random.nextDouble() * ratio < 0.30) { // 슈팅 중 30%은 유효슈팅
//                            effectiveShots[i]++;
//                            if (random.nextDouble() * ratio < 0.40) { // 유효슈팅 중 40%은 골
//                                goals[i]++;
//                                double scorer = random.nextDouble();
//                                if (scorer < 0.55) { // 55% 확률로 공격수가 골
//                                    // 골 넣은 선수 및 어시스트 선수 선정하기
//                                    // 골은 해당 포지션의 선수들 중 하나, 어시스트는 골 넣은 선수 제외한 나머지
//                                    if(i==0){ // 홈 팀
//                                        int index = random.nextInt(homePositionPlayers.get(1).size());
//                                        List<PlayerInTeam> attackPlayers = homePositionPlayers.get(1);
//                                        PlayerInTeam goalPlayer = attackPlayers.get(index);
//                                        PlayerInTeam assistPlayer = attackPlayers.get(index);
//                                        homeGoals.add(new Goal(
//                                                minute,
//                                                goalPlayer.get_id(),
//                                                goalPlayer.getName(),
//                                                assistPlayer.get_id(),
//                                                assistPlayer.getName()
//                                        ));
//                                    }else{ // 어웨이 팀
//                                        int index = random.nextInt(awayPositionPlayers.get(1).size());
//                                        List<PlayerInTeam> attackPlayers = awayPositionPlayers.get(1);
//                                        PlayerInTeam goalPlayer = attackPlayers.get(index);
//                                        PlayerInTeam assistPlayer = attackPlayers.get(index);
//                                        awayGoals.add(new Goal(
//                                                minute,
//                                                goalPlayer.get_id(),
//                                                goalPlayer.getName(),
//                                                assistPlayer.get_id(),
//                                                assistPlayer.getName()
//                                        ));
//                                    }
//                                } else if (scorer < 0.90) { // 추가 35% 확률로 미드필더가 골
//                                    // 골 넣은 선수 및 어시스트 선수 선정하기
//                                    // 팀에 해당하는 선수(팀 아이디를 불러오기)
//                                    // 골은 해당 포지션의 선수들 중 하나, 어시스트는 골 넣은 선수 제외한 나머지
//                                    if(i==0){ // 홈 팀
//                                        int index = random.nextInt(homePositionPlayers.get(2).size());
//                                        List<PlayerInTeam> attackPlayers = homePositionPlayers.get(2);
//                                        PlayerInTeam goalPlayer = attackPlayers.get(index);
//                                        PlayerInTeam assistPlayer = attackPlayers.get(index);
//                                        homeGoals.add(new Goal(
//                                                minute,
//                                                goalPlayer.get_id(),
//                                                goalPlayer.getName(),
//                                                assistPlayer.get_id(),
//                                                assistPlayer.getName()
//                                        ));
//                                    }else{ // 어웨이 팀
//                                        int index = random.nextInt(awayPositionPlayers.get(2).size());
//                                        List<PlayerInTeam> attackPlayers = awayPositionPlayers.get(2);
//                                        PlayerInTeam goalPlayer = attackPlayers.get(index);
//                                        PlayerInTeam assistPlayer = attackPlayers.get(index);
//                                        awayGoals.add(new Goal(
//                                                minute,
//                                                goalPlayer.get_id(),
//                                                goalPlayer.getName(),
//                                                assistPlayer.get_id(),
//                                                assistPlayer.getName()
//                                        ));
//                                    }
//                                } else {
//                                    // 나머지 확률로 수비수가 골
//                                    // 골은 해당 포지션의 선수들 중 하나, 어시스트는 골 넣은 선수 제외한 나머지
//                                    if(i==0){ // 홈 팀
//                                        int index = random.nextInt(homePositionPlayers.get(3).size());
//                                        List<PlayerInTeam> attackPlayers = homePositionPlayers.get(3);
//                                        PlayerInTeam goalPlayer = attackPlayers.get(index);
//                                        PlayerInTeam assistPlayer = attackPlayers.get(index);
//                                        homeGoals.add(new Goal(
//                                                minute,
//                                                goalPlayer.get_id(),
//                                                goalPlayer.getName(),
//                                                assistPlayer.get_id(),
//                                                assistPlayer.getName()
//                                        ));
//                                    }else{ // 어웨이 팀
//                                        int index = random.nextInt(awayPositionPlayers.get(3).size());
//                                        List<PlayerInTeam> attackPlayers = awayPositionPlayers.get(3);
//                                        PlayerInTeam goalPlayer = attackPlayers.get(index);
//                                        PlayerInTeam assistPlayer = attackPlayers.get(index);
//                                        awayGoals.add(new Goal(
//                                                minute,
//                                                goalPlayer.get_id(),
//                                                goalPlayer.getName(),
//                                                assistPlayer.get_id(),
//                                                assistPlayer.getName()
//                                        ));
//                                    }
//                                }
//                            }
//                        }
//                    }
//                }
//            }
//            // 아래가 가짜 데이터 이거를 진짜로 바꿔야 함(0->home, 1->away)
//            TeamStat homeStat = new TeamStat(
//                    null,
//                    homeTeam.getClubName(),
//                    homeGoals,
//                    shots[0],effectiveShots[0],connerKicks[0]
//            );
//            TeamStat awayStat = new TeamStat(
//                    null,
//                    awayTeam.getClubName(),
//                    awayGoals,
//                    shots[1],effectiveShots[1],connerKicks[1]
//            );
//
//            matches.add(createMatch(
//                    homeStat,
//                    awayStat,
//                    homeTeam.getHomeStadium()
//            ));
//        }
//        Round round = createRound(roundCount + 1, matches);
//        addRoundInSeason(season.getId(),round);
//    }
//
//    private Map<Integer,List<PlayerInTeam>> chunkPlayersToPosition(List<PlayerInTeam> players) {
//        Map<Integer,List<PlayerInTeam>> divisionToPosition = new HashMap<>();
//
//        for(int i=0;i<players.size();i++){
//            switch (players.get(i).getPosition()) {
//                case 1 -> {
//                    divisionToPosition.computeIfAbsent(1, k -> new ArrayList<PlayerInTeam>());
//                    divisionToPosition.get(1).add(players.get(i));
//                }
//                case 2 -> {
//                    divisionToPosition.computeIfAbsent(2, k -> new ArrayList<PlayerInTeam>());
//                    divisionToPosition.get(2).add(players.get(i));
//                }
//                case 3 -> {
//                    divisionToPosition.computeIfAbsent(3, k -> new ArrayList<PlayerInTeam>());
//                    divisionToPosition.get(3).add(players.get(i));
//                }
//                default -> {
//                    divisionToPosition.computeIfAbsent(4, k -> new ArrayList<PlayerInTeam>());
//                    divisionToPosition.get(4).add(players.get(i));
//                }
//            }
//        }
//        return divisionToPosition;
//    }
//
//
//
//
//    //로직 확인용
////    public void goalSimulation(){
////        Random random = new Random();
////        int shots = 0;
////        int effectiveShots = 0;
////        int goals = 0;
////
////        // 홈팀인지 어웨이팀인지 결정 필요
////        double homeAdvantage = 0.9;
////        double awayPenalty = 1.1;
////
////        //팀정보에서 선수들 오버롤들을 더해서 평균값내기(팀 오버롤)
////        double overall = 50;
////        double ratio = awayPenalty * (-0.002*overall+1.2);
////
////        for (int minute = 0; minute < 90; minute++) {
////            if (random.nextDouble() * ratio< 0.20) { // 20% 확률로 슈팅
////                shots++;
////                if (random.nextDouble() * ratio < 0.30) { // 슈팅 중 30%은 유효슈팅
////                    effectiveShots++;
////                    if (random.nextDouble() * ratio < 0.40) { // 유효슈팅 중 40%은 골
////                        goals++;
////                        // 골을 넣은 선수의 포지션 결정
////                        double scorer = random.nextDouble();
////                        if (scorer < 0.55) { // 55% 확률로 공격수가 골
////                            // 골 넣은 선수 및 어시스트 선수 선정하기
////                            // 골은 해당 포지션의 선수들 중 하나, 어시스트는 골 넣은 선수 제외한 나머지
////
////                        } else if (scorer < 0.90) { // 추가 35% 확률로 미드필더가 골
////                            // 골 넣은 선수 및 어시스트 선수 선정하기
////                            // 골은 해당 포지션의 선수들 중 하나, 어시스트는 골 넣은 선수 제외한 나머지
////
////                        } else { // 나머지 확률로 수비수가 골
////
////                        }
////                    }
////                }
////            }
////        }
////
////        // 생성된 골, 슈팅 , 유효 슈팅 TeamStat에 넣기
////
////
////
////    }
////
////    @Scheduled(fixedRate = 300000)
////    public void runMatch() {
////        Map<Object, Object> viewCounts = stringRedisTemplate.opsForHash().entries("team_views");
////
////        viewCounts.forEach((teamId, views) -> {
////            if (teamId != null && views != null) {
////                String teamIdStr = (String) teamId;
////                int viewCount = Integer.parseInt((String) views);
////
////                Update update = new Update().inc("views", viewCount);
////                Query query = new Query(Criteria.where("id").is(teamIdStr));
////
////                mongoTemplate.updateFirst(query, update, Team.class);
////                stringRedisTemplate.opsForHash().delete("team_views", teamId);
////            }
////        });
////    }
//}
