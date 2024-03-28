package com.example.learnMongoRedis.domain;

import com.example.learnMongoRedis.domain.model.Team;

import java.util.List;

public interface TeamService {
    List<Team> getAllTeam();
    Team getTeamById(String id);
}