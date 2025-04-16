package com.example.frontendpassengertransportation.controller;

import com.example.frontendpassengertransportation.model.BookingTicket;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

// Класс ViewBookingDetailsController отвечает за управление окном просмотра деталей бронирования и отмены бронирования
public class ViewBookingDetailsController {

    // Элементы интерфейса
    @FXML
    private Label transportTypeLabel; // Поле для отображения типа транспорта
    @FXML
    private Label departureCityLabel; // Поле для отображения города отправления
    @FXML
    private Label destinationCityLabel; // Поле для отображения города назначения
    @FXML
    private Label departureTimeLabel; // Поле для отображения времени отправления
    @FXML
    private Label arrivalTimeLabel; // Поле для отображения времени прибытия
    @FXML
    private Button cancelBookingButton; // Кнопка для отмены бронирования
    @FXML
    private Button backButton; // Кнопка "Назад"

    private BookingTicket booking; // Хранение данных о бронировании
    private String token; // Поле для хранения токена
    private String email; // Поле для хранения email пользователя

    // Метод для установки данных о бронировании
    public void setBooking(BookingTicket booking) {
        this.booking = booking;
        // Заполнение полей данными из бронирования
        transportTypeLabel.setText(booking.getRoute().getTransportType().getTransportType());
        departureCityLabel.setText(booking.getRoute().getDepartureCity().getCityName());
        destinationCityLabel.setText(booking.getRoute().getDestinationCity().getCityName());
        departureTimeLabel.setText(booking.getRoute().getFormattedDepartureTime());
        arrivalTimeLabel.setText(booking.getRoute().getFormattedArrivalTime());
    }

    // Метод для установки токена
    public void setToken(String token) {
        this.token = token;
    }

    // Метод для установки email
    public void setEmail(String email) {
        this.email = email;
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

    @FXML
    // Обработчик нажатия на кнопку "Отменить бронирование"
    private void handleCancelBookingButton() {
        // Проверка доступности сервера
        if (!isServerAvailable()) {
            showErrorAlert("Сервер недоступен. Пожалуйста, попробуйте позже.");
            return; // Выход из метода
        }

        // Создание диалогового окна для подтверждения отмены бронирования
        Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmationAlert.setTitle("Подтверждение отмены бронирования");
        confirmationAlert.setHeaderText("Вы уверены, что хотите отменить бронирование?");
        confirmationAlert.setContentText("После подтверждения ваше бронирование будет безвозвратно отменено.");

        // Ожидание ответа пользователя
        confirmationAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) { // Если пользователь нажал "ОК"
                try {
                    // Вызов метода для удаления бронирования
                    deleteBooking(booking.getIdBooking());

                    // Закрытие текущего окна (view_booking_details)
                    Stage currentStage = (Stage) cancelBookingButton.getScene().getWindow();
                    currentStage.close();

                    // Обновление окна бронирований (bookings_current_passenger)
                    Stage bookingsStage = (Stage) currentStage.getOwner();
                    if (bookingsStage != null) {
                        // Загрузка окна бронирований заново
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/frontendpassengertransportation/views/bookings_current_passenger.fxml"));
                        Parent root = loader.load();

                        // Передача токена и email в BookingsCurrentPassengerController
                        BookingsCurrentPassengerController controller = loader.getController();
                        controller.setToken(token);
                        controller.setEmail(email);

                        // Обновление сцены
                        Scene scene = new Scene(root, 1040, 740);
                        bookingsStage.setScene(scene);
                        bookingsStage.setTitle("Список всех ваших бронирований");
                        bookingsStage.show();
                    }
                } catch (IOException e) {
                    showErrorAlert("Не удалось обновить список бронирований после отмены.");
                }
            }
        });
    }

    // Метод для удаления бронирования через API
    private void deleteBooking(String id) {
        try {
            // Формирование URL для удаления бронирования
            URL url = new URL("http://localhost:8080/booking-tickets/" + id);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            // Установка метода DELETE
            connection.setRequestMethod("DELETE");
            // Добавление заголовка авторизации с токеном
            connection.setRequestProperty("Authorization", "Bearer " + token);

            // Получение кода ответа сервера
            int responseCode = connection.getResponseCode();
            String responseMessage;

            // Чтение тела ответа для ошибок
            if (responseCode >= 400) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getErrorStream()))) {
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    responseMessage = response.toString();
                }
            } else {
                responseMessage = "Бронирование успешно отменено.";
            }

            if (responseCode == HttpURLConnection.HTTP_OK) {
                showInfoAlert(responseMessage);
            } else if (responseCode == HttpURLConnection.HTTP_NOT_FOUND) {
                showErrorAlert(responseMessage.isEmpty() ? "Бронирование не найдено." : responseMessage);
            } else if (responseCode == HttpURLConnection.HTTP_BAD_REQUEST) {
                showErrorAlert(responseMessage.isEmpty() ? "Не удалось отменить бронирование из-за некорректных данных." : responseMessage);
            } else if (responseCode == HttpURLConnection.HTTP_FORBIDDEN) {
                showErrorAlert(responseMessage.isEmpty() ? "Нет доступа для выполнения этой операции." : responseMessage);
            } else {
                showErrorAlert(responseMessage.isEmpty() ? "Не удалось отменить бронирование. Код ошибки: " + responseCode : responseMessage);
            }
            connection.disconnect(); // Закрытие соединения
        } catch (IOException e) {
            // Обработка исключения при недоступности сервера
            showErrorAlert("Не удалось подключиться к серверу для отмены бронирования.");
        }
    }

    @FXML
    // Обработка нажатия на кнопку "Назад"
    private void handleBackButton() {
        // Закрытие текущего окна
        Stage currentStage = (Stage) backButton.getScene().getWindow();
        currentStage.close();
    }

    // Метод для отображения диалогового окна с ошибкой
    private void showErrorAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Ошибка");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Метод для отображения информационного диалогового окна
    private void showInfoAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Информация");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}