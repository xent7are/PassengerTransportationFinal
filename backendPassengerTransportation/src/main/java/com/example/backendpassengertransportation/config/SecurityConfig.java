package com.example.backendpassengertransportation.config;

import com.example.backendpassengertransportation.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

// Конфигурация безопасности приложения
// Настраивает правила доступа, отключает CSRF, использует JWT для аутентификации и определяет политику сессий
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    // Сервис для работы с пользователями
    private UserService userService;

    @Autowired
    // Фильтр для обработки JWT-токенов
    private JwtRequestFilter jwtRequestFilter;

    // Бин для BCryptPasswordEncoder
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Настройка цепочки фильтров безопасности
    // Разрешает доступ к /auth/** и Swagger без аутентификации, для остальных запросов требует аутентификацию
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // Отключение защиты от CSRF
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/**", "/swagger-ui/**", "/v3/api-docs/**").permitAll() // Разрешённые пути
                        .anyRequest().authenticated() // Все остальные пути требуют аутентификации
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS) // Без состояния сессий
                )
                .addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class); // Добавление JWT-фильтра
        return http.build();
    }
}