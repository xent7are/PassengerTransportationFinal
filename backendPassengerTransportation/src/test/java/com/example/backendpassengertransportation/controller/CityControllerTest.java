package com.example.backendpassengertransportation.controller;

import com.example.backendpassengertransportation.model.City;
import com.example.backendpassengertransportation.service.CityService;
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
class CityControllerTest {

    @Mock
    private CityService cityService;

    @InjectMocks
    private CityController cityController;

    // Вспомогательный метод для создания тестового города
    private City createTestCity(String id, String cityName) {
        City city = new City();
        city.setIdCity(id);
        city.setCityName(cityName);
        return city;
    }

    /**
     * Тест получения всех городов.
     * Проверка корректности возвращаемого списка городов.
     */
    @Test
    void testGetAllCities_Success() {
        // Создание тестовых данных: два города
        City city1 = createTestCity("c1", "Москва");
        City city2 = createTestCity("c2", "Санкт-Петербург");
        List<City> cities = Arrays.asList(city1, city2);

        // Мокирование сервиса
        when(cityService.getAllCities()).thenReturn(cities);

        // Вызов метода контроллера
        ResponseEntity<?> response = cityController.getAllCities();

        // Проверка статуса ответа
        assertEquals(HttpStatus.OK.value(), response.getStatusCodeValue());
        // Проверка тела ответа
        List<City> result = (List<City>) response.getBody();
        assertEquals(2, result.size());

        // Проверка полей первого города
        City returnedCity1 = result.get(0);
        assertEquals("c1", returnedCity1.getIdCity());
        assertEquals("Москва", returnedCity1.getCityName());

        // Проверка полей второго города
        City returnedCity2 = result.get(1);
        assertEquals("c2", returnedCity2.getIdCity());
        assertEquals("Санкт-Петербург", returnedCity2.getCityName());
    }

