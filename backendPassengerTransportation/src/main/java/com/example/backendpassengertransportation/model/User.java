package com.example.backendpassengertransportation.model;

import jakarta.persistence.*;
import java.util.Date;

// Модель для пользователя, хранит информацию о пользователе
@Entity
@Table(name = "users")
public class User {

    @Id
    @Column(name = "id_user")
    private String idUser; // ID пользователя

    @Column(name = "passenger_full_name", nullable = false)
    private String passengerFullName; // ФИО пассажира

    @Column(name = "passenger_phone", nullable = false)
    private String passengerPhone; // Телефон пассажира

    @Column(name = "passenger_email", nullable = false)
    private String passengerEmail; // Электронная почта пассажира

    @Column(name = "date_of_birth", nullable = false)
    @Temporal(TemporalType.DATE)
    private Date dateOfBirth; // Дата рождения

    @Column(name = "password", nullable = false)
    private String password; // Пароль

    // Конструктор по умолчанию
    public User() {
    }

    // Конструктор с параметрами
    public User(String idUser, String passengerFullName, String passengerPhone,
                String passengerEmail, Date dateOfBirth, String password) {
        this.idUser = idUser;
        this.passengerFullName = passengerFullName;
        this.passengerPhone = passengerPhone;
        this.passengerEmail = passengerEmail;
        this.dateOfBirth = dateOfBirth;
        this.password = password;
    }

    // Геттеры и сеттеры

    // Метод для получения идентификатора пользователя
    public String getIdUser() {
        return idUser;
    }

    // Метод для установки идентификатора пользователя
    public void setIdUser(String idUser) {
        this.idUser = idUser;
    }

    // Метод для получения полного имени пассажира
    public String getPassengerFullName() {
        return passengerFullName;
    }

    // Метод для установки полного имени пассажира
    public void setPassengerFullName(String passengerFullName) {
        this.passengerFullName = passengerFullName;
    }

    // Метод для получения телефона пассажира
    public String getPassengerPhone() {
        return passengerPhone;
    }

    // Метод для установки телефона пассажира
    public void setPassengerPhone(String passengerPhone) {
        this.passengerPhone = passengerPhone;
    }

    // Метод для получения электронной почты пассажира
    public String getPassengerEmail() {
        return passengerEmail;
    }

    // Метод для установки электронной почты пассажира
    public void setPassengerEmail(String passengerEmail) {
        this.passengerEmail = passengerEmail;
    }

    // Метод для получения даты рождения
    public Date getDateOfBirth() {
        return dateOfBirth;
    }

    // Метод для установки даты рождения
    public void setDateOfBirth(Date dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    // Метод для получения пароля
    public String getPassword() {
        return password;
    }

    // Метод для установки пароля
    public void setPassword(String password) {
        this.password = password;
    }
}