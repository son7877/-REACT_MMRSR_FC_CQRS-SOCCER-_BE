package com.example.learnMongoRedis.service;

import com.example.learnMongoRedis.domain.SeasonService;
import com.example.learnMongoRedis.domain.model.match.Match;
import com.example.learnMongoRedis.domain.model.match.Round;
import com.example.learnMongoRedis.domain.model.match.Season;
import com.example.learnMongoRedis.repository.SeasonRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SeasonServiceImpl implements SeasonService {

    private final SeasonRepository seasonRepository;

    public SeasonServiceImpl(SeasonRepository seasonRepository) {
        this.seasonRepository = seasonRepository;
    }

    @Override
    public List<Season> getAllSeason() {
        return seasonRepository.findAll();
    }

    @Override
    public List<Match> getMatchLastTwoSeason() {
        // 최근 2시즌 데이터 조회
        List<Season> recentSeasons = seasonRepository.findTop2ByOrderBySeasonDesc();

        // 모든 시즌의 모든 라운드에서 경기(match) 목록 추출
        List<Match> allMatches = new ArrayList<>();
        for (Season season : recentSeasons) {
            for (Round round : season.getRoundList()) {
                allMatches.addAll(round.getMatches());
            }
        }

        // 경기(match) 목록을 날짜 내림차순으로 정렬
        return allMatches.stream()
                .sorted(Comparator.comparing(Match::getDate).reversed())
                .collect(Collectors.toList());
    }
}
