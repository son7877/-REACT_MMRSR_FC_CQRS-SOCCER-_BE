package com.example.learnMongoRedis.controller;

import com.example.learnMongoRedis.domain.SeasonService;
import com.example.learnMongoRedis.domain.model.match.Season;
import com.example.learnMongoRedis.global.wrapper.BaseResponseEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/seasons")
@RequiredArgsConstructor
public class MatchController {

    private final SeasonService seasonService;

    @GetMapping("/all")
    public ResponseEntity<BaseResponseEntity<List<Season>>> getAllSeasons() {
        return BaseResponseEntity.ok(seasonService.getAllSeason(), "success");
    }

}