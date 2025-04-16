package com.example.backendpassengertransportation.controller;

import com.example.backendpassengertransportation.model.BookingTicket;
import com.example.backendpassengertransportation.model.Route;
import com.example.backendpassengertransportation.model.User;
import com.example.backendpassengertransportation.service.BookingTicketService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingTicketControllerTest {

    @Mock
    private BookingTicketService bookingTicketService;

    @InjectMocks
    private BookingTicketController bookingTicketController;

    // Вспомогательный метод для создания тестового бронирования
    private BookingTicket createTestBookingTicket(String id, String routeId, String userFullName, String userPhone, String userEmail, Timestamp bookingDate) {
        BookingTicket bookingTicket = new BookingTicket();
        bookingTicket.setIdBooking(id);

        Route route = new Route();
        route.setIdRoute(routeId);
        route.setDepartureTime(Timestamp.valueOf(LocalDateTime.now().plusHours(2))); // Время отправления через 2 часа
        route.setNumberAvailableSeats(10);
        bookingTicket.setRoute(route);

        User user = new User();
        user.setPassengerFullName(userFullName);
        user.setPassengerPhone(userPhone);
        user.setPassengerEmail(userEmail);
        bookingTicket.setUser(user);

        bookingTicket.setBookingDate(bookingDate);
        return bookingTicket;
    }

    /**
     * Тест получения всех бронирований.
     * Проверка корректности возвращаемого списка бронирований.
     */
    @Test
    void testGetAllBookingTickets_Success() {
        // Создание тестовых данных: два бронирования
        BookingTicket booking1 = createTestBookingTicket("b1", "r1", "Стебунов Никита Юрьевич", "+7 999 123-45-67", "stebunov@gmail.com", Timestamp.valueOf(LocalDateTime.now()));
        BookingTicket booking2 = createTestBookingTicket("b2", "r2", "Комарова Анна Васильевна", "+7 999 987-65-43", "komarova@gmail.com", Timestamp.valueOf(LocalDateTime.now()));
        List<BookingTicket> bookings = Arrays.asList(booking1, booking2);

        // Мокирование сервиса
        when(bookingTicketService.getAllBookingTickets()).thenReturn(bookings);

        // Вызов метода контроллера
        ResponseEntity<?> response = bookingTicketController.getAllBookingTickets();

        // Проверка статуса ответа
        assertEquals(HttpStatus.OK.value(), response.getStatusCodeValue());
        // Проверка тела ответа
        List<BookingTicket> result = (List<BookingTicket>) response.getBody();
        assertEquals(2, result.size());

        // Проверка полей первого бронирования
        BookingTicket returnedBooking1 = result.get(0);
        assertEquals("b1", returnedBooking1.getIdBooking());
        assertEquals("r1", returnedBooking1.getRoute().getIdRoute());
        assertEquals("Стебунов Никита Юрьевич", returnedBooking1.getUser().getPassengerFullName());
        assertEquals("+7 999 123-45-67", returnedBooking1.getUser().getPassengerPhone());
        assertEquals("stebunov@gmail.com", returnedBooking1.getUser().getPassengerEmail());

        // Проверка полей второго бронирования
        BookingTicket returnedBooking2 = result.get(1);
        assertEquals("b2", returnedBooking2.getIdBooking());
        assertEquals("r2", returnedBooking2.getRoute().getIdRoute());
        assertEquals("Комарова Анна Васильевна", returnedBooking2.getUser().getPassengerFullName());
        assertEquals("+7 999 987-65-43", returnedBooking2.getUser().getPassengerPhone());
        assertEquals("komarova@gmail.com", returnedBooking2.getUser().getPassengerEmail());
    }

    /**
     * Тест получения всех бронирований, когда бронирования не найдены.
     * Проверка возврата статуса 404 и сообщения об ошибке.
     */
    @Test
    void testGetAllBookingTickets_NotFound() {
        // Мокирование сервиса: возвращается пустой список
        when(bookingTicketService.getAllBookingTickets()).thenReturn(Collections.emptyList());

        // Вызов метода контроллера
        ResponseEntity<?> response = bookingTicketController.getAllBookingTickets();

        // Проверка статуса ответа
        assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatusCodeValue());
        // Проверка тела ответа
        assertEquals("Бронирования не найдены.", response.getBody());
    }

    /**
     * Тест получения всех бронирований при внутренней ошибке сервера.
     * Проверка возврата статуса 500 и сообщения об ошибке.
     */
    @Test
    void testGetAllBookingTickets_InternalServerError() {
        // Мокирование сервиса: выброс исключения
        when(bookingTicketService.getAllBookingTickets()).thenThrow(new RuntimeException("Внутренняя ошибка"));

        // Вызов метода контроллера
        ResponseEntity<?> response = bookingTicketController.getAllBookingTickets();

        // Проверка статуса ответа
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), response.getStatusCodeValue());
        // Проверка тела ответа
        assertEquals("Произошла ошибка при получении списка бронирований: Внутренняя ошибка", response.getBody());
    }

    /**
     * Тест получения бронирования по ID.
     * Проверка корректности возвращаемого бронирования.
     */
    @Test
    void testGetBookingTicketById_Success() {
        // Создание тестовых данных
        BookingTicket booking = createTestBookingTicket("b1", "r1", "Стебунов Никита Юрьевич", "+7 999 123-45-67", "stebunov@gmail.com", Timestamp.valueOf(LocalDateTime.now()));

        // Мокирование сервиса
        when(bookingTicketService.getBookingTicketById("b1")).thenReturn(booking);

        // Вызов метода контроллера
        ResponseEntity<?> response = bookingTicketController.getBookingTicketById("b1");

        // Проверка статуса ответа
        assertEquals(HttpStatus.OK.value(), response.getStatusCodeValue());
        // Проверка тела ответа
        BookingTicket result = (BookingTicket) response.getBody();
        assertEquals("b1", result.getIdBooking());
        assertEquals("r1", result.getRoute().getIdRoute());
        assertEquals("Стебунов Никита Юрьевич", result.getUser().getPassengerFullName());
        assertEquals("+7 999 123-45-67", result.getUser().getPassengerPhone());
        assertEquals("stebunov@gmail.com", result.getUser().getPassengerEmail());
    }

    /**
     * Тест получения бронирования по ID, когда бронирование не найдено.
     * Проверка возврата статуса 404 и сообщения об ошибке.
     */
    @Test
    void testGetBookingTicketById_NotFound() {
        // Мокирование сервиса: выброс исключения NoSuchElementException
        when(bookingTicketService.getBookingTicketById("b999")).thenThrow(new NoSuchElementException("Бронирование с ID b999 не найдено."));

        // Вызов метода контроллера
        ResponseEntity<?> response = bookingTicketController.getBookingTicketById("b999");

        // Проверка статуса ответа
        assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatusCodeValue());
        // Проверка тела ответа
        assertEquals("Бронирование с ID b999 не найдено.", response.getBody());
    }

    /**
     * Тест получения бронирования по ID при внутренней ошибке сервера.
     * Проверка возврата статуса 500 и сообщения об ошибке.
     */
    @Test
    void testGetBookingTicketById_InternalServerError() {
        // Мокирование сервиса: выброс исключения
        when(bookingTicketService.getBookingTicketById("b1")).thenThrow(new RuntimeException("Внутренняя ошибка"));

        // Вызов метода контроллера
        ResponseEntity<?> response = bookingTicketController.getBookingTicketById("b1");

        // Проверка статуса ответа
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), response.getStatusCodeValue());
        // Проверка тела ответа
        assertEquals("Произошла ошибка при получении бронирования: Внутренняя ошибка", response.getBody());
    }

    /**
     * Тест создания нового бронирования.
     * Проверка корректности создания и возврата статуса 201.
     */
    @Test
    void testCreateBookingTicket_Success() {
        // Создание тестовых данных
        BookingTicket booking = createTestBookingTicket("b1", "r1", "Стебунов Никита Юрьевич", "+7 999 123-45-67", "stebunov@gmail.com", Timestamp.valueOf(LocalDateTime.now()));

        // Мокирование сервиса
        when(bookingTicketService.createBookingTicket("r1", "Стебунов Никита Юрьевич", "+7 999 123-45-67", "stebunov@gmail.com")).thenReturn(booking);

        // Вызов метода контроллера
        ResponseEntity<?> response = bookingTicketController.createBookingTicket("r1", "Стебунов Никита Юрьевич", "+7 999 123-45-67", "stebunov@gmail.com");

        // Проверка статуса ответа
        assertEquals(HttpStatus.CREATED.value(), response.getStatusCodeValue());
        // Проверка тела ответа
        BookingTicket result = (BookingTicket) response.getBody();
        assertEquals("b1", result.getIdBooking());
        assertEquals("r1", result.getRoute().getIdRoute());
        assertEquals("Стебунов Никита Юрьевич", result.getUser().getPassengerFullName());
        assertEquals("+7 999 123-45-67", result.getUser().getPassengerPhone());
        assertEquals("stebunov@gmail.com", result.getUser().getPassengerEmail());
    }

    /**
     * Тест создания бронирования с некорректным форматом телефона.
     * Проверка возврата статуса 400 и сообщения об ошибке.
     */
    @Test
    void testCreateBookingTicket_InvalidPhoneFormat() {
        // Мокирование сервиса: выброс исключения IllegalArgumentException
        when(bookingTicketService.createBookingTicket("r1", "Стебунов Никита Юрьевич", "invalid-phone", "stebunov@gmail.com"))
                .thenThrow(new IllegalArgumentException("Неверный формат телефона. Используйте формат: +7 XXX XXX-XX-XX"));

        // Вызов метода контроллера
        ResponseEntity<?> response = bookingTicketController.createBookingTicket("r1", "Стебунов Никита Юрьевич", "invalid-phone", "stebunov@gmail.com");

        // Проверка статуса ответа
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatusCodeValue());
        // Проверка тела ответа
        assertEquals("Неверный формат телефона. Используйте формат: +7 XXX XXX-XX-XX", response.getBody());
    }

    /**
     * Тест создания бронирования с некорректным форматом email.
     * Проверка возврата статуса 400 и сообщения об ошибке.
     */
    @Test
    void testCreateBookingTicket_InvalidEmailFormat() {
        // Мокирование сервиса: выброс исключения IllegalArgumentException
        when(bookingTicketService.createBookingTicket("r1", "Стебунов Никита Юрьевич", "+7 999 123-45-67", "invalid-email"))
                .thenThrow(new IllegalArgumentException("Неверный формат email. Используйте формат: имя@домен (mail.ru, yandex.ru, gmail.com)"));

        // Вызов метода контроллера
        ResponseEntity<?> response = bookingTicketController.createBookingTicket("r1", "Стебунов Никита Юрьевич", "+7 999 123-45-67", "invalid-email");

        // Проверка статуса ответа
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatusCodeValue());
        // Проверка тела ответа
        assertEquals("Неверный формат email. Используйте формат: имя@домен (mail.ru, yandex.ru, gmail.com)", response.getBody());
    }

    /**
     * Тест создания бронирования, когда маршрут не найден.
     * Проверка возврата статуса 404 и сообщения об ошибке.
     */
    @Test
    void testCreateBookingTicket_RouteNotFound() {
        // Мокирование сервиса: выброс исключения NoSuchElementException
        when(bookingTicketService.createBookingTicket("r999", "Стебунов Никита Юрьевич", "+7 999 123-45-67", "stebunov@gmail.com"))
                .thenThrow(new NoSuchElementException("Маршрут с ID r999 не найден."));

        // Вызов метода контроллера
        ResponseEntity<?> response = bookingTicketController.createBookingTicket("r999", "Стебунов Никита Юрьевич", "+7 999 123-45-67", "stebunov@gmail.com");

        // Проверка статуса ответа
        assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatusCodeValue());
        // Проверка тела ответа
        assertEquals("Маршрут с ID r999 не найден.", response.getBody());
    }

    /**
     * Тест создания бронирования, когда пользователь не найден.
     * Проверка возврата статуса 404 и сообщения об ошибке.
     */
    @Test
    void testCreateBookingTicket_UserNotFound() {
        // Мокирование сервиса: выброс исключения NoSuchElementException
        when(bookingTicketService.createBookingTicket("r1", "Стебунов Никита Юрьевич", "+7 999 123-45-67", "stebunov@gmail.com"))
                .thenThrow(new NoSuchElementException("Пользователь с такими данными не найден в базе данных пассажирских перевозок."));

        // Вызов метода контроллера
        ResponseEntity<?> response = bookingTicketController.createBookingTicket("r1", "Стебунов Никита Юрьевич", "+7 999 123-45-67", "stebunov@gmail.com");

        // Проверка статуса ответа
        assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatusCodeValue());
        // Проверка тела ответа
        assertEquals("Пользователь с такими данными не найден в базе данных пассажирских перевозок.", response.getBody());
    }

    /**
     * Тест создания бронирования, когда маршрут уже отправился.
     * Проверка возврата статуса 400 и сообщения об ошибке.
     */
    @Test
    void testCreateBookingTicket_RouteAlreadyDeparted() {
        // Мокирование сервиса: выброс исключения IllegalStateException
        when(bookingTicketService.createBookingTicket("r1", "Стебунов Никита Юрьевич", "+7 999 123-45-67", "stebunov@gmail.com"))
                .thenThrow(new IllegalStateException("Бронирование невозможно: маршрут уже отправился."));

        // Вызов метода контроллера
        ResponseEntity<?> response = bookingTicketController.createBookingTicket("r1", "Стебунов Никита Юрьевич", "+7 999 123-45-67", "stebunov@gmail.com");

        // Проверка статуса ответа
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatusCodeValue());
        // Проверка тела ответа
        assertEquals("Бронирование невозможно: маршрут уже отправился.", response.getBody());
    }

    /**
     * Тест создания бронирования, когда до отправления менее 30 минут.
     * Проверка возврата статуса 400 и сообщения об ошибке.
     */
    @Test
    void testCreateBookingTicket_LessThan30MinutesToDeparture() {
        // Мокирование сервиса: выброс исключения IllegalStateException
        when(bookingTicketService.createBookingTicket("r1", "Стебунов Никита Юрьевич", "+7 999 123-45-67", "stebunov@gmail.com"))
                .thenThrow(new IllegalStateException("Бронирование невозможно: до отправления осталось менее 30 минут."));

        // Вызов метода контроллера
        ResponseEntity<?> response = bookingTicketController.createBookingTicket("r1", "Стебунов Никита Юрьевич", "+7 999 123-45-67", "stebunov@gmail.com");

        // Проверка статуса ответа
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatusCodeValue());
        // Проверка тела ответа
        assertEquals("Бронирование невозможно: до отправления осталось менее 30 минут.", response.getBody());
    }

    /**
     * Тест создания бронирования, когда нет доступных мест.
     * Проверка возврата статуса 400 и сообщения об ошибке.
     */
    @Test
    void testCreateBookingTicket_NoAvailableSeats() {
        // Мокирование сервиса: выброс исключения IllegalStateException
        when(bookingTicketService.createBookingTicket("r1", "Стебунов Никита Юрьевич", "+7 999 123-45-67", "stebunov@gmail.com"))
                .thenThrow(new IllegalStateException("Нет доступных мест для бронирования."));

        // Вызов метода контроллера
        ResponseEntity<?> response = bookingTicketController.createBookingTicket("r1", "Стебунов Никита Юрьевич", "+7 999 123-45-67", "stebunov@gmail.com");

        // Проверка статуса ответа
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatusCodeValue());
        // Проверка тела ответа
        assertEquals("Нет доступных мест для бронирования.", response.getBody());
    }

    /**
     * Тест создания бронирования при внутренней ошибке сервера.
     * Проверка возврата статуса 500 и сообщения об ошибке.
     */
    @Test
    void testCreateBookingTicket_InternalServerError() {
        // Мокирование сервиса: выброс исключения
        when(bookingTicketService.createBookingTicket("r1", "Стебунов Никита Юрьевич", "+7 999 123-45-67", "stebunov@gmail.com"))
                .thenThrow(new RuntimeException("Внутренняя ошибка"));

        // Вызов метода контроллера
        ResponseEntity<?> response = bookingTicketController.createBookingTicket("r1", "Стебунов Никита Юрьевич", "+7 999 123-45-67", "stebunov@gmail.com");

        // Проверка статуса ответа
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), response.getStatusCodeValue());
        // Проверка тела ответа
        assertEquals("Произошла ошибка при создании бронирования: Внутренняя ошибка", response.getBody());
    }

    /**
     * Тест удаления бронирования по ID.
     * Проверка корректности удаления и возврата статуса 200.
     */
    @Test
    void testDeleteBookingTicket_Success() {
        // Мокирование сервиса: метод выполняется без исключений
        doNothing().when(bookingTicketService).deleteBookingTicket("b1");

        // Вызов метода контроллера
        ResponseEntity<?> response = bookingTicketController.deleteBookingTicket("b1");

        // Проверка статуса ответа
        assertEquals(HttpStatus.OK.value(), response.getStatusCodeValue());
        // Проверка тела ответа
        assertEquals("Бронирование успешно удалено.", response.getBody());
    }

    /**
     * Тест удаления бронирования по ID, когда бронирование не найдено.
     * Проверка возврата статуса 404 и сообщения об ошибке.
     */
    @Test
    void testDeleteBookingTicket_NotFound() {
        // Мокирование сервиса: выброс исключения IllegalArgumentException
        doThrow(new IllegalArgumentException("Бронирование с ID b999 не найдено.")).when(bookingTicketService).deleteBookingTicket("b999");

        // Вызов метода контроллера
        ResponseEntity<?> response = bookingTicketController.deleteBookingTicket("b999");

        // Проверка статуса ответа
        assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatusCodeValue());
        // Проверка тела ответа
        assertEquals("Бронирование с ID b999 не найдено.", response.getBody());
    }

    /**
     * Тест удаления бронирования, когда маршрут уже отправился.
     * Проверка возврата статуса 400 и сообщения об ошибке.
     */
    @Test
    void testDeleteBookingTicket_RouteAlreadyDeparted() {
        // Мокирование сервиса: выброс исключения IllegalStateException
        doThrow(new IllegalStateException("Отмена бронирования невозможна: маршрут уже отправился.")).when(bookingTicketService).deleteBookingTicket("b1");

        // Вызов метода контроллера
        ResponseEntity<?> response = bookingTicketController.deleteBookingTicket("b1");

        // Проверка статуса ответа
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatusCodeValue());
        // Проверка тела ответа
        assertEquals("Отмена бронирования невозможна: маршрут уже отправился.", response.getBody());
    }

    /**
     * Тест удаления бронирования, когда до отправления менее 30 минут.
     * Проверка возврата статуса 400 и сообщения об ошибке.
     */
    @Test
    void testDeleteBookingTicket_LessThan30MinutesToDeparture() {
        // Мокирование сервиса: выброс исключения IllegalStateException
        doThrow(new IllegalStateException("Отмена бронирования невозможна: до отправления осталось менее 30 минут.")).when(bookingTicketService).deleteBookingTicket("b1");

        // Вызов метода контроллера
        ResponseEntity<?> response = bookingTicketController.deleteBookingTicket("b1");

        // Проверка статуса ответа
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatusCodeValue());
        // Проверка тела ответа
        assertEquals("Отмена бронирования невозможна: до отправления осталось менее 30 минут.", response.getBody());
    }

    /**
     * Тест удаления бронирования при внутренней ошибке сервера.
     * Проверка возврата статуса 500 и сообщения об ошибке.
     */
    @Test
    void testDeleteBookingTicket_InternalServerError() {
        // Мокирование сервиса: выброс исключения
        doThrow(new RuntimeException("Внутренняя ошибка")).when(bookingTicketService).deleteBookingTicket("b1");

        // Вызов метода контроллера
        ResponseEntity<?> response = bookingTicketController.deleteBookingTicket("b1");

        // Проверка статуса ответа
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), response.getStatusCodeValue());
        // Проверка тела ответа
        assertEquals("Произошла ошибка при удалении бронирования: Внутренняя ошибка", response.getBody());
    }

    /**
     * Тест поиска бронирования по маршруту и номеру телефона.
     * Проверка корректности возвращаемого бронирования.
     */
    @Test
    void testGetBookingTicketByRouteAndPassengerPhone_Success() {
        // Создание тестовых данных
        BookingTicket booking = createTestBookingTicket("b1", "r1", "Стебунов Никита Юрьевич", "+7 999 123-45-67", "stebunov@gmail.com", Timestamp.valueOf(LocalDateTime.now()));

        // Мокирование сервиса
        when(bookingTicketService.getBookingTicketByRouteAndPassengerPhone("r1", "+7 999 123-45-67")).thenReturn(booking);

        // Вызов метода контроллера
        ResponseEntity<?> response = bookingTicketController.getBookingTicketByRouteAndPassengerPhone("r1", "+7 999 123-45-67");

        // Проверка статуса ответа
        assertEquals(HttpStatus.OK.value(), response.getStatusCodeValue());
        // Проверка тела ответа
        BookingTicket result = (BookingTicket) response.getBody();
        assertEquals("b1", result.getIdBooking());
        assertEquals("r1", result.getRoute().getIdRoute());
        assertEquals("Стебунов Никита Юрьевич", result.getUser().getPassengerFullName());
        assertEquals("+7 999 123-45-67", result.getUser().getPassengerPhone());
        assertEquals("stebunov@gmail.com", result.getUser().getPassengerEmail());
    }

    /**
     * Тест поиска бронирования по маршруту и номеру телефона с некорректным форматом телефона.
     * Проверка возврата статуса 400 и сообщения об ошибке.
     */
    @Test
    void testGetBookingTicketByRouteAndPassengerPhone_InvalidPhoneFormat() {
        // Мокирование сервиса: выброс исключения IllegalArgumentException
        when(bookingTicketService.getBookingTicketByRouteAndPassengerPhone("r1", "invalid-phone"))
                .thenThrow(new IllegalArgumentException("Неверный формат телефона. Используйте формат: +7 XXX XXX-XX-XX"));

        // Вызов метода контроллера
        ResponseEntity<?> response = bookingTicketController.getBookingTicketByRouteAndPassengerPhone("r1", "invalid-phone");

        // Проверка статуса ответа
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatusCodeValue());
        // Проверка тела ответа
        assertEquals("Неверный формат телефона. Используйте формат: +7 XXX XXX-XX-XX", response.getBody());
    }

    /**
     * Тест поиска бронирования по маршруту и номеру телефона, когда маршрут не найден.
     * Проверка возврата статуса 400 и сообщения об ошибке.
     */
    @Test
    void testGetBookingTicketByRouteAndPassengerPhone_RouteNotFound() {
        // Мокирование сервиса: выброс исключения IllegalArgumentException
        when(bookingTicketService.getBookingTicketByRouteAndPassengerPhone("r999", "+7 999 123-45-67"))
                .thenThrow(new IllegalArgumentException("Маршрут с ID r999 не найден."));

        // Вызов метода контроллера
        ResponseEntity<?> response = bookingTicketController.getBookingTicketByRouteAndPassengerPhone("r999", "+7 999 123-45-67");

        // Проверка статуса ответа
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatusCodeValue());
        // Проверка тела ответа
        assertEquals("Маршрут с ID r999 не найден.", response.getBody());
    }

    /**
     * Тест поиска бронирования по маршруту и номеру телефона, когда бронирование не найдено.
     * Проверка возврата статуса 404 и сообщения об ошибке.
     */
    @Test
    void testGetBookingTicketByRouteAndPassengerPhone_NotFound() {
        // Мокирование сервиса: выброс исключения NoSuchElementException
        when(bookingTicketService.getBookingTicketByRouteAndPassengerPhone("r1", "+7 999 123-45-67"))
                .thenThrow(new NoSuchElementException("Бронирование с телефоном '+7 999 123-45-67' не найдено."));

        // Вызов метода контроллера
        ResponseEntity<?> response = bookingTicketController.getBookingTicketByRouteAndPassengerPhone("r1", "+7 999 123-45-67");

        // Проверка статуса ответа
        assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatusCodeValue());
        // Проверка тела ответа
        assertEquals("Бронирование с телефоном '+7 999 123-45-67' не найдено.", response.getBody());
    }

    /**
     * Тест поиска бронирования по маршруту и номеру телефона при внутренней ошибке сервера.
     * Проверка возврата статуса 500 и сообщения об ошибке.
     */
    @Test
    void testGetBookingTicketByRouteAndPassengerPhone_InternalServerError() {
        // Мокирование сервиса: выброс исключения
        when(bookingTicketService.getBookingTicketByRouteAndPassengerPhone("r1", "+7 999 123-45-67"))
                .thenThrow(new RuntimeException("Внутренняя ошибка"));

        // Вызов метода контроллера
        ResponseEntity<?> response = bookingTicketController.getBookingTicketByRouteAndPassengerPhone("r1", "+7 999 123-45-67");

        // Проверка статуса ответа
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), response.getStatusCodeValue());
        // Проверка тела ответа
        assertEquals("Произошла ошибка при поиске бронирования: Внутренняя ошибка", response.getBody());
    }

    /**
     * Тест получения всех бронирований для маршрута.
     * Проверка корректности возвращаемого списка бронирований.
     */
    @Test
    void testGetBookingTicketsByRoute_Success() {
        // Создание тестовых данных: два бронирования
        BookingTicket booking1 = createTestBookingTicket("b1", "r1", "Стебунов Никита Юрьевич", "+7 999 123-45-67", "stebunov@gmail.com", Timestamp.valueOf(LocalDateTime.now()));
        BookingTicket booking2 = createTestBookingTicket("b2", "r1", "Комарова Анна Васильевна", "+7 999 987-65-43", "komarova@gmail.com", Timestamp.valueOf(LocalDateTime.now()));
        List<BookingTicket> bookings = Arrays.asList(booking1, booking2);

        // Мокирование сервиса
        when(bookingTicketService.getBookingTicketsByRoute("r1")).thenReturn(bookings);

        // Вызов метода контроллера
        ResponseEntity<?> response = bookingTicketController.getBookingTicketsByRoute("r1");

        // Проверка статуса ответа
        assertEquals(HttpStatus.OK.value(), response.getStatusCodeValue());
        // Проверка тела ответа
        List<BookingTicket> result = (List<BookingTicket>) response.getBody();
        assertEquals(2, result.size());

        // Проверка полей первого бронирования
        BookingTicket returnedBooking1 = result.get(0);
        assertEquals("b1", returnedBooking1.getIdBooking());
        assertEquals("r1", returnedBooking1.getRoute().getIdRoute());
        assertEquals("Стебунов Никита Юрьевич", returnedBooking1.getUser().getPassengerFullName());
        assertEquals("+7 999 123-45-67", returnedBooking1.getUser().getPassengerPhone());
        assertEquals("stebunov@gmail.com", returnedBooking1.getUser().getPassengerEmail());

        // Проверка полей второго бронирования
        BookingTicket returnedBooking2 = result.get(1);
        assertEquals("b2", returnedBooking2.getIdBooking());
        assertEquals("r1", returnedBooking2.getRoute().getIdRoute());
        assertEquals("Комарова Анна Васильевна", returnedBooking2.getUser().getPassengerFullName());
        assertEquals("+7 999 987-65-43", returnedBooking2.getUser().getPassengerPhone());
        assertEquals("komarova@gmail.com", returnedBooking2.getUser().getPassengerEmail());
    }

    /**
     * Тест получения всех бронирований для маршрута, когда бронирования не найдены.
     * Проверка возврата статуса 404 и сообщения об ошибке.
     */
    @Test
    void testGetBookingTicketsByRoute_NotFound() {
        // Мокирование сервиса: возвращается пустой список
        when(bookingTicketService.getBookingTicketsByRoute("r1")).thenReturn(Collections.emptyList());

        // Вызов метода контроллера
        ResponseEntity<?> response = bookingTicketController.getBookingTicketsByRoute("r1");

        // Проверка статуса ответа
        assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatusCodeValue());
        // Проверка тела ответа
        assertEquals("Бронирования для маршрута с ID r1 не найдены.", response.getBody());
    }

    /**
     * Тест получения всех бронирований для маршрута, когда маршрут не найден.
     * Проверка возврата статуса 404 и сообщения об ошибке.
     */
    @Test
    void testGetBookingTicketsByRoute_RouteNotFound() {
        // Мокирование сервиса: выброс исключения IllegalArgumentException
        when(bookingTicketService.getBookingTicketsByRoute("r999"))
                .thenThrow(new IllegalArgumentException("Маршрут с ID r999 не найден."));

        // Вызов метода контроллера
        ResponseEntity<?> response = bookingTicketController.getBookingTicketsByRoute("r999");

        // Проверка статуса ответа
        assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatusCodeValue());
        // Проверка тела ответа
        assertEquals("Маршрут с ID r999 не найден.", response.getBody());
    }

    /**
     * Тест получения всех бронирований для маршрута при внутренней ошибке сервера.
     * Проверка возврата статуса 500 и сообщения об ошибке.
     */
    @Test
    void testGetBookingTicketsByRoute_InternalServerError() {
        // Мокирование сервиса: выброс исключения
        when(bookingTicketService.getBookingTicketsByRoute("r1")).thenThrow(new RuntimeException("Внутренняя ошибка"));

        // Вызов метода контроллера
        ResponseEntity<?> response = bookingTicketController.getBookingTicketsByRoute("r1");

        // Проверка статуса ответа
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), response.getStatusCodeValue());
        // Проверка тела ответа
        assertEquals("Произошла ошибка при получении бронирований: Внутренняя ошибка", response.getBody());
    }

    /**
     * Тест получения бронирований по email пассажира.
     * Проверка корректности возвращаемого списка бронирований.
     */
    @Test
    void testGetBookingTicketsByPassengerEmail_Success() {
        // Создание тестовых данных: два бронирования
        BookingTicket booking1 = createTestBookingTicket("b1", "r1", "Стебунов Никита Юрьевич", "+7 999 123-45-67", "stebunov@gmail.com", Timestamp.valueOf(LocalDateTime.now()));
        BookingTicket booking2 = createTestBookingTicket("b2", "r2", "Стебунов Никита Юрьевич", "+7 999 123-45-67", "stebunov@gmail.com", Timestamp.valueOf(LocalDateTime.now()));
        List<BookingTicket> bookings = Arrays.asList(booking1, booking2);

        // Мокирование сервиса
        when(bookingTicketService.getBookingTicketsByPassengerEmail("stebunov@gmail.com")).thenReturn(bookings);

        // Вызов метода контроллера
        ResponseEntity<?> response = bookingTicketController.getBookingTicketsByPassengerEmail("stebunov@gmail.com");

        // Проверка статуса ответа
        assertEquals(HttpStatus.OK.value(), response.getStatusCodeValue());
        // Проверка тела ответа
        List<BookingTicket> result = (List<BookingTicket>) response.getBody();
        assertEquals(2, result.size());

        // Проверка полей первого бронирования
        BookingTicket returnedBooking1 = result.get(0);
        assertEquals("b1", returnedBooking1.getIdBooking());
        assertEquals("r1", returnedBooking1.getRoute().getIdRoute());
        assertEquals("Стебунов Никита Юрьевич", returnedBooking1.getUser().getPassengerFullName());
        assertEquals("+7 999 123-45-67", returnedBooking1.getUser().getPassengerPhone());
        assertEquals("stebunov@gmail.com", returnedBooking1.getUser().getPassengerEmail());

        // Проверка полей второго бронирования
        BookingTicket returnedBooking2 = result.get(1);
        assertEquals("b2", returnedBooking2.getIdBooking());
        assertEquals("r2", returnedBooking2.getRoute().getIdRoute());
        assertEquals("Стебунов Никита Юрьевич", returnedBooking2.getUser().getPassengerFullName());
        assertEquals("+7 999 123-45-67", returnedBooking2.getUser().getPassengerPhone());
        assertEquals("stebunov@gmail.com", returnedBooking2.getUser().getPassengerEmail());
    }

    /**
     * Тест получения бронирований по email пассажира с некорректным форматом email.
     * Проверка возврата статуса 400 и сообщения об ошибке.
     */
    @Test
    void testGetBookingTicketsByPassengerEmail_InvalidEmailFormat() {
        // Мокирование сервиса: выброс исключения IllegalArgumentException
        when(bookingTicketService.getBookingTicketsByPassengerEmail("invalid-email"))
                .thenThrow(new IllegalArgumentException("Неверный формат email. Используйте формат: имя@домен (mail.ru, yandex.ru, gmail.com)"));

        // Вызов метода контроллера
        ResponseEntity<?> response = bookingTicketController.getBookingTicketsByPassengerEmail("invalid-email");

        // Проверка статуса ответа
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatusCodeValue());
        // Проверка тела ответа
        assertEquals("Неверный формат email. Используйте формат: имя@домен (mail.ru, yandex.ru, gmail.com)", response.getBody());
    }

    /**
     * Тест получения бронирований по email пассажира, когда пользователь не найден.
     * Проверка возврата статуса 404 и сообщения об ошибке.
     */
    @Test
    void testGetBookingTicketsByPassengerEmail_UserNotFound() {
        // Мокирование сервиса: выброс исключения NoSuchElementException
        when(bookingTicketService.getBookingTicketsByPassengerEmail("stebunov@gmail.com"))
                .thenThrow(new NoSuchElementException("Пользователь с email 'stebunov@gmail.com' не найден."));

        // Вызов метода контроллера
        ResponseEntity<?> response = bookingTicketController.getBookingTicketsByPassengerEmail("stebunov@gmail.com");

        // Проверка статуса ответа
        assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatusCodeValue());
        // Проверка тела ответа
        assertEquals("Пользователь с email 'stebunov@gmail.com' не найден.", response.getBody());
    }

    /**
     * Тест получения бронирований по email пассажира, когда бронирования не найдены.
     * Проверка возврата статуса 404 и сообщения об ошибке.
     */
    @Test
    void testGetBookingTicketsByPassengerEmail_NotFound() {
        // Мокирование сервиса: возвращается пустой список
        when(bookingTicketService.getBookingTicketsByPassengerEmail("stebunov@gmail.com")).thenReturn(Collections.emptyList());

        // Вызов метода контроллера
        ResponseEntity<?> response = bookingTicketController.getBookingTicketsByPassengerEmail("stebunov@gmail.com");

        // Проверка статуса ответа
        assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatusCodeValue());
        // Проверка тела ответа
        assertEquals("Бронирования для пользователя с email stebunov@gmail.com не найдены.", response.getBody());
    }

    /**
     * Тест получения бронирований по email пассажира при внутренней ошибке сервера.
     * Проверка возврата статуса 500 и сообщения об ошибке.
     */
    @Test
    void testGetBookingTicketsByPassengerEmail_InternalServerError() {
        // Мокирование сервиса: выброс исключения
        when(bookingTicketService.getBookingTicketsByPassengerEmail("stebunov@gmail.com")).thenThrow(new RuntimeException("Внутренняя ошибка"));

        // Вызов метода контроллера
        ResponseEntity<?> response = bookingTicketController.getBookingTicketsByPassengerEmail("stebunov@gmail.com");

        // Проверка статуса ответа
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), response.getStatusCodeValue());
        // Проверка тела ответа
        assertEquals("Произошла ошибка при получении бронирований: Внутренняя ошибка", response.getBody());
    }
}