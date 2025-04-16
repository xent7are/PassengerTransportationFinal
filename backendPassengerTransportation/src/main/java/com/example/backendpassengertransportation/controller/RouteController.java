package com.example.backendpassengertransportation.controller;

import com.example.backendpassengertransportation.model.Route;
import com.example.backendpassengertransportation.service.RouteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

// Контроллер для управления маршрутами, предоставляет CRUD-операции и поиск по различным критериям
@RestController
@RequestMapping("/routes")
public class RouteController {

    @Autowired
    // Сервис для работы с маршрутами
    private RouteService routeService;

    // Получение всех маршрутов (без пагинации)
    @Operation(
            summary = "Получение списка всех маршрутов",
            description = "Возвращает список всех доступных маршрутов в системе. " +
                    "Для выполнения запроса необходимо авторизоваться: в Swagger UI нажмите кнопку 'Authorize' в верхней части страницы и введите валидный JWT-токен. " +
                    "Если маршруты отсутствуют, возвращается статус 404 с сообщением об ошибке. " +
                    "При отсутствии доступа возвращается статус 403. " +
                    "При внутренней ошибке сервера возвращается статус 500 с пустым списком."
    )
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "Список маршрутов успешно получен."),
                    @ApiResponse(responseCode = "401", description = "Недействительный или отсутствующий токен."),
                    @ApiResponse(responseCode = "403", description = "Нет доступа к выполнению операции."),
                    @ApiResponse(responseCode = "404", description = "Маршруты не найдены."),
                    @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера.")
            }
    )
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("")
    public ResponseEntity<?> getAllRoutes() {
        try {
            // Получение списка всех маршрутов через сервис
            List<Route> routes = routeService.getAllRoutes();
            return ResponseEntity.ok(routes);
        } catch (NoSuchElementException e) {
            // Обработка ошибки отсутствия маршрутов
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            // Обработка внутренней ошибки сервера
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.emptyList());
        }
    }

    // Получение всех маршрутов с пагинацией
    @Operation(
            summary = "Получение списка всех маршрутов с пагинацией",
            description = "Возвращает список всех доступных маршрутов в системе с поддержкой пагинации. " +
                    "Для выполнения запроса необходимо авторизоваться: в Swagger UI нажмите кнопку 'Authorize' в верхней части страницы и введите валидный JWT-токен. " +
                    "Если маршруты отсутствуют, возвращается статус 404 с сообщением об ошибке. " +
                    "При отсутствии доступа возвращается статус 403. " +
                    "При внутренней ошибке сервера возвращается статус 500 с пустым списком."
    )
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "Список маршрутов успешно получен."),
                    @ApiResponse(responseCode = "401", description = "Недействительный или отсутствующий токен."),
                    @ApiResponse(responseCode = "403", description = "Нет доступа к выполнению операции."),
                    @ApiResponse(responseCode = "404", description = "Маршруты не найдены."),
                    @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера.")
            }
    )
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/paginated")
    public ResponseEntity<?> getAllRoutesWithPagination(
            @Parameter(description = "Номер страницы для пагинации (начинается с 0)", required = false)
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Количество маршрутов на странице", required = false)
            @RequestParam(defaultValue = "16") int size,
            @Parameter(description = "Минимальное время отправления в формате ISO_LOCAL_DATE_TIME (например, '2025-03-14T10:00:00'), необязательно", required = false)
            @RequestParam(required = false) String minDepartureTime) {
        try {
            // Получение списка маршрутов с пагинацией через сервис
            Page<Route> routesPage;
            if (minDepartureTime != null) {
                // Парсинг минимального времени отправления и получение маршрутов с учетом фильтра
                LocalDateTime minTime = LocalDateTime.parse(minDepartureTime, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                routesPage = routeService.getAllRoutesWithPagination(page, size, Timestamp.valueOf(minTime));
            } else {
                // Получение маршрутов без фильтра по времени отправления
                routesPage = routeService.getAllRoutesWithPagination(page, size);
            }
            // Проверка, пустая ли страница маршрутов
            if (routesPage.isEmpty()) {
                // Обработка случая, когда маршруты не найдены
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Маршруты не найдены.");
            }
            // Возврат списка маршрутов с пагинацией
            return ResponseEntity.ok(routesPage);
        } catch (Exception e) {
            // Обработка внутренней ошибки сервера
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.emptyList());
        }
    }

    // Получение маршрута по идентификатору
    @Operation(
            summary = "Получение маршрута по ID",
            description = "Возвращает маршрут по указанному идентификатору. " +
                    "Для выполнения запроса необходимо авторизоваться: в Swagger UI нажмите кнопку 'Authorize' в верхней части страницы и введите валидный JWT-токен. " +
                    "Если маршрут не найден, возвращается статус 404 с сообщением об ошибке. " +
                    "При отсутствии доступа возвращается статус 403. " +
                    "При внутренней ошибке сервера возвращается статус 500 с null."
    )
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "Маршрут успешно получен."),
                    @ApiResponse(responseCode = "401", description = "Недействительный или отсутствующий токен."),
                    @ApiResponse(responseCode = "403", description = "Нет доступа к выполнению операции."),
                    @ApiResponse(responseCode = "404", description = "Маршрут не найден."),
                    @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера.")
            }
    )
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/{id}")
    public ResponseEntity<?> getRouteById(
            @Parameter(description = "Идентификатор маршрута", required = true)
            @PathVariable String id) {
        try {
            // Поиск маршрута по идентификатору через сервис
            Route route = routeService.getRouteById(id);
            return ResponseEntity.ok(route);
        } catch (NoSuchElementException e) {
            // Обработка ошибки отсутствия маршрута
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            // Обработка внутренней ошибки сервера
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    // Получение маршрутов по типу транспорта
    @Operation(
            summary = "Получение маршрутов по типу транспорта",
            description = "Возвращает список маршрутов, соответствующих указанному типу транспорта. " +
                    "Для выполнения запроса необходимо авторизоваться: в Swagger UI нажмите кнопку 'Authorize' в верхней части страницы и введите валидный JWT-токен. " +
                    "Если маршруты не найдены, возвращается статус 404 с сообщением об ошибке. " +
                    "При отсутствии доступа возвращается статус 403. " +
                    "При внутренней ошибке сервера возвращается статус 500 с пустым списком."
    )
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "Список маршрутов успешно получен."),
                    @ApiResponse(responseCode = "401", description = "Недействительный или отсутствующий токен."),
                    @ApiResponse(responseCode = "403", description = "Нет доступа к выполнению операции."),
                    @ApiResponse(responseCode = "404", description = "Маршруты не найдены."),
                    @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера.")
            }
    )
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/transport/{transportType}")
    public ResponseEntity<?> getRoutesByTransportType(
            @Parameter(description = "Тип транспорта для поиска маршрутов", required = true)
            @PathVariable String transportType) {
        try {
            // Поиск маршрутов по типу транспорта через сервис
            List<Route> routes = routeService.getRoutesByTransportType(transportType);
            return ResponseEntity.ok(routes);
        } catch (NoSuchElementException e) {
            // Обработка ошибки отсутствия маршрутов
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            // Обработка внутренней ошибки сервера
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.emptyList());
        }
    }

    // Получение маршрутов по пунктам отправления и назначения
    @Operation(
            summary = "Получение маршрутов по пунктам отправления и назначения",
            description = "Возвращает список маршрутов по указанным пунктам отправления и назначения. " +
                    "Для выполнения запроса необходимо авторизоваться: в Swagger UI нажмите кнопку 'Authorize' в верхней части страницы и введите валидный JWT-токен. " +
                    "Если маршруты не найдены, возвращается статус 404 с сообщением об ошибке. " +
                    "При отсутствии доступа возвращается статус 403. " +
                    "При внутренней ошибке сервера возвращается статус 500 с пустым списком."
    )
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "Список маршрутов успешно получен."),
                    @ApiResponse(responseCode = "401", description = "Недействительный или отсутствующий токен."),
                    @ApiResponse(responseCode = "403", description = "Нет доступа к выполнению операции."),
                    @ApiResponse(responseCode = "404", description = "Маршруты не найдены."),
                    @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера.")
            }
    )
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/points")
    public ResponseEntity<?> getRoutesByDepartureAndDestinationPoint(
            @Parameter(description = "Город отправления", required = true)
            @RequestParam String departureCity,
            @Parameter(description = "Город назначения", required = true)
            @RequestParam String destinationCity) {
        try {
            // Поиск маршрутов по пунктам отправления и назначения через сервис
            List<Route> routes = routeService.getRoutesByDepartureAndDestinationPoint(departureCity, destinationCity);
            if (routes.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Маршруты не найдены.");
            }
            return ResponseEntity.ok(routes);
        } catch (NoSuchElementException e) {
            // Обработка ошибки отсутствия маршрутов
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            // Обработка внутренней ошибки сервера
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.emptyList());
        }
    }

    // Получение маршрутов по пункту отправления
    @Operation(
            summary = "Получение маршрутов по пункту отправления",
            description = "Возвращает список маршрутов по указанному пункту отправления. " +
                    "Для выполнения запроса необходимо авторизоваться: в Swagger UI нажмите кнопку 'Authorize' в верхней части страницы и введите валидный JWT-токен. " +
                    "Если маршруты не найдены, возвращается статус 404 с сообщением об ошибке. " +
                    "При отсутствии доступа возвращается статус 403. " +
                    "При внутренней ошибке сервера возвращается статус 500 с пустым списком."
    )
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "Список маршрутов успешно получен."),
                    @ApiResponse(responseCode = "401", description = "Недействительный или отсутствующий токен."),
                    @ApiResponse(responseCode = "403", description = "Нет доступа к выполнению операции."),
                    @ApiResponse(responseCode = "404", description = "Маршруты не найдены."),
                    @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера.")
            }
    )
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/departure/{departureCity}")
    public ResponseEntity<?> getRoutesByDepartureCity(
            @Parameter(description = "Город отправления для поиска маршрутов", required = true)
            @PathVariable String departureCity) {
        try {
            // Поиск маршрутов по пункту отправления через сервис
            List<Route> routes = routeService.getRoutesByDepartureCity(departureCity);
            return ResponseEntity.ok(routes);
        } catch (NoSuchElementException e) {
            // Обработка ошибки отсутствия маршрутов
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            // Обработка внутренней ошибки сервера
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.emptyList());
        }
    }

    // Получение маршрутов по пункту назначения
    @Operation(
            summary = "Получение маршрутов по пункту назначения",
            description = "Возвращает список маршрутов по указанному пункту назначения. " +
                    "Для выполнения запроса необходимо авторизоваться: в Swagger UI нажмите кнопку 'Authorize' в верхней части страницы и введите валидный JWT-токен. " +
                    "Если маршруты не найдены, возвращается статус 404 с сообщением об ошибке. " +
                    "При отсутствии доступа возвращается статус 403. " +
                    "При внутренней ошибке сервера возвращается статус 500 с пустым списком."
    )
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "Список маршрутов успешно получен."),
                    @ApiResponse(responseCode = "401", description = "Недействительный или отсутствующий токен."),
                    @ApiResponse(responseCode = "403", description = "Нет доступа к выполнению операции."),
                    @ApiResponse(responseCode = "404", description = "Маршруты не найдены."),
                    @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера.")
            }
    )
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/destination/{destinationCity}")
    public ResponseEntity<?> getRoutesByDestinationCity(
            @Parameter(description = "Город назначения для поиска маршрутов", required = true)
            @PathVariable String destinationCity) {
        try {
            // Поиск маршрутов по пункту назначения через сервис
            List<Route> routes = routeService.getRoutesByDestinationCity(destinationCity);
            return ResponseEntity.ok(routes);
        } catch (NoSuchElementException e) {
            // Обработка ошибки отсутствия маршрутов
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            // Обработка внутренней ошибки сервера
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.emptyList());
        }
    }

    // Создание нового маршрута
    @Operation(
            summary = "Создание нового маршрута",
            description = "Создает новый маршрут с указанными параметрами. " +
                    "Для выполнения запроса необходимо авторизоваться: в Swagger UI нажмите кнопку 'Authorize' в верхней части страницы и введите валидный JWT-токен. " +
                    "При успешном создании возвращается статус 201 и созданный маршрут. " +
                    "Если переданы некорректные параметры, возвращается статус 400 с сообщением об ошибке. " +
                    "При отсутствии доступа возвращается статус 403. " +
                    "При внутренней ошибке сервера возвращается статус 500 с пустым списком."
    )
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "201", description = "Маршрут успешно создан."),
                    @ApiResponse(responseCode = "401", description = "Недействительный или отсутствующий токен."),
                    @ApiResponse(responseCode = "403", description = "Нет доступа к выполнению операции."),
                    @ApiResponse(responseCode = "400", description = "Некорректные параметры."),
                    @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера.")
            }
    )
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("")
    public ResponseEntity<?> createRoute(
            @Parameter(description = "Тип транспорта для маршрута", required = true)
            @RequestParam String transportType,
            @Parameter(description = "Город отправления", required = true)
            @RequestParam String departureCity,
            @Parameter(description = "Город назначения", required = true)
            @RequestParam String destinationCity,
            @Parameter(description = "Время отправления в формате 'dd.MM.yyyy HH:mm'", required = true)
            @RequestParam String departureTime,
            @Parameter(description = "Время прибытия в формате 'dd.MM.yyyy HH:mm'", required = true)
            @RequestParam String arrivalTime,
            @Parameter(description = "Общее количество мест", required = true)
            @RequestParam int totalNumberSeats,
            @Parameter(description = "Количество доступных мест", required = true)
            @RequestParam int numberAvailableSeats) {
        try {
            // Создание нового маршрута через сервис
            Route newRoute = routeService.createRoute(transportType, departureCity, destinationCity,
                    departureTime, arrivalTime, totalNumberSeats, numberAvailableSeats);
            return ResponseEntity.status(HttpStatus.CREATED).body(newRoute);
        } catch (IllegalArgumentException e) {
            // Обработка ошибки некорректных параметров
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            // Обработка внутренней ошибки сервера
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.emptyList());
        }
    }

    // Удаление маршрута по идентификатору
    @Operation(
            summary = "Удаление маршрута по ID",
            description = "Удаляет маршрут по указанному идентификатору. " +
                    "Для выполнения запроса необходимо авторизоваться: в Swagger UI нажмите кнопку 'Authorize' в верхней части страницы и введите валидный JWT-токен. " +
                    "При успешном удалении возвращается статус 200 с сообщением об успехе. " +
                    "Если маршрут не найден, возвращается статус 404 с сообщением об ошибке. " +
                    "При отсутствии доступа возвращается статус 403. " +
                    "При некорректных параметрах возвращается статус 400."
    )
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "Маршрут успешно удален."),
                    @ApiResponse(responseCode = "401", description = "Недействительный или отсутствующий токен."),
                    @ApiResponse(responseCode = "403", description = "Нет доступа к выполнению операции."),
                    @ApiResponse(responseCode = "400", description = "Некорректные параметры."),
                    @ApiResponse(responseCode = "404", description = "Маршрут не найден.")
            }
    )
    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteRoute(
            @Parameter(description = "Идентификатор маршрута для удаления", required = true)
            @PathVariable String id) {
        try {
            // Удаление маршрута через сервис
            routeService.deleteRoute(id);
            return ResponseEntity.ok("Маршрут успешно удален.");
        } catch (NoSuchElementException e) {
            // Обработка ошибки отсутствия маршрута
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            // Обработка ошибки некорректных параметров
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            // Обработка внутренней ошибки сервера
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.emptyList());
        }
    }

    // Получение маршрутов по точной дате отправления
    @Operation(
            summary = "Поиск маршрутов по точной дате отправления",
            description = "Возвращает список маршрутов, отправляющихся в указанную дату. " +
                    "Для выполнения запроса необходимо авторизоваться: в Swagger UI нажмите кнопку 'Authorize' в верхней части страницы и введите валидный JWT-токен. " +
                    "Если маршруты не найдены, возвращается статус 404 с сообщением об ошибке. " +
                    "При отсутствии доступа возвращается статус 403. " +
                    "При некорректном формате даты возвращается статус 400. " +
                    "Дата должна быть в формате 'dd.MM.yyyy'."
    )
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "Список маршрутов успешно получен."),
                    @ApiResponse(responseCode = "401", description = "Недействительный или отсутствующий токен."),
                    @ApiResponse(responseCode = "403", description = "Нет доступа к выполнению операции."),
                    @ApiResponse(responseCode = "400", description = "Некорректный формат даты."),
                    @ApiResponse(responseCode = "404", description = "Маршруты не найдены.")
            }
    )
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/exactDate")
    public ResponseEntity<?> getRoutesForExactDate(
            @Parameter(description = "Точная дата отправления в формате 'dd.MM.yyyy'", required = true)
            @RequestParam String exactDate) {
        try {
            // Поиск маршрутов по точной дате через сервис
            List<Route> routes = routeService.fetchRoutesForExactDate(exactDate);
            return ResponseEntity.ok(routes);
        } catch (NoSuchElementException e) {
            // Обработка ошибки отсутствия маршрутов
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            // Обработка ошибки некорректного формата даты
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            // Обработка внутренней ошибки сервера
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.emptyList());
        }
    }

    // Получение маршрутов по диапазону дат отправления
    @Operation(
            summary = "Поиск маршрутов по диапазону дат отправления",
            description = "Возвращает список маршрутов, отправляющихся в указанный диапазон дат. " +
                    "Для выполнения запроса необходимо авторизоваться: в Swagger UI нажмите кнопку 'Authorize' в верхней части страницы и введите валидный JWT-токен. " +
                    "Если маршруты не найдены, возвращается статус 404 с сообщением об ошибке. " +
                    "При отсутствии доступа возвращается статус 403. " +
                    "При некорректном формате дат возвращается статус 400. " +
                    "Даты должны быть в формате 'dd.MM.yyyy'."
    )
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "Список маршрутов успешно получен."),
                    @ApiResponse(responseCode = "401", description = "Недействительный или отсутствующий токен."),
                    @ApiResponse(responseCode = "403", description = "Нет доступа к выполнению операции."),
                    @ApiResponse(responseCode = "400", description = "Некорректный формат дат."),
                    @ApiResponse(responseCode = "404", description = "Маршруты не найдены.")
            }
    )
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/dateRange")
    public ResponseEntity<?> getRoutesWithinDateRange(
            @Parameter(description = "Начальная дата диапазона в формате 'dd.MM.yyyy'", required = true)
            @RequestParam String startDate,
            @Parameter(description = "Конечная дата диапазона в формате 'dd.MM.yyyy'", required = true)
            @RequestParam String endDate) {
        try {
            // Поиск маршрутов по диапазону дат через сервис
            List<Route> routes = routeService.fetchRoutesWithinDateRange(startDate, endDate);
            return ResponseEntity.ok(routes);
        } catch (NoSuchElementException e) {
            // Обработка ошибки отсутствия маршрутов
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            // Обработка ошибки некорректного формата дат
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            // Обработка внутренней ошибки сервера
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.emptyList());
        }
    }
}