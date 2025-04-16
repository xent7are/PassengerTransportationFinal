package com.example.frontendpassengertransportation.controller;

import com.example.frontendpassengertransportation.model.Route;
import com.example.frontendpassengertransportation.model.User;
import com.google.gson.Gson;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

// Класс BookingTicketController отвечает за управление окном бронирования билетов
public class BookingTicketController {

    // Элементы интерфейса
    @FXML
    private Label transportTypeLabel; // Label для отображения типа транспорта
    @FXML
    private Label departureCityLabel; // Label для отображения города отправления
    @FXML
    private Label destinationCityLabel; // Label для отображения города назначения
    @FXML
    private Label departureTimeLabel; // Label для отображения времени отправления
    @FXML
    private Label arrivalTimeLabel; // Label для отображения времени прибытия
    @FXML
    private TextField passengerFullNameField; // Поле для ввода имени пассажира
    @FXML
    private TextField passengerPhoneField; // Поле для ввода телефона пассажира
    @FXML
    private TextField passengerEmailField; // Поле для ввода email пассажира
    @FXML
    private Button bookButton; // Кнопка для бронирования
    @FXML
    private Button backButton; // Кнопка для возврата на предыдущее окно

    private Route route; // Объект маршрута, переданный из предыдущего окна
    private String token; // Поле для хранения JWT-токена
    private String email; // Поле для хранения email пользователя

    @FXML
    // Метод initialize вызывается при загрузке FXML
    public void initialize() {
        // Отключение фокуса на текстовых полях
        passengerFullNameField.setFocusTraversable(false);
        passengerPhoneField.setFocusTraversable(false);
        passengerEmailField.setFocusTraversable(false);
    }

    // Метод для установки маршрута и обновления интерфейса
    public void setRoute(Route route) {
        this.route = route;
        // Обновление интерфейса с данными маршрута
        updateUI();
    }

    // Метод для установки токена, переданного из AllRoutesController
    public void setToken(String token) {
        this.token = token;
    }

    // Метод для установки email и загрузки данных пользователя
    public void setEmail(String email) {
        this.email = email;
        // Загрузка данных пользователя по email
        loadUserDataByEmail();
    }

    // Метод для обновления интерфейса с данными маршрута
    private void updateUI() {
        if (route != null) {
            // Установка данных маршрута в соответствующие Label с использованием геттеров
            transportTypeLabel.setText(route.getTransportType().getTransportType());
            departureCityLabel.setText(route.getDepartureCity().getCityName());
            destinationCityLabel.setText(route.getDestinationCity().getCityName());
            departureTimeLabel.setText(route.getFormattedDepartureTime());
            arrivalTimeLabel.setText(route.getFormattedArrivalTime());
        }
    }

