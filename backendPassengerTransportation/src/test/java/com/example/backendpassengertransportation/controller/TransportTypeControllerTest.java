package com.example.backendpassengertransportation.controller;

import com.example.backendpassengertransportation.model.TransportType;
import com.example.backendpassengertransportation.service.TransportTypeService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransportTypeControllerTest {

    @Mock
    private TransportTypeService transportTypeService;

    @InjectMocks
    private TransportTypeController transportTypeController;

    // Вспомогательный метод для создания тестового типа транспорта
    private TransportType createTestTransportType(String id, String transportType) {
        TransportType type = new TransportType();
        type.setIdTransportType(id);
        type.setTransportType(transportType);
        return type;
    }

    /**
     * Тест получения всех типов транспорта.
     * Проверка корректности возвращаемого списка типов транспорта.
     */
    @Test
    void testGetAllTransportTypes_Success() {
        // Создание тестовых данных: два типа транспорта
        TransportType type1 = createTestTransportType("t1", "Автобус");
        TransportType type2 = createTestTransportType("t2", "Поезд");
        List<TransportType> transportTypes = Arrays.asList(type1, type2);

        // Мокирование сервиса
        when(transportTypeService.getAllTransportTypes()).thenReturn(transportTypes);

        // Вызов метода контроллера
        ResponseEntity<?> response = transportTypeController.getAllTransportTypes();

        // Проверка статуса ответа
        assertEquals(HttpStatus.OK.value(), response.getStatusCodeValue());
        // Проверка тела ответа
        List<TransportType> result = (List<TransportType>) response.getBody();
        assertEquals(2, result.size());

        // Проверка полей первого типа транспорта
        TransportType returnedType1 = result.get(0);
        assertEquals("t1", returnedType1.getIdTransportType());
        assertEquals("Автобус", returnedType1.getTransportType());

        // Проверка полей второго типа транспорта
        TransportType returnedType2 = result.get(1);
        assertEquals("t2", returnedType2.getIdTransportType());
        assertEquals("Поезд", returnedType2.getTransportType());
    }

    /**
     * Тест получения всех типов транспорта, когда типы не найдены.
     * Проверка возврата статуса 404 и сообщения об ошибке.
     */
    @Test
    void testGetAllTransportTypes_NotFound() {
        // Мокирование сервиса: возвращается пустой список
        when(transportTypeService.getAllTransportTypes()).thenReturn(Collections.emptyList());

        // Вызов метода контроллера
        ResponseEntity<?> response = transportTypeController.getAllTransportTypes();

        // Проверка статуса ответа
        assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatusCodeValue());
        // Проверка тела ответа
        assertEquals("Типы транспорта не найдены.", response.getBody());
    }

    /**
     * Тест получения всех типов транспорта при внутренней ошибке сервера.
     * Проверка возврата статуса 500 и пустого списка.
     */
    @Test
    void testGetAllTransportTypes_InternalServerError() {
        // Мокирование сервиса: выброс исключения
        when(transportTypeService.getAllTransportTypes()).thenThrow(new RuntimeException("Внутренняя ошибка"));

        // Вызов метода контроллера
        ResponseEntity<?> response = transportTypeController.getAllTransportTypes();

        // Проверка статуса ответа
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), response.getStatusCodeValue());
        // Проверка тела ответа
        assertEquals(Collections.emptyList(), response.getBody());
    }

    /**
     * Тест получения типа транспорта по ID.
     * Проверка корректности возвращаемого типа транспорта.
     */
    @Test
    void testGetTransportTypeById_Success() {
        // Создание тестовых данных
        TransportType transportType = createTestTransportType("t1", "Автобус");

        // Мокирование сервиса
        when(transportTypeService.getTransportTypeById("t1")).thenReturn(transportType);

        // Вызов метода контроллера
        ResponseEntity<?> response = transportTypeController.getTransportTypeById("t1");

        // Проверка статуса ответа
        assertEquals(HttpStatus.OK.value(), response.getStatusCodeValue());
        // Проверка тела ответа
        TransportType result = (TransportType) response.getBody();
        assertEquals("t1", result.getIdTransportType());
        assertEquals("Автобус", result.getTransportType());
    }

    /**
     * Тест получения типа транспорта по ID, когда тип не найден.
     * Проверка возврата статуса 404 и сообщения об ошибке.
     */
    @Test
    void testGetTransportTypeById_NotFound() {
        // Мокирование сервиса: возвращается null
        when(transportTypeService.getTransportTypeById("t999")).thenReturn(null);

        // Вызов метода контроллера
        ResponseEntity<?> response = transportTypeController.getTransportTypeById("t999");

        // Проверка статуса ответа
        assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatusCodeValue());
        // Проверка тела ответа
        assertEquals("Тип транспорта с ID t999 не найден.", response.getBody());
    }

    /**
     * Тест получения типа транспорта по ID при внутренней ошибке сервера.
     * Проверка возврата статуса 500 и null.
     */
    @Test
    void testGetTransportTypeById_InternalServerError() {
        // Мокирование сервиса: выброс исключения
        when(transportTypeService.getTransportTypeById("t1")).thenThrow(new RuntimeException("Внутренняя ошибка"));

        // Вызов метода контроллера
        ResponseEntity<?> response = transportTypeController.getTransportTypeById("t1");

        // Проверка статуса ответа
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), response.getStatusCodeValue());
        // Проверка тела ответа
        assertNull(response.getBody());
    }

    /**
     * Тест создания нового типа транспорта.
     * Проверка корректности создания и возврата статуса 201.
     */
    @Test
    void testCreateTransportType_Success() {
        // Создание тестовых данных
        TransportType transportType = createTestTransportType("t1", "Автобус");

        // Мокирование сервиса
        when(transportTypeService.createTransportType("Автобус")).thenReturn(transportType);

        // Вызов метода контроллера
        ResponseEntity<?> response = transportTypeController.createTransportType("Автобус");

        // Проверка статуса ответа
        assertEquals(HttpStatus.CREATED.value(), response.getStatusCodeValue());
        // Проверка тела ответа
        TransportType result = (TransportType) response.getBody();
        assertEquals("t1", result.getIdTransportType());
        assertEquals("Автобус", result.getTransportType());
    }

    /**
     * Тест создания типа транспорта с некорректным названием.
     * Проверка возврата статуса 400 и сообщения об ошибке.
     */
    @Test
    void testCreateTransportType_InvalidTransportType() {
        // Мокирование сервиса: выброс исключения IllegalArgumentException
        when(transportTypeService.createTransportType("")).thenThrow(new IllegalArgumentException("Название типа транспорта не может быть пустым."));

        // Вызов метода контроллера
        ResponseEntity<?> response = transportTypeController.createTransportType("");

        // Проверка статуса ответа
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatusCodeValue());
        // Проверка тела ответа
        assertEquals("Название типа транспорта не может быть пустым.", response.getBody());
    }

    /**
     * Тест создания типа транспорта при внутренней ошибке сервера.
     * Проверка возврата статуса 500 и сообщения об ошибке.
     */
    @Test
    void testCreateTransportType_InternalServerError() {
        // Мокирование сервиса: выброс исключения
        when(transportTypeService.createTransportType("Автобус")).thenThrow(new RuntimeException("Внутренняя ошибка"));

        // Вызов метода контроллера
        ResponseEntity<?> response = transportTypeController.createTransportType("Автобус");

        // Проверка статуса ответа
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), response.getStatusCodeValue());
        // Проверка тела ответа
        assertEquals("Произошла ошибка при создании типа транспорта: Внутренняя ошибка", response.getBody());
    }

    /**
     * Тест обновления типа транспорта.
     * Проверка корректности обновления и возврата статуса 200.
     */
    @Test
    void testUpdateTransportType_Success() {
        // Создание тестовых данных
        TransportType transportType = createTestTransportType("t1", "Автобус");

        // Мокирование сервиса
        when(transportTypeService.updateTransportType("t1", "Автобус")).thenReturn(transportType);

        // Вызов метода контроллера
        ResponseEntity<?> response = transportTypeController.updateTransportType("t1", "Автобус");

        // Проверка статуса ответа
        assertEquals(HttpStatus.OK.value(), response.getStatusCodeValue());
        // Проверка тела ответа
        TransportType result = (TransportType) response.getBody();
        assertEquals("t1", result.getIdTransportType());
        assertEquals("Автобус", result.getTransportType());
    }

    /**
     * Тест обновления типа транспорта с некорректным названием.
     * Проверка возврата статуса 400 и сообщения об ошибке.
     */
    @Test
    void testUpdateTransportType_InvalidTransportType() {
        // Мокирование сервиса: выброс исключения IllegalArgumentException
        when(transportTypeService.updateTransportType("t1", "")).thenThrow(new IllegalArgumentException("Название типа транспорта не может быть пустым."));

        // Вызов метода контроллера
        ResponseEntity<?> response = transportTypeController.updateTransportType("t1", "");

        // Проверка статуса ответа
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatusCodeValue());
        // Проверка тела ответа
        assertEquals("Название типа транспорта не может быть пустым.", response.getBody());
    }

    /**
     * Тест обновления типа транспорта, когда тип не найден.
     * Проверка возврата статуса 404 и сообщения об ошибке.
     */
    @Test
    void testUpdateTransportType_NotFound() {
        // Мокирование сервиса: выброс исключения NoSuchElementException
        when(transportTypeService.updateTransportType("t999", "Автобус")).thenThrow(new NoSuchElementException("Тип транспорта с ID t999 не найден."));

        // Вызов метода контроллера
        ResponseEntity<?> response = transportTypeController.updateTransportType("t999", "Автобус");

        // Проверка статуса ответа
        assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatusCodeValue());
        // Проверка тела ответа
        assertEquals("Тип транспорта с ID t999 не найден.", response.getBody());
    }

    /**
     * Тест обновления типа транспорта при внутренней ошибке сервера.
     * Проверка возврата статуса 500 и сообщения об ошибке.
     */
    @Test
    void testUpdateTransportType_InternalServerError() {
        // Мокирование сервиса: выброс исключения
        when(transportTypeService.updateTransportType("t1", "Автобус")).thenThrow(new RuntimeException("Внутренняя ошибка"));

        // Вызов метода контроллера
        ResponseEntity<?> response = transportTypeController.updateTransportType("t1", "Автобус");

        // Проверка статуса ответа
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), response.getStatusCodeValue());
        // Проверка тела ответа
        assertEquals("Произошла ошибка при обновлении типа транспорта: Внутренняя ошибка", response.getBody());
    }

    /**
     * Тест удаления типа транспорта по ID.
     * Проверка корректности удаления и возврата статуса 200.
     */
    @Test
    void testDeleteTransportType_Success() {
        // Мокирование сервиса: метод выполняется без исключений
        doNothing().when(transportTypeService).deleteTransportType("t1");

        // Вызов метода контроллера
        ResponseEntity<?> response = transportTypeController.deleteTransportType("t1");

        // Проверка статуса ответа
        assertEquals(HttpStatus.OK.value(), response.getStatusCodeValue());
        // Проверка тела ответа
        assertEquals("Тип транспорта успешно удален.", response.getBody());
    }

    /**
     * Тест удаления типа транспорта по ID, когда тип не найден.
     * Проверка возврата статуса 404 и сообщения об ошибке.
     */
    @Test
    void testDeleteTransportType_NotFound() {
        // Мокирование сервиса: выброс исключения IllegalArgumentException
        doThrow(new IllegalArgumentException("Тип транспорта с ID t999 не найден.")).when(transportTypeService).deleteTransportType("t999");

        // Вызов метода контроллера
        ResponseEntity<?> response = transportTypeController.deleteTransportType("t999");

        // Проверка статуса ответа
        assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatusCodeValue());
        // Проверка тела ответа
        assertEquals("Тип транспорта с ID t999 не найден.", response.getBody());
    }

    /**
     * Тест удаления типа транспорта при внутренней ошибке сервера.
     * Проверка возврата статуса 500 и сообщения об ошибке.
     */
    @Test
    void testDeleteTransportType_InternalServerError() {
        // Мокирование сервиса: выброс исключения
        doThrow(new RuntimeException("Внутренняя ошибка")).when(transportTypeService).deleteTransportType("t1");

        // Вызов метода контроллера
        ResponseEntity<?> response = transportTypeController.deleteTransportType("t1");

        // Проверка статуса ответа
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), response.getStatusCodeValue());
        // Проверка тела ответа
        assertEquals("Произошла ошибка при удалении типа транспорта: Внутренняя ошибка", response.getBody());
    }
}