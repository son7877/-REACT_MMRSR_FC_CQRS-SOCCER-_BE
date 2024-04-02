package com.example.learnMongoRedis.domain.model.match;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
@Data
@AllArgsConstructor
public class TeamStat{
    public String id;
    public String name;
    public ArrayList<Goal> goals;
    public int shots;
    public int effectiveShots;
    public int connerKicks;
}
