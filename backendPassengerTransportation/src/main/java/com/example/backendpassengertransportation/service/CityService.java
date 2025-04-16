package com.example.backendpassengertransportation.service;

import com.example.backendpassengertransportation.model.City;
import com.example.backendpassengertransportation.repository.CityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

// Сервис для управления городами, предоставляет CRUD-операции
@Service
public class CityService {

    // Репозиторий для работы с городами
    @Autowired
    private CityRepository cityRepository;

    // Метод для получения всех городов
    public List<City> getAllCities() {
        return cityRepository.findAll();
    }

    // Метод для получения города по ID
    public City getCityById(String idCity) {
        return cityRepository.findById(idCity).orElse(null);
    }

    // Метод для создания нового города
    public City createCity(String cityName) {
        if (cityName == null || cityName.isEmpty()) {
            throw new IllegalArgumentException("Название города не может быть пустым.");
        }
        String newIdCity = generateNewId("c");
        City newCity = new City(newIdCity, cityName);
        return cityRepository.save(newCity);
    }

    // Метод для генерации нового ID в формате "c + число"
    private String generateNewId(String prefix) {
        List<City> allCities = cityRepository.findAll();
        int maxId = 0;
        for (City city : allCities) {
            String id = city.getIdCity().substring(1);
            int num = Integer.parseInt(id);
            maxId = Math.max(maxId, num);
        }
        return prefix + (maxId + 1);
    }

    // Метод для обновления города
    public City updateCity(String idCity, String cityName) {
        City city = cityRepository.findById(idCity)
                .orElseThrow(() -> new IllegalArgumentException("Город с ID " + idCity + " не найден."));
        if (cityName != null && !cityName.isEmpty()) {
            city.setCityName(cityName);
        }
        return cityRepository.save(city);
    }

    // Метод для удаления города по ID
    public void deleteCity(String idCity) {
        City city = cityRepository.findById(idCity)
                .orElseThrow(() -> new IllegalArgumentException("Город с ID " + idCity + " не найден."));
        cityRepository.deleteById(idCity);
    }
}