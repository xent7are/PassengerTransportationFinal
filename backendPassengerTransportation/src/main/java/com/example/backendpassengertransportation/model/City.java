package com.example.backendpassengertransportation.model;

import jakarta.persistence.*;

// Модель для города, хранит информацию о названии города
@Entity
@Table(name = "cities")
public class City {

    @Id
    @Column(name = "id_city")
    private String idCity; // ID города

    @Column(name = "city", nullable = false)
    private String cityName; // Название города

    // Конструктор по умолчанию
    public City() {
    }

    // Конструктор с параметрами
    public City(String idCity, String cityName) {
        this.idCity = idCity;
        this.cityName = cityName;
    }

    // Геттеры и сеттеры

    // Метод для получения идентификатора города
    public String getIdCity() {
        return idCity;
    }

    // Метод для установки идентификатора города
    public void setIdCity(String idCity) {
        this.idCity = idCity;
    }

    // Метод для получения названия города
    public String getCityName() {
        return cityName;
    }

    // Метод для установки названия города
    public void setCityName(String cityName) {
        this.cityName = cityName;
    }
}