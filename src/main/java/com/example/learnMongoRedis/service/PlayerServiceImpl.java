package com.example.learnMongoRedis.service;

import com.example.learnMongoRedis.domain.PlayerService;
import com.example.learnMongoRedis.domain.TokenService;
import com.example.learnMongoRedis.domain.model.Player;
import com.example.learnMongoRedis.domain.model.PlayerOfMonthly;
import com.example.learnMongoRedis.global.error_handler.AppError;
import com.example.learnMongoRedis.repository.PlayerOfMonthlyRepository;
import com.example.learnMongoRedis.repository.PlayerRepository;
import com.example.learnMongoRedis.repository.UserRepository;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class PlayerServiceImpl implements PlayerService {

    private final PlayerRepository playerRepository;
    private final PlayerOfMonthlyRepository playerOfMonthlyRepository;
    public PlayerServiceImpl(PlayerRepository playerRepository, PlayerOfMonthlyRepository playerOfMonthlyRepository) {
        this.playerRepository = playerRepository;
        this.playerOfMonthlyRepository = playerOfMonthlyRepository;
    }

    @Override
    public List<Player> getAllPlayer() {
        return playerRepository.findAll();
    }

    @Override
    public List<PlayerOfMonthly> getBestPlayerOfMonth() {
        return  playerOfMonthlyRepository.findAll();
    }
}