package com.example.backendpassengertransportation.repository;

import com.example.backendpassengertransportation.model.City;
import com.example.backendpassengertransportation.model.Route;
import com.example.backendpassengertransportation.model.TransportType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.sql.Timestamp;
import java.util.List;

// Репозиторий для работы с маршрутами, предоставляет методы для поиска и управления маршрутами
@Repository
public interface RouteRepository extends JpaRepository<Route, String> {

    // Поиск маршрутов по типу транспорта (через связанную сущность)
    List<Route> findByTransportType(TransportType transportType);

    // Поиск маршрутов по городам отправления и назначения (через связанные сущности)
    List<Route> findByDepartureCityAndDestinationCity(City departureCity, City destinationCity);

    // Поиск маршрутов по городу отправления (через связанную сущность)
    List<Route> findByDepartureCity(City departureCity);

    // Поиск маршрутов по городу назначения (через связанную сущность)
    List<Route> findByDestinationCity(City destinationCity);

    // Поиск маршрутов с количеством доступных мест больше указанного значения
    List<Route> findByNumberAvailableSeatsGreaterThan(int seats);

    // Поиск маршрутов с временем отправления после указанного значения с поддержкой пагинации
    @Query("SELECT r FROM Route r WHERE r.departureTime > :minTime")
    Page<Route> findByDepartureTimeAfter(@Param("minTime") Timestamp minTime, Pageable pageable);
}