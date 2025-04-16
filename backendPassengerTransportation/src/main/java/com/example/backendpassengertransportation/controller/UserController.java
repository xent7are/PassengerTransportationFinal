package com.example.backendpassengertransportation.controller;

import com.example.backendpassengertransportation.model.User;
import com.example.backendpassengertransportation.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

// Контроллер для управления пользователями, предоставляет CRUD-операции
@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    // Сервис для работы с пользователями
    private UserService userService;

    // Получение всех пользователей
    @Operation(
            summary = "Получение списка всех пользователей",
            description = "Возвращает список всех пользователей, доступных в системе. " +
                    "Для выполнения запроса необходимо авторизоваться: в Swagger UI нажмите кнопку 'Authorize' в верхней части страницы и введите валидный JWT-токен. " +
                    "Если пользователи отсутствуют, возвращается статус 404 с сообщением об ошибке. " +
                    "При отсутствии доступа возвращается статус 403. " +
                    "При внутренней ошибке сервера возвращается статус 500 с пустым списком."
    )
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "Список пользователей успешно получен."),
                    @ApiResponse(responseCode = "401", description = "Недействительный или отсутствующий токен."),
                    @ApiResponse(responseCode = "403", description = "Нет доступа к выполнению операции."),
                    @ApiResponse(responseCode = "404", description = "Пользователи не найдены."),
                    @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера.")
            }
    )
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("")
    public ResponseEntity<?> getAllUsers() {
        try {
            // Получение списка всех пользователей через сервис
            List<User> users = userService.getAllUsers();
            if (users.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Пользователи не найдены.");
            }
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            // Обработка внутренней ошибки сервера
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.emptyList());
        }
    }

    // Получение пользователя по идентификатору
    @Operation(
            summary = "Получение пользователя по ID",
            description = "Возвращает пользователя по указанному идентификатору. " +
                    "Для выполнения запроса необходимо авторизоваться: в Swagger UI нажмите кнопку 'Authorize' в верхней части страницы и введите валидный JWT-токен. " +
                    "Если пользователь не найден, возвращается статус 404 с сообщением об ошибке. " +
                    "При отсутствии доступа возвращается статус 403. " +
                    "При внутренней ошибке сервера возвращается статус 500 с null."
    )
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "Пользователь успешно получен."),
                    @ApiResponse(responseCode = "401", description = "Недействительный или отсутствующий токен."),
                    @ApiResponse(responseCode = "403", description = "Нет доступа к выполнению операции."),
                    @ApiResponse(responseCode = "404", description = "Пользователь не найден."),
                    @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера.")
            }
    )
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(
            @Parameter(description = "Идентификатор пользователя", required = true)
            @PathVariable String id) {
        try {
            // Поиск пользователя по идентификатору через сервис
            User user = userService.getUserById(id);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Пользователь с ID " + id + " не найден.");
            }
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            // Обработка внутренней ошибки сервера
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    // Получение пользователя по email
    @Operation(
            summary = "Получение пользователя по email",
            description = "Возвращает пользователя по указанному email. " +
                    "Для выполнения запроса необходимо авторизоваться: в Swagger UI нажмите кнопку 'Authorize' в верхней части страницы и введите валидный JWT-токен. " +
                    "Если пользователь не найден, возвращается статус 404 с сообщением об ошибке. " +
                    "При отсутствии доступа возвращается статус 403."
    )
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "Пользователь успешно получен."),
                    @ApiResponse(responseCode = "401", description = "Недействительный или отсутствующий токен."),
                    @ApiResponse(responseCode = "403", description = "Нет доступа к выполнению операции."),
                    @ApiResponse(responseCode = "404", description = "Пользователь не найден.")
            }
    )
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/user-by-email")
    public ResponseEntity<?> getUserByEmail(
            @Parameter(description = "Email пользователя для поиска", required = true)
            @RequestParam String email) {
        try {
            // Поиск пользователя по email через сервис
            User user = userService.getUserByEmail(email);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Пользователь с email " + email + " не найден.");
            }
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            // Обработка внутренней ошибки сервера
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    // Создание нового пользователя
    @Operation(
            summary = "Создание нового пользователя",
            description = "Создает нового пользователя с указанными данными. " +
                    "Для выполнения запроса необходимо авторизоваться: в Swagger UI нажмите кнопку 'Authorize' в верхней части страницы и введите валидный JWT-токен. " +
                    "При успешном создании возвращается статус 201 и созданный пользователь. " +
                    "Если переданы некорректные параметры, возвращается статус 400 с сообщением об ошибке. " +
                    "При отсутствии доступа возвращается статус 403. " +
                    "Дата рождения должна быть в формате 'dd.MM.yyyy'."
    )
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "201", description = "Пользователь успешно создан."),
                    @ApiResponse(responseCode = "401", description = "Недействительный или отсутствующий токен."),
                    @ApiResponse(responseCode = "403", description = "Нет доступа к выполнению операции."),
                    @ApiResponse(responseCode = "400", description = "Некорректные параметры.")
            }
    )
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("")
    public ResponseEntity<?> createUser(
            @Parameter(description = "Полное имя пользователя", required = true)
            @RequestParam String passengerFullName,
            @Parameter(description = "Номер телефона пользователя в формате '+7 XXX XXX-XX-XX'", required = true)
            @RequestParam String passengerPhone,
            @Parameter(description = "Email пользователя", required = true)
            @RequestParam String passengerEmail,
            @Parameter(description = "Дата рождения пользователя в формате 'dd.MM.yyyy'", required = true)
            @RequestParam String dateOfBirth,
            @Parameter(description = "Пароль пользователя", required = true)
            @RequestParam String password) {
        try {
            // Парсинг даты из формата dd.MM.yyyy
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
            LocalDate birthDate = LocalDate.parse(dateOfBirth, formatter);
            // Преобразование в java.sql.Date для сохранения в формате yyyy-MM-dd
            java.sql.Date sqlBirthDate = java.sql.Date.valueOf(birthDate);
            // Создание нового пользователя через сервис
            User newUser = userService.createUser(passengerFullName, passengerPhone, passengerEmail, sqlBirthDate, password);
            return ResponseEntity.status(HttpStatus.CREATED).body(newUser);
        } catch (DateTimeParseException e) {
            // Обработка ошибки некорректного формата даты
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Неверный формат даты рождения. Используйте 'dd.MM.yyyy'.");
        } catch (IllegalArgumentException e) {
            // Обработка ошибки некорректных параметров
            String errorMessage = e.getMessage();
            if (errorMessage.contains("Неверный формат электронной почты")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                        "Неверный формат электронной почты. Email должен быть полностью на английском языке. " +
                                "Допустимые домены: mail.ru, inbox.ru, yandex.ru, gmail.com. Пример корректной электронной почты: example123@gmail.com."
                );
            } else if (errorMessage.contains("Дата рождения не может быть позже текущей даты") ||
                    errorMessage.contains("Наша транспортная компания не уверена") ||
                    errorMessage.contains("У вас прекрасный возраст")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMessage);
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMessage);
        }
    }

    // Обновление пользователя по идентификатору
    @Operation(
            summary = "Обновление пользователя по ID",
            description = "Обновляет пользователя с указанным идентификатором. " +
                    "Для выполнения запроса необходимо авторизоваться: в Swagger UI нажмите кнопку 'Authorize' в верхней части страницы и введите валидный JWT-токен. " +
                    "При успешном обновлении возвращается статус 200 и обновленный пользователь. " +
                    "Если пользователь не найден, возвращается статус 404 с сообщением об ошибке. " +
                    "При отсутствии доступа возвращается статус 403. " +
                    "При некорректных параметрах возвращается статус 400 с сообщением об ошибке. " +
                    "Дата рождения должна быть в формате 'dd.MM.yyyy' (если указана)."
    )
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "Пользователь успешно обновлен."),
                    @ApiResponse(responseCode = "401", description = "Недействительный или отсутствующий токен."),
                    @ApiResponse(responseCode = "403", description = "Нет доступа к выполнению операции."),
                    @ApiResponse(responseCode = "400", description = "Некорректные параметры."),
                    @ApiResponse(responseCode = "404", description = "Пользователь не найден.")
            }
    )
    @SecurityRequirement(name = "bearerAuth")
    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(
            @Parameter(description = "Идентификатор пользователя для обновления", required = true)
            @PathVariable String id,
            @Parameter(description = "Новое полное имя пользователя (опционально)", required = false)
            @RequestParam(required = false) String passengerFullName,
            @Parameter(description = "Новый номер телефона пользователя в формате '+7 XXX XXX-XX-XX' (опционально)", required = false)
            @RequestParam(required = false) String passengerPhone,
            @Parameter(description = "Новый email пользователя (опционально)", required = false)
            @RequestParam(required = false) String passengerEmail,
            @Parameter(description = "Новая дата рождения пользователя в формате 'dd.MM.yyyy' (опционально)", required = false)
            @RequestParam(required = false) String dateOfBirth,
            @Parameter(description = "Новый пароль пользователя (опционально)", required = false)
            @RequestParam(required = false) String password) {
        try {
            java.sql.Date sqlBirthDate = null;
            if (dateOfBirth != null) {
                // Парсинг даты из формата dd.MM.yyyy
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
                LocalDate birthDate = LocalDate.parse(dateOfBirth, formatter);
                // Преобразование в java.sql.Date для сохранения в формате yyyy-MM-dd
                sqlBirthDate = java.sql.Date.valueOf(birthDate);
            }
            // Обновление пользователя через сервис
            User updatedUser = userService.updateUser(id, passengerFullName, passengerPhone, passengerEmail, sqlBirthDate, password);
            return ResponseEntity.ok(updatedUser);
        } catch (DateTimeParseException e) {
            // Обработка ошибки некорректного формата даты
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Неверный формат даты рождения. Используйте 'dd.MM.yyyy'.");
        } catch (IllegalArgumentException e) {
            // Обработка ошибки некорректных параметров
            String errorMessage = e.getMessage();
            if (errorMessage.contains("Неверный формат электронной почты")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                        "Неверный формат электронной почты. Email должен быть полностью на английском языке. " +
                                "Допустимые домены: mail.ru, inbox.ru, yandex.ru, gmail.com. Пример корректной электронной почты: example123@gmail.com."
                );
            } else if (errorMessage.contains("Дата рождения не может быть позже текущей даты") ||
                    errorMessage.contains("Наша транспортная компания не уверена")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMessage);
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMessage);
        } catch (NoSuchElementException e) {
            // Обработка ошибки отсутствия пользователя
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    // Удаление пользователя по идентификатору
    @Operation(
            summary = "Удаление пользователя по ID",
            description = "Удаляет пользователя по указанному идентификатору. " +
                    "Для выполнения запроса необходимо авторизоваться: в Swagger UI нажмите кнопку 'Authorize' в верхней части страницы и введите валидный JWT-токен. " +
                    "При успешном удалении возвращается статус 200 с сообщением об успехе. " +
                    "Если пользователь не найден, возвращается статус 404 с сообщением об ошибке. " +
                    "При отсутствии доступа возвращается статус 403."
    )
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "Пользователь успешно удален."),
                    @ApiResponse(responseCode = "401", description = "Недействительный или отсутствующий токен."),
                    @ApiResponse(responseCode = "403", description = "Нет доступа к выполнению операции."),
                    @ApiResponse(responseCode = "404", description = "Пользователь не найден.")
            }
    )
    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteUser(
            @Parameter(description = "Идентификатор пользователя для удаления", required = true)
            @PathVariable String id) {
        try {
            // Удаление пользователя через сервис
            userService.deleteUser(id);
            return ResponseEntity.ok("Пользователь успешно удален.");
        } catch (IllegalArgumentException e) {
            // Обработка ошибки отсутствия пользователя
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }
}