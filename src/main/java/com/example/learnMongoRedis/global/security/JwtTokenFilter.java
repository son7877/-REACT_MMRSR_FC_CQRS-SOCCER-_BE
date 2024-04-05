package com.example.learnMongoRedis.global.security;

import com.example.learnMongoRedis.domain.UserService;
import com.example.learnMongoRedis.global.security.model.AccessPayload;
import com.example.learnMongoRedis.global.wrapper.BaseResponseEntity;
import com.google.gson.Gson;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;

// JwtTokenFilter 클래스: 요청이 들어올 때마다 실행되는 필터
@Component
@RequiredArgsConstructor
@Log4j2
public class JwtTokenFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserService userService;

    private static final String[] PERMIT_URL_ARRAY = {
            "/api/v1/auth/sign-in",
            "/api/v1/auth/refresh",
            "/error"
    };

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String path = request.getRequestURI();
        boolean isPermitted = Arrays.stream(PERMIT_URL_ARRAY).anyMatch(path::startsWith);

        if (isPermitted) {
            filterChain.doFilter(request, response);
            return;
        }
        
        String token = request.getHeader("Authorization");
        if (token != null) {
            try {
                if (token.startsWith("Bearer ")) token = token.substring(7);
                AccessPayload payload = jwtUtil.validateAccessToken(token);
                UserDetails userDetails = userService.getUserDetail(payload.getUserId());
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(authentication);
                filterChain.doFilter(request, response);
            } catch (Exception e) {
                handleInvalidAccessToken(response, e.getMessage());
            }
        } else  {
            handleInvalidAccessToken(response, "헤더에 토큰이 없습니다.");
        }
    }

    // 유효하지 않은 액세스 토큰 처리
    private void handleInvalidAccessToken(HttpServletResponse response, String errorMessage) throws IOException {
        String result = new Gson().toJson(BaseResponseEntity.fail(HttpStatus.UNAUTHORIZED, errorMessage).getBody());
        response.setContentType("application/json; charset=UTF-8");
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        PrintWriter writer = response.getWriter();
        writer.println(result);
    }
}