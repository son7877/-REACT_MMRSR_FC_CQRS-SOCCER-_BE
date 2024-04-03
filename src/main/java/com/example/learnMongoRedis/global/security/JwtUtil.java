package com.example.learnMongoRedis.global.security;

import com.example.learnMongoRedis.global.error_handler.AppError;
import com.example.learnMongoRedis.global.resouce.Constants;
import com.example.learnMongoRedis.global.security.model.AccessPayload;
import com.example.learnMongoRedis.global.security.model.RefreshPayload;
import com.example.learnMongoRedis.global.security.model.TokenType;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtil {
    private final String secretKey;

    private Key secretKeyHash(){
        return Keys.hmacShaKeyFor(secretKey.getBytes());
    }

    public JwtUtil(@Qualifier("jwtSecretKey") String secretKey) {
        this.secretKey = secretKey;
    }

    public String createAccessToken(AccessPayload accessPayload) {
        Map<String, Object> headerMap = new HashMap<String, Object>();
        headerMap.put("typ", "JWT");
        headerMap.put("alg", "HS256");

        Map<String, Object> claims = new HashMap<String, Object>();
        claims.put("userId", accessPayload.getUserId());
        claims.put("tokenType", accessPayload.getTokenType().getValue());

        Date expireTime = new Date();
        expireTime.setTime(expireTime.getTime() + Constants.ACCESS_TOKEN_EXPIRE); // 10분
//        expireTime.setTime(expireTime.getTime() + 3000); // 1초

        return Jwts.builder()
                .setHeader(headerMap)
                .setClaims(claims)
                .setExpiration(expireTime)
                .signWith(secretKeyHash(), io.jsonwebtoken.SignatureAlgorithm.HS256)
                .compact();
    }

    public String createRefreshToken(RefreshPayload refreshPayload) {
        Map<String, Object> headerMap = new HashMap<>();
        headerMap.put("typ", "JWT");
        headerMap.put("alg", "HS256");

        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", refreshPayload.getUserId());
        claims.put("tokenType", refreshPayload.getTokenType().getValue());

        Date expireTime = new Date();
        expireTime.setTime(expireTime.getTime() + Constants.REFRESH_TOKEN_EXPIRE);
        return Jwts.builder()
                .setHeader(headerMap)
                .setClaims(claims)
                .setExpiration(expireTime)
                .signWith(secretKeyHash(), io.jsonwebtoken.SignatureAlgorithm.HS256)
                .compact();
    }

    public AccessPayload validateAccessToken(String token) throws Exception{
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(secretKeyHash())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            if (claims.get("tokenType", Integer.class) != TokenType.ACCESS_TOKEN.getValue()) {
                throw new AppError.Unexpected.IllegalArgumentException("토큰 타입이 잘못되었습니다.");
            }
            return new AccessPayload(claims.get("userId", String.class));
        } catch (ExpiredJwtException e) {
            throw new AppError.Expected.ValidationFailedException("토큰이 만료되었습니다.");
        } catch (JwtException e) {
            throw new AppError.Expected.ValidationFailedException("토큰이 유효하지 않습니다.");
        }
    }


    public RefreshPayload validateRefreshToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(secretKeyHash())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            if (claims.get("tokenType", Integer.class) != TokenType.REFRESH_TOKEN.getValue()) {
                throw new AppError.Unexpected.IllegalArgumentException("올바른 토큰타입이 아닙니다.");
            }

            return new RefreshPayload(claims.get("userId", String.class));
        } catch (ExpiredJwtException e) {
            throw new AppError.Expected.ValidationFailedException("토큰이 만료되었습니다.");
        } catch (JwtException e) {
            throw new AppError.Expected.ValidationFailedException("토큰이 유효하지 않습니다.");
        }
    }
}
