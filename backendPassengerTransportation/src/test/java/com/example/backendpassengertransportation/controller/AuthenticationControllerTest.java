package com.example.backendpassengertransportation.controller;

import com.example.backendpassengertransportation.model.User;
import com.example.backendpassengertransportation.service.UserService;
import com.example.backendpassengertransportation.util.JwtUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;

import java.sql.Date;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthenticationController authenticationController;

    // Вспомогательный метод для создания тестового пользователя
    private User createTestUser(String fullName, String phone, String email, Date birthDate, String password) {
        User user = new User();
        user.setPassengerFullName(fullName);
        user.setPassengerPhone(phone);
        user.setPassengerEmail(email);
        user.setDateOfBirth(birthDate);
        user.setPassword(password);
        return user;
    }

    /**
     * Тест успешной аутентификации пользователя.
     * Проверка возврата JWT-токена и статуса 200.
     */
    @Test
    void testCreateAuthenticationToken_Success() {
        // Тестовые данные
        String fullName = "Стебунов Никита Юрьевич";
        String email = "stebunov@gmail.com";
        String password = "password123";
        User user = createTestUser(fullName, "+7 999 123-45-67", email, Date.valueOf(LocalDate.now().minusYears(30)), "$2a$10$hashedPassword");
        String jwtToken = "jwt.token.value";

        // Создание UserDetails
        UserDetails userDetails = new org.springframework.security.core.userdetails.User(
                email, user.getPassword(), new ArrayList<>()
        );

        // Настройка заглушек
        when(userService.getUserByFullNameAndEmail(fullName, email)).thenReturn(user);
        when(userService.verifyPassword(password, user.getPassword())).thenReturn(true);
        when(userService.loadUserByEmail(email)).thenReturn(userDetails);

        // Мокация статического метода JwtUtil.generateToken
        try (MockedStatic<JwtUtil> jwtUtilMock = mockStatic(JwtUtil.class)) {
            jwtUtilMock.when(() -> JwtUtil.generateToken(email)).thenReturn(jwtToken);

            // Вызов метода контроллера
            ResponseEntity<?> response = authenticationController.createAuthenticationToken(fullName, email, password);

            // Проверка статуса ответа
            assertEquals(HttpStatus.OK.value(), response.getStatusCodeValue());
            // Проверка тела ответа
            Map<String, String> result = (Map<String, String>) response.getBody();
            assertEquals(jwtToken, result.get("token"));

            // Проверка вызова мока
            jwtUtilMock.verify(() -> JwtUtil.generateToken(email), times(1));
        }
    }

    /**
     * Тест аутентификации с пустыми полями.
     * Проверка возврата статуса 400 и сообщения об ошибке.
     */
    @Test
    void testCreateAuthenticationToken_EmptyFields() {
        // Вызов метода контроллера с пустыми значениями
        ResponseEntity<?> response = authenticationController.createAuthenticationToken("", "stebunov@gmail.com", "password123");

        // Проверка статуса ответа
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatusCodeValue());
        // Проверка тела ответа
        assertEquals("Все поля должны быть заполнены.", response.getBody());

        // Проверка с null значениями
        response = authenticationController.createAuthenticationToken(null, "stebunov@gmail.com", "password123");
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatusCodeValue());
        assertEquals("Все поля должны быть заполнены.", response.getBody());

        response = authenticationController.createAuthenticationToken("Стебунов Никита Юрьевич", "", "password123");
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatusCodeValue());
        assertEquals("Все поля должны быть заполнены.", response.getBody());

        response = authenticationController.createAuthenticationToken("Стебунов Никита Юрьевич", "stebunov@gmail.com", "");
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatusCodeValue());
        assertEquals("Все поля должны быть заполнены.", response.getBody());
    }

    /**
     * Тест аутентификации с некорректным форматом email.
     * Проверка возврата статуса 400 и сообщения об ошибке.
     */
    @Test
    void testCreateAuthenticationToken_InvalidEmailFormat() {
        // Вызов метода контроллера с некорректным email
        ResponseEntity<?> response = authenticationController.createAuthenticationToken(
                "Стебунов Никита Юрьевич", "invalid-email", "password123");

        // Проверка статуса ответа
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatusCodeValue());
        // Проверка тела ответа
        assertEquals(
                "Неверный формат электронной почты. Email должен быть полностью на английском языке. " +
                        "Допустимые домены: mail.ru, inbox.ru, yandex.ru, gmail.com. Пример корректной электронной почты: example123@gmail.com.",
                response.getBody());
    }

    /**
     * Тест аутентификации с неверными учетными данными (пользователь не найден).
     * Проверка возврата статуса 401 и сообщения об ошибке.
     */
    @Test
    void testCreateAuthenticationToken_UserNotFound() {
        // Мокирование сервиса: выброс исключения
        when(userService.getUserByFullNameAndEmail("Стебунов Никита Юрьевич", "stebunov@gmail.com"))
                .thenThrow(new IllegalArgumentException("Пользователь не найден."));

        // Вызов метода контроллера
        ResponseEntity<?> response = authenticationController.createAuthenticationToken(
                "Стебунов Никита Юрьевич", "stebunov@gmail.com", "password123");

        // Проверка статуса ответа
        assertEquals(HttpStatus.UNAUTHORIZED.value(), response.getStatusCodeValue());
        // Проверка тела ответа
        assertEquals("Неверные учетные данные.", response.getBody());
    }

    /**
     * Тест аутентификации с неверным паролем.
     * Проверка возврата статуса 401 и сообщения об ошибке.
     */
    @Test
    void testCreateAuthenticationToken_IncorrectPassword() {
        // Создание тестовых данных
        String fullName = "Стебунов Никита Юрьевич";
        String email = "stebunov@gmail.com";
        User user = createTestUser(fullName, "+7 999 123-45-67", email, Date.valueOf(LocalDate.now().minusYears(30)), "$2a$10$hashedPassword");

        // Мокирование сервиса
        when(userService.getUserByFullNameAndEmail(fullName, email)).thenReturn(user);
        when(userService.verifyPassword("wrongPassword", user.getPassword())).thenReturn(false);

        // Вызов метода контроллера
        ResponseEntity<?> response = authenticationController.createAuthenticationToken(fullName, email, "wrongPassword");

        // Проверка статуса ответа
        assertEquals(HttpStatus.UNAUTHORIZED.value(), response.getStatusCodeValue());
        // Проверка тела ответа
        assertEquals("Неверные учетные данные.", response.getBody());
    }

    /**
     * Тест аутентификации при внутренней ошибке сервера.
     * Проверка возврата статуса 500 и сообщения об ошибке.
     */
    @Test
    void testCreateAuthenticationToken_InternalServerError() {
        // Мокирование сервиса: выброс исключения
        when(userService.getUserByFullNameAndEmail("Стебунов Никита Юрьевич", "stebunov@gmail.com"))
                .thenThrow(new RuntimeException("Внутренняя ошибка"));

        // Вызов метода контроллера
        ResponseEntity<?> response = authenticationController.createAuthenticationToken(
                "Стебунов Никита Юрьевич", "stebunov@gmail.com", "password123");

        // Проверка статуса ответа
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), response.getStatusCodeValue());
        // Проверка тела ответа
        assertEquals("Произошла ошибка при аутентификации.", response.getBody());
    }

    /**
     * Тест успешной регистрации нового пользователя.
     * Проверка возврата статуса 201 и данных пользователя.
     */
    @Test
    void testRegisterUser_Success() {
        // Создание тестовых данных
        String fullName = "Стебунов Никита Юрьевич";
        String phone = "+7 999 123-45-67";
        String email = "stebunov@gmail.com";
        String dateOfBirth = "01.01.1990";
        String password = "password123";
        User user = createTestUser(fullName, phone, email, Date.valueOf(LocalDate.of(1990, 1, 1)), "$2a$10$hashedPassword");

        // Мокирование сервиса
        when(userService.createUser(fullName, phone, email, Date.valueOf(LocalDate.of(1990, 1, 1)), password)).thenReturn(user);

        // Вызов метода контроллера
        ResponseEntity<?> response = authenticationController.registerUser(fullName, phone, email, dateOfBirth, password);

        // Проверка статуса ответа
        assertEquals(HttpStatus.CREATED.value(), response.getStatusCodeValue());
        // Проверка тела ответа
        User result = (User) response.getBody();
        assertEquals(fullName, result.getPassengerFullName());
        assertEquals(phone, result.getPassengerPhone());
        assertEquals(email, result.getPassengerEmail());
        assertEquals(Date.valueOf(LocalDate.of(1990, 1, 1)), result.getDateOfBirth());
    }

    /**
     * Тест регистрации с некорректным форматом даты рождения.
     * Проверка возврата статуса 400 и сообщения об ошибке.
     */
    @Test
    void testRegisterUser_InvalidDateFormat() {
        // Вызов метода контроллера с некорректной датой
        ResponseEntity<?> response = authenticationController.registerUser(
                "Стебунов Никита Юрьевич", "+7 999 123-45-67", "stebunov@gmail.com", "2025-01-01", "password123");

        // Проверка статуса ответа
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatusCodeValue());
        // Проверка тела ответа
        assertEquals("Неверный формат даты рождения. Используйте 'dd.MM.yyyy'.", response.getBody());
    }

    /**
     * Тест регистрации с пустыми полями.
     * Проверка возврата статуса 400 и сообщения об ошибке.
     */
    @Test
    void testRegisterUser_EmptyFields() {
        // Мокирование сервиса: выброс исключения
        when(userService.createUser("", "+7 999 123-45-67", "stebunov@gmail.com", Date.valueOf(LocalDate.of(1990, 1, 1)), "password123"))
                .thenThrow(new IllegalArgumentException("Все поля должны быть заполнены."));

        // Вызов метода контроллера
        ResponseEntity<?> response = authenticationController.registerUser(
                "", "+7 999 123-45-67", "stebunov@gmail.com", "01.01.1990", "password123");

        // Проверка статуса ответа
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatusCodeValue());
        // Проверка тела ответа
        assertEquals("Все поля должны быть заполнены.", response.getBody());

        // Проверка других пустых полей
        when(userService.createUser("Стебунов Никита Юрьевич", "", "stebunov@gmail.com", Date.valueOf(LocalDate.of(1990, 1, 1)), "password123"))
                .thenThrow(new IllegalArgumentException("Все поля должны быть заполнены."));
        response = authenticationController.registerUser(
                "Стебунов Никита Юрьевич", "", "stebunov@gmail.com", "01.01.1990", "password123");
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatusCodeValue());
        assertEquals("Все поля должны быть заполнены.", response.getBody());

        when(userService.createUser("Стебунов Никита Юрьевич", "+7 999 123-45-67", "", Date.valueOf(LocalDate.of(1990, 1, 1)), "password123"))
                .thenThrow(new IllegalArgumentException("Все поля должны быть заполнены."));
        response = authenticationController.registerUser(
                "Стебунов Никита Юрьевич", "+7 999 123-45-67", "", "01.01.1990", "password123");
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatusCodeValue());
        assertEquals("Все поля должны быть заполнены.", response.getBody());

        when(userService.createUser("Стебунов Никита Юрьевич", "+7 999 123-45-67", "stebunov@gmail.com", Date.valueOf(LocalDate.of(1990, 1, 1)), ""))
                .thenThrow(new IllegalArgumentException("Все поля должны быть заполнены."));
        response = authenticationController.registerUser(
                "Стебунов Никита Юрьевич", "+7 999 123-45-67", "stebunov@gmail.com", "01.01.1990", "");
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatusCodeValue());
        assertEquals("Все поля должны быть заполнены.", response.getBody());
    }

    /**
     * Тест регистрации с некорректным форматом телефона.
     * Проверка возврата статуса 400 и сообщения об ошибке.
     */
    @Test
    void testRegisterUser_InvalidPhoneFormat() {
        // Мокирование сервиса: выброс исключения
        when(userService.createUser("Стебунов Никита Юрьевич", "invalid-phone", "stebunov@gmail.com", Date.valueOf(LocalDate.of(1990, 1, 1)), "password123"))
                .thenThrow(new IllegalArgumentException("Неверный формат телефона. Используйте формат: +7 XXX XXX-XX-XX"));

        // Вызов метода контроллера
        ResponseEntity<?> response = authenticationController.registerUser(
                "Стебунов Никита Юрьевич", "invalid-phone", "stebunov@gmail.com", "01.01.1990", "password123");

        // Проверка статуса ответа
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatusCodeValue());
        // Проверка тела ответа
        assertEquals("Неверный формат телефона. Используйте формат: +7 XXX XXX-XX-XX", response.getBody());
    }

    /**
     * Тест регистрации с некорректным форматом email.
     * Проверка возврата статуса 400 и сообщения об ошибке.
     */
    @Test
    void testRegisterUser_InvalidEmailFormat() {
        // Мокирование сервиса: выброс исключения
        when(userService.createUser("Стебунов Никита Юрьевич", "+7 999 123-45-67", "invalid-email", Date.valueOf(LocalDate.of(1990, 1, 1)), "password123"))
                .thenThrow(new IllegalArgumentException("Неверный формат электронной почты."));

        // Вызов метода контроллера
        ResponseEntity<?> response = authenticationController.registerUser(
                "Стебунов Никита Юрьевич", "+7 999 123-45-67", "invalid-email", "01.01.1990", "password123");

        // Проверка статуса ответа
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatusCodeValue());
        // Проверка тела ответа
        assertEquals(
                "Неверный формат электронной почты. Email должен быть полностью на английском языке. " +
                        "Допустимые домены: mail.ru, inbox.ru, yandex.ru, gmail.com. Пример корректной электронной почты: example123@gmail.com.",
                response.getBody());
    }

    /**
     * Тест регистрации с датой рождения в будущем.
     * Проверка возврата статуса 400 и сообщения об ошибке.
     */
    @Test
    void testRegisterUser_FutureBirthDate() {
        // Мокирование сервиса: выброс исключения
        when(userService.createUser("Стебунов Никита Юрьевич", "+7 999 123-45-67", "stebunov@gmail.com", Date.valueOf(LocalDate.of(2026, 1, 1)), "password123"))
                .thenThrow(new IllegalArgumentException("Дата рождения не может быть позже текущей даты."));

        // Вызов метода контроллера
        ResponseEntity<?> response = authenticationController.registerUser(
                "Стебунов Никита Юрьевич", "+7 999 123-45-67", "stebunov@gmail.com", "01.01.2026", "password123");

        // Проверка статуса ответа
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatusCodeValue());
        // Проверка тела ответа
        assertEquals("Дата рождения не может быть позже текущей даты.", response.getBody());
    }

    /**
     * Тест регистрации с возрастом менее 14 лет.
     * Проверка возврата статуса 400 и сообщения об ошибке.
     */
    @Test
    void testRegisterUser_Under14YearsOld() {
        // Мокирование сервиса: выброс исключения
        when(userService.createUser("Стебунов Никита Юрьевич", "+7 999 123-45-67", "stebunov@gmail.com", Date.valueOf(LocalDate.now().minusYears(10)), "password123"))
                .thenThrow(new IllegalArgumentException("Наша транспортная компания не уверена, что вы можете пользоваться нашими услугами без сопровождения родителей."));

        // Вызов метода контроллера
        ResponseEntity<?> response = authenticationController.registerUser(
                "Стебунов Никита Юрьевич", "+7 999 123-45-67", "stebunov@gmail.com", LocalDate.now().minusYears(10).format(DateTimeFormatter.ofPattern("dd.MM.yyyy")), "password123");

        // Проверка статуса ответа
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatusCodeValue());
        // Проверка тела ответа
        assertEquals("Наша транспортная компания не уверена, что вы можете пользоваться нашими услугами без сопровождения родителей.", response.getBody());
    }

    /**
     * Тест регистрации с возрастом более 100 лет.
     * Проверка возврата статуса 400 и сообщения об ошибке.
     */
    @Test
    void testRegisterUser_Over100YearsOld() {
        // Мокирование сервиса: выброс исключения
        when(userService.createUser("Стебунов Никита Юрьевич", "+7 999 123-45-67", "stebunov@gmail.com", Date.valueOf(LocalDate.now().minusYears(101)), "password123"))
                .thenThrow(new IllegalArgumentException("У вас прекрасный возраст, но наша транспортная компания сомневается в корректности указанной даты рождения."));

        // Вызов метода контроллера
        ResponseEntity<?> response = authenticationController.registerUser(
                "Стебунов Никита Юрьевич", "+7 999 123-45-67", "stebunov@gmail.com", LocalDate.now().minusYears(101).format(DateTimeFormatter.ofPattern("dd.MM.yyyy")), "password123");

        // Проверка статуса ответа
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatusCodeValue());
        // Проверка тела ответа
        assertEquals("У вас прекрасный возраст, но наша транспортная компания сомневается в корректности указанной даты рождения.", response.getBody());
    }

    /**
     * Тест регистрации с уже существующим номером телефона.
     * Проверка возврата статуса 409 и сообщения об ошибке.
     */
    @Test
    void testRegisterUser_PhoneAlreadyExists() {
        // Мокирование сервиса: выброс исключения
        when(userService.createUser("Стебунов Никита Юрьевич", "+7 999 123-45-67", "stebunov@gmail.com", Date.valueOf(LocalDate.of(1990, 1, 1)), "password123"))
                .thenThrow(new IllegalArgumentException("Пользователь с таким номером телефона уже существует."));

        // Вызов метода контроллера
        ResponseEntity<?> response = authenticationController.registerUser(
                "Стебунов Никита Юрьевич", "+7 999 123-45-67", "stebunov@gmail.com", "01.01.1990", "password123");

        // Проверка статуса ответа
        assertEquals(HttpStatus.CONFLICT.value(), response.getStatusCodeValue());
        // Проверка тела ответа
        assertEquals("Пользователь с таким номером телефона уже существует.", response.getBody());
    }

    /**
     * Тест регистрации с уже существующим email.
     * Проверка возврата статуса 409 и сообщения об ошибке.
     */
    @Test
    void testRegisterUser_EmailAlreadyExists() {
        // Мокирование сервиса: выброс исключения
        when(userService.createUser("Стебунов Никита Юрьевич", "+7 999 123-45-67", "stebunov@gmail.com", Date.valueOf(LocalDate.of(1990, 1, 1)), "password123"))
                .thenThrow(new IllegalArgumentException("Пользователь с таким email уже существует."));

        // Вызов метода контроллера
        ResponseEntity<?> response = authenticationController.registerUser(
                "Стебунов Никита Юрьевич", "+7 999 123-45-67", "stebunov@gmail.com", "01.01.1990", "password123");

        // Проверка статуса ответа
        assertEquals(HttpStatus.CONFLICT.value(), response.getStatusCodeValue());
        // Проверка тела ответа
        assertEquals("Пользователь с таким email уже существует.", response.getBody());
    }

    /**
     * Тест регистрации при внутренней ошибке сервера.
     * Проверка возврата статуса 500 и сообщения об ошибке.
     */
    @Test
    void testRegisterUser_InternalServerError() {
        // Мокирование сервиса: выброс исключения
        when(userService.createUser("Стебунов Никита Юрьевич", "+7 999 123-45-67", "stebunov@gmail.com", Date.valueOf(LocalDate.of(1990, 1, 1)), "password123"))
                .thenThrow(new RuntimeException("Внутренняя ошибка"));

        // Вызов метода контроллера
        ResponseEntity<?> response = authenticationController.registerUser(
                "Стебунов Никита Юрьевич", "+7 999 123-45-67", "stebunov@gmail.com", "01.01.1990", "password123");

        // Проверка статуса ответа
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), response.getStatusCodeValue());
        // Проверка тела ответа
        assertEquals("Произошла ошибка при регистрации пользователя.", response.getBody());
    }
}