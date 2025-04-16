package com.example.backendpassengertransportation.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;

// Конфигурация Swagger для документации API
// Определяет общую информацию об API и схему безопасности для использования JWT-токенов
@OpenAPIDefinition(
        info = @Info(
                title = "Passenger Transportation Booking System API",
                description = "API для управления системой бронирования билетов на пассажирские перевозки. " +
                        "Позволяет пользователям осуществлять бронирование билетов, управлять маршрутами, " +
                        "проверять доступность мест и получать информацию о пассажирах.",
                version = "1.0.0"
        )
)
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT"
)
public class OpenApiConfig {
        // Конфигурация для Swagger
}