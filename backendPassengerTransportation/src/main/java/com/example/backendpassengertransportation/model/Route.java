package com.example.backendpassengertransportation.model;

import jakarta.persistence.*;
import java.sql.Timestamp;

// Модель для маршрута, хранит информацию о маршруте, включая города, тип транспорта, время и места
@Entity
@Table(name = "routes")
public class Route {

    @Id
    @Column(name = "id_route")
    private String idRoute; // ID маршрута

    @ManyToOne
    @JoinColumn(name = "id_transport_type", nullable = false)
    private TransportType transportType; // Тип транспорта

    @ManyToOne
    @JoinColumn(name = "departure_city", nullable = false)
    private City departureCity; // Город отправления

    @ManyToOne
    @JoinColumn(name = "destination_city", nullable = false)
    private City destinationCity; // Город назначения

    @Column(name = "departure_time", nullable = false)
    private Timestamp departureTime; // Время отправления

    @Column(name = "arrival_time", nullable = false)
    private Timestamp arrivalTime; // Время прибытия

    @Column(name = "total_number_seats", nullable = false)
    private int totalNumberSeats; // Общее количество мест

    @Column(name = "number_available_seats", nullable = false)
    private int numberAvailableSeats; // Доступные места

    // Конструктор по умолчанию
    public Route() {
    }

    // Конструктор с параметрами
    public Route(String idRoute, TransportType transportType, City departureCity,
                 City destinationCity, Timestamp departureTime, Timestamp arrivalTime,
                 int totalNumberSeats, int numberAvailableSeats) {
        this.idRoute = idRoute;
        this.transportType = transportType;
        this.departureCity = departureCity;
        this.destinationCity = destinationCity;
        this.departureTime = departureTime;
        this.arrivalTime = arrivalTime;
        this.totalNumberSeats = totalNumberSeats;
        this.numberAvailableSeats = numberAvailableSeats;
    }

    // Геттеры и сеттеры

    // Метод для получения ID маршрута
    public String getIdRoute() {
        return idRoute;
    }

    // Метод для установки ID маршрута
    public void setIdRoute(String idRoute) {
        this.idRoute = idRoute;
    }

    // Метод для получения типа транспорта
    public TransportType getTransportType() {
        return transportType;
    }

    // Метод для установки типа транспорта
    public void setTransportType(TransportType transportType) {
        this.transportType = transportType;
    }

    // Метод для получения города отправления
    public City getDepartureCity() {
        return departureCity;
    }

    // Метод для установки города отправления
    public void setDepartureCity(City departureCity) {
        this.departureCity = departureCity;
    }

    // Метод для получения города назначения
    public City getDestinationCity() {
        return destinationCity;
    }

    // Метод для установки города назначения
    public void setDestinationCity(City destinationCity) {
        this.destinationCity = destinationCity;
    }

    // Метод для получения времени отправления
    public Timestamp getDepartureTime() {
        return departureTime;
    }

    // Метод для установки времени отправления
    public void setDepartureTime(Timestamp departureTime) {
        this.departureTime = departureTime;
    }

    // Метод для получения времени прибытия
    public Timestamp getArrivalTime() {
        return arrivalTime;
    }

    // Метод для установки времени прибытия
    public void setArrivalTime(Timestamp arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    // Метод для получения общего количества мест
    public int getTotalNumberSeats() {
        return totalNumberSeats;
    }

    // Метод для установки общего количества мест
    public void setTotalNumberSeats(int totalNumberSeats) {
        this.totalNumberSeats = totalNumberSeats;
    }

    // Метод для получения количества доступных мест
    public int getNumberAvailableSeats() {
        return numberAvailableSeats;
    }

    // Метод для установки количества доступных мест
    public void setNumberAvailableSeats(int numberAvailableSeats) {
        this.numberAvailableSeats = numberAvailableSeats;
    }
}