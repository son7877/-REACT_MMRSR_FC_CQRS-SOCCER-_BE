package com.example.learnMongoRedis.service.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@EqualsAndHashCode
public class UserSignInDto {
    @NotNull
    @NotEmpty
    private final String password;

    @NotNull
    @NotEmpty
    private final String username;

    @JsonCreator
    public UserSignInDto(@JsonProperty("password") String password,
                         @JsonProperty("username") String username) {
        this.password = password;
        this.username = username;
    }
}
