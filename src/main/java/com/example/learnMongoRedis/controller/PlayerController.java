package com.example.learnMongoRedis.controller;

import com.example.learnMongoRedis.domain.PlayerService;
import com.example.learnMongoRedis.domain.TeamService;
import com.example.learnMongoRedis.domain.model.Player;
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
@RequestMapping("/api/v1/players")
@RequiredArgsConstructor
public class PlayerController {
    private final PlayerService playerService;

    @GetMapping("/all")
    public ResponseEntity<BaseResponseEntity<List<Player>>> getAllPlayer() {
        return BaseResponseEntity.ok(playerService.getAllPlayer(), "success");
    }
}