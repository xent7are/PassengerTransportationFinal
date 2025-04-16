package com.example.frontendpassengertransportation.controller;

import com.example.frontendpassengertransportation.model.City;
import com.example.frontendpassengertransportation.model.TransportType;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

// Класс MainController отвечает за управление главным окном приложения
public class MainController {

    // Поля для хранения данных
    private String token; // Токен авторизации
    private String email; // Email пользователя

    // Элементы интерфейса
    @FXML
    private ComboBox<String> transportTypeComboBox; // ComboBox для выбора типа транспорта
    @FXML
    private ComboBox<String> departureCityComboBox; // ComboBox для выбора города отправления
    @FXML
    private ComboBox<String> destinationCityComboBox; // ComboBox для выбора города назначения
    @FXML
    private DatePicker startDatePicker; // DatePicker для выбора начальной даты
    @FXML
    private DatePicker endDatePicker; // DatePicker для выбора конечной даты
    @FXML
    private Button showAllRoutesButton; // Кнопка для отображения всех маршрутов
    @FXML
    private Button ShowPassengerBookingsButton; // Кнопка для просмотра бронирований пассажира
    @FXML
    private Button backButton; // Кнопка "Назад"
    @FXML
    private Button accountButton; // Кнопка для просмотра данных пользователя

    // Форматтер для дат в формате дд.мм.гггг
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    // Метод для установки токена
    public void setToken(String token) {
        this.token = token;
    }

    // Метод для установки email
    public void setEmail(String email) {
        this.email = email;
    }

    // Метод для инициализации контроллера
    public void initialize() {
        // Настройка обработчиков событий и DatePicker
        setupEventHandlers();
        setupDatePickers();
    }

    // Метод для загрузки данных в ComboBox
    public void loadComboBoxData() {
        // Проверка доступности сервера перед загрузкой данных
        if (isServerAvailable()) {
            loadTransportTypes();
            loadCities();
        } else {
            showErrorAlert("Сервер недоступен. Пожалуйста, попробуйте позже.");
        }
    }

    // Метод для настройки обработчиков событий
    private void setupEventHandlers() {
        // Обработчик для ComboBox типа транспорта
        transportTypeComboBox.setOnShowing(event -> {
            // Проверка доступности сервера
            if (!isServerAvailable()) {
                showErrorAlert("Сервер недоступен. Пожалуйста, попробуйте позже.");
                Platform.runLater(() -> transportTypeComboBox.hide());
            }
        });

        // Обработчик для ComboBox города отправления
        departureCityComboBox.setOnShowing(event -> {
            // Проверка доступности сервера
            if (!isServerAvailable()) {
                showErrorAlert("Сервер недоступен. Пожалуйста, попробуйте позже.");
                Platform.runLater(() -> departureCityComboBox.hide());
            }
        });

        // Обработчик для ComboBox города назначения
        destinationCityComboBox.setOnShowing(event -> {
            // Проверка доступности сервера
            if (!isServerAvailable()) {
                showErrorAlert("Сервер недоступен. Пожалуйста, попробуйте позже.");
                Platform.runLater(() -> destinationCityComboBox.hide());
            }
        });

        // Обработчик для DatePicker начальной даты
        startDatePicker.setOnShowing(event -> {
            // Проверка доступности сервера
            if (!isServerAvailable()) {
                showErrorAlert("Сервер недоступен. Пожалуйста, попробуйте позже.");
                Platform.runLater(() -> startDatePicker.hide());
            }
        });

        // Обработчик для DatePicker конечной даты
        endDatePicker.setOnShowing(event -> {
            // Проверка доступности сервера
            if (!isServerAvailable()) {
                showErrorAlert("Сервер недоступен. Пожалуйста, попробуйте позже.");
                Platform.runLater(() -> endDatePicker.hide());
            }
        });
    }

