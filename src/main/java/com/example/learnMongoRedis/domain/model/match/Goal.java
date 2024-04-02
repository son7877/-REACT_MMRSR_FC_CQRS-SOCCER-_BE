package com.example.learnMongoRedis.domain.model.match;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Goal{
    public int time;
    public String playerId;
    public String playerName;
    public String assistName;
}