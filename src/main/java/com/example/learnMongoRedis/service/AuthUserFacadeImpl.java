package com.example.learnMongoRedis.service;

import com.example.learnMongoRedis.domain.TokenService;
import com.example.learnMongoRedis.domain.UserService;
import com.example.learnMongoRedis.domain.model.User;
import com.example.learnMongoRedis.global.error_handler.AppError;
import com.example.learnMongoRedis.global.resouce.Constants;
import com.example.learnMongoRedis.global.security.JwtUtil;
import com.example.learnMongoRedis.global.security.model.AccessPayload;
import com.example.learnMongoRedis.global.security.model.RefreshPayload;
import com.example.learnMongoRedis.service.dto.TokenResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


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

    @Transactional
    public TokenResponseDto validationAccessToken(String accessToken, String refreshToken)  {
        try {
            AccessPayload payload = jwtUtil.validateAccessToken(accessToken);
            if (payload != null) {
                return new TokenResponseDto(accessToken, refreshToken);
            } else {
                // userID가 일치하지 않을 경우, 예외를 던집니다.
                throw new AppError.Expected.ValidationFailedException("사용자 ID가 일치하지 않습니다.");
            }
        } catch (Exception e) {
            // accessToken 인증 실패 시 refreshToken을 사용해 토큰을 새로 발급합니다.
            return tokenRefresh(refreshToken);
        }
    }
}