    // Метод для настройки DatePicker
    private void setupDatePickers() {
        StringConverter<LocalDate> converter = new StringConverter<LocalDate>() {
            @Override
            public String toString(LocalDate date) {
                // Преобразование даты в строку в формате дд.мм.гггг
                if (date != null) {
                    return dateFormatter.format(date);
                }
                return "";
            }

            @Override
            public LocalDate fromString(String string) {
                // Преобразование строки в дату
                if (string != null && !string.isEmpty()) {
                    try {
                        return LocalDate.parse(string, dateFormatter);
                    } catch (DateTimeParseException e) {
                        return null; // Возврат null при некорректной дате
                    }
                }
                return null;
            }
        };

        // Применение конвертера к DatePicker
        startDatePicker.setConverter(converter);
        endDatePicker.setConverter(converter);
    }

    // Метод для проверки доступности сервера
    private boolean isServerAvailable() {
        try {
            // Создание подключения к серверу
            URL url = new URL("http://localhost:8080/routes");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            // Добавление заголовка авторизации с токеном
            connection.setRequestProperty("Authorization", "Bearer " + token);
            // Установка таймаута подключения
            connection.setConnectTimeout(5000);
            connection.connect();
            // Проверка успешного ответа от сервера
            return connection.getResponseCode() == 200;
        } catch (IOException e) {
            // Возврат false при недоступности сервера
            return false;
        }
    }

