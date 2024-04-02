package com.example.learnMongoRedis.repository;

import com.example.learnMongoRedis.domain.model.match.Season;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.Optional;

public interface SeasonRepository extends MongoRepository<Season, String> {

    Optional<Season> findTopByOrderByIdDesc();
    @Query(value="{}", sort="{_id: -1}")
    Optional<Season> findLatest();

}
