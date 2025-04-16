package com.example.frontendpassengertransportation.model;

import com.google.gson.annotations.SerializedName;

// Класс, представляющий пользователя в системе бронирования билетов
public class User {

    // Аннотация @SerializedName используется для сопоставления поля с JSON ключом
    @SerializedName("idUser")
    private String idUser; // ID пользователя

    @SerializedName("passengerFullName")
    private String passengerFullName; // Полное имя пассажира

    @SerializedName("passengerPhone")
    private String passengerPhone; // Номер телефона пассажира

    @SerializedName("passengerEmail")
    private String passengerEmail; // Электронная почта пассажира

    @SerializedName("dateOfBirth")
    private String dateOfBirth; // Дата рождения пользователя

    @SerializedName("password")
    private String password; // Пароль пользователя

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

    // Метод для получения номера телефона пассажира
    public String getPassengerPhone() {
        return passengerPhone;
    }

    // Метод для установки номера телефона пассажира
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

    // Метод для получения даты рождения пользователя
    public String getDateOfBirth() {
        return dateOfBirth;
    }

    // Метод для установки даты рождения пользователя
    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    // Метод для получения пароля пользователя
    public String getPassword() {
        return password;
    }

    // Метод для установки пароля пользователя
    public void setPassword(String password) {
        this.password = password;
    }
}