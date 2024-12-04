package com.sk.backend.config;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtTokenProvider jwtTokenProvider;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        if ("/api/post/add".equals(request.getRequestURI())) {
            String accessToken = jwtTokenProvider.resolveToken(request);  // access token 추출
            if (accessToken != null && jwtTokenProvider.validateToken(accessToken)) { // access token이 유효한 경우
                String email = jwtTokenProvider.getEmailFromToken(accessToken);
                setAuthenticationContext(request, email);
                filterChain.doFilter(request, response);  // 필터 통과
            } else { // access token이 유효하지 않은 경우
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("{\"message\": \"Access token expired\"}");  // access token이 만료되었음을 알림
                response.getWriter().flush();
                return;
            }
        }
        filterChain.doFilter(request, response);
    }

    private void setAuthenticationContext(HttpServletRequest request, String email) {
        if (SecurityContextHolder.getContext().getAuthentication() == null && email != null) {
            UserDetails userDetails = userDetailsService.loadUserByUsername(email);
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
    }
}

