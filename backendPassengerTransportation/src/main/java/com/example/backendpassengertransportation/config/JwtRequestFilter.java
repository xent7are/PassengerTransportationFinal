package com.example.backendpassengertransportation.config;

import com.example.backendpassengertransportation.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

// Фильтр для обработки JWT-токенов в запросах
// Проверяет наличие и валидность JWT-токена в заголовке Authorization,
// извлекает электронную почту и устанавливает аутентификацию в контексте безопасности
@Component
public class JwtRequestFilter extends OncePerRequestFilter {

    @Autowired
    // Сервис для загрузки данных пользователя
    private UserDetailsService userDetailsService;

    // Метод фильтрации запросов
    // Пропускает запросы к /auth/** без проверки токена, для остальных запросов проверяет JWT-токен и устанавливает аутентификацию
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        // Пропуск запросов к /auth/**
        String path = request.getRequestURI();
        if (path.startsWith("/auth/")) {
            chain.doFilter(request, response);
            return;
        }

        // Извлечение заголовка Authorization
        final String authorizationHeader = request.getHeader("Authorization");
        String email = null;
        String jwt = null;

        // Проверка наличия токена в заголовке и извлечение электронной почты
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwt = authorizationHeader.substring(7);
            email = JwtUtil.extractUsername(jwt);
        }

        // Установка аутентификации в контексте безопасности, если пользователь найден и токен валиден
        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(email);
            if (JwtUtil.validateToken(jwt, userDetails.getUsername())) {
                UsernamePasswordAuthenticationToken authenticationToken =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            }
        }
        // Продолжение цепочки фильтров
        chain.doFilter(request, response);
    }
}