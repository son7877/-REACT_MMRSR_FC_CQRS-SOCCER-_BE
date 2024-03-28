package com.example.learnMongoRedis.controller;

import com.example.learnMongoRedis.domain.TokenService;
import com.example.learnMongoRedis.domain.UserService;
import com.example.learnMongoRedis.service.AuthUserFacadeImpl;
import com.example.learnMongoRedis.global.wrapper.BaseResponseEntity;
import com.example.learnMongoRedis.service.dto.TokenRefreshDto;
import com.example.learnMongoRedis.service.dto.TokenResponseDto;
import com.example.learnMongoRedis.service.dto.UserSignInDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Log4j2
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final UserService userService;
    private final AuthUserFacadeImpl authUserFacade;
    private final TokenService tokenService;

    @PostMapping("/sign-in")
    public ResponseEntity<BaseResponseEntity<TokenResponseDto>> signIn(@Validated @RequestBody UserSignInDto userSignUpDto) {
        TokenResponseDto result = authUserFacade.signIn(userSignUpDto.getUsername(), userSignUpDto.getPassword());
        return BaseResponseEntity.ok(result, "success");
    }

    @PostMapping("/refresh")
    public ResponseEntity<BaseResponseEntity<TokenResponseDto>> tokenRefresh(@RequestBody TokenRefreshDto tokenRefreshDto) {
            TokenResponseDto result = authUserFacade.tokenRefresh(tokenRefreshDto.getRefreshToken());
            return BaseResponseEntity.ok(result, "success");
    }
}