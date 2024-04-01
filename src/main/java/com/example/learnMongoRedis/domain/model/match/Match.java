package com.example.learnMongoRedis.domain.model.match;

import com.example.learnMongoRedis.domain.model.match.TeamStat;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
public class Match {
    @Id
    private String id;
    public String date;
    public String stadium;
    public TeamStat homeTeam;
    public TeamStat awayTeam;

}





