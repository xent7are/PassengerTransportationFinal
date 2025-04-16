package com.example.backendpassengertransportation.service;

import com.example.backendpassengertransportation.model.BookingTicket;
import com.example.backendpassengertransportation.model.Route;
import com.example.backendpassengertransportation.model.User;
import com.example.backendpassengertransportation.repository.BookingTicketRepository;
import com.example.backendpassengertransportation.repository.RouteRepository;
import com.example.backendpassengertransportation.repository.UserRepository;
import com.example.backendpassengertransportation.util.ValidationUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class BookingTicketService {

    // Репозиторий для работы с бронированиями билетов
    @Autowired
    private BookingTicketRepository bookingTicketRepository;

    // Репозиторий для работы с маршрутами
    @Autowired
    private RouteRepository routeRepository;

    // Репозиторий для работы с пользователями
    @Autowired
    private UserRepository userRepository;

    // Получение списка всех бронирований
    public List<BookingTicket> getAllBookingTickets() {
        return bookingTicketRepository.findAll();
    }

    // Получение бронирования по ID
    public BookingTicket getBookingTicketById(String idBooking) {
        return bookingTicketRepository.findById(idBooking)
                .orElseThrow(() -> new NoSuchElementException("Бронирование с ID " + idBooking + " не найдено."));
    }

    // Создание нового бронирования с проверкой времени до отправления
    @Transactional
    public BookingTicket createBookingTicket(String routeId, String passengerFullName, String passengerPhone, String passengerEmail) {
        // Проверка формата телефона
        if (!ValidationUtil.isValidPhoneFormat(passengerPhone)) {
            throw new IllegalArgumentException("Неверный формат телефона. Используйте формат: +7 XXX XXX-XX-XX");
        }

        // Проверка формата email
        if (!ValidationUtil.isValidEmailFormat(passengerEmail)) {
            throw new IllegalArgumentException("Неверный формат email. Используйте формат: имя@домен (mail.ru, yandex.ru, gmail.com)");
        }

        // Проверка маршрута
        Route route = routeRepository.findById(routeId)
                .orElseThrow(() -> new NoSuchElementException("Маршрут с ID " + routeId + " не найден."));

        // Получение текущего времени и времени отправления маршрута
        LocalDateTime currentTime = LocalDateTime.now();
        LocalDateTime departureTime = route.getDepartureTime().toLocalDateTime();

        // Проверка, что маршрут уже отправился
        if (currentTime.isAfter(departureTime)) {
            throw new IllegalStateException("Бронирование невозможно: маршрут уже отправился.");
        }

        // Проверка времени до отправления: должно быть не менее 30 минут
        long minutesUntilDeparture = ChronoUnit.MINUTES.between(currentTime, departureTime);
        if (minutesUntilDeparture < 30) {
            throw new IllegalStateException("Бронирование невозможно: до отправления осталось менее 30 минут.");
        }

        // Проверка пользователя
        User user = userRepository.findByPassengerFullNameAndPassengerPhoneAndPassengerEmail(passengerFullName, passengerPhone, passengerEmail)
                .orElseThrow(() -> new NoSuchElementException("Пользователь с такими данными не найден в базе данных пассажирских перевозок."));

        // Проверка доступности мест
        if (route.getNumberAvailableSeats() <= 0) {
            throw new IllegalStateException("Нет доступных мест для бронирования.");
        }

        // Уменьшение количества мест
        route.setNumberAvailableSeats(route.getNumberAvailableSeats() - 1);
        routeRepository.save(route);

        // Создание бронирования
        Timestamp bookingDate = Timestamp.valueOf(LocalDateTime.now());
        String newIdBooking = generateNewId("b");
        BookingTicket bookingTicket = new BookingTicket(newIdBooking, route, user, bookingDate);
        return bookingTicketRepository.save(bookingTicket);
    }

    // Генерация нового ID для бронирования
    private String generateNewId(String prefix) {
        List<BookingTicket> allBookings = bookingTicketRepository.findAll();
        int maxId = 0;
        for (BookingTicket booking : allBookings) {
            String id = booking.getIdBooking().substring(1);
            int num = Integer.parseInt(id);
            maxId = Math.max(maxId, num);
        }
        return prefix + (maxId + 1);
    }

    // Удаление бронирования с проверкой времени до отправления
    @Transactional
    public void deleteBookingTicket(String idBooking) {
        // Проверка существования бронирования
        BookingTicket bookingTicket = bookingTicketRepository.findById(idBooking)
                .orElseThrow(() -> new IllegalArgumentException("Бронирование с ID " + idBooking + " не найдено."));

        Route route = bookingTicket.getRoute();

        // Получение текущего времени и времени отправления маршрута
        LocalDateTime currentTime = LocalDateTime.now();
        LocalDateTime departureTime = route.getDepartureTime().toLocalDateTime();

        // Проверка, что маршрут уже отправился
        if (currentTime.isAfter(departureTime)) {
            throw new IllegalStateException("Отмена бронирования невозможна: маршрут уже отправился.");
        }

        // Проверка времени до отправления: должно быть не менее 30 минут
        long minutesUntilDeparture = ChronoUnit.MINUTES.between(currentTime, departureTime);
        if (minutesUntilDeparture < 30) {
            throw new IllegalStateException("Отмена бронирования невозможна: до отправления осталось менее 30 минут.");
        }

        // Увеличение количества доступных мест
        route.setNumberAvailableSeats(route.getNumberAvailableSeats() + 1);
        routeRepository.save(route);
        bookingTicketRepository.deleteById(idBooking);
    }

    // Поиск бронирования по маршруту и номеру телефона
    public BookingTicket getBookingTicketByRouteAndPassengerPhone(String routeId, String passengerPhone) {
        // Проверка формата телефона
        if (!ValidationUtil.isValidPhoneFormat(passengerPhone)) {
            throw new IllegalArgumentException("Неверный формат телефона. Используйте формат: +7 XXX XXX-XX-XX");
        }
        // Проверка маршрута
        Route route = routeRepository.findById(routeId)
                .orElseThrow(() -> new IllegalArgumentException("Маршрут с ID " + routeId + " не найден."));
        Optional<BookingTicket> bookingTicket = bookingTicketRepository.findByRouteAndUserPassengerPhone(route, passengerPhone);
        if (bookingTicket.isEmpty()) {
            throw new NoSuchElementException("Бронирование с телефоном '" + passengerPhone + "' не найдено.");
        }
        return bookingTicket.get();
    }

    // Получение всех бронирований для маршрута
    public List<BookingTicket> getBookingTicketsByRoute(String routeId) {
        // Проверка маршрута
        Route route = routeRepository.findById(routeId)
                .orElseThrow(() -> new IllegalArgumentException("Маршрут с ID " + routeId + " не найден."));
        return bookingTicketRepository.findByRoute(route);
    }

    // Получение бронирований по email пассажира
    public List<BookingTicket> getBookingTicketsByPassengerEmail(String passengerEmail) {
        // Проверка формата email
        if (!ValidationUtil.isValidEmailFormat(passengerEmail)) {
            throw new IllegalArgumentException("Неверный формат email. Используйте формат: имя@домен (mail.ru, yandex.ru, gmail.com)");
        }
        // Проверка пользователя
        User user = userRepository.findByPassengerEmail(passengerEmail)
                .orElseThrow(() -> new NoSuchElementException("Пользователь с email '" + passengerEmail + "' не найден."));
        return bookingTicketRepository.findByUser(user);
    }
}