    // Метод для загрузки типов транспорта из API
    private void loadTransportTypes() {
        try {
            // Создание подключения к API
            URL url = new URL("http://localhost:8080/transport-types");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            // Добавление заголовка авторизации с токеном
            connection.setRequestProperty("Authorization", "Bearer " + token);

            // Чтение ответа от сервера
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder content = new StringBuilder();
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }

            // Закрытие потока и соединения
            in.close();
            connection.disconnect();

            // Парсинг JSON в список типов транспорта
            Gson gson = new Gson();
            List<TransportType> transportTypes = gson.fromJson(content.toString(), new TypeToken<List<TransportType>>() {}.getType());

            // Создание списка для ComboBox
            ObservableList<String> transportTypeList = FXCollections.observableArrayList();
            for (TransportType type : transportTypes) {
                transportTypeList.add(type.getTransportType());
            }

            // Добавление опции "Микс" в начало списка
            transportTypeList.add(0, "Микс");

            // Установка элементов и выбор по умолчанию
            transportTypeComboBox.setItems(transportTypeList);
            transportTypeComboBox.getSelectionModel().selectFirst();
        } catch (Exception e) {
            // Отображение сообщения об ошибке при загрузке типов транспорта
            showErrorAlert("Ошибка загрузки типов транспорта. Пожалуйста, попробуйте позже.");
        }
    }

    // Метод для загрузки городов из API
    private void loadCities() {
        try {
            // Создание подключения к API
            URL url = new URL("http://localhost:8080/cities");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            // Добавление заголовка авторизации с токеном
            connection.setRequestProperty("Authorization", "Bearer " + token);

            // Чтение ответа от сервера
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder content = new StringBuilder();
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }

            // Закрытие потока и соединения
            in.close();
            connection.disconnect();

            // Парсинг JSON в список городов
            Gson gson = new Gson();
            List<City> cities = gson.fromJson(content.toString(), new TypeToken<List<City>>() {}.getType());

            // Создание списка для ComboBox
            ObservableList<String> cityList = FXCollections.observableArrayList();
            for (City city : cities) {
                cityList.add(city.getCityName());
            }

            // Установка списка городов в ComboBox
            departureCityComboBox.setItems(cityList);
            destinationCityComboBox.setItems(cityList);
        } catch (Exception e) {
            // Отображение сообщения об ошибке при загрузке городов
            showErrorAlert("Ошибка загрузки городов. Пожалуйста, попробуйте позже.");
        }
    }

    // Метод для обработки нажатия кнопки "Показать все маршруты"
    @FXML
    private void handleShowAllRoutesButton() {
        try {
            // Проверка доступности сервера
            if (!isServerAvailable()) {
                showErrorAlert("Сервер недоступен. Пожалуйста, попробуйте позже.");
                return;
            }

            // Закрытие текущего окна
            Stage currentStage = (Stage) showAllRoutesButton.getScene().getWindow();
            currentStage.close();

            // Загрузка окна со всеми маршрутами
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/frontendpassengertransportation/views/routes_all.fxml"));
            Parent root = loader.load();

            // Передача токена и email в контроллер и загрузка маршрутов
            AllRoutesController allRoutesController = loader.getController();
            allRoutesController.setToken(token);
            allRoutesController.setEmail(email);
            allRoutesController.loadInitialRoutes();

            // Создание и отображение нового окна
            Scene scene = new Scene(root, 1040, 740);
            Stage newStage = new Stage();
            newStage.setTitle("Список всех маршрутов");
            newStage.setScene(scene);
            newStage.show();
        } catch (IOException e) {
            // Отображение сообщения об ошибке при загрузке окна
            showErrorAlert("Не удалось открыть окно со списком всех маршрутов.");
        }
    }

    // Метод для обработки нажатия кнопки "Показать маршруты по фильтру"
    @FXML
    private void handleShowRoutesWithFilterButton() {
        try {
            // Проверка доступности сервера
            if (!isServerAvailable()) {
                showErrorAlert("Сервер недоступен. Пожалуйста, попробуйте позже.");
                return;
            }

            // Получение данных из полей
            String transportType = transportTypeComboBox.getValue();
            String departureCity = departureCityComboBox.getValue();
            String destinationCity = destinationCityComboBox.getValue();
            LocalDate startDate = startDatePicker.getValue();
            LocalDate endDate = endDatePicker.getValue();

            // Игнорирование фильтра по типу транспорта при выборе "Микс"
            if ("Микс".equals(transportType)) {
                transportType = null;
            }

            // Проверка совпадения городов
            if (departureCity != null && destinationCity != null && departureCity.equals(destinationCity)) {
                showErrorAlert("Город отправления и город назначения не могут совпадать.");
                return;
            }

            // Проверка корректности дат
            String startDateText = startDatePicker.getEditor().getText();
            String endDateText = endDatePicker.getEditor().getText();

            if (!startDateText.isEmpty() && startDate == null) {
                showErrorAlert("Некорректная начальная дата. Указана несуществующая дата или неверный формат (дд.мм.гггг).");
                return;
            }

            if (!endDateText.isEmpty() && endDate == null) {
                showErrorAlert("Некорректная конечная дата. Указана несуществующая дата или неверный формат (дд.мм.гггг).");
                return;
            }

            // Проверка заполнения хотя бы одного поля
            if (transportType == null && departureCity == null && destinationCity == null && startDate == null && endDate == null) {
                showErrorAlert("Для выполнения поиска маршрутов по фильтру необходимо заполнить хотя бы одно поле.");
                return;
            }

            // Проверка наличия начальной даты при указании конечной
            if (startDate == null && endDate != null) {
                showErrorAlert("Если вы вводите конечную дату, необходимо также ввести начальную дату.");
                return;
            }

            // Проверка порядка дат
            if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
                showErrorAlert("Дата начала не может быть позже даты конца.");
                return;
            }

            // Проверка актуальности дат
            if (startDate != null && startDate.isBefore(LocalDate.now())) {
                showErrorAlert("Дата начала не может быть раньше текущей.");
                return;
            }

            if (endDate != null && endDate.isBefore(LocalDate.now())) {
                showErrorAlert("Дата конца не может быть раньше текущей.");
                return;
            }

            // Закрытие текущего окна
            Stage currentStage = (Stage) transportTypeComboBox.getScene().getWindow();
            currentStage.close();

            // Загрузка окна с отфильтрованными маршрутами
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/frontendpassengertransportation/views/routes_with_filter.fxml"));
            Parent root = loader.load();

            // Передача данных в контроллер
            RoutesWithFilterController controller = loader.getController();
            controller.setToken(token);
            controller.setEmail(email);
            controller.setFilterData(transportType, departureCity, destinationCity, startDate, endDate);

            // Создание и отображение нового окна
            Scene scene = new Scene(root, 1040, 740);
            Stage newStage = new Stage();
            newStage.setTitle("Отфильтрованные маршруты");
            newStage.setScene(scene);
            newStage.show();
        } catch (IOException e) {
            // Отображение сообщения об ошибке при загрузке окна
            showErrorAlert("Не удалось открыть окно отфильтрованных маршрутов.");
        }
    }

    // Метод для обработки нажатия кнопки "Мои бронирования"
    @FXML
    private void handleShowPassengerBookingsButton() {
        try {
            // Проверка доступности сервера
            if (!isServerAvailable()) {
                showErrorAlert("Сервер недоступен. Пожалуйста, попробуйте позже.");
                return;
            }

            // Закрытие текущего окна
            Stage currentStage = (Stage) ShowPassengerBookingsButton.getScene().getWindow();
            currentStage.close();

            // Загрузка окна бронирований
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/frontendpassengertransportation/views/bookings_current_passenger.fxml"));
            Parent root = loader.load();

            // Передача токена и email в контроллер
            BookingsCurrentPassengerController controller = loader.getController();
            controller.setToken(token);
            controller.setEmail(email);

            // Создание и отображение нового окна
            Scene scene = new Scene(root, 1040, 740);
            Stage newStage = new Stage();
            newStage.setTitle("Список всех ваших бронирований");
            newStage.setScene(scene);
            newStage.show();
        } catch (IOException e) {
            // Отображение сообщения об ошибке при загрузке окна
            showErrorAlert("Не удалось открыть окно бронирований.");
        }
    }

    // Метод для обработки нажатия кнопки "Мои данные"
    @FXML
    private void handleAccountButton() {
        try {
            // Проверка доступности сервера
            if (!isServerAvailable()) {
                showErrorAlert("Сервер недоступен. Пожалуйста, попробуйте позже.");
                return;
            }

            // Загрузка окна данных пассажира
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/frontendpassengertransportation/views/passenger_data.fxml"));
            Parent root = loader.load();

            // Передача токена и email в контроллер
            PassengerDataController controller = loader.getController();
            controller.setToken(token);
            controller.setEmail(email);

            // Создание и настройка модального окна
            Scene scene = new Scene(root, 600, 375);
            Stage newStage = new Stage();
            newStage.setTitle("Данные пассажира");
            newStage.setScene(scene);
            newStage.initModality(Modality.APPLICATION_MODAL);
            newStage.initOwner(accountButton.getScene().getWindow());
            newStage.showAndWait();
        } catch (IOException e) {
            // Отображение сообщения об ошибке при загрузке окна
            showErrorAlert("Не удалось открыть окно данных пассажира.");
        }
    }

    // Метод для обработки нажатия кнопки "Назад"
    @FXML
    private void handleBackButton() {
        try {
            // Закрытие текущего окна
            Stage stage = (Stage) backButton.getScene().getWindow();
            stage.close();

            // Загрузка окна авторизации
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/frontendpassengertransportation/views/authorization.fxml"));
            Parent root = loader.load();

            // Создание и отображение окна авторизации
            Scene scene = new Scene(root, 640, 640);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            // Отображение сообщения об ошибке при загрузке окна
            showErrorAlert("Не удалось открыть окно авторизации.");
        }
    }

    // Метод для отображения диалогового окна с ошибкой
    private void showErrorAlert(String message) {
        // Создание и настройка диалогового окна с ошибкой
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Ошибка");
        alert.setHeaderText(null);
        alert.setContentText(message);
        // Отображение диалогового окна и ожидание закрытия
        alert.showAndWait();
    }
}