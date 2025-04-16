package com.example.frontendpassengertransportation.controller;

import com.example.frontendpassengertransportation.model.User;
import com.google.gson.Gson;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

// Класс PassengerDataController отвечает за управление окном данных пассажира
public class PassengerDataController {

    private String token; // Поле для хранения токена
    private String email; // Поле для хранения email пользователя

    // Элементы управления из FXML
    @FXML
    private TextField passengerFullNameField; // Поле для ФИО пассажира
    @FXML
    private TextField passengerPhoneField; // Поле для номера телефона
    @FXML
    private TextField passengerEmailField; // Поле для электронной почты
    @FXML
    private Button backButton; // Кнопка "Назад"

    // Метод для установки токена
    public void setToken(String token) {
        this.token = token;
    }

    // Метод для установки email
    public void setEmail(String email) {
        this.email = email;
        // Загрузка данных пользователя после установки email
        loadUserDataByEmail();
    }

    // Метод initialize вызывается при загрузке FXML
    @FXML
    public void initialize() {
        // Отключение фокуса на текстовых полях
        passengerFullNameField.setFocusTraversable(false);
        passengerPhoneField.setFocusTraversable(false);
        passengerEmailField.setFocusTraversable(false);
    }

    // Метод для загрузки данных пользователя по email
    private void loadUserDataByEmail() {
        try {
            // Создание URL для запроса пользователя по email
            URL url = new URL("http://localhost:8080/users/user-by-email?email=" + java.net.URLEncoder.encode(email, StandardCharsets.UTF_8.toString()));
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            // Добавление заголовка авторизации с токеном
            connection.setRequestProperty("Authorization", "Bearer " + token);

            // Получение кода ответа от сервера
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) { // 200 OK
                // Чтение ответа от сервера
                try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                    String response = br.lines().collect(java.util.stream.Collectors.joining());
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
            // Обработка исключения при недоступности сервера или ошибке ввода-вывода
            showErrorAlert("Ошибка при загрузке данных пользователя. Сервер недоступен.");
        }
    }

    // Обработчик кнопки "Назад"
    @FXML
    private void handleBackButton() {
        try {
            // Закрытие текущего окна
            Stage currentStage = (Stage) backButton.getScene().getWindow();
            currentStage.close();
        } catch (Exception e) {
            // Обработка ошибки при закрытии окна
            showErrorAlert("Не удалось закрыть окно.");
        }
    }

    // Метод для отображения ошибки с помощью Alert
    private void showErrorAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Ошибка");
        alert.setHeaderText(null);
        alert.setContentText(message);
        // Отображение диалогового окна и ожидание закрытия
        alert.showAndWait();
    }
}