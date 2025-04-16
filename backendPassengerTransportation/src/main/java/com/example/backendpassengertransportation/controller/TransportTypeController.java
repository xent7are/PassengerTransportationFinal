package com.example.backendpassengertransportation.controller;

import com.example.backendpassengertransportation.model.TransportType;
import com.example.backendpassengertransportation.service.TransportTypeService;
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

// Контроллер для управления типами транспорта, предоставляет CRUD-операции
@RestController
@RequestMapping("/transport-types")
public class TransportTypeController {

    @Autowired
    // Сервис для работы с типами транспорта
    private TransportTypeService transportTypeService;

    // Получение всех типов транспорта
    @Operation(
            summary = "Получение списка всех типов транспорта",
            description = "Возвращает список всех типов транспорта, доступных в системе. " +
                    "Для выполнения запроса необходимо авторизоваться: в Swagger UI нажмите кнопку 'Authorize' в верхней части страницы и введите валидный JWT-токен. " +
                    "Если типы транспорта отсутствуют, возвращается статус 404 с сообщением об ошибке. " +
                    "При отсутствии доступа возвращается статус 403. " +
                    "При внутренней ошибке сервера возвращается статус 500 с пустым списком."
    )
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "Список типов транспорта успешно получен."),
                    @ApiResponse(responseCode = "401", description = "Недействительный или отсутствующий токен."),
                    @ApiResponse(responseCode = "403", description = "Нет доступа к выполнению операции."),
                    @ApiResponse(responseCode = "404", description = "Типы транспорта не найдены."),
                    @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера.")
            }
    )
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("")
    public ResponseEntity<?> getAllTransportTypes() {
        try {
            // Получение списка всех типов транспорта через сервис
            List<TransportType> transportTypes = transportTypeService.getAllTransportTypes();
            if (transportTypes.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Типы транспорта не найдены.");
            }
            return ResponseEntity.ok(transportTypes);
        } catch (Exception e) {
            // Обработка внутренней ошибки сервера
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.emptyList());
        }
    }

    // Получение типа транспорта по идентификатору
    @Operation(
            summary = "Получение типа транспорта по ID",
            description = "Возвращает тип транспорта по указанному идентификатору. " +
                    "Для выполнения запроса необходимо авторизоваться: в Swagger UI нажмите кнопку 'Authorize' в верхней части страницы и введите валидный JWT-токен. " +
                    "Если тип транспорта не найден, возвращается статус 404 с сообщением об ошибке. " +
                    "При отсутствии доступа возвращается статус 403. " +
                    "При внутренней ошибке сервера возвращается статус 500 с null."
    )
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "Тип транспорта успешно получен."),
                    @ApiResponse(responseCode = "401", description = "Недействительный или отсутствующий токен."),
                    @ApiResponse(responseCode = "403", description = "Нет доступа к выполнению операции."),
                    @ApiResponse(responseCode = "404", description = "Тип транспорта не найден."),
                    @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера.")
            }
    )
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/{id}")
    public ResponseEntity<?> getTransportTypeById(
            @Parameter(description = "Идентификатор типа транспорта", required = true)
            @PathVariable String id) {
        try {
            // Поиск типа транспорта по идентификатору через сервис
            TransportType transportType = transportTypeService.getTransportTypeById(id);
            if (transportType == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Тип транспорта с ID " + id + " не найден.");
            }
            return ResponseEntity.ok(transportType);
        } catch (Exception e) {
            // Обработка внутренней ошибки сервера
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    // Создание нового типа транспорта
    @Operation(
            summary = "Создание нового типа транспорта",
            description = "Создает новый тип транспорта с указанным названием. " +
                    "Для выполнения запроса необходимо авторизоваться: в Swagger UI нажмите кнопку 'Authorize' в верхней части страницы и введите валидный JWT-токен. " +
                    "При успешном создании возвращается статус 201 и созданный тип транспорта. " +
                    "Если переданы некорректные параметры, возвращается статус 400 с сообщением об ошибке. " +
                    "При отсутствии доступа возвращается статус 403. " +
                    "При внутренней ошибке сервера возвращается статус 500."
    )
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "201", description = "Тип транспорта успешно создан."),
                    @ApiResponse(responseCode = "401", description = "Недействительный или отсутствующий токен."),
                    @ApiResponse(responseCode = "403", description = "Нет доступа к выполнению операции."),
                    @ApiResponse(responseCode = "400", description = "Некорректные параметры."),
                    @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера.")
            }
    )
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("")
    public ResponseEntity<?> createTransportType(
            @Parameter(description = "Название нового типа транспорта", required = true)
            @RequestParam String transportType) {
        try {
            // Создание нового типа транспорта через сервис
            TransportType newTransportType = transportTypeService.createTransportType(transportType);
            return ResponseEntity.status(HttpStatus.CREATED).body(newTransportType);
        } catch (IllegalArgumentException e) {
            // Обработка ошибки некорректных параметров
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            // Обработка внутренней ошибки сервера
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Произошла ошибка при создании типа транспорта: " + e.getMessage());
        }
    }

    // Обновление типа транспорта по идентификатору
    @Operation(
            summary = "Обновление типа транспорта по ID",
            description = "Обновляет тип транспорта с указанным идентификатором. " +
                    "Для выполнения запроса необходимо авторизоваться: в Swagger UI нажмите кнопку 'Authorize' в верхней части страницы и введите валидный JWT-токен. " +
                    "При успешном обновлении возвращается статус 200 и обновленный тип транспорта. " +
                    "Если тип транспорта не найден, возвращается статус 404 с сообщением об ошибке. " +
                    "При отсутствии доступа возвращается статус 403. " +
                    "При некорректных параметрах возвращается статус 400 с сообщением об ошибке. " +
                    "При внутренней ошибке сервера возвращается статус 500."
    )
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "Тип транспорта успешно обновлен."),
                    @ApiResponse(responseCode = "401", description = "Недействительный или отсутствующий токен."),
                    @ApiResponse(responseCode = "403", description = "Нет доступа к выполнению операции."),
                    @ApiResponse(responseCode = "400", description = "Некорректные параметры."),
                    @ApiResponse(responseCode = "404", description = "Тип транспорта не найден."),
                    @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера.")
            }
    )
    @SecurityRequirement(name = "bearerAuth")
    @PutMapping("/{id}")
    public ResponseEntity<?> updateTransportType(
            @Parameter(description = "Идентификатор типа транспорта для обновления", required = true)
            @PathVariable String id,
            @Parameter(description = "Новое название типа транспорта", required = true)
            @RequestParam String transportType) {
        try {
            // Обновление типа транспорта через сервис
            TransportType updatedTransportType = transportTypeService.updateTransportType(id, transportType);
            return ResponseEntity.ok(updatedTransportType);
        } catch (IllegalArgumentException e) {
            // Обработка ошибки некорректных параметров
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (NoSuchElementException e) {
            // Обработка ошибки отсутствия типа транспорта
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            // Обработка внутренней ошибки сервера
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Произошла ошибка при обновлении типа транспорта: " + e.getMessage());
        }
    }

    // Удаление типа транспорта по идентификатору
    @Operation(
            summary = "Удаление типа транспорта по ID",
            description = "Удаляет тип транспорта по указанному идентификатору. " +
                    "Для выполнения запроса необходимо авторизоваться: в Swagger UI нажмите кнопку 'Authorize' в верхней части страницы и введите валидный JWT-токен. " +
                    "При успешном удалении возвращается статус 200 с сообщением об успехе. " +
                    "Если тип транспорта не найден, возвращается статус 404 с сообщением об ошибке. " +
                    "При отсутствии доступа возвращается статус 403. " +
                    "При внутренней ошибке сервера возвращается статус 500."
    )
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "Тип транспорта успешно удален."),
                    @ApiResponse(responseCode = "401", description = "Недействительный или отсутствующий токен."),
                    @ApiResponse(responseCode = "403", description = "Нет доступа к выполнению операции."),
                    @ApiResponse(responseCode = "404", description = "Тип транспорта не найден."),
                    @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера.")
            }
    )
    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTransportType(
            @Parameter(description = "Идентификатор типа транспорта для удаления", required = true)
            @PathVariable String id) {
        try {
            // Удаление типа транспорта через сервис
            transportTypeService.deleteTransportType(id);
            return ResponseEntity.ok("Тип транспорта успешно удален.");
        } catch (IllegalArgumentException e) {
            // Обработка ошибки отсутствия типа транспорта
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            // Обработка внутренней ошибки сервера
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Произошла ошибка при удалении типа транспорта: " + e.getMessage());
        }
    }
}