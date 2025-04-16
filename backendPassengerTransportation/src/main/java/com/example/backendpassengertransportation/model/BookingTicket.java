package com.example.backendpassengertransportation.model;

import jakarta.persistence.*;
import java.sql.Timestamp;

// Модель для бронирования билетов, хранит информацию о бронировании, связанном маршруте и пользователе
@Entity
@Table(name = "booking_tickets")
public class BookingTicket {

    @Id
    @Column(name = "id_booking")
    private String idBooking; // ID бронирования

    @ManyToOne
    @JoinColumn(name = "id_route", nullable = false)
    private Route route; // Связанный маршрут

    @ManyToOne
    @JoinColumn(name = "id_user", nullable = false)
    private User user; // Связанный пользователь

    @Column(name = "booking_date", nullable = false)
    private Timestamp bookingDate; // Дата бронирования

    // Конструктор по умолчанию
    public BookingTicket() {
    }

    // Конструктор с параметрами
    public BookingTicket(String idBooking, Route route, User user, Timestamp bookingDate) {
        this.idBooking = idBooking;
        this.route = route;
        this.user = user;
        this.bookingDate = bookingDate;
    }

    // Геттеры и сеттеры

    // Метод для получения ID бронирования
    public String getIdBooking() {
        return idBooking;
    }

    // Метод для установки ID бронирования
    public void setIdBooking(String idBooking) {
        this.idBooking = idBooking;
    }

    // Метод для получения связанного маршрута
    public Route getRoute() {
        return route;
    }

    // Метод для установки связанного маршрута
    public void setRoute(Route route) {
        this.route = route;
    }

    // Метод для получения связанного пользователя
    public User getUser() {
        return user;
    }

    // Метод для установки связанного пользователя
    public void setUser(User user) {
        this.user = user;
    }

    // Метод для получения даты бронирования
    public Timestamp getBookingDate() {
        return bookingDate;
    }

    // Метод для установки даты бронирования
    public void setBookingDate(Timestamp bookingDate) {
        this.bookingDate = bookingDate;
    }
}