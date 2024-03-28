package com.example.learnMongoRedis.global.security.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
@EqualsAndHashCode
public class AccessPayload {
    private final String userId;
    private final TokenType tokenType = TokenType.ACCESS_TOKEN;
}
