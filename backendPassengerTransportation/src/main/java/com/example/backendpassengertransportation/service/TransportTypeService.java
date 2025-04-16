package com.example.backendpassengertransportation.service;

import com.example.backendpassengertransportation.model.TransportType;
import com.example.backendpassengertransportation.repository.TransportTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

// Сервис для управления типами транспорта, предоставляет CRUD-операции
@Service
public class TransportTypeService {

    // Репозиторий для работы с типами транспорта
    @Autowired
    private TransportTypeRepository transportTypeRepository;

    // Метод для получения всех типов транспорта
    public List<TransportType> getAllTransportTypes() {
        return transportTypeRepository.findAll();
    }

    // Метод для получения типа транспорта по ID
    public TransportType getTransportTypeById(String idTransportType) {
        return transportTypeRepository.findById(idTransportType).orElse(null);
    }

    // Метод для создания нового типа транспорта
    public TransportType createTransportType(String transportType) {
        if (transportType == null || transportType.isEmpty()) {
            throw new IllegalArgumentException("Название типа транспорта не может быть пустым.");
        }
        String newIdTransportType = generateNewId("t");
        TransportType newTransportType = new TransportType(newIdTransportType, transportType);
        return transportTypeRepository.save(newTransportType);
    }

    // Метод для генерации нового ID в формате "t + число"
    private String generateNewId(String prefix) {
        List<TransportType> allTransportTypes = transportTypeRepository.findAll();
        int maxId = 0;
        for (TransportType transport : allTransportTypes) {
            String id = transport.getIdTransportType().substring(1);
            int num = Integer.parseInt(id);
            maxId = Math.max(maxId, num);
        }
        return prefix + (maxId + 1);
    }

    // Метод для обновления типа транспорта
    public TransportType updateTransportType(String idTransportType, String transportType) {
        TransportType existingTransportType = transportTypeRepository.findById(idTransportType)
                .orElseThrow(() -> new IllegalArgumentException("Тип транспорта с ID " + idTransportType + " не найден."));
        if (transportType != null && !transportType.isEmpty()) {
            existingTransportType.setTransportType(transportType);
        }
        return transportTypeRepository.save(existingTransportType);
    }

    // Метод для удаления типа транспорта по ID
    public void deleteTransportType(String idTransportType) {
        TransportType transportType = transportTypeRepository.findById(idTransportType)
                .orElseThrow(() -> new IllegalArgumentException("Тип транспорта с ID " + idTransportType + " не найден."));
        transportTypeRepository.deleteById(idTransportType);
    }
}