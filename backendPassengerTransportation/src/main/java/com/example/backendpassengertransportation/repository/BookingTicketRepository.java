package com.example.backendpassengertransportation.repository;

import com.example.backendpassengertransportation.model.BookingTicket;
import com.example.backendpassengertransportation.model.Route;
import com.example.backendpassengertransportation.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

// Репозиторий для работы с бронированиями билетов, предоставляет методы для поиска и управления бронированиями
@Repository
public interface BookingTicketRepository extends JpaRepository<BookingTicket, String> {

    // Проверка наличия бронирования для конкретного маршрута
    boolean existsByRoute(Route route);

    // Поиск бронирования по маршруту и телефону связанного пользователя
    Optional<BookingTicket> findByRouteAndUserPassengerPhone(Route route, String passengerPhone);

    // Получение всех бронирований для определенного маршрута
    List<BookingTicket> findByRoute(Route route);

    // Получение всех бронирований для определенного пользователя
    List<BookingTicket> findByUser(User user);
}