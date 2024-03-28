package com.example.learnMongoRedis.controller;

import com.example.learnMongoRedis.domain.model.User;
import com.example.learnMongoRedis.service.AuthUserFacadeImpl;
import com.example.learnMongoRedis.domain.TokenService;
import com.example.learnMongoRedis.domain.UserService;
import com.example.learnMongoRedis.global.wrapper.BaseResponseEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final AuthUserFacadeImpl authUserFacade;
    private final TokenService tokenService;

    @GetMapping("/all")
    public ResponseEntity<BaseResponseEntity<List<User>>> getAllUsers() {
        return BaseResponseEntity.ok(userService.getUserList(), "success");
    }

}