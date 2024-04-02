package com.example.learnMongoRedis.service;

import com.example.learnMongoRedis.domain.model.match.Round;
import com.example.learnMongoRedis.domain.model.match.Season;
import com.example.learnMongoRedis.domain.model.Team;
import com.example.learnMongoRedis.domain.model.match.Match;
import com.example.learnMongoRedis.domain.model.match.TeamStat;
import com.example.learnMongoRedis.global.error_handler.AppError;
import com.example.learnMongoRedis.repository.PlayerRepository;
import com.example.learnMongoRedis.repository.SeasonRepository;
import com.example.learnMongoRedis.repository.TeamRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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

    private Season getCurrentSeason() {

        Optional<Season> getSeason = seasonRepository.findTopByOrderByIdDesc();
        Season currentSeason = null;
        if(getSeason.isPresent()) {
            currentSeason = getSeason.get();
        } else {
            currentSeason = createSeason(2024);
        }
        log.error("&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&");
        log.error(currentSeason.toString());
        log.error("&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&");

        if (currentSeason.getRoundCount() >= 29) {
            log.error("++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
            return createSeason(currentSeason.getSeason() + 1);
        } else {
            return currentSeason;
        }
    }

    // 만약 시즌이 없거나 시즌의 라운드가 30인경우 새로 생성
    @Transactional
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
                null,
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

    private void addRoundInSeason(String seasonId, Round round) {
        Query query = new Query(Criteria.where("id").is(seasonId));
        Update update = new Update().inc("roundCount", 1).push("roundList", round);
        mongoTemplate.findAndModify(query, update, Season.class);
    }

    public void runSimulation() {
        List<Team> teams = teamRepository.findAll();
        List<Match> matches = new ArrayList<>();
        Season season = getCurrentSeason();
        log.error("********************************************");
        log.error(season.toString());
        log.error("********************************************");
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
            TeamStat homeStat = DummyMatchUtils.generateTeamStat(homeTeam.getClubName());
            TeamStat awayStat = DummyMatchUtils.generateTeamStat(awayTeam.getClubName());

            matches.add(createMatch(
                    homeStat,
                    awayStat,
                    homeTeam.getHomeStadium()
            ));
        }

        Round round = createRound(roundCount + 1, matches);
        addRoundInSeason(season.getId(),round);
    }
//
//    @Scheduled(fixedRate = 300000)
//    public void runMatch() {
//        Map<Object, Object> viewCounts = stringRedisTemplate.opsForHash().entries("team_views");
//
//        viewCounts.forEach((teamId, views) -> {
//            if (teamId != null && views != null) {
//                String teamIdStr = (String) teamId;
//                int viewCount = Integer.parseInt((String) views);
//
//                Update update = new Update().inc("views", viewCount);
//                Query query = new Query(Criteria.where("id").is(teamIdStr));
//
//                mongoTemplate.updateFirst(query, update, Team.class);
//                stringRedisTemplate.opsForHash().delete("team_views", teamId);
//            }
//        });
//    }
}
