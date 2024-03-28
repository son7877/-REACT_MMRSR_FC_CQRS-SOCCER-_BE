package com.example.learnMongoRedis.domain.model;

import lombok.Data;

import java.util.List;

@Data
public class Awards {
    private List<Integer> fa;
    private List<Integer> champions;
    private List<Integer> league;

    // Constructors, Getters, and Setters
}