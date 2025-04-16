package com.example.backendpassengertransportation.controller;

import com.example.backendpassengertransportation.model.BookingTicket;
import com.example.backendpassengertransportation.service.BookingTicketService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;

// Контроллер для управления бронированиями билетов, предоставляет CRUD-операции и поиск
@RestController
@RequestMapping("/booking-tickets")
public class BookingTicketController {

    @Autowired
    // Сервис для работы с бронированиями билетов
    private BookingTicketService bookingTicketService;

    // Получение всех бронирований с обработкой ошибок
    @Operation(
            summary = "Получение списка всех бронирований",
            description = "Возвращает список всех бронирований, доступных в системе. " +
                    "Для выполнения запроса необходимо авторизоваться: в Swagger UI нажмите кнопку 'Authorize' в верхней части страницы и введите валидный JWT-токен. " +
                    "Если бронирования отсутствуют, возвращается статус 404 с сообщением об ошибке. " +
                    "При отсутствии доступа возвращается статус 403. " +
                    "При внутренней ошибке сервера возвращается статус 500 с пустым списком."
    )
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "Список бронирований успешно получен."),
                    @ApiResponse(responseCode = "401", description = "Недействительный или отсутствующий токен."),
                    @ApiResponse(responseCode = "403", description = "Нет доступа к выполнению операции."),
                    @ApiResponse(responseCode = "404", description = "Бронирования не найдены."),
                    @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера.")
            }
    )
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("")
    public ResponseEntity<?> getAllBookingTickets() {
        try {
            // Получение списка всех бронирований через сервис
            List<BookingTicket> bookingTickets = bookingTicketService.getAllBookingTickets();
            if (bookingTickets.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Бронирования не найдены.");
            }
            return ResponseEntity.ok(bookingTickets);
        } catch (Exception e) {
            // Обработка внутренней ошибки сервера
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Произошла ошибка при получении списка бронирований: " + e.getMessage());
        }
    }

    // Получение бронирования по идентификатору с обработкой ошибок
    @Operation(
            summary = "Получение бронирования по ID",
            description = "Возвращает бронирование по указанному идентификатору. " +
                    "Для выполнения запроса необходимо авторизоваться: в Swagger UI нажмите кнопку 'Authorize' в верхней части страницы и введите валидный JWT-токен. " +
                    "Если бронирование не найдено, возвращается статус 404 с сообщением об ошибке. " +
                    "При отсутствии доступа возвращается статус 403. " +
                    "При внутренней ошибке сервера возвращается статус 500 с null."
    )
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "Бронирование успешно получено."),
                    @ApiResponse(responseCode = "401", description = "Недействительный или отсутствующий токен."),
                    @ApiResponse(responseCode = "403", description = "Нет доступа к выполнению операции."),
                    @ApiResponse(responseCode = "404", description = "Бронирование не найдено."),
                    @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера.")
            }
    )
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/{id}")
    public ResponseEntity<?> getBookingTicketById(
            @Parameter(description = "Идентификатор бронирования", required = true)
            @PathVariable String id) {
        try {
            // Поиск бронирования по идентификатору через сервис
            BookingTicket bookingTicket = bookingTicketService.getBookingTicketById(id);
            return ResponseEntity.ok(bookingTicket);
        } catch (NoSuchElementException e) {
            // Обработка ошибки отсутствия бронирования
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            // Обработка внутренней ошибки сервера
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Произошла ошибка при получении бронирования: " + e.getMessage());
        }
    }

    // Создание нового бронирования с проверкой времени до отправления
    @Operation(
            summary = "Создание нового бронирования",
            description = "Создает новое бронирование для указанного маршрута и пользователя. " +
                    "Для выполнения запроса необходимо авторизоваться: в Swagger UI нажмите кнопку 'Authorize' в верхней части страницы и введите валидный JWT-токен. " +
                    "При успешном создании возвращается статус 201 и созданное бронирование. " +
                    "Если переданы некорректные параметры, возвращается статус 400 с сообщением об ошибке. " +
                    "Если маршрут или пользователь не найдены, возвращается статус 404. " +
                    "Если маршрут уже отправился или до отправления осталось менее 30 минут, возвращается статус 400 с сообщением об ошибке. " +
                    "При отсутствии доступа возвращается статус 403. " +
                    "При внутренней ошибке сервера возвращается статус 500 с сообщением об ошибке."
    )
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "201", description = "Бронирование успешно создано."),
                    @ApiResponse(responseCode = "401", description = "Недействительный или отсутствующий токен."),
                    @ApiResponse(responseCode = "403", description = "Нет доступа к выполнению операции."),
                    @ApiResponse(responseCode = "400", description = "Некорректные параметры, маршрут уже отправился или до отправления осталось менее 30 минут."),
                    @ApiResponse(responseCode = "404", description = "Маршрут или пользователь не найдены."),
                    @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера.")
            }
    )
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("")
    public ResponseEntity<?> createBookingTicket(
            @Parameter(description = "Идентификатор маршрута", required = true)
            @RequestParam String routeId,
            @Parameter(description = "Полное имя пассажира", required = true)
            @RequestParam String passengerFullName,
            @Parameter(description = "Номер телефона пассажира", required = true)
            @RequestParam String passengerPhone,
            @Parameter(description = "Email пассажира", required = true)
            @RequestParam String passengerEmail) {
        try {
            // Создание нового бронирования через сервис
            BookingTicket newBookingTicket = bookingTicketService.createBookingTicket(routeId, passengerFullName, passengerPhone, passengerEmail);
            return ResponseEntity.status(HttpStatus.CREATED).body(newBookingTicket);
        } catch (IllegalArgumentException | IllegalStateException e) {
            // Обработка ошибки некорректных параметров или состояния маршрута
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (NoSuchElementException e) {
            // Обработка ошибки отсутствия маршрута или пользователя
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            // Обработка внутренней ошибки сервера
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Произошла ошибка при создании бронирования: " + e.getMessage());
        }
    }

    // Удаление бронирования по идентификатору с проверкой времени до отправления
    @Operation(
            summary = "Удаление бронирования по ID",
            description = "Удаляет бронирование по указанному идентификатору и восстанавливает количество доступных мест на маршруте. " +
                    "Для выполнения запроса необходимо авторизоваться: в Swagger UI нажмите кнопку 'Authorize' в верхней части страницы и введите валидный JWT-токен. " +
                    "При успешном удалении возвращается статус 200 с сообщением об успехе. " +
                    "Если бронирование не найдено, возвращается статус 404 с сообщением об ошибке. " +
                    "Если маршрут уже отправился или до отправления осталось менее 30 минут, возвращается статус 400 с сообщением об ошибке. " +
                    "При отсутствии доступа возвращается статус 403. " +
                    "При некорректных данных возвращается статус 400 с сообщением об ошибке."
    )
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "Бронирование успешно удалено."),
                    @ApiResponse(responseCode = "401", description = "Недействительный или отсутствующий токен."),
                    @ApiResponse(responseCode = "403", description = "Нет доступа к выполнению операции."),
                    @ApiResponse(responseCode = "400", description = "Некорректные данные, маршрут уже отправился или до отправления осталось менее 30 минут."),
                    @ApiResponse(responseCode = "404", description = "Бронирование не найдено.")
            }
    )
    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteBookingTicket(
            @Parameter(description = "Идентификатор бронирования для удаления", required = true)
            @PathVariable String id) {
        try {
            // Удаление бронирования по идентификатору через сервис
            bookingTicketService.deleteBookingTicket(id);
            return ResponseEntity.ok("Бронирование успешно удалено.");
        } catch (IllegalStateException e) {
            // Обработка ошибки некорректного состояния маршрута
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            // Обработка ошибки отсутствия бронирования
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            // Обработка внутренней ошибки сервера
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Произошла ошибка при удалении бронирования: " + e.getMessage());
        }
    }

    // Поиск бронирования по идентификатору маршрута и номеру телефона пассажира с обработкой ошибок
    @Operation(
            summary = "Поиск бронирования по маршруту и номеру телефона",
            description = "Возвращает бронирование, связанное с указанным маршрутом и номером телефона пассажира. " +
                    "Для выполнения запроса необходимо авторизоваться: в Swagger UI нажмите кнопку 'Authorize' в верхней части страницы и введите валидный JWT-токен. " +
                    "Если бронирование не найдено, возвращается статус 404 с сообщением об ошибке. " +
                    "При отсутствии доступа возвращается статус 403. " +
                    "При некорректных параметрах возвращается статус 400. " +
                    "При внутренней ошибке сервера возвращается статус 500 с сообщением об ошибке. " +
                    "Номер телефона должен быть в формате '+7 XXX XXX-XX-XX'."
    )
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "Бронирование успешно найдено."),
                    @ApiResponse(responseCode = "401", description = "Недействительный или отсутствующий токен."),
                    @ApiResponse(responseCode = "403", description = "Нет доступа к выполнению операции."),
                    @ApiResponse(responseCode = "400", description = "Некорректные параметры."),
                    @ApiResponse(responseCode = "404", description = "Бронирование не найдено."),
                    @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера.")
            }
    )
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/route/{routeId}/phone/{passengerPhone}")
    public ResponseEntity<?> getBookingTicketByRouteAndPassengerPhone(
            @Parameter(description = "Идентификатор маршрута для поиска бронирования", required = true)
            @PathVariable String routeId,
            @Parameter(description = "Номер телефона пассажира в формате '+7 XXX XXX-XX-XX'", required = true)
            @PathVariable String passengerPhone) {
        try {
            // Поиск бронирования по идентификатору маршрута и номеру телефона пассажира
            BookingTicket bookingTicket = bookingTicketService.getBookingTicketByRouteAndPassengerPhone(routeId, passengerPhone);
            return ResponseEntity.ok(bookingTicket);
        } catch (IllegalArgumentException e) {
            // Обработка ошибки некорректных параметров
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (NoSuchElementException e) {
            // Обработка ошибки отсутствия бронирования
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            // Обработка внутренней ошибки сервера
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Произошла ошибка при поиске бронирования: " + e.getMessage());
        }
    }

    // Получение всех бронирований по идентификатору маршрута с обработкой ошибок
    @Operation(
            summary = "Получение всех бронирований для маршрута",
            description = "Возвращает список всех бронирований для указанного маршрута. " +
                    "Для выполнения запроса необходимо авторизоваться: в Swagger UI нажмите кнопку 'Authorize' в верхней части страницы и введите валидный JWT-токен. " +
                    "Если бронирования не найдены, возвращается статус 404 с пустым списком. " +
                    "При отсутствии доступа возвращается статус 403. " +
                    "При внутренней ошибке сервера возвращается статус 500 с пустым списком."
    )
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "Список бронирований успешно получен."),
                    @ApiResponse(responseCode = "401", description = "Недействительный или отсутствующий токен."),
                    @ApiResponse(responseCode = "403", description = "Нет доступа к выполнению операции."),
                    @ApiResponse(responseCode = "404", description = "Бронирования не найдены."),
                    @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера.")
            }
    )
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/route/{routeId}")
    public ResponseEntity<?> getBookingTicketsByRoute(
            @Parameter(description = "Идентификатор маршрута для получения бронирований", required = true)
            @PathVariable String routeId) {
        try {
            // Получение списка бронирований для указанного маршрута через сервис
            List<BookingTicket> bookingTickets = bookingTicketService.getBookingTicketsByRoute(routeId);
            if (bookingTickets.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Бронирования для маршрута с ID " + routeId + " не найдены.");
            }
            return ResponseEntity.ok(bookingTickets);
        } catch (IllegalArgumentException e) {
            // Обработка ошибки некорректных параметров
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            // Обработка внутренней ошибки сервера
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Произошла ошибка при получении бронирований: " + e.getMessage());
        }
    }

    // Получение бронирований по электронной почте пассажира с обработкой ошибок
    @Operation(
            summary = "Получение бронирований по email пассажира",
            description = "Возвращает список бронирований, сделанных пассажиром с указанной электронной почтой. " +
                    "Для выполнения запроса необходимо авторизоваться: в Swagger UI нажмите кнопку 'Authorize' в верхней части страницы и введите валидный JWT-токен. " +
                    "Если бронирования не найдены, возвращается статус 404 с пустым списком. " +
                    "При отсутствии доступа возвращается статус 403. " +
                    "При некорректных параметрах возвращается статус 400. " +
                    "При внутренней ошибке сервера возвращается статус 500 с пустым списком."
    )
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "Список бронирований успешно получен."),
                    @ApiResponse(responseCode = "401", description = "Недействительный или отсутствующий токен."),
                    @ApiResponse(responseCode = "403", description = "Нет доступа к выполнению операции."),
                    @ApiResponse(responseCode = "400", description = "Некорректные параметры."),
                    @ApiResponse(responseCode = "404", description = "Бронирования не найдены."),
                    @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера.")
            }
    )
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/email/{passengerEmail}")
    public ResponseEntity<?> getBookingTicketsByPassengerEmail(
            @Parameter(description = "Электронная почта пассажира для поиска бронирований", required = true)
            @PathVariable String passengerEmail) {
        try {
            // Поиск бронирований по электронной почте пассажира через сервис
            List<BookingTicket> bookingTickets = bookingTicketService.getBookingTicketsByPassengerEmail(passengerEmail);
            if (bookingTickets.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Бронирования для пользователя с email " + passengerEmail + " не найдены.");
            }
            return ResponseEntity.ok(bookingTickets);
        } catch (IllegalArgumentException e) {
            // Обработка ошибки некорректных параметров
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (NoSuchElementException e) {
            // Обработка ошибки отсутствия пользователя или бронирований
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            // Обработка внутренней ошибки сервера
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Произошла ошибка при получении бронирований: " + e.getMessage());
        }
    }
}