package com.example.learnMongoRedis.domain.model.match;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
public class TeamStat{
    public String teamId;
    public String name;
    public List<Goal> goals;
    public int shots;
    public int effectiveShots;
    public int connerKicks;
    public int possession;
}
