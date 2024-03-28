package com.example.learnMongoRedis.service;

import com.example.learnMongoRedis.domain.TeamService;
import com.example.learnMongoRedis.domain.model.Team;
import com.example.learnMongoRedis.global.error_handler.AppError;
import com.example.learnMongoRedis.global.wrapper.BaseResponseEntity;
import com.example.learnMongoRedis.repository.TeamRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TeamServiceImpl implements TeamService {

    private final TeamRepository teamRepository;
    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    public TeamServiceImpl(TeamRepository teamRepository) {
        this.teamRepository = teamRepository;
    }

    @Override
    public List<Team> getAllTeam() {
        return teamRepository.findAll();
    }

    @Override
    public Team getTeamById(String id) {
        Team team = teamRepository.findById(id).orElseThrow(() -> new AppError.Unexpected.EntityNotFoundException("찾는 팀이 없습니다."));
        incrementTeamViewCount(id);
        return team;
    }

    private void incrementTeamViewCount(String teamId) {
        stringRedisTemplate.opsForHash().increment("team_views", teamId, 1);
    }

    @Scheduled(fixedRate = 300000)
    public void updateViewCountsFromRedisToMongo() {
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