    // Метод для загрузки данных пользователя по email
    private void loadUserDataByEmail() {
        try {
            // Создание URL для запроса пользователя по email
            URL url = new URL("http://localhost:8080/users/user-by-email?email=" + encodeValue(email));
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            // Добавление заголовка авторизации с токеном
            connection.setRequestProperty("Authorization", "Bearer " + token);

            // Получение кода ответа от сервера
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) { // 200 OK
                // Чтение ответа от сервера
                try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                    String response = br.lines().collect(Collectors.joining());
                    Gson gson = new Gson();
                    User user = gson.fromJson(response, User.class);

                    // Заполнение полей данными пользователя
                    passengerFullNameField.setText(user.getPassengerFullName());
                    passengerPhoneField.setText(user.getPassengerPhone());
                    passengerEmailField.setText(user.getPassengerEmail());
                }
            } else if (responseCode == HttpURLConnection.HTTP_NOT_FOUND) { // 404
                showErrorAlert("Пользователь с email " + email + " не найден.");
            } else {
                showErrorAlert("Не удалось загрузить данные пользователя: код ошибки " + responseCode);
            }
            connection.disconnect(); // Закрытие соединения
        } catch (IOException e) {
            showErrorAlert("Ошибка при загрузке данных пользователя. Сервер недоступен.");
        }
    }

    // Метод для обработки нажатия кнопки "Забронировать"
    @FXML
    private void handleBookButton() {
        // Получение данных из полей ввода
        String passengerName = passengerFullNameField.getText().trim();
        String passengerPhone = passengerPhoneField.getText().trim();
        String passengerEmail = passengerEmailField.getText().trim();

        try {
            // Формирование параметров запроса с использованием encodeValue
            String postData = "routeId=" + encodeValue(route.getIdRoute()) +
                    "&passengerFullName=" + encodeValue(passengerName) +
                    "&passengerPhone=" + encodeValue(passengerPhone) +
                    "&passengerEmail=" + encodeValue(passengerEmail);

            // Вызов метода для отправки запроса на создание бронирования
            createBookingTicket(postData);
        } catch (Exception e) {
            showErrorAlert("Произошла ошибка при отправке данных на сервер.");
        }
    }

    // Метод для проверки доступности сервера
    private boolean isServerAvailable() {
        try {
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
            // Если сервер недоступен
            return false;
        }
    }

    // Метод для отправки запроса на создание бронирования
    private void createBookingTicket(String postData) {
        try {
            if (!isServerAvailable()) {
                showErrorAlert("Сервер недоступен. Пожалуйста, попробуйте позже.");
                return; // Выход из метода
            }

            // Создание URL для отправки запроса на бэкэнд
            URL url = new URL("http://localhost:8080/booking-tickets");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            // Добавление заголовка авторизации с токеном
            connection.setRequestProperty("Authorization", "Bearer " + token);
            // Установка заголовка Content-Type
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            // Разрешение отправки данных
            connection.setDoOutput(true);

            // Отправка данных на бэкэнд
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = postData.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            // Получение кода ответа от сервера
            int responseCode = connection.getResponseCode();
            String responseMessage;

            // Чтение тела ответа
            if (responseCode >= 400) {
                // Чтение сообщения об ошибке из error stream
                try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getErrorStream(), StandardCharsets.UTF_8))) {
                    responseMessage = br.lines().collect(Collectors.joining());
                }
            } else {
                // Чтение ответа для успешного создания
                try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                    responseMessage = br.lines().collect(Collectors.joining());
                }
            }

            if (responseCode == HttpURLConnection.HTTP_CREATED) {
                // Успешное создание бронирования
                showSuccessAlert("Бронирование успешно создано!");
                // Закрытие текущего окна
                Stage stage = (Stage) bookButton.getScene().getWindow();
                stage.close();
                // Открытие главного окна (main.fxml)
                openMainWindow();
            } else if (responseCode == HttpURLConnection.HTTP_BAD_REQUEST) {
                showErrorAlert(responseMessage.isEmpty() ? "Не удалось создать бронирование: некорректные данные." : responseMessage);
            } else if (responseCode == HttpURLConnection.HTTP_NOT_FOUND) {
                showErrorAlert(responseMessage.isEmpty() ? "Маршрут или пользователь не найдены." : responseMessage);
            } else if (responseCode == HttpURLConnection.HTTP_FORBIDDEN) {
                showErrorAlert(responseMessage.isEmpty() ? "Нет доступа для выполнения этой операции." : responseMessage);
            } else {
                showErrorAlert(responseMessage.isEmpty() ? "Не удалось создать бронирование: код ошибки " + responseCode : responseMessage);
            }
            connection.disconnect(); // Закрытие соединения
        } catch (IOException e) {
            showErrorAlert("Произошла ошибка при отправке данных на сервер.");
        }
    }

    // Метод для открытия главного окна
    private void openMainWindow() {
        try {
            // Загрузка главного окна (main.fxml)
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/frontendpassengertransportation/views/main.fxml"));
            Parent root = loader.load();

            // Передача токена и email в MainController
            MainController mainController = loader.getController();
            mainController.setToken(token);
            mainController.setEmail(email);
            // Загрузка данных в ComboBox перед отображением окна
            mainController.loadComboBoxData();

            // Создание новой сцены
            Scene scene = new Scene(root, 1040, 740);

            // Создание нового окна
            Stage newStage = new Stage();
            newStage.setTitle("Бронирование билетов на транспорт");
            newStage.setScene(scene);
            newStage.show();
        } catch (IOException e) {
            showErrorAlert("Не удалось открыть главное окно.");
        }
    }

    // Метод для обработки нажатия кнопки "Назад"
    @FXML
    private void handleBackButton() {
        try {
            // Закрытие текущего окна
            Stage currentStage = (Stage) backButton.getScene().getWindow();
            currentStage.close();

            // Загрузка главного окна (main.fxml)
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/frontendpassengertransportation/views/main.fxml"));
            Parent root = loader.load();

            // Передача токена и email в MainController
            MainController mainController = loader.getController();
            mainController.setToken(token);
            mainController.setEmail(email);
            // Загрузка данных в ComboBox перед отображением окна
            mainController.loadComboBoxData();

            // Создание новой сцены
            Scene scene = new Scene(root, 1040, 740);

            // Создание нового окна
            Stage newStage = new Stage();
            newStage.setTitle("Список всех маршрутов");
            newStage.setScene(scene);
            newStage.show();
        } catch (IOException e) {
            showErrorAlert("Не удалось открыть главное окно.");
        }
    }

    // Метод для кодирования значений в URL-формат
    private String encodeValue(String value) {
        try {
            // Кодирование строки в UTF-8
            return java.net.URLEncoder.encode(value, StandardCharsets.UTF_8.toString());
        } catch (Exception e) {
            // Возврат исходной строки при ошибке кодирования
            return value;
        }
    }

    // Метод для отображения Alert с ошибкой
    private void showErrorAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Ошибка");
        alert.setHeaderText(null);
        alert.setContentText(message);
        // Установка размера Alert
        alert.getDialogPane().setPrefSize(400, 180);
        alert.showAndWait();
    }

    // Метод для отображения Alert с успехом
    private void showSuccessAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Успех");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}