package com.example.backendpassengertransportation.service;

import com.example.backendpassengertransportation.model.City;
import com.example.backendpassengertransportation.model.Route;
import com.example.backendpassengertransportation.model.TransportType;
import com.example.backendpassengertransportation.repository.CityRepository;
import com.example.backendpassengertransportation.repository.RouteRepository;
import com.example.backendpassengertransportation.repository.TransportTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

// Сервис для управления маршрутами, предоставляет CRUD-операции и поиск по различным критериям
@Service
public class RouteService {

    // Репозиторий для работы с маршрутами
    @Autowired
    private RouteRepository routeRepository;

    // Репозиторий для работы с типами транспорта
    @Autowired
    private TransportTypeRepository transportTypeRepository;

    // Репозиторий для работы с городами
    @Autowired
    private CityRepository cityRepository;

    // Метод для получения всех маршрутов (без пагинации)
    public List<Route> getAllRoutes() {
        List<Route> routes = routeRepository.findAll();
        if (routes.isEmpty()) {
            throw new NoSuchElementException("Маршруты не найдены.");
        }
        return routes;
    }

    // Метод для получения маршрутов с пагинацией и фильтрацией по минимальному времени отправления
    public Page<Route> getAllRoutesWithPagination(int page, int size, Timestamp minDepartureTime) {
        // Создается объект Pageable с сортировкой по departureTime в порядке возрастания
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "departureTime"));
        return routeRepository.findByDepartureTimeAfter(minDepartureTime, pageable);
    }

    // Метод для получения маршрутов с пагинацией без фильтрации по времени
    public Page<Route> getAllRoutesWithPagination(int page, int size) {
        // Создается объект Pageable с сортировкой по departureTime в порядке возрастания
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "departureTime"));
        return routeRepository.findAll(pageable);
    }

    // Метод для получения маршрута по ID
    public Route getRouteById(String idRoute) {
        Route route = routeRepository.findById(idRoute).orElse(null);
        if (route == null) {
            throw new NoSuchElementException("Маршрут с ID " + idRoute + " не найден.");
        }
        return route;
    }

    // Метод для поиска маршрутов по пункту отправления
    public List<Route> getRoutesByDepartureCity(String departureCityName) {
        City departureCity = cityRepository.findByCityName(departureCityName)
                .orElseThrow(() -> new NoSuchElementException("Город отправления '" + departureCityName + "' не найден."));
        List<Route> routes = routeRepository.findByDepartureCity(departureCity);
        if (routes.isEmpty()) {
            throw new NoSuchElementException("Маршруты с указанным пунктом отправления не найдены.");
        }
        return routes;
    }

    // Метод для поиска маршрутов по пункту назначения
    public List<Route> getRoutesByDestinationCity(String destinationCityName) {
        City destinationCity = cityRepository.findByCityName(destinationCityName)
                .orElseThrow(() -> new NoSuchElementException("Город назначения '" + destinationCityName + "' не найден."));
        List<Route> routes = routeRepository.findByDestinationCity(destinationCity);
        if (routes.isEmpty()) {
            throw new NoSuchElementException("Маршруты с указанным пунктом назначения не найдены.");
        }
        return routes;
    }

    // Метод для создания нового маршрута
    public Route createRoute(String transportType, String departureCityName, String destinationCityName,
                             String departureTime, String arrivalTime, int totalNumberSeats,
                             int numberAvailableSeats) {
        // Проверка на пустые значения
        if (transportType == null || transportType.isEmpty() ||
                departureCityName == null || departureCityName.isEmpty() ||
                destinationCityName == null || destinationCityName.isEmpty() ||
                departureTime == null || departureTime.isEmpty() ||
                arrivalTime == null || arrivalTime.isEmpty()) {
            throw new IllegalArgumentException("Все поля должны быть заполнены.");
        }

        // Проверка корректности количества мест
        if (totalNumberSeats <= 0) {
            throw new IllegalArgumentException("Общее количество мест должно быть больше нуля.");
        }
        if (numberAvailableSeats < 0 || numberAvailableSeats > totalNumberSeats) {
            throw new IllegalArgumentException("Количество доступных мест должно быть от 0 до общего количества.");
        }

        // Проверка корректности даты и времени
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        try {
            LocalDateTime departureDateTime = LocalDateTime.parse(departureTime, formatter);
            LocalDateTime arrivalDateTime = LocalDateTime.parse(arrivalTime, formatter);
            if (arrivalDateTime.isBefore(departureDateTime)) {
                throw new IllegalArgumentException("Время прибытия не может быть раньше времени отправления.");
            }
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Неверный формат времени. Используйте 'yyyy-MM-dd HH:mm'.");
        }

        TransportType transport = transportTypeRepository.findByTransportType(transportType)
                .orElseThrow(() -> new NoSuchElementException("Тип транспорта '" + transportType + "' не найден."));
        City departureCity = cityRepository.findByCityName(departureCityName)
                .orElseThrow(() -> new NoSuchElementException("Город отправления '" + departureCityName + "' не найден."));
        City destinationCity = cityRepository.findByCityName(destinationCityName)
                .orElseThrow(() -> new NoSuchElementException("Город назначения '" + destinationCityName + "' не найден."));

        String newIdRoute = generateNewId("r");
        Route newRoute = new Route(newIdRoute, transport, departureCity, destinationCity,
                Timestamp.valueOf(departureTime), Timestamp.valueOf(arrivalTime), totalNumberSeats, numberAvailableSeats);
        return routeRepository.save(newRoute);
    }

    // Метод для генерации нового ID в формате "r + число"
    private String generateNewId(String prefix) {
        List<Route> allRoutes = routeRepository.findAll();
        int maxId = 0;
        for (Route route : allRoutes) {
            String id = route.getIdRoute().substring(1);
            int num = Integer.parseInt(id);
            maxId = Math.max(maxId, num);
        }
        return prefix + (maxId + 1);
    }

    // Метод для удаления маршрута по ID
    public void deleteRoute(String idRoute) {
        Route route = routeRepository.findById(idRoute)
                .orElseThrow(() -> new IllegalArgumentException("Маршрут с ID " + idRoute + " не найден."));
        routeRepository.deleteById(idRoute);
    }

    // Метод для поиска маршрутов по типу транспорта
    public List<Route> getRoutesByTransportType(String transportType) {
        TransportType transport = transportTypeRepository.findByTransportType(transportType)
                .orElseThrow(() -> new NoSuchElementException("Тип транспорта '" + transportType + "' не найден."));
        List<Route> routes = routeRepository.findByTransportType(transport);
        if (routes.isEmpty()) {
            throw new IllegalStateException("Маршруты с указанным типом транспорта не найдены.");
        }
        return routes;
    }

    // Метод для поиска маршрутов по пунктам отправления и назначения
    public List<Route> getRoutesByDepartureAndDestinationPoint(String departureCityName, String destinationCityName) {
        if (departureCityName.isEmpty() || destinationCityName.isEmpty()) {
            throw new IllegalStateException("Пункты отправления и назначения должны быть указаны.");
        }
        City departureCity = cityRepository.findByCityName(departureCityName)
                .orElseThrow(() -> new NoSuchElementException("Город отправления '" + departureCityName + "' не найден."));
        City destinationCity = cityRepository.findByCityName(destinationCityName)
                .orElseThrow(() -> new NoSuchElementException("Город назначения '" + destinationCityName + "' не найден."));
        return routeRepository.findByDepartureCityAndDestinationCity(departureCity, destinationCity);
    }

    // Метод для поиска маршрутов на указанную дату
    public List<Route> fetchRoutesForExactDate(String exactDate) {
        List<Route> resultRoutes = new ArrayList<>();
        DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        try {
            LocalDate searchDate = LocalDate.parse(exactDate, inputFormatter);
            List<Route> allRoutes = routeRepository.findAll();
            for (Route route : allRoutes) {
                LocalDate routeDate = route.getDepartureTime().toLocalDateTime().toLocalDate();
                if (routeDate.isEqual(searchDate)) {
                    resultRoutes.add(route);
                }
            }
            if (resultRoutes.isEmpty()) {
                throw new NoSuchElementException("Маршруты на указанную дату не найдены.");
            }
            return resultRoutes;
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Неверный формат даты. Используйте 'dd.MM.yyyy'.");
        }
    }

    // Метод для поиска маршрутов в диапазоне дат
    public List<Route> fetchRoutesWithinDateRange(String startDateStr, String endDateStr) {
        List<Route> resultRoutes = new ArrayList<>();
        DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        try {
            LocalDate startDate = LocalDate.parse(startDateStr, inputFormatter);
            LocalDate endDate = LocalDate.parse(endDateStr, inputFormatter);
            List<Route> allRoutes = routeRepository.findAll();
            for (Route route : allRoutes) {
                LocalDate routeDate = route.getDepartureTime().toLocalDateTime().toLocalDate();
                if (!routeDate.isBefore(startDate) && !routeDate.isAfter(endDate)) {
                    resultRoutes.add(route);
                }
            }
            if (resultRoutes.isEmpty()) {
                throw new NoSuchElementException("Маршруты в указанном диапазоне дат не найдены.");
            }
            return resultRoutes;
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Неверный формат даты. Используйте 'dd.MM.yyyy'.");
        }
    }
}