    /**
     * Тест получения всех городов, когда города не найдены.
     * Проверка возврата статуса 404 и сообщения об ошибке.
     */
    @Test
    void testGetAllCities_NotFound() {
        // Мокирование сервиса: возвращается пустой список
        when(cityService.getAllCities()).thenReturn(Collections.emptyList());

        // Вызов метода контроллера
        ResponseEntity<?> response = cityController.getAllCities();

        // Проверка статуса ответа
        assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatusCodeValue());
        // Проверка тела ответа
        assertEquals("Города не найдены.", response.getBody());
    }

    /**
     * Тест получения всех городов при внутренней ошибке сервера.
     * Проверка возврата статуса 500 и пустого списка.
     */
    @Test
    void testGetAllCities_InternalServerError() {
        // Мокирование сервиса: выброс исключения
        when(cityService.getAllCities()).thenThrow(new RuntimeException("Внутренняя ошибка"));

        // Вызов метода контроллера
        ResponseEntity<?> response = cityController.getAllCities();

        // Проверка статуса ответа
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), response.getStatusCodeValue());
        // Проверка тела ответа
        assertEquals(Collections.emptyList(), response.getBody());
    }

    /**
     * Тест получения города по ID.
     * Проверка корректности возвращаемого города.
     */
    @Test
    void testGetCityById_Success() {
        // Создание тестовых данных
        City city = createTestCity("c1", "Москва");

        // Мокирование сервиса
        when(cityService.getCityById("c1")).thenReturn(city);

        // Вызов метода контроллера
        ResponseEntity<?> response = cityController.getCityById("c1");

        // Проверка статуса ответа
        assertEquals(HttpStatus.OK.value(), response.getStatusCodeValue());
        // Проверка тела ответа
        City result = (City) response.getBody();
        assertEquals("c1", result.getIdCity());
        assertEquals("Москва", result.getCityName());
    }

    /**
     * Тест получения города по ID, когда город не найден.
     * Проверка возврата статуса 404 и сообщения об ошибке.
     */
    @Test
    void testGetCityById_NotFound() {
        // Мокирование сервиса: возвращается null
        when(cityService.getCityById("c999")).thenReturn(null);

        // Вызов метода контроллера
        ResponseEntity<?> response = cityController.getCityById("c999");

        // Проверка статуса ответа
        assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatusCodeValue());
        // Проверка тела ответа
        assertEquals("Город с ID c999 не найден.", response.getBody());
    }

    /**
     * Тест получения города по ID при внутренней ошибке сервера.
     * Проверка возврата статуса 500 и null.
     */
    @Test
    void testGetCityById_InternalServerError() {
        // Мокирование сервиса: выброс исключения
        when(cityService.getCityById("c1")).thenThrow(new RuntimeException("Внутренняя ошибка"));

        // Вызов метода контроллера
        ResponseEntity<?> response = cityController.getCityById("c1");

        // Проверка статуса ответа
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), response.getStatusCodeValue());
        // Проверка тела ответа
        assertNull(response.getBody());
    }

    /**
     * Тест создания нового города.
     * Проверка корректности создания и возврата статуса 201.
     */
    @Test
    void testCreateCity_Success() {
        // Создание тестовых данных
        City city = createTestCity("c1", "Москва");

        // Мокирование сервиса
        when(cityService.createCity("Москва")).thenReturn(city);

        // Вызов метода контроллера
        ResponseEntity<?> response = cityController.createCity("Москва");

        // Проверка статуса ответа
        assertEquals(HttpStatus.CREATED.value(), response.getStatusCodeValue());
        // Проверка тела ответа
        City result = (City) response.getBody();
        assertEquals("c1", result.getIdCity());
        assertEquals("Москва", result.getCityName());
    }

    /**
     * Тест создания города с некорректным названием.
     * Проверка возврата статуса 400 и сообщения об ошибке.
     */
    @Test
    void testCreateCity_InvalidCityName() {
        // Мокирование сервиса: выброс исключения IllegalArgumentException
        when(cityService.createCity("")).thenThrow(new IllegalArgumentException("Название города не может быть пустым."));

        // Вызов метода контроллера
        ResponseEntity<?> response = cityController.createCity("");

        // Проверка статуса ответа
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatusCodeValue());
        // Проверка тела ответа
        assertEquals("Название города не может быть пустым.", response.getBody());
    }

    /**
     * Тест создания города при внутренней ошибке сервера.
     * Проверка возврата статуса 500 и сообщения об ошибке.
     */
    @Test
    void testCreateCity_InternalServerError() {
        // Мокирование сервиса: выброс исключения
        when(cityService.createCity("Москва")).thenThrow(new RuntimeException("Внутренняя ошибка"));

        // Вызов метода контроллера
        ResponseEntity<?> response = cityController.createCity("Москва");

        // Проверка статуса ответа
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), response.getStatusCodeValue());
        // Проверка тела ответа
        assertEquals("Произошла ошибка при создании города: Внутренняя ошибка", response.getBody());
    }

    /**
     * Тест обновления города.
     * Проверка корректности обновления и возврата статуса 200.
     */
    @Test
    void testUpdateCity_Success() {
        // Создание тестовых данных
        City city = createTestCity("c1", "Москва");

        // Мокирование сервиса
        when(cityService.updateCity("c1", "Москва")).thenReturn(city);

        // Вызов метода контроллера
        ResponseEntity<?> response = cityController.updateCity("c1", "Москва");

        // Проверка статуса ответа
        assertEquals(HttpStatus.OK.value(), response.getStatusCodeValue());
        // Проверка тела ответа
        City result = (City) response.getBody();
        assertEquals("c1", result.getIdCity());
        assertEquals("Москва", result.getCityName());
    }

    /**
     * Тест обновления города с некорректным названием.
     * Проверка возврата статуса 400 и сообщения об ошибке.
     */
    @Test
    void testUpdateCity_InvalidCityName() {
        // Мокирование сервиса: выброс исключения IllegalArgumentException
        when(cityService.updateCity("c1", "")).thenThrow(new IllegalArgumentException("Название города не может быть пустым."));

        // Вызов метода контроллера
        ResponseEntity<?> response = cityController.updateCity("c1", "");

        // Проверка статуса ответа
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatusCodeValue());
        // Проверка тела ответа
        assertEquals("Название города не может быть пустым.", response.getBody());
    }

    /**
     * Тест обновления города, когда город не найден.
     * Проверка возврата статуса 404 и сообщения об ошибке.
     */
    @Test
    void testUpdateCity_NotFound() {
        // Мокирование сервиса: выброс исключения NoSuchElementException
        when(cityService.updateCity("c999", "Москва")).thenThrow(new NoSuchElementException("Город с ID c999 не найден."));

        // Вызов метода контроллера
        ResponseEntity<?> response = cityController.updateCity("c999", "Москва");

        // Проверка статуса ответа
        assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatusCodeValue());
        // Проверка тела ответа
        assertEquals("Город с ID c999 не найден.", response.getBody());
    }

    /**
     * Тест обновления города при внутренней ошибке сервера.
     * Проверка возврата статуса 500 и сообщения об ошибке.
     */
    @Test
    void testUpdateCity_InternalServerError() {
        // Мокирование сервиса: выброс исключения
        when(cityService.updateCity("c1", "Москва")).thenThrow(new RuntimeException("Внутренняя ошибка"));

        // Вызов метода контроллера
        ResponseEntity<?> response = cityController.updateCity("c1", "Москва");

        // Проверка статуса ответа
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), response.getStatusCodeValue());
        // Проверка тела ответа
        assertEquals("Произошла ошибка при обновлении города: Внутренняя ошибка", response.getBody());
    }

    /**
     * Тест удаления города по ID.
     * Проверка корректности удаления и возврата статуса 200.
     */
    @Test
    void testDeleteCity_Success() {
        // Мокирование сервиса: метод выполняется без исключений
        doNothing().when(cityService).deleteCity("c1");

        // Вызов метода контроллера
        ResponseEntity<?> response = cityController.deleteCity("c1");

        // Проверка статуса ответа
        assertEquals(HttpStatus.OK.value(), response.getStatusCodeValue());
        // Проверка тела ответа
        assertEquals("Город успешно удален.", response.getBody());
    }

    /**
     * Тест удаления города по ID, когда город не найден.
     * Проверка возврата статуса 404 и сообщения об ошибке.
     */
    @Test
    void testDeleteCity_NotFound() {
        // Мокирование сервиса: выброс исключения IllegalArgumentException
        doThrow(new IllegalArgumentException("Город с ID c999 не найден.")).when(cityService).deleteCity("c999");

        // Вызов метода контроллера
        ResponseEntity<?> response = cityController.deleteCity("c999");

        // Проверка статуса ответа
        assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatusCodeValue());
        // Проверка тела ответа
        assertEquals("Город с ID c999 не найден.", response.getBody());
    }

    /**
     * Тест удаления города при внутренней ошибке сервера.
     * Проверка возврата статуса 500 и сообщения об ошибке.
     */
    @Test
    void testDeleteCity_InternalServerError() {
        // Мокирование сервиса: выброс исключения
        doThrow(new RuntimeException("Внутренняя ошибка")).when(cityService).deleteCity("c1");

        // Вызов метода контроллера
        ResponseEntity<?> response = cityController.deleteCity("c1");

        // Проверка статуса ответа
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), response.getStatusCodeValue());
        // Проверка тела ответа
        assertEquals("Произошла ошибка при удалении города: Внутренняя ошибка", response.getBody());
    }
}