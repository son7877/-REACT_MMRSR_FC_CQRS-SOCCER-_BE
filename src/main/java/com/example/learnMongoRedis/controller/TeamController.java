package com.example.learnMongoRedis.controller;

import com.example.learnMongoRedis.domain.TeamService;
import com.example.learnMongoRedis.domain.model.Team;
import com.example.learnMongoRedis.global.wrapper.BaseResponseEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/teams")
@RequiredArgsConstructor
public class TeamController {
    private final TeamService teamService;

    @GetMapping("/all")
    public ResponseEntity<BaseResponseEntity<List<Team>>> getAllTeam() {
        return BaseResponseEntity.ok(teamService.getAllTeam(), "success");
    }

    @GetMapping("/{teamId}")
    public ResponseEntity<BaseResponseEntity<Team>> getTeam(@PathVariable("teamId") String teamId) {
        return BaseResponseEntity.ok(teamService.getTeamById(teamId), "success");
    }

}