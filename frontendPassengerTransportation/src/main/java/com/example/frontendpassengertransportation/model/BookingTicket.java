package com.example.frontendpassengertransportation.model;

import com.google.gson.annotations.SerializedName;

// Класс, представляющий бронирование билета в системе
public class BookingTicket {

    // Аннотация @SerializedName используется для сопоставления поля с JSON ключом
    @SerializedName("idBooking")
    private String idBooking; // ID бронирования

    @SerializedName("route")
    private Route route; // Маршрут, связанный с бронированием

    @SerializedName("user")
    private User user; // Пользователь, связанный с бронированием

    @SerializedName("bookingDate")
    private String bookingDate; // Дата и время бронирования

    // Метод для получения идентификатора бронирования
    public String getIdBooking() {
        return idBooking;
    }

    // Метод для установки идентификатора бронирования
    public void setIdBooking(String idBooking) {
        this.idBooking = idBooking;
    }

    // Метод для получения маршрута, связанного с бронированием
    public Route getRoute() {
        return route;
    }

    // Метод для установки маршрута, связанного с бронированием
    public void setRoute(Route route) {
        this.route = route;
    }

    // Метод для получения пользователя, связанного с бронированием
    public User getUser() {
        return user;
    }

    // Метод для установки пользователя, связанного с бронированием
    public void setUser(User user) {
        this.user = user;
    }

    // Метод для получения даты и времени бронирования
    public String getBookingDate() {
        return bookingDate;
    }

    // Метод для установки даты и времени бронирования
    public void setBookingDate(String bookingDate) {
        this.bookingDate = bookingDate;
    }
}