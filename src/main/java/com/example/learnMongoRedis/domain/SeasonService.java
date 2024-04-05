package com.example.learnMongoRedis.domain;


import com.example.learnMongoRedis.domain.model.match.Match;
import com.example.learnMongoRedis.domain.model.match.Season;

import java.util.List;

public interface SeasonService {

    public List<Season> getAllSeason();

    public List<Match> getMatchLastTwoSeason();
}
