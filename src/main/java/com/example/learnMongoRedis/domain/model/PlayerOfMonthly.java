package com.example.learnMongoRedis.domain.model;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "playerOfMonthly")
@Data
public class PlayerOfMonthly {
    private String id;
    private String name;
    private int age;
    private int goal;
    private int assist;
    private int totalGoalsScored;
    private int totalAssists;
    private String teamId;
    private int overall;
    private int monthlyScore;
    private int rank;
}