package com.example.backendpassengertransportation.controller;

import com.example.backendpassengertransportation.model.User;
import com.example.backendpassengertransportation.service.UserService;
import com.example.backendpassengertransportation.util.JwtUtil;
import com.example.backendpassengertransportation.util.ValidationUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;

// Контроллер для аутентификации и регистрации пользователей
@RestController
@RequestMapping("/auth")
public class AuthenticationController {

    @Autowired
    // Сервис для работы с пользователями
    private UserService userService;

    @Autowired
    // Утилита для работы с JWT-токенами
    private JwtUtil jwtUtil;

    // Аутентификация пользователя
    @Operation(
            summary = "Аутентификация пользователя",
            description = "Авторизует пользователя по ФИО, электронной почте и паролю, возвращает JWT-токен для доступа к защищенным ресурсам. " +
                    "После успешной аутентификации токен можно использовать для выполнения других запросов. " +
                    "Для выполнения других запросов необходимо авторизоваться: в Swagger UI нажмите кнопку 'Authorize' в верхней части страницы и введите полученный JWT-токен."
    )
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "JWT токен успешно создан."),
                    @ApiResponse(responseCode = "401", description = "Неверные учетные данные."),
                    @ApiResponse(responseCode = "400", description = "Некорректные данные запроса: пустые поля или неверный формат электронной почты.")
            }
    )
    @PostMapping("/login")
    public ResponseEntity<?> createAuthenticationToken(
            @Parameter(description = "Полное имя пользователя для входа", required = true)
            @RequestParam String passengerFullName,

            @Parameter(description = "Электронная почта пользователя для входа", required = true)
            @RequestParam String passengerEmail,

            @Parameter(description = "Пароль для входа", required = true)
            @RequestParam String password) {
        try {
            // Проверка на пустые значения
            if (passengerFullName == null || passengerFullName.isEmpty() ||
                    passengerEmail == null || passengerEmail.isEmpty() ||
                    password == null || password.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Все поля должны быть заполнены.");
            }

            // Проверка формата электронной почты
            if (!ValidationUtil.isValidEmailFormat(passengerEmail)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                        "Неверный формат электронной почты. Email должен быть полностью на английском языке. " +
                                "Допустимые домены: mail.ru, inbox.ru, yandex.ru, gmail.com. Пример корректной электронной почты: example123@gmail.com."
                );
            }

            // Поиск пользователя по ФИО и электронной почте
            User user = userService.getUserByFullNameAndEmail(passengerFullName, passengerEmail);

            // Проверка пароля
            if (!userService.verifyPassword(password, user.getPassword())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Неверные учетные данные.");
            }

            // Загрузка данных пользователя по электронной почте
            final UserDetails userDetails = userService.loadUserByEmail(passengerEmail);
            // Генерация JWT-токена с использованием электронной почты
            final String jwt = jwtUtil.generateToken(userDetails.getUsername());

            // Формирование ответа с токеном
            Map<String, String> response = new HashMap<>();
            response.put("token", jwt);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            // Обработка ошибки отсутствия пользователя или неверных данных
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Неверные учетные данные.");
        } catch (Exception e) {
            // Обработка других ошибок
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Произошла ошибка при аутентификации.");
        }
    }

    // Регистрация нового пользователя
    @Operation(
            summary = "Регистрация нового пользователя",
            description = "Создает нового пользователя и возвращает данные пользователя. " +
                    "После успешной регистрации можно выполнить аутентификацию через /auth/login, чтобы получить JWT-токен. " +
                    "Для выполнения других запросов необходимо авторизоваться: в Swagger UI нажмите кнопку 'Authorize' в верхней части страницы и введите полученный JWT-токен."
    )
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "201", description = "Пользователь успешно создан."),
                    @ApiResponse(responseCode = "400", description = "Некорректные данные запроса: пустые поля, неверный формат телефона, email или даты рождения."),
                    @ApiResponse(responseCode = "409", description = "Пользователь с таким ФИО, номером телефона или email уже существует.")
            }
    )
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(
            @Parameter(description = "Полное имя пользователя для регистрации", required = true)
            @RequestParam String passengerFullName,

            @Parameter(description = "Номер телефона пользователя в формате '+7 XXX XXX-XX-XX'", required = true)
            @RequestParam String passengerPhone,

            @Parameter(description = "Email пользователя для регистрации", required = true)
            @RequestParam String passengerEmail,

            @Parameter(description = "Дата рождения пользователя в формате 'dd.MM.yyyy'", required = true)
            @RequestParam String dateOfBirth,

            @Parameter(description = "Пароль для регистрации", required = true)
            @RequestParam String password) {
        try {
            // Парсинг даты рождения из строки в формате "dd.MM.yyyy"
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
            LocalDate birthDate = LocalDate.parse(dateOfBirth, formatter);
            java.sql.Date sqlBirthDate = java.sql.Date.valueOf(birthDate);

            // Создание нового пользователя через сервис
            User newUser = userService.createUser(
                    passengerFullName,
                    passengerPhone,
                    passengerEmail,
                    sqlBirthDate,
                    password
            );
            // Возврат созданного пользователя
            return ResponseEntity.status(HttpStatus.CREATED).body(newUser);
        } catch (DateTimeParseException e) {
            // Обработка ошибки некорректного формата даты
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Неверный формат даты рождения. Используйте 'dd.MM.yyyy'.");
        } catch (IllegalArgumentException e) {
            // Обработка ошибок валидации из UserService
            String errorMessage = e.getMessage();
            if (errorMessage.contains("Все поля должны быть заполнены")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMessage);
            } else if (errorMessage.contains("Неверный формат телефона")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMessage);
            } else if (errorMessage.contains("Неверный формат электронной почты")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                        "Неверный формат электронной почты. Email должен быть полностью на английском языке. " +
                                "Допустимые домены: mail.ru, inbox.ru, yandex.ru, gmail.com. Пример корректной электронной почты: example123@gmail.com."
                );
            } else if (errorMessage.contains("Дата рождения не может быть позже текущей даты") ||
                    errorMessage.contains("Наша транспортная компания не уверена") ||
                    errorMessage.contains("У вас прекрасный возраст")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMessage);
            } else if (errorMessage.contains("Пользователь с таким номером телефона уже существует") ||
                    errorMessage.contains("Пользователь с таким email уже существует")) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(errorMessage);
            }
            // Обработка других возможных ошибок
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMessage);
        } catch (Exception e) {
            // Обработка непредвиденных ошибок
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Произошла ошибка при регистрации пользователя.");
        }
    }
}