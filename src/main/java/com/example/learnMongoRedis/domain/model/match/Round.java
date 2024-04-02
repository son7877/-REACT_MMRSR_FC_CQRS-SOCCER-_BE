package com.example.learnMongoRedis.domain.model.match;

import com.example.learnMongoRedis.domain.model.match.Match;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class Round {
    private int round;
    private List<Match> matches;
}
