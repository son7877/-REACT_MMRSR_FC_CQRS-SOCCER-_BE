package com.example.learnMongoRedis.domain.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "players")
@Data
public class Player {
    @Id
    private String id;
    private String name;
    private String position;
    private int salary; // Consider changing to an appropriate type
    private int number;
    private String mainFoot;
    private int age;
    private boolean assertion;
    private int goal;
    private int assist;
    private int totalGamesPlayed;
    private int totalGoalsScored;
    private int totalAssists;
    private String teamId;
    private int weeklyGoals;
    private int weeklyAssists;
    private int overall;
}

