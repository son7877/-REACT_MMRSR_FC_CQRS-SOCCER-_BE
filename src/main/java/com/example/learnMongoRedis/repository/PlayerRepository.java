package com.example.learnMongoRedis.repository;


import com.example.learnMongoRedis.domain.model.Player;
import com.example.learnMongoRedis.domain.model.Team;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface PlayerRepository extends MongoRepository<Player, String> {

}

