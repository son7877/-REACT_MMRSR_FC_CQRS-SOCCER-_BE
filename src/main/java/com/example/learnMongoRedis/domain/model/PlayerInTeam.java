package com.example.learnMongoRedis.domain.model;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
public class PlayerInTeam {
    private String _id;
    private String name;
    private String position;
    private int number;
    private int age;
    private boolean assertion;
    private boolean overall;
}