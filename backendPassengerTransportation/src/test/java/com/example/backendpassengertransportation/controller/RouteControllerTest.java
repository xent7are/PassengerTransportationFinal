package com.example.backendpassengertransportation.controller;

import com.example.backendpassengertransportation.model.City;
import com.example.backendpassengertransportation.model.Route;
import com.example.backendpassengertransportation.model.TransportType;
import com.example.backendpassengertransportation.service.RouteService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RouteControllerTest {

    @Mock
    private RouteService routeService;

    @InjectMocks
    private RouteController routeController;

    // Вспомогательный метод для создания тестового маршрута
    private Route createTestRoute(String id, String transportTypeName, String departureCityName, String destinationCityName,
                                  Timestamp departureTime, Timestamp arrivalTime, int totalSeats, int availableSeats) {
        Route route = new Route();
        route.setIdRoute(id);
        TransportType transportType = new TransportType();
        transportType.setTransportType(transportTypeName);
        route.setTransportType(transportType);
        City departureCity = new City();
        departureCity.setCityName(departureCityName);
        route.setDepartureCity(departureCity);
        City destinationCity = new City();
        destinationCity.setCityName(destinationCityName);
        route.setDestinationCity(destinationCity);
        route.setDepartureTime(departureTime);
        route.setArrivalTime(arrivalTime);
        route.setTotalNumberSeats(totalSeats);
        route.setNumberAvailableSeats(availableSeats);
        return route;
    }

    /**
     * Тест получения всех маршрутов без пагинации.
     * Проверка корректности возвращаемого списка маршрутов.
     */
    @Test
    void testGetAllRoutes_Success() {
        // Создание тестовых данных: два маршрута
        Route route1 = createTestRoute("r1", "Автобус", "Москва", "Санкт-Петербург", Timestamp.valueOf("2025-03-14 10:00:00"), Timestamp.valueOf("2025-03-14 18:00:00"), 50, 50);
        Route route2 = createTestRoute("r2", "Поезд", "Казань", "Екатеринбург", Timestamp.valueOf("2025-03-15 12:00:00"), Timestamp.valueOf("2025-03-16 08:00:00"), 100, 100);
        List<Route> routes = Arrays.asList(route1, route2);

        // Мокирование сервиса: при вызове метода getAllRoutes() возвращается тестовый список маршрутов
        when(routeService.getAllRoutes()).thenReturn(routes);

        // Вызов метода контроллера
        ResponseEntity<?> response = routeController.getAllRoutes();

        // Проверка, что статус ответа должен быть 200 (OK)
        assertEquals(HttpStatus.OK.value(), response.getStatusCodeValue());
        // Проверка, что тело ответа содержит список маршрутов
        List<Route> result = (List<Route>) response.getBody();
        assertEquals(2, result.size());

        // Проверка, что поля возвращаемых маршрутов совпадают с ожидаемыми значениями
        Route returnedRoute1 = result.get(0);
        assertEquals("r1", returnedRoute1.getIdRoute());
        assertEquals("Автобус", returnedRoute1.getTransportType().getTransportType());
        assertEquals("Москва", returnedRoute1.getDepartureCity().getCityName());
        assertEquals("Санкт-Петербург", returnedRoute1.getDestinationCity().getCityName());
        assertEquals(Timestamp.valueOf("2025-03-14 10:00:00"), returnedRoute1.getDepartureTime());
        assertEquals(Timestamp.valueOf("2025-03-14 18:00:00"), returnedRoute1.getArrivalTime());
        assertEquals(50, returnedRoute1.getTotalNumberSeats());
        assertEquals(50, returnedRoute1.getNumberAvailableSeats());

        Route returnedRoute2 = result.get(1);
        assertEquals("r2", returnedRoute2.getIdRoute());
        assertEquals("Поезд", returnedRoute2.getTransportType().getTransportType());
        assertEquals("Казань", returnedRoute2.getDepartureCity().getCityName());
        assertEquals("Екатеринбург", returnedRoute2.getDestinationCity().getCityName());
        assertEquals(Timestamp.valueOf("2025-03-15 12:00:00"), returnedRoute2.getDepartureTime());
        assertEquals(Timestamp.valueOf("2025-03-16 08:00:00"), returnedRoute2.getArrivalTime());
        assertEquals(100, returnedRoute2.getTotalNumberSeats());
        assertEquals(100, returnedRoute2.getNumberAvailableSeats());
    }

    /**
     * Тест получения всех маршрутов без пагинации, когда маршруты не найдены.
     * Проверка возврата статуса 404 и сообщения об ошибке.
     */
    @Test
    void testGetAllRoutes_NotFound() {
        when(routeService.getAllRoutes()).thenThrow(new NoSuchElementException("Маршруты не найдены."));
        ResponseEntity<?> response = routeController.getAllRoutes();
        assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatusCodeValue());
        assertEquals("Маршруты не найдены.", response.getBody());
    }

    /**
     * Тест получения всех маршрутов без пагинации при внутренней ошибке сервера.
     * Проверка возврата статуса 500 и пустого списка.
     */
    @Test
    void testGetAllRoutes_InternalServerError() {
        when(routeService.getAllRoutes()).thenThrow(new RuntimeException("Внутренняя ошибка"));
        ResponseEntity<?> response = routeController.getAllRoutes();
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), response.getStatusCodeValue());
        assertEquals(Collections.emptyList(), response.getBody());
    }

    /**
     * Тест получения всех маршрутов с пагинацией без фильтра по времени.
     * Проверка корректности возвращаемой страницы маршрутов.
     */
    @Test
    void testGetAllRoutesWithPagination_Success_NoFilter() {
        // Создание тестовых данных: страница с одним маршрутом
        Route route = createTestRoute("r1", "Автобус", "Москва", "Санкт-Петербург", Timestamp.valueOf("2025-03-14 10:00:00"), Timestamp.valueOf("2025-03-14 18:00:00"), 50, 50);
        Page<Route> page = new PageImpl<>(Collections.singletonList(route));

        // Мокирование сервиса: при вызове метода getAllRoutesWithPagination() возвращается тестовая страница
        when(routeService.getAllRoutesWithPagination(0, 16)).thenReturn(page);

        // Вызов метода контроллера
        ResponseEntity<?> response = routeController.getAllRoutesWithPagination(0, 16, null);

        // Проверка, что статус ответа должен быть 200 (OK)
        assertEquals(HttpStatus.OK.value(), response.getStatusCodeValue());
        // Проверка, что тело ответа содержит страницу с маршрутом
        Page<Route> result = (Page<Route>) response.getBody();
        assertEquals(1, result.getContent().size());

        // Проверка, что поля возвращаемого маршрута совпадают с ожидаемыми значениями
        Route returnedRoute = result.getContent().get(0);
        assertEquals("r1", returnedRoute.getIdRoute());
        assertEquals("Автобус", returnedRoute.getTransportType().getTransportType());
        assertEquals("Москва", returnedRoute.getDepartureCity().getCityName());
        assertEquals("Санкт-Петербург", returnedRoute.getDestinationCity().getCityName());
        assertEquals(Timestamp.valueOf("2025-03-14 10:00:00"), returnedRoute.getDepartureTime());
        assertEquals(Timestamp.valueOf("2025-03-14 18:00:00"), returnedRoute.getArrivalTime());
        assertEquals(50, returnedRoute.getTotalNumberSeats());
        assertEquals(50, returnedRoute.getNumberAvailableSeats());
    }

    /**
     * Тест получения всех маршрутов с пагинацией с фильтром по минимальному времени.
     * Проверка корректности возвращаемой страницы маршрутов.
     */
    @Test
    void testGetAllRoutesWithPagination_Success_WithFilter() {
        // Создание тестовых данных: страница с одним маршрутом
        Route route = createTestRoute("r1", "Автобус", "Москва", "Санкт-Петербург", Timestamp.valueOf("2025-03-14 10:00:00"), Timestamp.valueOf("2025-03-14 18:00:00"), 50, 50);
        Page<Route> page = new PageImpl<>(Collections.singletonList(route));

        // Мокирование сервиса: при вызове метода с фильтром возвращается тестовая страница
        when(routeService.getAllRoutesWithPagination(0, 16, Timestamp.valueOf("2025-03-14 00:00:00"))).thenReturn(page);

        // Вызов метода контроллера с фильтром по времени
        ResponseEntity<?> response = routeController.getAllRoutesWithPagination(0, 16, "2025-03-14T00:00:00");

        // Проверка, что статус ответа должен быть 200 (OK)
        assertEquals(HttpStatus.OK.value(), response.getStatusCodeValue());
        // Проверка, что тело ответа содержит страницу с маршрутом
        Page<Route> result = (Page<Route>) response.getBody();
        assertEquals(1, result.getContent().size());

        // Проверка, что поля возвращаемого маршрута совпадают с ожидаемыми значениями
        Route returnedRoute = result.getContent().get(0);
        assertEquals("r1", returnedRoute.getIdRoute());
        assertEquals("Автобус", returnedRoute.getTransportType().getTransportType());
        assertEquals("Москва", returnedRoute.getDepartureCity().getCityName());
        assertEquals("Санкт-Петербург", returnedRoute.getDestinationCity().getCityName());
        assertEquals(Timestamp.valueOf("2025-03-14 10:00:00"), returnedRoute.getDepartureTime());
        assertEquals(Timestamp.valueOf("2025-03-14 18:00:00"), returnedRoute.getArrivalTime());
        assertEquals(50, returnedRoute.getTotalNumberSeats());
        assertEquals(50, returnedRoute.getNumberAvailableSeats());
    }

    /**
     * Тест получения всех маршрутов с пагинацией, когда маршруты не найдены.
     * Проверка возврата статуса 404 и сообщения об ошибке.
     */
    @Test
    void testGetAllRoutesWithPagination_NotFound() {
        when(routeService.getAllRoutesWithPagination(0, 16)).thenReturn(Page.empty());
        ResponseEntity<?> response = routeController.getAllRoutesWithPagination(0, 16, null);
        assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatusCodeValue());
        assertEquals("Маршруты не найдены.", response.getBody());
    }

    /**
     * Тест получения всех маршрутов с пагинацией при внутренней ошибке сервера.
     * Проверка возврата статуса 500 и пустого списка.
     */
    @Test
    void testGetAllRoutesWithPagination_InternalServerError() {
        when(routeService.getAllRoutesWithPagination(0, 16)).thenThrow(new RuntimeException("Внутренняя ошибка"));
        ResponseEntity<?> response = routeController.getAllRoutesWithPagination(0, 16, null);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), response.getStatusCodeValue());
        assertEquals(Collections.emptyList(), response.getBody());
    }

    /**
     * Тест получения маршрута по ID.
     * Проверка корректности возвращаемого маршрута.
     */
    @Test
    void testGetRouteById_Success() {
        // Создание тестового маршрута
        Route route = createTestRoute("r1", "Автобус", "Москва", "Санкт-Петербург", Timestamp.valueOf("2025-03-14 10:00:00"), Timestamp.valueOf("2025-03-14 18:00:00"), 50, 50);

        // Мокирование сервиса: при вызове метода getRouteById("r1") возвращается тестовый маршрут
        when(routeService.getRouteById("r1")).thenReturn(route);

        // Вызов метода контроллера
        ResponseEntity<?> response = routeController.getRouteById("r1");

        // Проверка, что статус ответа должен быть 200 (OK)
        assertEquals(HttpStatus.OK.value(), response.getStatusCodeValue());
        // Проверка, что тело ответа содержит маршрут
        Route returnedRoute = (Route) response.getBody();
        assertEquals("r1", returnedRoute.getIdRoute());
        assertEquals("Автобус", returnedRoute.getTransportType().getTransportType());
        assertEquals("Москва", returnedRoute.getDepartureCity().getCityName());
        assertEquals("Санкт-Петербург", returnedRoute.getDestinationCity().getCityName());
        assertEquals(Timestamp.valueOf("2025-03-14 10:00:00"), returnedRoute.getDepartureTime());
        assertEquals(Timestamp.valueOf("2025-03-14 18:00:00"), returnedRoute.getArrivalTime());
        assertEquals(50, returnedRoute.getTotalNumberSeats());
        assertEquals(50, returnedRoute.getNumberAvailableSeats());
    }

    /**
     * Тест получения маршрута по ID, когда маршрут не найден.
     * Проверка возврата статуса 404 и сообщения об ошибке.
     */
    @Test
    void testGetRouteById_NotFound() {
        when(routeService.getRouteById("r999")).thenThrow(new NoSuchElementException("Маршрут с ID r999 не найден."));
        ResponseEntity<?> response = routeController.getRouteById("r999");
        assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatusCodeValue());
        assertEquals("Маршрут с ID r999 не найден.", response.getBody());
    }

    /**
     * Тест получения маршрута по ID при внутренней ошибке сервера.
     * Проверка возврата статуса 500 и null.
     */
    @Test
    void testGetRouteById_InternalServerError() {
        when(routeService.getRouteById("r1")).thenThrow(new RuntimeException("Внутренняя ошибка"));
        ResponseEntity<?> response = routeController.getRouteById("r1");
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), response.getStatusCodeValue());
        assertNull(response.getBody());
    }

    /**
     * Тест получения маршрутов по типу транспорта.
     * Проверка корректности возвращаемого списка маршрутов.
     */
    @Test
    void testGetRoutesByTransportType_Success() {
        // Создание тестовых данных: два маршрута с типом транспорта "Автобус"
        Route route1 = createTestRoute("r1", "Автобус", "Москва", "Санкт-Петербург", Timestamp.valueOf("2025-03-14 10:00:00"), Timestamp.valueOf("2025-03-14 18:00:00"), 50, 50);
        Route route2 = createTestRoute("r2", "Автобус", "Казань", "Екатеринбург", Timestamp.valueOf("2025-03-15 12:00:00"), Timestamp.valueOf("2025-03-16 08:00:00"), 100, 100);
        List<Route> routes = Arrays.asList(route1, route2);

        // Мокирование сервиса: при вызове метода getRoutesByTransportType("Автобус") возвращается тестовый список
        when(routeService.getRoutesByTransportType("Автобус")).thenReturn(routes);

        // Вызов метода контроллера
        ResponseEntity<?> response = routeController.getRoutesByTransportType("Автобус");

        // Проверка, что статус ответа должен быть 200 (OK)
        assertEquals(HttpStatus.OK.value(), response.getStatusCodeValue());
        // Проверка, что тело ответа содержит список маршрутов
        List<Route> result = (List<Route>) response.getBody();
        assertEquals(2, result.size());

        // Проверка, что поля возвращаемых маршрутов совпадают с ожидаемыми значениями
        Route returnedRoute1 = result.get(0);
        assertEquals("r1", returnedRoute1.getIdRoute());
        assertEquals("Автобус", returnedRoute1.getTransportType().getTransportType());
        assertEquals("Москва", returnedRoute1.getDepartureCity().getCityName());
        assertEquals("Санкт-Петербург", returnedRoute1.getDestinationCity().getCityName());
        assertEquals(Timestamp.valueOf("2025-03-14 10:00:00"), returnedRoute1.getDepartureTime());
        assertEquals(Timestamp.valueOf("2025-03-14 18:00:00"), returnedRoute1.getArrivalTime());
        assertEquals(50, returnedRoute1.getTotalNumberSeats());
        assertEquals(50, returnedRoute1.getNumberAvailableSeats());

        Route returnedRoute2 = result.get(1);
        assertEquals("r2", returnedRoute2.getIdRoute());
        assertEquals("Автобус", returnedRoute2.getTransportType().getTransportType());
        assertEquals("Казань", returnedRoute2.getDepartureCity().getCityName());
        assertEquals("Екатеринбург", returnedRoute2.getDestinationCity().getCityName());
        assertEquals(Timestamp.valueOf("2025-03-15 12:00:00"), returnedRoute2.getDepartureTime());
        assertEquals(Timestamp.valueOf("2025-03-16 08:00:00"), returnedRoute2.getArrivalTime());
        assertEquals(100, returnedRoute2.getTotalNumberSeats());
        assertEquals(100, returnedRoute2.getNumberAvailableSeats());
    }

    /**
     * Тест получения маршрутов по типу транспорта, когда маршруты не найдены.
     * Проверка возврата статуса 404 и сообщения об ошибке.
     */
    @Test
    void testGetRoutesByTransportType_NotFound() {
        when(routeService.getRoutesByTransportType("Самолет")).thenThrow(new NoSuchElementException("Маршруты с указанным типом транспорта не найдены."));
        ResponseEntity<?> response = routeController.getRoutesByTransportType("Самолет");
        assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatusCodeValue());
        assertEquals("Маршруты с указанным типом транспорта не найдены.", response.getBody());
    }

    /**
     * Тест получения маршрутов по типу транспорта при внутренней ошибке сервера.
     * Проверка возврата статуса 500 и пустого списка.
     */
    @Test
    void testGetRoutesByTransportType_InternalServerError() {
        when(routeService.getRoutesByTransportType("Автобус")).thenThrow(new RuntimeException("Внутренняя ошибка"));
        ResponseEntity<?> response = routeController.getRoutesByTransportType("Автобус");
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), response.getStatusCodeValue());
        assertEquals(Collections.emptyList(), response.getBody());
    }

    /**
     * Тест получения маршрутов по пунктам отправления и назначения.
     * Проверка корректности возвращаемого списка маршрутов.
     */
    @Test
    void testGetRoutesByDepartureAndDestinationPoint_Success() {
        // Создание тестовых данных: два маршрута с указанными пунктами
        Route route1 = createTestRoute("r1", "Автобус", "Москва", "Санкт-Петербург", Timestamp.valueOf("2025-03-14 10:00:00"), Timestamp.valueOf("2025-03-14 18:00:00"), 50, 50);
        Route route2 = createTestRoute("r2", "Поезд", "Москва", "Санкт-Петербург", Timestamp.valueOf("2025-03-15 12:00:00"), Timestamp.valueOf("2025-03-16 08:00:00"), 100, 100);
        List<Route> routes = Arrays.asList(route1, route2);

        // Мокирование сервиса: при вызове метода getRoutesByDepartureAndDestinationPoint() возвращается тестовый список
        when(routeService.getRoutesByDepartureAndDestinationPoint("Москва", "Санкт-Петербург")).thenReturn(routes);

        // Вызов метода контроллера
        ResponseEntity<?> response = routeController.getRoutesByDepartureAndDestinationPoint("Москва", "Санкт-Петербург");

        // Проверка, что статус ответа должен быть 200 (OK)
        assertEquals(HttpStatus.OK.value(), response.getStatusCodeValue());
        // Проверка, что тело ответа содержит список маршрутов
        List<Route> result = (List<Route>) response.getBody();
        assertEquals(2, result.size());

        // Проверка, что поля возвращаемых маршрутов совпадают с ожидаемыми значениями
        Route returnedRoute1 = result.get(0);
        assertEquals("r1", returnedRoute1.getIdRoute());
        assertEquals("Автобус", returnedRoute1.getTransportType().getTransportType());
        assertEquals("Москва", returnedRoute1.getDepartureCity().getCityName());
        assertEquals("Санкт-Петербург", returnedRoute1.getDestinationCity().getCityName());
        assertEquals(Timestamp.valueOf("2025-03-14 10:00:00"), returnedRoute1.getDepartureTime());
        assertEquals(Timestamp.valueOf("2025-03-14 18:00:00"), returnedRoute1.getArrivalTime());
        assertEquals(50, returnedRoute1.getTotalNumberSeats());
        assertEquals(50, returnedRoute1.getNumberAvailableSeats());

        Route returnedRoute2 = result.get(1);
        assertEquals("r2", returnedRoute2.getIdRoute());
        assertEquals("Поезд", returnedRoute2.getTransportType().getTransportType());
        assertEquals("Москва", returnedRoute2.getDepartureCity().getCityName());
        assertEquals("Санкт-Петербург", returnedRoute2.getDestinationCity().getCityName());
        assertEquals(Timestamp.valueOf("2025-03-15 12:00:00"), returnedRoute2.getDepartureTime());
        assertEquals(Timestamp.valueOf("2025-03-16 08:00:00"), returnedRoute2.getArrivalTime());
        assertEquals(100, returnedRoute2.getTotalNumberSeats());
        assertEquals(100, returnedRoute2.getNumberAvailableSeats());
    }

    /**
     * Тест получения маршрутов по пунктам отправления и назначения, когда маршруты не найдены.
     * Проверка возврата статуса 404 и сообщения об ошибке.
     */
    @Test
    void testGetRoutesByDepartureAndDestinationPoint_NotFound() {
        when(routeService.getRoutesByDepartureAndDestinationPoint("Казань", "Екатеринбург")).thenReturn(Collections.emptyList());
        ResponseEntity<?> response = routeController.getRoutesByDepartureAndDestinationPoint("Казань", "Екатеринбург");
        assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatusCodeValue());
        assertEquals("Маршруты не найдены.", response.getBody());
    }

    /**
     * Тест получения маршрутов по пунктам отправления и назначения при внутренней ошибке сервера.
     * Проверка возврата статуса 500 и пустого списка.
     */
    @Test
    void testGetRoutesByDepartureAndDestinationPoint_InternalServerError() {
        when(routeService.getRoutesByDepartureAndDestinationPoint("Москва", "Санкт-Петербург")).thenThrow(new RuntimeException("Внутренняя ошибка"));
        ResponseEntity<?> response = routeController.getRoutesByDepartureAndDestinationPoint("Москва", "Санкт-Петербург");
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), response.getStatusCodeValue());
        assertEquals(Collections.emptyList(), response.getBody());
    }

    /**
     * Тест получения маршрутов по пункту отправления.
     * Проверка корректности возвращаемого списка маршрутов.
     */
    @Test
    void testGetRoutesByDepartureCity_Success() {
        // Создание тестовых данных: два маршрута с пунктом отправления "Москва"
        Route route1 = createTestRoute("r1", "Автобус", "Москва", "Санкт-Петербург", Timestamp.valueOf("2025-03-14 10:00:00"), Timestamp.valueOf("2025-03-14 18:00:00"), 50, 50);
        Route route2 = createTestRoute("r2", "Поезд", "Москва", "Казань", Timestamp.valueOf("2025-03-15 12:00:00"), Timestamp.valueOf("2025-03-16 08:00:00"), 100, 100);
        List<Route> routes = Arrays.asList(route1, route2);

        // Мокирование сервиса: при вызове метода getRoutesByDepartureCity("Москва") возвращается тестовый список
        when(routeService.getRoutesByDepartureCity("Москва")).thenReturn(routes);

        // Вызов метода контроллера
        ResponseEntity<?> response = routeController.getRoutesByDepartureCity("Москва");

        // Проверка, что статус ответа должен быть 200 (OK)
        assertEquals(HttpStatus.OK.value(), response.getStatusCodeValue());
        // Проверка, что тело ответа содержит список маршрутов
        List<Route> result = (List<Route>) response.getBody();
        assertEquals(2, result.size());

        // Проверка, что поля возвращаемых маршрутов совпадают с ожидаемыми значениями
        Route returnedRoute1 = result.get(0);
        assertEquals("r1", returnedRoute1.getIdRoute());
        assertEquals("Автобус", returnedRoute1.getTransportType().getTransportType());
        assertEquals("Москва", returnedRoute1.getDepartureCity().getCityName());
        assertEquals("Санкт-Петербург", returnedRoute1.getDestinationCity().getCityName());
        assertEquals(Timestamp.valueOf("2025-03-14 10:00:00"), returnedRoute1.getDepartureTime());
        assertEquals(Timestamp.valueOf("2025-03-14 18:00:00"), returnedRoute1.getArrivalTime());
        assertEquals(50, returnedRoute1.getTotalNumberSeats());
        assertEquals(50, returnedRoute1.getNumberAvailableSeats());

        Route returnedRoute2 = result.get(1);
        assertEquals("r2", returnedRoute2.getIdRoute());
        assertEquals("Поезд", returnedRoute2.getTransportType().getTransportType());
        assertEquals("Москва", returnedRoute2.getDepartureCity().getCityName());
        assertEquals("Казань", returnedRoute2.getDestinationCity().getCityName());
        assertEquals(Timestamp.valueOf("2025-03-15 12:00:00"), returnedRoute2.getDepartureTime());
        assertEquals(Timestamp.valueOf("2025-03-16 08:00:00"), returnedRoute2.getArrivalTime());
        assertEquals(100, returnedRoute2.getTotalNumberSeats());
        assertEquals(100, returnedRoute2.getNumberAvailableSeats());
    }

    /**
     * Тест получения маршрутов по пункту отправления, когда маршруты не найдены.
     * Проверка возврата статуса 404 и сообщения об ошибке.
     */
    @Test
    void testGetRoutesByDepartureCity_NotFound() {
        when(routeService.getRoutesByDepartureCity("Казань")).thenThrow(new NoSuchElementException("Маршруты с указанным пунктом отправления не найдены."));
        ResponseEntity<?> response = routeController.getRoutesByDepartureCity("Казань");
        assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatusCodeValue());
        assertEquals("Маршруты с указанным пунктом отправления не найдены.", response.getBody());
    }

    /**
     * Тест получения маршрутов по пункту отправления при внутренней ошибке сервера.
     * Проверка возврата статуса 500 и пустого списка.
     */
    @Test
    void testGetRoutesByDepartureCity_InternalServerError() {
        when(routeService.getRoutesByDepartureCity("Москва")).thenThrow(new RuntimeException("Внутренняя ошибка"));
        ResponseEntity<?> response = routeController.getRoutesByDepartureCity("Москва");
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), response.getStatusCodeValue());
        assertEquals(Collections.emptyList(), response.getBody());
    }

    /**
     * Тест получения маршрутов по пункту назначения.
     * Проверка корректности возвращаемого списка маршрутов.
     */
    @Test
    void testGetRoutesByDestinationCity_Success() {
        // Создание тестовых данных: два маршрута с пунктом назначения "Санкт-Петербург"
        Route route1 = createTestRoute("r1", "Автобус", "Москва", "Санкт-Петербург", Timestamp.valueOf("2025-03-14 10:00:00"), Timestamp.valueOf("2025-03-14 18:00:00"), 50, 50);
        Route route2 = createTestRoute("r2", "Поезд", "Казань", "Санкт-Петербург", Timestamp.valueOf("2025-03-15 12:00:00"), Timestamp.valueOf("2025-03-16 08:00:00"), 100, 100);
        List<Route> routes = Arrays.asList(route1, route2);

        // Мокирование сервиса: при вызове метода getRoutesByDestinationCity("Санкт-Петербург") возвращается тестовый список
        when(routeService.getRoutesByDestinationCity("Санкт-Петербург")).thenReturn(routes);

        // Вызов метода контроллера
        ResponseEntity<?> response = routeController.getRoutesByDestinationCity("Санкт-Петербург");

        // Проверка, что статус ответа должен быть 200 (OK)
        assertEquals(HttpStatus.OK.value(), response.getStatusCodeValue());
        // Проверка, что тело ответа содержит список маршрутов
        List<Route> result = (List<Route>) response.getBody();
        assertEquals(2, result.size());

        // Проверка, что поля возвращаемых маршрутов совпадают с ожидаемыми значениями
        Route returnedRoute1 = result.get(0);
        assertEquals("r1", returnedRoute1.getIdRoute());
        assertEquals("Автобус", returnedRoute1.getTransportType().getTransportType());
        assertEquals("Москва", returnedRoute1.getDepartureCity().getCityName());
        assertEquals("Санкт-Петербург", returnedRoute1.getDestinationCity().getCityName());
        assertEquals(Timestamp.valueOf("2025-03-14 10:00:00"), returnedRoute1.getDepartureTime());
        assertEquals(Timestamp.valueOf("2025-03-14 18:00:00"), returnedRoute1.getArrivalTime());
        assertEquals(50, returnedRoute1.getTotalNumberSeats());
        assertEquals(50, returnedRoute1.getNumberAvailableSeats());

        Route returnedRoute2 = result.get(1);
        assertEquals("r2", returnedRoute2.getIdRoute());
        assertEquals("Поезд", returnedRoute2.getTransportType().getTransportType());
        assertEquals("Казань", returnedRoute2.getDepartureCity().getCityName());
        assertEquals("Санкт-Петербург", returnedRoute2.getDestinationCity().getCityName());
        assertEquals(Timestamp.valueOf("2025-03-15 12:00:00"), returnedRoute2.getDepartureTime());
        assertEquals(Timestamp.valueOf("2025-03-16 08:00:00"), returnedRoute2.getArrivalTime());
        assertEquals(100, returnedRoute2.getTotalNumberSeats());
        assertEquals(100, returnedRoute2.getNumberAvailableSeats());
    }

    /**
     * Тест получения маршрутов по пункту назначения, когда маршруты не найдены.
     * Проверка возврата статуса 404 и сообщения об ошибке.
     */
    @Test
    void testGetRoutesByDestinationCity_NotFound() {
        when(routeService.getRoutesByDestinationCity("Екатеринбург")).thenThrow(new NoSuchElementException("Маршруты с указанным пунктом назначения не найдены."));
        ResponseEntity<?> response = routeController.getRoutesByDestinationCity("Екатеринбург");
        assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatusCodeValue());
        assertEquals("Маршруты с указанным пунктом назначения не найдены.", response.getBody());
    }

    /**
     * Тест получения маршрутов по пункту назначения при внутренней ошибке сервера.
     * Проверка возврата статуса 500 и пустого списка.
     */
    @Test
    void testGetRoutesByDestinationCity_InternalServerError() {
        when(routeService.getRoutesByDestinationCity("Санкт-Петербург")).thenThrow(new RuntimeException("Внутренняя ошибка"));
        ResponseEntity<?> response = routeController.getRoutesByDestinationCity("Санкт-Петербург");
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), response.getStatusCodeValue());
        assertEquals(Collections.emptyList(), response.getBody());
    }

    /**
     * Тест создания нового маршрута.
     * Проверка корректности создания и возврата статуса 201.
     */
    @Test
    void testCreateRoute_Success() {
        // Создание тестового маршрута
        Route route = createTestRoute("r1", "Автобус", "Москва", "Санкт-Петербург", Timestamp.valueOf("2025-03-14 10:00:00"), Timestamp.valueOf("2025-03-14 18:00:00"), 50, 50);

        // Мокирование сервиса: при вызове метода createRoute() возвращается тестовый маршрут
        when(routeService.createRoute("Автобус", "Москва", "Санкт-Петербург", "14.03.2025 10:00", "14.03.2025 18:00", 50, 50))
                .thenReturn(route);

        // Вызов метода контроллера
        ResponseEntity<?> response = routeController.createRoute("Автобус", "Москва", "Санкт-Петербург", "14.03.2025 10:00", "14.03.2025 18:00", 50, 50);

        // Проверка, что статус ответа должен быть 201 (Created)
        assertEquals(HttpStatus.CREATED.value(), response.getStatusCodeValue());
        // Проверка, что тело ответа содержит созданный маршрут
        Route returnedRoute = (Route) response.getBody();
        assertEquals("r1", returnedRoute.getIdRoute());
        assertEquals("Автобус", returnedRoute.getTransportType().getTransportType());
        assertEquals("Москва", returnedRoute.getDepartureCity().getCityName());
        assertEquals("Санкт-Петербург", returnedRoute.getDestinationCity().getCityName());
        assertEquals(Timestamp.valueOf("2025-03-14 10:00:00"), returnedRoute.getDepartureTime());
        assertEquals(Timestamp.valueOf("2025-03-14 18:00:00"), returnedRoute.getArrivalTime());
        assertEquals(50, returnedRoute.getTotalNumberSeats());
        assertEquals(50, returnedRoute.getNumberAvailableSeats());
    }

    /**
     * Тест создания нового маршрута с некорректными данными.
     * Проверка возврата статуса 400 и сообщения об ошибке.
     */
    @Test
    void testCreateRoute_InvalidData() {
        when(routeService.createRoute("", "Москва", "Санкт-Петербург", "14.03.2025 10:00", "14.03.2025 18:00", 50, 50))
                .thenThrow(new IllegalArgumentException("Все поля должны быть заполнены."));
        ResponseEntity<?> response = routeController.createRoute("", "Москва", "Санкт-Петербург", "14.03.2025 10:00", "14.03.2025 18:00", 50, 50);
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatusCodeValue());
        assertEquals("Все поля должны быть заполнены.", response.getBody());
    }

    /**
     * Тест создания нового маршрута при внутренней ошибке сервера.
     * Проверка возврата статуса 500 и пустого списка.
     */
    @Test
    void testCreateRoute_InternalServerError() {
        when(routeService.createRoute("Автобус", "Москва", "Санкт-Петербург", "14.03.2025 10:00", "14.03.2025 18:00", 50, 50))
                .thenThrow(new RuntimeException("Внутренняя ошибка"));
        ResponseEntity<?> response = routeController.createRoute("Автобус", "Москва", "Санкт-Петербург", "14.03.2025 10:00", "14.03.2025 18:00", 50, 50);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), response.getStatusCodeValue());
        assertEquals(Collections.emptyList(), response.getBody());
    }

    /**
     * Тест удаления маршрута по ID.
     * Проверка корректности удаления и возврата статуса 200.
     */
    @Test
    void testDeleteRoute_Success() {
        doNothing().when(routeService).deleteRoute("r1");
        ResponseEntity<?> response = routeController.deleteRoute("r1");
        assertEquals(HttpStatus.OK.value(), response.getStatusCodeValue());
        assertEquals("Маршрут успешно удален.", response.getBody());
    }

    /**
     * Тест удаления маршрута по ID, когда маршрут не найден.
     * Проверка возврата статуса 404 и сообщения об ошибке.
     */
    @Test
    void testDeleteRoute_NotFound() {
        doThrow(new NoSuchElementException("Маршрут с ID r999 не найден.")).when(routeService).deleteRoute("r999");
        ResponseEntity<?> response = routeController.deleteRoute("r999");
        assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatusCodeValue());
        assertEquals("Маршрут с ID r999 не найден.", response.getBody());
    }

    /**
     * Тест удаления маршрута по ID с некорректными параметрами.
     * Проверка возврата статуса 400 и сообщения об ошибке.
     */
    @Test
    void testDeleteRoute_InvalidData() {
        doThrow(new IllegalArgumentException("Некорректный ID")).when(routeService).deleteRoute("r1");
        ResponseEntity<?> response = routeController.deleteRoute("r1");
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatusCodeValue());
        assertEquals("Некорректный ID", response.getBody());
    }

    /**
     * Тест удаления маршрута по ID при внутренней ошибке сервера.
     * Проверка возврата статуса 500 и пустого списка.
     */
    @Test
    void testDeleteRoute_InternalServerError() {
        doThrow(new RuntimeException("Внутренняя ошибка")).when(routeService).deleteRoute("r1");
        ResponseEntity<?> response = routeController.deleteRoute("r1");
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), response.getStatusCodeValue());
        assertEquals(Collections.emptyList(), response.getBody());
    }

    /**
     * Тест получения маршрутов по точной дате отправления.
     * Проверка корректности возвращаемого списка маршрутов.
     */
    @Test
    void testGetRoutesForExactDate_Success() {
        // Создание тестовых данных: два маршрута с датой отправления "14.03.2025"
        Route route1 = createTestRoute("r1", "Автобус", "Москва", "Санкт-Петербург", Timestamp.valueOf("2025-03-14 10:00:00"), Timestamp.valueOf("2025-03-14 18:00:00"), 50, 50);
        Route route2 = createTestRoute("r2", "Поезд", "Казань", "Екатеринбург", Timestamp.valueOf("2025-03-14 12:00:00"), Timestamp.valueOf("2025-03-15 08:00:00"), 100, 100);
        List<Route> routes = Arrays.asList(route1, route2);

        // Мокирование сервиса: при вызове метода fetchRoutesForExactDate("14.03.2025") возвращается тестовый список
        when(routeService.fetchRoutesForExactDate("14.03.2025")).thenReturn(routes);

        // Вызов метода контроллера
        ResponseEntity<?> response = routeController.getRoutesForExactDate("14.03.2025");

        // Проверка, что статус ответа должен быть 200 (OK)
        assertEquals(HttpStatus.OK.value(), response.getStatusCodeValue());
        // Проверка, что тело ответа содержит список маршрутов
        List<Route> result = (List<Route>) response.getBody();
        assertEquals(2, result.size());

        // Проверка, что поля возвращаемых маршрутов совпадают с ожидаемыми значениями
        Route returnedRoute1 = result.get(0);
        assertEquals("r1", returnedRoute1.getIdRoute());
        assertEquals("Автобус", returnedRoute1.getTransportType().getTransportType());
        assertEquals("Москва", returnedRoute1.getDepartureCity().getCityName());
        assertEquals("Санкт-Петербург", returnedRoute1.getDestinationCity().getCityName());
        assertEquals(Timestamp.valueOf("2025-03-14 10:00:00"), returnedRoute1.getDepartureTime());
        assertEquals(Timestamp.valueOf("2025-03-14 18:00:00"), returnedRoute1.getArrivalTime());
        assertEquals(50, returnedRoute1.getTotalNumberSeats());
        assertEquals(50, returnedRoute1.getNumberAvailableSeats());

        Route returnedRoute2 = result.get(1);
        assertEquals("r2", returnedRoute2.getIdRoute());
        assertEquals("Поезд", returnedRoute2.getTransportType().getTransportType());
        assertEquals("Казань", returnedRoute2.getDepartureCity().getCityName());
        assertEquals("Екатеринбург", returnedRoute2.getDestinationCity().getCityName());
        assertEquals(Timestamp.valueOf("2025-03-14 12:00:00"), returnedRoute2.getDepartureTime());
        assertEquals(Timestamp.valueOf("2025-03-15 08:00:00"), returnedRoute2.getArrivalTime());
        assertEquals(100, returnedRoute2.getTotalNumberSeats());
        assertEquals(100, returnedRoute2.getNumberAvailableSeats());
    }

    /**
     * Тест получения маршрутов по точной дате отправления, когда маршруты не найдены.
     * Проверка возврата статуса 404 и сообщения об ошибке.
     */
    @Test
    void testGetRoutesForExactDate_NotFound() {
        when(routeService.fetchRoutesForExactDate("15.03.2025")).thenThrow(new NoSuchElementException("Маршруты на указанную дату не найдены."));
        ResponseEntity<?> response = routeController.getRoutesForExactDate("15.03.2025");
        assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatusCodeValue());
        assertEquals("Маршруты на указанную дату не найдены.", response.getBody());
    }

    /**
     * Тест получения маршрутов по точной дате отправления с некорректным форматом даты.
     * Проверка возврата статуса 400 и сообщения об ошибке.
     */
    @Test
    void testGetRoutesForExactDate_InvalidDateFormat() {
        when(routeService.fetchRoutesForExactDate("2025-03-14")).thenThrow(new IllegalArgumentException("Неверный формат даты. Используйте 'dd.MM.yyyy'."));
        ResponseEntity<?> response = routeController.getRoutesForExactDate("2025-03-14");
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatusCodeValue());
        assertEquals("Неверный формат даты. Используйте 'dd.MM.yyyy'.", response.getBody());
    }

    /**
     * Тест получения маршрутов по точной дате отправления при внутренней ошибке сервера.
     * Проверка возврата статуса 500 и пустого списка.
     */
    @Test
    void testGetRoutesForExactDate_InternalServerError() {
        when(routeService.fetchRoutesForExactDate("14.03.2025")).thenThrow(new RuntimeException("Внутренняя ошибка"));
        ResponseEntity<?> response = routeController.getRoutesForExactDate("14.03.2025");
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), response.getStatusCodeValue());
        assertEquals(Collections.emptyList(), response.getBody());
    }

    /**
     * Тест получения маршрутов по диапазону дат отправления.
     * Проверка корректности возвращаемого списка маршрутов.
     */
    @Test
    void testGetRoutesWithinDateRange_Success() {
        // Создание тестовых данных: два маршрута в диапазоне дат
        Route route1 = createTestRoute("r1", "Автобус", "Москва", "Санкт-Петербург", Timestamp.valueOf("2025-03-14 10:00:00"), Timestamp.valueOf("2025-03-14 18:00:00"), 50, 50);
        Route route2 = createTestRoute("r2", "Поезд", "Казань", "Екатеринбург", Timestamp.valueOf("2025-03-15 12:00:00"), Timestamp.valueOf("2025-03-16 08:00:00"), 100, 100);
        List<Route> routes = Arrays.asList(route1, route2);

        // Мокирование сервиса: при вызове метода fetchRoutesWithinDateRange() возвращается тестовый список
        when(routeService.fetchRoutesWithinDateRange("14.03.2025", "15.03.2025")).thenReturn(routes);

        // Вызов метода контроллера
        ResponseEntity<?> response = routeController.getRoutesWithinDateRange("14.03.2025", "15.03.2025");

        // Проверка, что статус ответа должен быть 200 (OK)
        assertEquals(HttpStatus.OK.value(), response.getStatusCodeValue());
        // Проверка, что тело ответа содержит список маршрутов
        List<Route> result = (List<Route>) response.getBody();
        assertEquals(2, result.size());

        // Проверка, что поля возвращаемых маршрутов совпадают с ожидаемыми значениями
        Route returnedRoute1 = result.get(0);
        assertEquals("r1", returnedRoute1.getIdRoute());
        assertEquals("Автобус", returnedRoute1.getTransportType().getTransportType());
        assertEquals("Москва", returnedRoute1.getDepartureCity().getCityName());
        assertEquals("Санкт-Петербург", returnedRoute1.getDestinationCity().getCityName());
        assertEquals(Timestamp.valueOf("2025-03-14 10:00:00"), returnedRoute1.getDepartureTime());
        assertEquals(Timestamp.valueOf("2025-03-14 18:00:00"), returnedRoute1.getArrivalTime());
        assertEquals(50, returnedRoute1.getTotalNumberSeats());
        assertEquals(50, returnedRoute1.getNumberAvailableSeats());

        Route returnedRoute2 = result.get(1);
        assertEquals("r2", returnedRoute2.getIdRoute());
        assertEquals("Поезд", returnedRoute2.getTransportType().getTransportType());
        assertEquals("Казань", returnedRoute2.getDepartureCity().getCityName());
        assertEquals("Екатеринбург", returnedRoute2.getDestinationCity().getCityName());
        assertEquals(Timestamp.valueOf("2025-03-15 12:00:00"), returnedRoute2.getDepartureTime());
        assertEquals(Timestamp.valueOf("2025-03-16 08:00:00"), returnedRoute2.getArrivalTime());
        assertEquals(100, returnedRoute2.getTotalNumberSeats());
        assertEquals(100, returnedRoute2.getNumberAvailableSeats());
    }

    /**
     * Тест получения маршрутов по диапазону дат отправления, когда маршруты не найдены.
     * Проверка возврата статуса 404 и сообщения об ошибке.
     */
    @Test
    void testGetRoutesWithinDateRange_NotFound() {
        when(routeService.fetchRoutesWithinDateRange("16.03.2025", "17.03.2025")).thenThrow(new NoSuchElementException("Маршруты в указанном диапазоне дат не найдены."));
        ResponseEntity<?> response = routeController.getRoutesWithinDateRange("16.03.2025", "17.03.2025");
        assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatusCodeValue());
        assertEquals("Маршруты в указанном диапазоне дат не найдены.", response.getBody());
    }

    /**
     * Тест получения маршрутов по диапазону дат отправления с некорректным форматом даты.
     * Проверка возврата статуса 400 и сообщения об ошибке.
     */
    @Test
    void testGetRoutesWithinDateRange_InvalidDateFormat() {
        when(routeService.fetchRoutesWithinDateRange("2025-03-14", "2025-03-15")).thenThrow(new IllegalArgumentException("Неверный формат даты. Используйте 'dd.MM.yyyy'."));
        ResponseEntity<?> response = routeController.getRoutesWithinDateRange("2025-03-14", "2025-03-15");
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatusCodeValue());
        assertEquals("Неверный формат даты. Используйте 'dd.MM.yyyy'.", response.getBody());
    }

    /**
     * Тест получения маршрутов по диапазону дат отправления при внутренней ошибке сервера.
     * Проверка возврата статуса 500 и пустого списка.
     */
    @Test
    void testGetRoutesWithinDateRange_InternalServerError() {
        when(routeService.fetchRoutesWithinDateRange("14.03.2025", "15.03.2025")).thenThrow(new RuntimeException("Внутренняя ошибка"));
        ResponseEntity<?> response = routeController.getRoutesWithinDateRange("14.03.2025", "15.03.2025");
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), response.getStatusCodeValue());
        assertEquals(Collections.emptyList(), response.getBody());
    }
}