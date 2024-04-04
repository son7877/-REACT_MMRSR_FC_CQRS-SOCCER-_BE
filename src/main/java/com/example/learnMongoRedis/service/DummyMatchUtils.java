//package com.example.learnMongoRedis.service;
//
//import com.example.learnMongoRedis.domain.model.match.Goal;
//import com.example.learnMongoRedis.domain.model.match.Match;
//import com.example.learnMongoRedis.domain.model.match.TeamStat;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.UUID;
//import java.util.concurrent.ThreadLocalRandom;
//
//public class DummyMatchUtils {
//
//    public static List<Match> generateDummyMatches(int count) {
//        List<Match> matches = new ArrayList<>();
//        for (int i = 0; i < count; i++) {
//            matches.add(new Match(
//                UUID.randomUUID().toString(),
//                "2023-04-" + (10 + i), // Simple date incrementation for uniqueness
//                "Stadium " + i,
//                generateTeamStat("Home Team " + i),
//                generateTeamStat("Away Team " + i)
//            ));
//        }
//        return matches;
//    }
//
//    public static TeamStat generateTeamStat(String teamName) {
//        return new TeamStat(
//            null,
//            teamName,
//            generateGoals(),
//            34,
//                23,
//                12
//        );
//    }
//
//    private static ArrayList<Goal> generateGoals() {
//        ArrayList<Goal> goals = new ArrayList<>();
//        int goalsCount = ThreadLocalRandom.current().nextInt(0, 5);
//        for (int i = 0; i < goalsCount; i++) {
//            goals.add(new Goal(
//                    45,
//                "Player" + i,
//                "손흥민",
//                "케인"
//            ));
//        }
//        return goals;
//    }
//}