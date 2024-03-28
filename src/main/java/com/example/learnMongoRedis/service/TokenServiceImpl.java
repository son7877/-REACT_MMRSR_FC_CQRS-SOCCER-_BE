package com.example.learnMongoRedis.service;

import com.example.learnMongoRedis.domain.TokenService;
import com.example.learnMongoRedis.global.error_handler.AppError;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class TokenServiceImpl implements TokenService {

    private final StringRedisTemplate stringRedisTemplate;

    @Autowired
    public TokenServiceImpl(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public void storeUserIdWithToken(String refreshToken, String userId, long expirationInSeconds) {
        stringRedisTemplate.opsForValue().set("refreshToken:" + refreshToken, userId, expirationInSeconds, TimeUnit.MILLISECONDS);
    }

    @Override
    public String validationRefreshTokenAndReturnUserID(String refreshToken) {
        String userId = stringRedisTemplate.opsForValue().get("refreshToken:" + refreshToken);
        if(userId == null) throw new AppError.Expected.UnauthorizedException("리프레쉬 토큰이 유효하지 않습니다.");
        return userId;
    }


    @Override
    public void removeToken(String refreshToken) {
        stringRedisTemplate.delete("refreshToken:" + refreshToken);
    }
}