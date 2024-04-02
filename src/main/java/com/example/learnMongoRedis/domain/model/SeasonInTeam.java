package com.example.learnMongoRedis.domain.model;

import lombok.Data;

@Data
public class SeasonInTeam {
    private int season = 2020;
    private int wins = 20;
    private int draws = 5;
    private int lose = 5;
    private int totalGoal = 55;
    private int totalConceded = 30;
    public int point() {
        return wins*3+draws;
    }
    public int getDiff() {
        return totalGoal - totalConceded;
    }
}