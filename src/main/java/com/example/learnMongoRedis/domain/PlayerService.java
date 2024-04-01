package com.example.learnMongoRedis.domain;

import com.example.learnMongoRedis.domain.model.Player;

import java.io.IOException;
import java.util.List;

public interface PlayerService {
    void insertPlayer(List<Player> players) throws IOException;

}
