package com.example.learnMongoRedis.domain.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "seasons")
@Data
public class Season {
    @Id
    private String id;
    private int season;
    private int roundCount;
    private List<Round> roundList;

}
