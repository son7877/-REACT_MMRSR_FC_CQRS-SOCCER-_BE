package com.example.learnMongoRedis.domain.model;

import lombok.Data;

@Data
public class SeasonInTeam {
    private int season;
    private int wins;
    private int draws;
    private int lose;
    private int totalGoal;
    private int totalConceded;
    public int point() {
        return wins*3+draws;
    }
    public int getDiff() {
        return totalGoal - totalConceded;
    }
}