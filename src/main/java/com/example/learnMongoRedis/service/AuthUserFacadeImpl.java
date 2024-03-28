package com.example.learnMongoRedis.service;

import com.example.learnMongoRedis.domain.TokenService;
import com.example.learnMongoRedis.domain.model.User;
import com.example.learnMongoRedis.domain.UserService;
import com.example.learnMongoRedis.global.error_handler.AppError;
import com.example.learnMongoRedis.global.resouce.Constants;
import com.example.learnMongoRedis.global.security.model.AccessPayload;
import com.example.learnMongoRedis.global.security.JwtUtil;
import com.example.learnMongoRedis.global.security.model.RefreshPayload;
import com.example.learnMongoRedis.service.dto.TokenResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;


@Service
@RequiredArgsConstructor
@Log4j2
public class AuthUserFacadeImpl {
    private final UserService userService;
    private final TokenService tokenService;
    private final JwtUtil jwtUtil;

    @Transactional
    public TokenResponseDto signIn(String username, String password) {
        User user = userService.login(username, password);
        AccessPayload accessPayload = new AccessPayload(user.getId());
        RefreshPayload refreshPayload = new RefreshPayload(user.getId());
        String refreshToken = jwtUtil.createRefreshToken(refreshPayload);
        String accessToken = jwtUtil.createAccessToken(accessPayload);
        tokenService.storeUserIdWithToken(refreshToken, user.getId(), Constants.REFRESH_TOKEN_EXPIRE);
        return new TokenResponseDto(accessToken, refreshToken);
    }

    @Transactional
    public TokenResponseDto tokenRefresh(String refreshToken) {
        String userID = tokenService.validationRefreshTokenAndReturnUserID(refreshToken);
        RefreshPayload refreshPayload = jwtUtil.validateRefreshToken(refreshToken);
        String newRefreshToken = jwtUtil.createRefreshToken(refreshPayload);
        String accessToken = jwtUtil.createAccessToken(new AccessPayload(userID));
        tokenService.storeUserIdWithToken(newRefreshToken, userID, Constants.REFRESH_TOKEN_EXPIRE);
        tokenService.removeToken(refreshToken);
        return new TokenResponseDto(accessToken, newRefreshToken);
    }
}