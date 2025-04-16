package com.example.frontendpassengertransportation.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

// Класс RegistrationController отвечает за управление окном регистрации приложения
public class RegistrationController {

    // Элементы интерфейса
    @FXML
    private TextField fullNameField; // Поле для ввода ФИО
    @FXML
    private TextField phoneField; // Поле для ввода телефона
    @FXML
    private TextField emailField; // Поле для ввода email
    @FXML
    private DatePicker birthDatePicker; // Поле для выбора даты рождения
    @FXML
    private PasswordField passwordField; // Поле для ввода пароля
    @FXML
    private Button registerButton; // Кнопка "Зарегистрироваться"
    @FXML
    private Button backButton; // Кнопка "Назад"

    // Метод для инициализации контроллера после загрузки FXML
    @FXML
    public void initialize() {
        // Отключение фокуса на текстовых полях и элементах управления
        fullNameField.setFocusTraversable(false);
        phoneField.setFocusTraversable(false);
        emailField.setFocusTraversable(false);
        birthDatePicker.setFocusTraversable(false);
        passwordField.setFocusTraversable(false);
    }

    // Метод для обработки нажатия кнопки "Назад"
    @FXML
    private void handleBackButton() {
        // Закрытие текущего окна
        Stage currentStage = (Stage) backButton.getScene().getWindow();
        currentStage.close();

        try {
            // Загрузка FXML-файла для окна авторизации
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/frontendpassengertransportation/views/authorization.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root, 640, 640);
            Stage authStage = new Stage();
            authStage.setTitle("Авторизация");
            authStage.setScene(scene);
            // Отображение окна авторизации
            authStage.show();
        } catch (IOException e) {
            // Отображение сообщения об ошибке при неудачной загрузке окна
            showErrorAlert("Не удалось открыть окно авторизации.");
        }
    }

    // Метод для обработки нажатия кнопки "Зарегистрироваться"
    @FXML
    private void handleRegisterButton() {
        // Получение данных из текстовых полей
        String fullName = fullNameField.getText().trim();
        String phone = phoneField.getText().trim();
        String email = emailField.getText().trim();
        LocalDate birthDate = birthDatePicker.getValue();
        String password = passwordField.getText().trim();

        // Проверка заполненности всех полей
        if (fullName.isEmpty() || phone.isEmpty() || email.isEmpty() || birthDate == null || password.isEmpty()) {
            // Отображение сообщения об ошибке при пустых полях
            showErrorAlert("Все поля должны быть заполнены.");
            return;
        }

        // Форматирование даты рождения
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        String formattedBirthDate = birthDate.format(formatter);

        // Формирование строки данных для отправки на сервер
        String postData = "passengerFullName=" + encodeValue(fullName) +
                "&passengerPhone=" + encodeValue(phone) +
                "&passengerEmail=" + encodeValue(email) +
                "&dateOfBirth=" + encodeValue(formattedBirthDate) +
                "&password=" + encodeValue(password);

        try {
            // Создание подключения к серверу
            URL url = new URL("http://localhost:8080/auth/register");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setDoOutput(true);

            // Отправка данных на сервер
            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = postData.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            // Получение кода ответа от сервера
            int responseCode = conn.getResponseCode();
            StringBuilder response = new StringBuilder();

            // Чтение ответа сервера
            try (BufferedReader br = new BufferedReader(new InputStreamReader(
                    responseCode >= 400 ? conn.getErrorStream() : conn.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = br.readLine()) != null) {
                    response.append(line);
                }
            }

            // Закрытие соединения
            conn.disconnect();

            // Проверка успешного ответа (201)
            if (responseCode == 201) {
                // Отображение сообщения об успешной регистрации
                showInfoAlert("Вы успешно зарегистрировались!");

                // Закрытие текущего окна
                Stage currentStage = (Stage) registerButton.getScene().getWindow();
                currentStage.close();

                // Загрузка окна авторизации
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/frontendpassengertransportation/views/authorization.fxml"));
                Parent root = loader.load();
                Scene scene = new Scene(root, 640, 640);
                Stage authStage = new Stage();
                authStage.setTitle("Авторизация");
                authStage.setScene(scene);
                // Отображение окна авторизации
                authStage.show();
            } else if (responseCode == 400 || responseCode == 409) {
                // Отображение сообщения об ошибке с текстом от сервера
                showErrorAlert(response.toString());
            } else {
                // Отображение сообщения об ошибке с кодом ответа
                showErrorAlert("Произошла ошибка: " + response.toString());
            }
        } catch (IOException e) {
            // Отображение сообщения об ошибке при недоступности сервера
            showErrorAlert("Сервер недоступен. Пожалуйста, попробуйте позже.");
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

    // Метод для отображения диалогового окна с ошибкой
    private void showErrorAlert(String message) {
        // Создание и настройка диалогового окна с ошибкой
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Ошибка");
        alert.setHeaderText(null);
        alert.setContentText(message);
        // Установка размера диалогового окна
        alert.getDialogPane().setPrefSize(400, 180);
        // Отображение диалогового окна и ожидание закрытия
        alert.showAndWait();
    }

    // Метод для отображения диалогового окна с успешным сообщением
    private void showInfoAlert(String message) {
        // Создание и настройка диалогового окна с успешным сообщением
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Успех");
        alert.setHeaderText(null);
        alert.setContentText(message);
        // Установка размера диалогового окна
        alert.getDialogPane().setPrefSize(400, 180);
        // Отображение диалогового окна и ожидание закрытия
        alert.showAndWait();
    }
}