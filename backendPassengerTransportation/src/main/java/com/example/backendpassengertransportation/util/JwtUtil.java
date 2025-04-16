package com.example.backendpassengertransportation.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.springframework.stereotype.Component;

// Утилита для работы с JWT-токенами, предоставляет методы генерации и валидации токенов
@Component
public class JwtUtil {

    // Секретный ключ для подписи токенов (должен быть длиной не менее 32 символов для HS256)
    private static final String SECRET = "2C74A653F1225125803D023FF386CE931A6F60BA2247E85642259971951216D3";
    private static final Key SECRET_KEY = new SecretKeySpec(SECRET.getBytes(), SignatureAlgorithm.HS256.getJcaName());

    // Время жизни токена (10 часов)
    private static final long EXPIRATION_TIME = 1000 * 60 * 60 * 10;

    // Извлечение имени пользователя из токена
    // В данном контексте username представляет электронную почту пользователя
    public static String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // Извлечение даты истечения срока действия токена
    public static Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    // Универсальный метод для извлечения данных из токена
    public static <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    // Извлечение всех данных из токена
    private static Claims extractAllClaims(String token) {
        return Jwts.parser()
                .setSigningKey(SECRET_KEY)
                .parseClaimsJws(token)
                .getBody();
    }

    // Проверка, истек ли срок действия токена
    private static Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    // Генерация токена для пользователя
    public static String generateToken(String username) {
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, username);
    }

    // Создание токена с заданными данными и временем жизни
    private static String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
                .compact();
    }

    // Проверка валидности токена
    public static Boolean validateToken(String token, String username) {
        final String extractedUsername = extractUsername(token);
        return (extractedUsername.equals(username) && !isTokenExpired(token));
    }
}