package com.example.learnMongoRedis.domain;

public interface TokenService {
    void storeUserIdWithToken(String refreshToken, String userId, long expirationInSeconds);

    String validationRefreshTokenAndReturnUserID(String refreshToken);

    void removeToken(String refreshToken);
}
