package com.example.learnMongoRedis.domain;

import com.example.learnMongoRedis.domain.model.Player;
import com.example.learnMongoRedis.domain.model.PlayerOfMonthly;

import java.io.IOException;
import java.util.List;

public interface PlayerService {
    public List<Player> getAllPlayer();
    public List<PlayerOfMonthly> getBestPlayerOfMonth();
}
