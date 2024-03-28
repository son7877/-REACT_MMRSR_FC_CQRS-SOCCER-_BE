package com.example.learnMongoRedis.domain.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.List;

@Document(collection = "teams")
@Data
public class Team {
    @Id
    private String id;
    private String clubName;
    private String homeTown;
    private String director;
    private String owner;
    private String homeStadium;
    private List<Player> players;
    private Awards awards;
    private List<String> rivalTeam;
    private int views;
}



