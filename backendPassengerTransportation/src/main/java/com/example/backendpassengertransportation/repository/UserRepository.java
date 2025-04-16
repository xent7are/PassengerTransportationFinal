package com.example.backendpassengertransportation.repository;

import com.example.backendpassengertransportation.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

// Репозиторий для работы с пользователями, предоставляет методы для поиска и управления пользователями
@Repository
public interface UserRepository extends JpaRepository<User, String> {

    // Поиск пользователя по email
    Optional<User> findByPassengerEmail(String email);

    // Поиск пользователя по номеру телефона
    Optional<User> findByPassengerPhone(String phone);

    // Поиск пользователя по ФИО
    Optional<User> findByPassengerFullName(String passengerFullName);

    // Поиск пользователя по ФИО и email
    // Добавлено: используется для аутентификации по двум полям
    Optional<User> findByPassengerFullNameAndPassengerEmail(String passengerFullName, String passengerEmail);

    // Поиск пользователя по ФИО, номеру телефона и email
    Optional<User> findByPassengerFullNameAndPassengerPhoneAndPassengerEmail(
            String passengerFullName, String passengerPhone, String passengerEmail);
}