package com.example.learnMongoRedis.global.security.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
@EqualsAndHashCode
public class RefreshPayload {
    @NonNull
    private final String userId;
    private final TokenType tokenType = TokenType.REFRESH_TOKEN;
}