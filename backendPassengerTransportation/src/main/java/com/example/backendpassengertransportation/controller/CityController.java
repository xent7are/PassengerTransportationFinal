package com.example.backendpassengertransportation.controller;

import com.example.backendpassengertransportation.model.City;
import com.example.backendpassengertransportation.service.CityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

// Контроллер для управления городами, предоставляет CRUD-операции
@RestController
@RequestMapping("/cities")
public class CityController {

    @Autowired
    // Сервис для работы с городами
    private CityService cityService;

    // Получение всех городов
    @Operation(
            summary = "Получение списка всех городов",
            description = "Возвращает список всех городов, доступных в системе. " +
                    "Для выполнения запроса необходимо авторизоваться: в Swagger UI нажмите кнопку 'Authorize' в верхней части страницы и введите валидный JWT-токен. " +
                    "Если города отсутствуют, возвращается статус 404 с сообщением об ошибке. " +
                    "При отсутствии доступа возвращается статус 403. " +
                    "При внутренней ошибке сервера возвращается статус 500 с пустым списком."
    )
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "Список городов успешно получен."),
                    @ApiResponse(responseCode = "401", description = "Недействительный или отсутствующий токен."),
                    @ApiResponse(responseCode = "403", description = "Нет доступа к выполнению операции."),
                    @ApiResponse(responseCode = "404", description = "Города не найдены."),
                    @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера.")
            }
    )
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("")
    public ResponseEntity<?> getAllCities() {
        try {
            // Получение списка всех городов через сервис
            List<City> cities = cityService.getAllCities();
            if (cities.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Города не найдены.");
            }
            return ResponseEntity.ok(cities);
        } catch (Exception e) {
            // Обработка внутренней ошибки сервера
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.emptyList());
        }
    }

    // Получение города по идентификатору
    @Operation(
            summary = "Получение города по ID",
            description = "Возвращает город по указанному идентификатору. " +
                    "Для выполнения запроса необходимо авторизоваться: в Swagger UI нажмите кнопку 'Authorize' в верхней части страницы и введите валидный JWT-токен. " +
                    "Если город не найден, возвращается статус 404 с сообщением об ошибке. " +
                    "При отсутствии доступа возвращается статус 403. " +
                    "При внутренней ошибке сервера возвращается статус 500 с null."
    )
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "Город успешно получен."),
                    @ApiResponse(responseCode = "401", description = "Недействительный или отсутствующий токен."),
                    @ApiResponse(responseCode = "403", description = "Нет доступа к выполнению операции."),
                    @ApiResponse(responseCode = "404", description = "Город не найден."),
                    @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера.")
            }
    )
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/{id}")
    public ResponseEntity<?> getCityById(
            @Parameter(description = "Идентификатор города", required = true)
            @PathVariable String id) {
        try {
            // Поиск города по идентификатору через сервис
            City city = cityService.getCityById(id);
            if (city == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Город с ID " + id + " не найден.");
            }
            return ResponseEntity.ok(city);
        } catch (Exception e) {
            // Обработка внутренней ошибки сервера
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    // Создание нового города
    @Operation(
            summary = "Создание нового города",
            description = "Создает новый город с указанным названием. " +
                    "Для выполнения запроса необходимо авторизоваться: в Swagger UI нажмите кнопку 'Authorize' в верхней части страницы и введите валидный JWT-токен. " +
                    "При успешном создании возвращается статус 201 и созданный город. " +
                    "Если переданы некорректные параметры, возвращается статус 400 с сообщением об ошибке. " +
                    "При отсутствии доступа возвращается статус 403. " +
                    "При внутренней ошибке сервера возвращается статус 500."
    )
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "201", description = "Город успешно создан."),
                    @ApiResponse(responseCode = "401", description = "Недействительный или отсутствующий токен."),
                    @ApiResponse(responseCode = "403", description = "Нет доступа к выполнению операции."),
                    @ApiResponse(responseCode = "400", description = "Некорректные параметры."),
                    @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера.")
            }
    )
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("")
    public ResponseEntity<?> createCity(
            @Parameter(description = "Название нового города", required = true)
            @RequestParam String cityName) {
        try {
            // Создание нового города через сервис
            City newCity = cityService.createCity(cityName);
            return ResponseEntity.status(HttpStatus.CREATED).body(newCity);
        } catch (IllegalArgumentException e) {
            // Обработка ошибки некорректных параметров
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            // Обработка внутренней ошибки сервера
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Произошла ошибка при создании города: " + e.getMessage());
        }
    }

    // Обновление города по идентификатору
    @Operation(
            summary = "Обновление города по ID",
            description = "Обновляет город с указанным идентификатором. " +
                    "Для выполнения запроса необходимо авторизоваться: в Swagger UI нажмите кнопку 'Authorize' в верхней части страницы и введите валидный JWT-токен. " +
                    "При успешном обновлении возвращается статус 200 и обновленный город. " +
                    "Если город не найден, возвращается статус 404 с сообщением об ошибке. " +
                    "При отсутствии доступа возвращается статус 403. " +
                    "При некорректных параметрах возвращается статус 400 с сообщением об ошибке. " +
                    "При внутренней ошибке сервера возвращается статус 500."
    )
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "Город успешно обновлен."),
                    @ApiResponse(responseCode = "401", description = "Недействительный или отсутствующий токен."),
                    @ApiResponse(responseCode = "403", description = "Нет доступа к выполнению операции."),
                    @ApiResponse(responseCode = "400", description = "Некорректные параметры."),
                    @ApiResponse(responseCode = "404", description = "Город не найден."),
                    @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера.")
            }
    )
    @SecurityRequirement(name = "bearerAuth")
    @PutMapping("/{id}")
    public ResponseEntity<?> updateCity(
            @Parameter(description = "Идентификатор города для обновления", required = true)
            @PathVariable String id,
            @Parameter(description = "Новое название города", required = true)
            @RequestParam String cityName) {
        try {
            // Обновление города через сервис
            City updatedCity = cityService.updateCity(id, cityName);
            return ResponseEntity.ok(updatedCity);
        } catch (IllegalArgumentException e) {
            // Обработка ошибки некорректных параметров
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (NoSuchElementException e) {
            // Обработка ошибки отсутствия города
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            // Обработка внутренней ошибки сервера
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Произошла ошибка при обновлении города: " + e.getMessage());
        }
    }

    // Удаление города по идентификатору
    @Operation(
            summary = "Удаление города по ID",
            description = "Удаляет город по указанному идентификатору. " +
                    "Для выполнения запроса необходимо авторизоваться: в Swagger UI нажмите кнопку 'Authorize' в верхней части страницы и введите валидный JWT-токен. " +
                    "При успешном удалении возвращается статус 200 с сообщением об успехе. " +
                    "Если город не найден, возвращается статус 404 с сообщением об ошибке. " +
                    "При отсутствии доступа возвращается статус 403. " +
                    "При внутренней ошибке сервера возвращается статус 500."
    )
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "Город успешно удален."),
                    @ApiResponse(responseCode = "401", description = "Недействительный или отсутствующий токен."),
                    @ApiResponse(responseCode = "403", description = "Нет доступа к выполнению операции."),
                    @ApiResponse(responseCode = "404", description = "Город не найден."),
                    @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера.")
            }
    )
    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCity(
            @Parameter(description = "Идентификатор города для удаления", required = true)
            @PathVariable String id) {
        try {
            // Удаление города через сервис
            cityService.deleteCity(id);
            return ResponseEntity.ok("Город успешно удален.");
        } catch (IllegalArgumentException e) {
            // Обработка ошибки отсутствия города
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            // Обработка внутренней ошибки сервера
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Произошла ошибка при удалении города: " + e.getMessage());
        }
    }
}