package com.example.learnMongoRedis.domain.model;

import com.example.learnMongoRedis.domain.model.match.Match;
import lombok.Data;

import java.util.List;

@Data
public class Round {
    private List<Match> matches;
}
