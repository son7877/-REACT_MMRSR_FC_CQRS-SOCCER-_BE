package com.example.learnMongoRedis.repository;

import com.example.learnMongoRedis.domain.model.PlayerOfMonthly;
import com.example.learnMongoRedis.domain.model.match.Season;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface PlayerOfMonthlyRepository extends MongoRepository<PlayerOfMonthly, String> {

}
