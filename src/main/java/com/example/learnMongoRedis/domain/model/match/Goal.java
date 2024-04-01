package com.example.learnMongoRedis.domain.model.match;

import lombok.Data;

@Data
public class Goal{
    public String time;
    public String playerId;
    public String playerName;
    public String assistName;
}