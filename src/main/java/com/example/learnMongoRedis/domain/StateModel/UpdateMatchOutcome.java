package com.example.learnMongoRedis.domain.StateModel;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UpdateMatchOutcome {
    private int goal;
    private int conceded;
    private MatchResultState matchResultState;

}
