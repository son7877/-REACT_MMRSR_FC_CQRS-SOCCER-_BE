package com.example.learnMongoRedis.domain.StateModel;

public enum MatchResultState {
    WIN,
    DRAW,
    LOSE;

    public static MatchResultState convertMatchResultState(int goals, int conceded) {
        if (goals > conceded) {
            return WIN;
        } else if (goals < conceded) {
            return LOSE;
        } else {
            return DRAW;
        }
    }
}
