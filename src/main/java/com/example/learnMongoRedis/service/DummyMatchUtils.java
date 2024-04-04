package com.example.learnMongoRedis.global;

import com.example.learnMongoRedis.domain.model.match.Goal;
import com.example.learnMongoRedis.domain.model.match.TeamStat;

import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

public class DummyMatchUtils {

    public static TeamStat generateTeamStat(String teamId, String teamName) {
        return new TeamStat(
                teamId,
                teamName,
                generateGoals(),
                34,
                23,
                12
        );
    }

    private static ArrayList<Goal> generateGoals() {
        ArrayList<Goal> goals = new ArrayList<>();
        int goalsCount = ThreadLocalRandom.current().nextInt(0, 5);
        for (int i = 0; i < goalsCount; i++) {
            goals.add(new Goal(
                    45,
                    "66092b7629b17017ca43ddac",
                    "손흥민",
                    "66092b7629b17017ca43ddad",
                    "케인"
            ));
        }
        return goals;
    }
}