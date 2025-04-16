package com.example.backendpassengertransportation.repository;

import com.example.backendpassengertransportation.model.TransportType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

// Репозиторий для работы с типами транспорта, предоставляет методы для поиска и управления типами транспорта
@Repository
public interface TransportTypeRepository extends JpaRepository<TransportType, String> {

    // Поиск типа транспорта по названию
    Optional<TransportType> findByTransportType(String transportType);
}