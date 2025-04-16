package com.example.backendpassengertransportation.model;

import jakarta.persistence.*;

// Модель для типа транспорта, хранит информацию о названии типа транспорта
@Entity
@Table(name = "types_of_transport")
public class TransportType {

    @Id
    @Column(name = "id_transport_type")
    private String idTransportType; // ID типа транспорта

    @Column(name = "transport_type", nullable = false)
    private String transportType; // Название типа транспорта

    // Конструктор по умолчанию
    public TransportType() {
    }

    // Конструктор с параметрами
    public TransportType(String idTransportType, String transportType) {
        this.idTransportType = idTransportType;
        this.transportType = transportType;
    }

    // Геттеры и сеттеры

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