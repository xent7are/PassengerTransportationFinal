package com.example.frontendpassengertransportation.model;

import com.google.gson.annotations.SerializedName;

// Класс, представляющий город в системе бронирования билетов
public class City {

    // Аннотация @SerializedName используется для сопоставления поля с JSON ключом
    @SerializedName("idCity")
    private String idCity; // ID города

    @SerializedName("cityName")
    private String cityName; // Название города

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