package com.example.learnMongoRedis.domain.StateModel;

import com.example.learnMongoRedis.domain.model.Team;
import com.example.learnMongoRedis.domain.model.match.Season;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class SimulationData {
    List<Team> teams;
    Season season;
}
