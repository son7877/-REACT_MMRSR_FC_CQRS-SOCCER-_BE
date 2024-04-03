package com.example.learnMongoRedis.domain.model.match;

import com.example.learnMongoRedis.domain.model.match.TeamStat;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class Match {
    public String date;
    public String stadium;
    public TeamStat homeTeam;
    public TeamStat awayTeam;

}





