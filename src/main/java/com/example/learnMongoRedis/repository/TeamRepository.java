package com.example.learnMongoRedis.repository;


import com.example.learnMongoRedis.domain.model.Team;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface TeamRepository extends MongoRepository<Team, String> {

}

