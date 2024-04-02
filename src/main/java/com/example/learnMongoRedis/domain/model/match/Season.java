package com.example.learnMongoRedis.domain.model.match;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "seasons")
@Data
@Builder
public class Season {
    @Id
    private String id;
    private int season;
    private int roundCount;
    private List<Round> roundList;
}
