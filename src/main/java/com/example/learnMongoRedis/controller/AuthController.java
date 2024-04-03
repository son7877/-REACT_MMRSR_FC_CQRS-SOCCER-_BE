package com.example.learnMongoRedis.controller;

import com.example.learnMongoRedis.domain.TokenService;
import com.example.learnMongoRedis.domain.UserService;
import com.example.learnMongoRedis.global.security.JwtUtil;
import com.example.learnMongoRedis.global.security.model.AccessPayload;
import com.example.learnMongoRedis.service.AuthUserFacadeImpl;
import com.example.learnMongoRedis.global.wrapper.BaseResponseEntity;
import com.example.learnMongoRedis.service.SimulationMatch;
import com.example.learnMongoRedis.service.dto.TokenRefreshDto;
import com.example.learnMongoRedis.service.dto.TokenResponseDto;
import com.example.learnMongoRedis.service.dto.TokenValidationDto;
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
    private final AuthUserFacadeImpl authUserFacade;
    private final TokenService tokenService;
    private final SimulationMatch simulationMatch;

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

    @PostMapping("/validToken")
    public ResponseEntity<BaseResponseEntity<TokenResponseDto>> validationAccessToken(@RequestBody TokenValidationDto tokenValidationDto) {
        TokenResponseDto result = authUserFacade.validationAccessToken(
                tokenValidationDto.getAccessToken(),
                tokenValidationDto.getRefreshToken()
        );
        return BaseResponseEntity.ok(result, "success");
    }
    @PostMapping("/test")
    public void tokenRefresh() {
        simulationMatch.runSimulation();
    }

}