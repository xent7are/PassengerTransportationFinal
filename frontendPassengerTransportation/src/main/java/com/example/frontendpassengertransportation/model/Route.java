package com.example.frontendpassengertransportation.model;

import com.google.gson.annotations.SerializedName;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

// Класс, представляющий маршрут в системе пассажирских перевозок
public class Route {

    // Аннотация @SerializedName используется для сопоставления поля с JSON ключом
    @SerializedName("idRoute")
    private String idRoute; // Идентификатор маршрута

    @SerializedName("transportType")
    private TransportType transportType; // Тип транспорта для маршрута

    @SerializedName("departureCity")
    private City departureCity; // Город отправления

    @SerializedName("destinationCity")
    private City destinationCity; // Город назначения

    @SerializedName("departureTime")
    private String departureTime; // Время отправления в виде строки

    @SerializedName("arrivalTime")
    private String arrivalTime; // Время прибытия в виде строки

    @SerializedName("totalNumberSeats")
    private int totalNumberSeats; // Общее количество мест

    @SerializedName("numberAvailableSeats")
    private int numberAvailableSeats; // Количество доступных мест

    // Форматтер для вывода даты и времени в формате "dd.MM.yyyy HH:mm"
    private static final DateTimeFormatter OUTPUT_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    // Метод для получения идентификатора маршрута
    public String getIdRoute() {
        return idRoute;
    }

    // Метод для установки идентификатора маршрута
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
    public String getDepartureTime() {
        return departureTime;
    }

    // Метод для установки времени отправления
    public void setDepartureTime(String departureTime) {
        this.departureTime = departureTime;
    }

    // Метод для получения времени прибытия
    public String getArrivalTime() {
        return arrivalTime;
    }

    // Метод для установки времени прибытия
    public void setArrivalTime(String arrivalTime) {
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

    // Метод для получения отформатированного времени отправления в формате "dd.MM.yyyy HH:mm"
    public String getFormattedDepartureTime() {
        if (departureTime == null || departureTime.isEmpty()) return "";
        // Парсинг строки с тайм-зоной в OffsetDateTime
        OffsetDateTime offsetDateTime = OffsetDateTime.parse(departureTime);
        // Преобразование в LocalDateTime (тайм-зона уже учтена в строке)
        LocalDateTime localDateTime = offsetDateTime.toLocalDateTime();
        return localDateTime.format(OUTPUT_FORMATTER);
    }

    // Метод для получения отформатированного времени прибытия в формате "dd.MM.yyyy HH:mm"
    public String getFormattedArrivalTime() {
        if (arrivalTime == null || arrivalTime.isEmpty()) return "";
        OffsetDateTime offsetDateTime = OffsetDateTime.parse(arrivalTime);
        LocalDateTime localDateTime = offsetDateTime.toLocalDateTime();
        return localDateTime.format(OUTPUT_FORMATTER);
    }

    // Метод для получения времени отправления в формате LocalDateTime для фильтрации
    public LocalDateTime getDepartureTimeAsLocalDateTime() {
        if (departureTime == null || departureTime.isEmpty()) return null;
        OffsetDateTime offsetDateTime = OffsetDateTime.parse(departureTime);
        return offsetDateTime.toLocalDateTime();
    }
}