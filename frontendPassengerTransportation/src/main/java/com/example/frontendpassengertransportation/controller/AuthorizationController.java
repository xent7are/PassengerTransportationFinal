package com.example.frontendpassengertransportation.controller;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;

// Класс AuthorizationController отвечает за управление окном авторизации приложения
public class AuthorizationController {

    // Элементы интерфейса
    @FXML
    private TextField fullNameField; // Поле для ввода ФИО
    @FXML
    private TextField emailField; // Поле для ввода email
    @FXML
    private PasswordField passwordField; // Поле для ввода пароля
    @FXML
    private Button registerButton; // Кнопка "Регистрация"
    @FXML
    private Button loginButton; // Кнопка "Войти"

    // Метод initialize вызывается при загрузке FXML
    @FXML
    public void initialize() {
        // Отключение фокуса на текстовых полях
        fullNameField.setFocusTraversable(false);
        emailField.setFocusTraversable(false);
        passwordField.setFocusTraversable(false);
    }

    // Метод для обработки нажатия кнопки "Регистрация"
    @FXML
    private void handleRegisterButton() {
        try {
            // Закрытие текущего окна
            Stage currentStage = (Stage) registerButton.getScene().getWindow();
            currentStage.close();

            // Загрузка FXML-файла для окна регистрации
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/frontendpassengertransportation/views/registration.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root, 640, 640);
            Stage regStage = new Stage();
            regStage.setTitle("Регистрация");
            regStage.setScene(scene);
            // Отображение окна регистрации
            regStage.show();
        } catch (IOException e) {
            // Отображение сообщения об ошибке при неудачной загрузке окна
            showErrorAlert("Не удалось открыть окно регистрации.");
        }
    }

    // Метод для обработки нажатия кнопки "Войти"
    @FXML
    private void handleLoginButton() {
        // Получение данных из текстовых полей
        String fullName = fullNameField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText().trim();

        // Проверка заполненности всех полей
        if (fullName.isEmpty() || email.isEmpty() || password.isEmpty()) {
            // Отображение сообщения об ошибке при пустых полях
            showErrorAlert("Все поля должны быть заполнены.");
            return;
        }

        // Формирование строки данных для отправки на сервер
        String postData = "passengerFullName=" + encodeValue(fullName) +
                "&passengerEmail=" + encodeValue(email) +
                "&password=" + encodeValue(password);

        try {
            // Создание подключения к серверу
            URL url = new URL("http://localhost:8080/auth/login");
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

            // Проверка успешного ответа (200)
            if (responseCode == 200) {
                // Парсинг JSON-ответа с помощью Gson
                Gson gson = new Gson();
                java.util.Map<String, String> responseMap = gson.fromJson(response.toString(), new TypeToken<Map<String, String>>() {}.getType());
                String jwtToken = responseMap.get("token");

                // Проверка наличия токена в ответе
                if (jwtToken == null) {
                    // Отображение сообщения об ошибке при отсутствии токена
                    showErrorAlert("Токен не найден в ответе сервера.");
                    return;
                }

                // Отображение сообщения об успешной авторизации
                showInfoAlert("Авторизация прошла успешно!");

                // Закрытие текущего окна
                Stage currentStage = (Stage) loginButton.getScene().getWindow();
                currentStage.close();

                // Загрузка главного окна приложения
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/frontendpassengertransportation/views/main.fxml"));
                Parent root = loader.load();
                MainController mainController = loader.getController();
                // Передача токена и email в контроллер главного окна
                mainController.setToken(jwtToken);
                mainController.setEmail(email);
                // Загрузка данных в ComboBox перед отображением окна
                mainController.loadComboBoxData();

                // Создание и отображение главного окна
                Scene scene = new Scene(root, 1040, 740);
                Stage mainStage = new Stage();
                mainStage.setTitle("Бронирование билетов");
                mainStage.setScene(scene);
                mainStage.show();
            } else if (responseCode == 401 || responseCode == 403) {
                // Отображение сообщения об ошибке при неверных учетных данных
                showErrorAlert("Неверные учетные данные.");
            } else if (responseCode == 400) {
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