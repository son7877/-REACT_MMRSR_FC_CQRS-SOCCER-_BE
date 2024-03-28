package com.example.learnMongoRedis.repository;


import com.example.learnMongoRedis.domain.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserRepository extends MongoRepository<User, String> {
    User findByLoginId(String loginId);

}

