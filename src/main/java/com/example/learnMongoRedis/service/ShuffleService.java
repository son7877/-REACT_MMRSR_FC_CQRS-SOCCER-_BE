package com.example.learnMongoRedis.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ShuffleService {
    public static void main(String[] args) {
        List<String> teams = new ArrayList<String>();
        Collections.addAll(teams,
                "Team 1", "Team 2", "Team 3", "Team 4",
                "Team 5", "Team 6", "Team 7", "Team 8"
        ); // 그냥 확인차 예시로 임의의 8개 팀 만들어 봄

        // 팀 섞기 -> 로직 확인 후 나중에
        // Collections.shuffle(teams);

        int totalTeams = teams.size();  // 총 팀수
        int totalRounds = (totalTeams - 1) * 2; // 홈 앤 어웨이 방식 -> 자기 팀 제외 후 2배
        int matchesPerRound = totalTeams / 2; // 라운드 당 경기 수

        for (int round = 0; round < totalRounds; round++) {
            System.out.println("Round " + (round+1)); // 1라운드부터 시작

            // 4라운드가 진행 될 때마다 이달의 팀, 이달의 선수 선정 (0~3,4~7, 8~11, 12~14 합산)

            for (int match = 0; match < matchesPerRound; match++) {
                // 매치 팀 인덱스 설정
                int homeTeamIndex = (round % (totalTeams - 1) + match) % (totalTeams - 1);
                int awayTeamIndex = ((totalTeams - 1) - match + round % (totalTeams - 1)) % (totalTeams - 1);

                // 한 팀(맨 끝 인덱스인 팀 고정)
                if (match == 0) {
                    awayTeamIndex = totalTeams - 1;
                }

                // 절반 라운드가 진행되면 1라운드의 홈,어웨이만 바꿔서 같은 방식으로 진행
                if(round >= totalTeams -1){
                    int temp = homeTeamIndex;
                    homeTeamIndex = awayTeamIndex;
                    awayTeamIndex = temp;
                }


                // if(homeTeamIndex == 0 || awayTeamIndex == 0) {
                //    System.out.println(teams.get(homeTeamIndex) + " vs " + teams.get(awayTeamIndex));
                // }
                System.out.println(teams.get(homeTeamIndex) + " vs " + teams.get(awayTeamIndex));

            }
            System.out.println();
        }
    }
}
