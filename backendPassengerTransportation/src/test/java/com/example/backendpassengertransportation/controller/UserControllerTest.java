package com.example.backendpassengertransportation.controller;

import com.example.backendpassengertransportation.model.User;
import com.example.backendpassengertransportation.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.sql.Date;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    // Вспомогательный метод для создания тестового пользователя
    private User createTestUser(String id, String fullName, String phone, String email, Date dateOfBirth, String password) {
        User user = new User();
        user.setIdUser(id);
        user.setPassengerFullName(fullName);
        user.setPassengerPhone(phone);
        user.setPassengerEmail(email);
        user.setDateOfBirth(dateOfBirth);
        user.setPassword(password);
        return user;
    }

    /**
     * Тест получения всех пользователей.
     * Проверка успешного сценария с возвратом списка пользователей.
     */
    @Test
    void testGetAllUsers_Success() {
        // Создание тестовых данных: два пользователя
        User user1 = createTestUser("u1", "Иванов Иван Иванович", "+7 999 123-45-67", "ivanov@gmail.com", Date.valueOf("1990-01-01"), "hashedPassword123");
        User user2 = createTestUser("u2", "Петров Петр Петрович", "+7 999 987-65-43", "petrov@yandex.ru", Date.valueOf("1985-05-05"), "hashedPassword456");
        List<User> users = Arrays.asList(user1, user2);

        // Мокирование сервиса
        when(userService.getAllUsers()).thenReturn(users);

        // Вызов метода контроллера
        ResponseEntity<?> response = userController.getAllUsers();

        // Проверка статуса ответа
        assertEquals(HttpStatus.OK.value(), response.getStatusCodeValue());
        // Проверка тела ответа
        List<User> result = (List<User>) response.getBody();
        assertEquals(2, result.size());

        // Проверка полей первого пользователя
        User returnedUser1 = result.get(0);
        assertEquals("u1", returnedUser1.getIdUser());
        assertEquals("Иванов Иван Иванович", returnedUser1.getPassengerFullName());
        assertEquals("+7 999 123-45-67", returnedUser1.getPassengerPhone());
        assertEquals("ivanov@gmail.com", returnedUser1.getPassengerEmail());
        assertEquals(Date.valueOf("1990-01-01"), returnedUser1.getDateOfBirth());
        assertEquals("hashedPassword123", returnedUser1.getPassword());

        // Проверка полей второго пользователя
        User returnedUser2 = result.get(1);
        assertEquals("u2", returnedUser2.getIdUser());
        assertEquals("Петров Петр Петрович", returnedUser2.getPassengerFullName());
        assertEquals("+7 999 987-65-43", returnedUser2.getPassengerPhone());
        assertEquals("petrov@yandex.ru", returnedUser2.getPassengerEmail());
        assertEquals(Date.valueOf("1985-05-05"), returnedUser2.getDateOfBirth());
        assertEquals("hashedPassword456", returnedUser2.getPassword());
    }

    /**
     * Тест получения всех пользователей, когда пользователи не найдены.
     * Проверка возврата статуса 404 и сообщения об ошибке.
     */
    @Test
    void testGetAllUsers_NotFound() {
        // Мокирование сервиса: возвращается пустой список
        when(userService.getAllUsers()).thenReturn(Collections.emptyList());

        // Вызов метода контроллера
        ResponseEntity<?> response = userController.getAllUsers();

        // Проверка статуса ответа
        assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatusCodeValue());
        // Проверка тела ответа
        assertEquals("Пользователи не найдены.", response.getBody());
    }

    /**
     * Тест получения всех пользователей при внутренней ошибке сервера.
     * Проверка возврата статуса 500 и пустого списка.
     */
    @Test
    void testGetAllUsers_InternalServerError() {
        // Мокирование сервиса: выброс исключения
        when(userService.getAllUsers()).thenThrow(new RuntimeException("Внутренняя ошибка"));

        // Вызов метода контроллера
        ResponseEntity<?> response = userController.getAllUsers();

        // Проверка статуса ответа
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), response.getStatusCodeValue());
        // Проверка тела ответа
        assertEquals(Collections.emptyList(), response.getBody());
    }

    /**
     * Тест получения пользователя по ID.
     * Проверка успешного сценария с возвратом пользователя.
     */
    @Test
    void testGetUserById_Success() {
        // Создание тестового пользователя
        User user = createTestUser("u1", "Иванов Иван Иванович", "+7 999 123-45-67", "ivanov@gmail.com", Date.valueOf("1990-01-01"), "hashedPassword123");

        // Мокирование сервиса
        when(userService.getUserById("u1")).thenReturn(user);

        // Вызов метода контроллера
        ResponseEntity<?> response = userController.getUserById("u1");

        // Проверка статуса ответа
        assertEquals(HttpStatus.OK.value(), response.getStatusCodeValue());
        // Проверка тела ответа
        User returnedUser = (User) response.getBody();
        assertEquals("u1", returnedUser.getIdUser());
        assertEquals("Иванов Иван Иванович", returnedUser.getPassengerFullName());
        assertEquals("+7 999 123-45-67", returnedUser.getPassengerPhone());
        assertEquals("ivanov@gmail.com", returnedUser.getPassengerEmail());
        assertEquals(Date.valueOf("1990-01-01"), returnedUser.getDateOfBirth());
        assertEquals("hashedPassword123", returnedUser.getPassword());
    }

    /**
     * Тест получения пользователя по ID, когда пользователь не найден.
     * Проверка возврата статуса 404 и сообщения об ошибке.
     */
    @Test
    void testGetUserById_NotFound() {
        // Мокирование сервиса: возвращается null
        when(userService.getUserById("u999")).thenReturn(null);

        // Вызов метода контроллера
        ResponseEntity<?> response = userController.getUserById("u999");

        // Проверка статуса ответа
        assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatusCodeValue());
        // Проверка тела ответа
        assertEquals("Пользователь с ID u999 не найден.", response.getBody());
    }

    /**
     * Тест получения пользователя по ID при внутренней ошибке сервера.
     * Проверка возврата статуса 500 и null.
     */
    @Test
    void testGetUserById_InternalServerError() {
        // Мокирование сервиса: выброс исключения
        when(userService.getUserById("u1")).thenThrow(new RuntimeException("Внутренняя ошибка"));

        // Вызов метода контроллера
        ResponseEntity<?> response = userController.getUserById("u1");

        // Проверка статуса ответа
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), response.getStatusCodeValue());
        // Проверка тела ответа
        assertNull(response.getBody());
    }

    /**
     * Тест получения пользователя по email.
     * Проверка успешного сценария с возвратом пользователя.
     */
    @Test
    void testGetUserByEmail_Success() {
        // Создание тестового пользователя
        User user = createTestUser("u1", "Иванов Иван Иванович", "+7 999 123-45-67", "ivanov@gmail.com", Date.valueOf("1990-01-01"), "hashedPassword123");

        // Мокирование сервиса
        when(userService.getUserByEmail("ivanov@gmail.com")).thenReturn(user);

        // Вызов метода контроллера
        ResponseEntity<?> response = userController.getUserByEmail("ivanov@gmail.com");

        // Проверка статуса ответа
        assertEquals(HttpStatus.OK.value(), response.getStatusCodeValue());
        // Проверка тела ответа
        User returnedUser = (User) response.getBody();
        assertEquals("u1", returnedUser.getIdUser());
        assertEquals("Иванов Иван Иванович", returnedUser.getPassengerFullName());
        assertEquals("+7 999 123-45-67", returnedUser.getPassengerPhone());
        assertEquals("ivanov@gmail.com", returnedUser.getPassengerEmail());
        assertEquals(Date.valueOf("1990-01-01"), returnedUser.getDateOfBirth());
        assertEquals("hashedPassword123", returnedUser.getPassword());
    }

    /**
     * Тест получения пользователя по email, когда пользователь не найден.
     * Проверка возврата статуса 404 и сообщения об ошибке.
     */
    @Test
    void testGetUserByEmail_NotFound() {
        // Мокирование сервиса: возврат null вместо выброса исключения
        when(userService.getUserByEmail("nonexistent@gmail.com")).thenReturn(null);

        // Вызов метода контроллера
        ResponseEntity<?> response = userController.getUserByEmail("nonexistent@gmail.com");

        // Проверка статуса ответа
        assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatusCodeValue());
        // Проверка тела ответа
        assertEquals("Пользователь с email nonexistent@gmail.com не найден.", response.getBody());
    }

    /**
     * Тест получения пользователя по email при внутренней ошибке сервера.
     * Проверка возврата статуса 500 и null.
     */
    @Test
    void testGetUserByEmail_InternalServerError() {
        // Мокирование сервиса: выброс исключения
        when(userService.getUserByEmail("ivanov@gmail.com")).thenThrow(new RuntimeException("Внутренняя ошибка"));

        // Вызов метода контроллера
        ResponseEntity<?> response = userController.getUserByEmail("ivanov@gmail.com");

        // Проверка статуса ответа
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), response.getStatusCodeValue());
        // Проверка тела ответа
        assertNull(response.getBody());
    }

    /**
     * Тест создания нового пользователя.
     * Проверка успешного сценария с возвратом созданного пользователя.
     */
    @Test
    void testCreateUser_Success() {
        // Создание тестового пользователя
        User user = createTestUser("u1", "Стебунов Никита Юрьевич", "+7 999 123-45-67", "stebunov@gmail.com", Date.valueOf("1995-03-15"), "hashedPassword789");

        // Мокирование сервиса
        when(userService.createUser("Стебунов Никита Юрьевич", "+7 999 123-45-67", "stebunov@gmail.com", Date.valueOf("1995-03-15"), "password789"))
                .thenReturn(user);

        // Вызов метода контроллера
        ResponseEntity<?> response = userController.createUser("Стебунов Никита Юрьевич", "+7 999 123-45-67", "stebunov@gmail.com", "15.03.1995", "password789");

        // Проверка статуса ответа
        assertEquals(HttpStatus.CREATED.value(), response.getStatusCodeValue());
        // Проверка тела ответа
        User returnedUser = (User) response.getBody();
        assertEquals("u1", returnedUser.getIdUser());
        assertEquals("Стебунов Никита Юрьевич", returnedUser.getPassengerFullName());
        assertEquals("+7 999 123-45-67", returnedUser.getPassengerPhone());
        assertEquals("stebunov@gmail.com", returnedUser.getPassengerEmail());
        assertEquals(Date.valueOf("1995-03-15"), returnedUser.getDateOfBirth());
        assertEquals("hashedPassword789", returnedUser.getPassword());
    }

    /**
     * Тест создания нового пользователя с некорректным форматом даты.
     * Проверка возврата статуса 400 и сообщения об ошибке.
     */
    @Test
    void testCreateUser_InvalidDateFormat() {
        // Вызов метода контроллера с некорректным форматом даты
        ResponseEntity<?> response = userController.createUser("Комарова Анна Васильевна", "+7 999 111-22-33", "komarova@gmail.com", "15-03-1990", "password123");

        // Проверка статуса ответа
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatusCodeValue());
        // Проверка тела ответа
        assertEquals("Неверный формат даты рождения. Используйте 'dd.MM.yyyy'.", response.getBody());
    }

    /**
     * Тест создания нового пользователя с пустыми полями.
     * Проверка возврата статуса 400 и сообщения об ошибке.
     */
    @Test
    void testCreateUser_EmptyFields() {
        // Мокирование сервиса: выброс исключения IllegalArgumentException
        when(userService.createUser("", "+7 999 111-22-33", "komarova@gmail.com", Date.valueOf("1990-03-15"), "password123"))
                .thenThrow(new IllegalArgumentException("Все поля должны быть заполнены."));

        // Вызов метода контроллера
        ResponseEntity<?> response = userController.createUser("", "+7 999 111-22-33", "komarova@gmail.com", "15.03.1990", "password123");

        // Проверка статуса ответа
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatusCodeValue());
        // Проверка тела ответа
        assertEquals("Все поля должны быть заполнены.", response.getBody());
    }

    /**
     * Тест создания нового пользователя с некорректным форматом телефона.
     * Проверка возврата статуса 400 и сообщения об ошибке.
     */
    @Test
    void testCreateUser_InvalidPhoneFormat() {
        // Мокирование сервиса: выброс исключения IllegalArgumentException
        when(userService.createUser("Комарова Анна Васильевна", "9991112233", "komarova@gmail.com", Date.valueOf("1990-03-15"), "password123"))
                .thenThrow(new IllegalArgumentException("Неверный формат телефона. Используйте формат: +7 XXX XXX-XX-XX"));

        // Вызов метода контроллера
        ResponseEntity<?> response = userController.createUser("Комарова Анна Васильевна", "9991112233", "komarova@gmail.com", "15.03.1990", "password123");

        // Проверка статуса ответа
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatusCodeValue());
        // Проверка тела ответа
        assertEquals("Неверный формат телефона. Используйте формат: +7 XXX XXX-XX-XX", response.getBody());
    }

    /**
     * Тест создания нового пользователя с некорректным форматом email.
     * Проверка возврата статуса 400 и сообщения об ошибке.
     */
    @Test
    void testCreateUser_InvalidEmailFormat() {
        // Мокирование сервиса: выброс исключения IllegalArgumentException
        when(userService.createUser("Комарова Анна Васильевна", "+7 999 111-22-33", "invalid-email", Date.valueOf("1990-03-15"), "password123"))
                .thenThrow(new IllegalArgumentException("Неверный формат электронной почты. Используйте формат: имя_пользователя@домен. Допустимые домены: mail.ru, inbox.ru, yandex.ru, gmail.com."));

        // Вызов метода контроллера
        ResponseEntity<?> response = userController.createUser("Комарова Анна Васильевна", "+7 999 111-22-33", "invalid-email", "15.03.1990", "password123");

        // Проверка статуса ответа
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatusCodeValue());
        // Проверка тела ответа
        assertEquals("Неверный формат электронной почты. Email должен быть полностью на английском языке. Допустимые домены: mail.ru, inbox.ru, yandex.ru, gmail.com. Пример корректной электронной почты: example123@gmail.com.", response.getBody());
    }

    /**
     * Тест создания нового пользователя с датой рождения в будущем.
     * Проверка возврата статуса 400 и сообщения об ошибке.
     */
    @Test
    void testCreateUser_FutureDateOfBirth() {
        // Вычисление будущей даты
        String futureDate = LocalDate.now().plusDays(1).format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));

        // Мокирование сервиса: выброс исключения IllegalArgumentException
        when(userService.createUser("Комарова Анна Васильевна", "+7 999 111-22-33", "komarova@gmail.com", Date.valueOf(LocalDate.now().plusDays(1)), "password123"))
                .thenThrow(new IllegalArgumentException("Дата рождения не может быть позже текущей даты."));

        // Вызов метода контроллера
        ResponseEntity<?> response = userController.createUser("Комарова Анна Васильевна", "+7 999 111-22-33", "komarova@gmail.com", futureDate, "password123");

        // Проверка статуса ответа
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatusCodeValue());
        // Проверка тела ответа
        assertEquals("Дата рождения не может быть позже текущей даты.", response.getBody());
    }

    /**
     * Тест обновления пользователя.
     * Проверка успешного сценария с возвратом обновленного пользователя.
     */
    @Test
    void testUpdateUser_Success() {
        // Создание тестового пользователя
        User user = createTestUser("u1", "Сидоров Алексей Викторович", "+7 999 555-66-77", "sidorov@gmail.com", Date.valueOf("1988-07-20"), "hashedPassword999");

        // Мокирование сервиса
        when(userService.updateUser("u1", "Сидоров Алексей Викторович", null, null, null, null)).thenReturn(user);

        // Вызов метода контроллера
        ResponseEntity<?> response = userController.updateUser("u1", "Сидоров Алексей Викторович", null, null, null, null);

        // Проверка статуса ответа
        assertEquals(HttpStatus.OK.value(), response.getStatusCodeValue());
        // Проверка тела ответа
        User returnedUser = (User) response.getBody();
        assertEquals("u1", returnedUser.getIdUser());
        assertEquals("Сидоров Алексей Викторович", returnedUser.getPassengerFullName());
        assertEquals("+7 999 555-66-77", returnedUser.getPassengerPhone());
        assertEquals("sidorov@gmail.com", returnedUser.getPassengerEmail());
        assertEquals(Date.valueOf("1988-07-20"), returnedUser.getDateOfBirth());
        assertEquals("hashedPassword999", returnedUser.getPassword());
    }

    /**
     * Тест обновления пользователя с некорректным форматом даты.
     * Проверка возврата статуса 400 и сообщения об ошибке.
     */
    @Test
    void testUpdateUser_InvalidDateFormat() {
        // Вызов метода контроллера с некорректным форматом даты
        ResponseEntity<?> response = userController.updateUser("u1", null, null, null, "20-07-1988", null);

        // Проверка статуса ответа
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatusCodeValue());
        // Проверка тела ответа
        assertEquals("Неверный формат даты рождения. Используйте 'dd.MM.yyyy'.", response.getBody());
    }

    /**
     * Тест обновления пользователя с некорректным форматом телефона.
     * Проверка возврата статуса 400 и сообщения об ошибке.
     */
    @Test
    void testUpdateUser_InvalidPhoneFormat() {
        // Мокирование сервиса: выброс исключения IllegalArgumentException
        when(userService.updateUser("u1", null, "9995556677", null, null, null))
                .thenThrow(new IllegalArgumentException("Неверный формат телефона. Используйте формат: +7 XXX XXX-XX-XX"));

        // Вызов метода контроллера
        ResponseEntity<?> response = userController.updateUser("u1", null, "9995556677", null, null, null);

        // Проверка статуса ответа
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatusCodeValue());
        // Проверка тела ответа
        assertEquals("Неверный формат телефона. Используйте формат: +7 XXX XXX-XX-XX", response.getBody());
    }

    /**
     * Тест обновления пользователя с некорректным форматом email.
     * Проверка возврата статуса 400 и сообщения об ошибке.
     */
    @Test
    void testUpdateUser_InvalidEmailFormat() {
        // Мокирование сервиса: выброс исключения IllegalArgumentException
        when(userService.updateUser("u1", null, null, "invalid-email", null, null))
                .thenThrow(new IllegalArgumentException("Неверный формат электронной почты. Используйте формат: имя_пользователя@домен. Допустимые домены: mail.ru, inbox.ru, yandex.ru, gmail.com."));

        // Вызов метода контроллера
        ResponseEntity<?> response = userController.updateUser("u1", null, null, "invalid-email", null, null);

        // Проверка статуса ответа
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatusCodeValue());
        // Проверка тела ответа
        assertEquals("Неверный формат электронной почты. Email должен быть полностью на английском языке. Допустимые домены: mail.ru, inbox.ru, yandex.ru, gmail.com. Пример корректной электронной почты: example123@gmail.com.", response.getBody());
    }

    /**
     * Тест обновления пользователя с датой рождения в будущем.
     * Проверка возврата статуса 400 и сообщения об ошибке.
     */
    @Test
    void testUpdateUser_FutureDateOfBirth() {
        // Вычисление будущей даты
        String futureDate = LocalDate.now().plusDays(1).format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));

        // Мокирование сервиса: выброс исключения IllegalArgumentException
        when(userService.updateUser("u1", null, null, null, Date.valueOf(LocalDate.now().plusDays(1)), null))
                .thenThrow(new IllegalArgumentException("Дата рождения не может быть позже текущей даты."));

        // Вызов метода контроллера
        ResponseEntity<?> response = userController.updateUser("u1", null, null, null, futureDate, null);

        // Проверка статуса ответа
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatusCodeValue());
        // Проверка тела ответа
        assertEquals("Дата рождения не может быть позже текущей даты.", response.getBody());
    }

    /**
     * Тест обновления пользователя, когда пользователь не найден.
     * Проверка возврата статуса 404 и сообщения об ошибке.
     */
    @Test
    void testUpdateUser_NotFound() {
        // Мокирование сервиса: выброс исключения NoSuchElementException
        when(userService.updateUser("u999", "Сидоров Алексей Викторович", null, null, null, null))
                .thenThrow(new IllegalArgumentException("Пользователь не найден."));

        // Вызов метода контроллера
        ResponseEntity<?> response = userController.updateUser("u999", "Сидоров Алексей Викторович", null, null, null, null);

        // Проверка статуса ответа
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatusCodeValue());
        // Проверка тела ответа
        assertEquals("Пользователь не найден.", response.getBody());
    }

    /**
     * Тест удаления пользователя.
     * Проверка успешного сценария с возвратом сообщения об успехе.
     */
    @Test
    void testDeleteUser_Success() {
        // Мокирование сервиса
        doNothing().when(userService).deleteUser("u1");

        // Вызов метода контроллера
        ResponseEntity<String> response = userController.deleteUser("u1");

        // Проверка статуса ответа
        assertEquals(HttpStatus.OK.value(), response.getStatusCodeValue());
        // Проверка тела ответа
        assertEquals("Пользователь успешно удален.", response.getBody());
    }

    /**
     * Тест удаления пользователя, когда пользователь не найден.
     * Проверка возврата статуса 404 и сообщения об ошибке.
     */
    @Test
    void testDeleteUser_NotFound() {
        // Мокирование сервиса: выброс исключения IllegalArgumentException
        doThrow(new IllegalArgumentException("Пользователь не найден.")).when(userService).deleteUser("u999");

        // Вызов метода контроллера
        ResponseEntity<String> response = userController.deleteUser("u999");

        // Проверка статуса ответа
        assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatusCodeValue());
        // Проверка тела ответа
        assertEquals("Пользователь не найден.", response.getBody());
    }
}