package com.example.learnMongoRedis.service;

import com.example.learnMongoRedis.domain.model.Player;
import com.example.learnMongoRedis.domain.model.Team;
import com.example.learnMongoRedis.repository.PlayerRepository;
import com.example.learnMongoRedis.repository.TeamRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class SimulationMatch {

    private final TeamRepository teamRepository;
    private final PlayerRepository playerRepository;
    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    public SimulationMatch(TeamRepository teamRepository, PlayerRepository playerRepository) {
        this.teamRepository = teamRepository;
        this.playerRepository = playerRepository;
    }

    @Autowired
    public void matchTeams(){

    }

    @Scheduled(fixedRate = 300000)
    public void runMatch() {
        Map<Object, Object> viewCounts = stringRedisTemplate.opsForHash().entries("team_views");

        viewCounts.forEach((teamId, views) -> {
            if (teamId != null && views != null) {
                String teamIdStr = (String) teamId;
                int viewCount = Integer.parseInt((String) views);

                Update update = new Update().inc("views", viewCount);
                Query query = new Query(Criteria.where("id").is(teamIdStr));

                mongoTemplate.updateFirst(query, update, Team.class);
                stringRedisTemplate.opsForHash().delete("team_views", teamId);
            }
        });
    }
}
