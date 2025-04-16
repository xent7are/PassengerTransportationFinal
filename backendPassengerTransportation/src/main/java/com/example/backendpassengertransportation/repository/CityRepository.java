package com.example.backendpassengertransportation.repository;

import com.example.backendpassengertransportation.model.City;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

// Репозиторий для работы с городами, предоставляет методы для поиска и управления городами
@Repository
public interface CityRepository extends JpaRepository<City, String> {

    // Поиск города по названию
    Optional<City> findByCityName(String cityName);
}