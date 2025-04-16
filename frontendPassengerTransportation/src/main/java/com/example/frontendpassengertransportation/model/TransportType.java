package com.example.frontendpassengertransportation.model;

import com.google.gson.annotations.SerializedName;

// Класс, представляющий тип транспорта в системе бронирования билетов
public class TransportType {

    // Аннотация @SerializedName используется для сопоставления поля с JSON ключом
    @SerializedName("idTransportType")
    private String idTransportType; // ID типа транспорта

    @SerializedName("transportType")
    private String transportType; // Название типа транспорта

    // Метод для получения идентификатора типа транспорта
    public String getIdTransportType() {
        return idTransportType;
    }

    // Метод для установки идентификатора типа транспорта
    public void setIdTransportType(String idTransportType) {
        this.idTransportType = idTransportType;
    }

    // Метод для получения названия типа транспорта
    public String getTransportType() {
        return transportType;
    }

    // Метод для установки названия типа транспорта
    public void setTransportType(String transportType) {
        this.transportType = transportType;
    }
}