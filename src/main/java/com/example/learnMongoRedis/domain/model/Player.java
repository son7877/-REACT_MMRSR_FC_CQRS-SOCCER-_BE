package com.example.learnMongoRedis.domain.model;

import lombok.Data;

@Data
public class Player {
    private String name;
    private String position;
    private String salary; // Consider changing to an appropriate type
    private int number;
    private String mainFoot;
    private int age;
    private boolean assertion;

    // Constructors, Getters, and Setters
}