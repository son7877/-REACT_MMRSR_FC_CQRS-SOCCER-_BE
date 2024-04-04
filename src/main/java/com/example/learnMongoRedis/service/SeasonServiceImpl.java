package com.example.learnMongoRedis.service;

import com.example.learnMongoRedis.domain.SeasonService;
import com.example.learnMongoRedis.domain.model.match.Season;
import com.example.learnMongoRedis.repository.SeasonRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SeasonServiceImpl implements SeasonService {

    private final SeasonRepository seasonRepository;

    public SeasonServiceImpl(SeasonRepository seasonRepository) {
        this.seasonRepository = seasonRepository;
    }

    @Override
    public List<Season> getAllSeason() {
        return seasonRepository.findAll();
    }
}
