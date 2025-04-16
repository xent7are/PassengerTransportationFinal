package com.example.frontendpassengertransportation.controller;

import com.example.frontendpassengertransportation.model.BookingTicket;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.ResourceBundle;

// Класс BookingsCurrentPassengerController отвечает за управление окном списка бронирований текущего пассажира
public class BookingsCurrentPassengerController implements Initializable {

    private String token; // Поле для хранения токена
    private String email; // Поле для хранения email пользователя

    // Элементы интерфейса
    @FXML
    private TableView<BookingTicket> bookingsTable; // Таблица для отображения списка бронирований
    @FXML
    private TableColumn<BookingTicket, String> transportTypeColumn; // Колонка для типа транспорта
    @FXML
    private TableColumn<BookingTicket, String> departureCityColumn; // Колонка для города отправления
    @FXML
    private TableColumn<BookingTicket, String> destinationCityColumn; // Колонка для города назначения
    @FXML
    private TableColumn<BookingTicket, String> departureTimeColumn; // Колонка для времени отправления
    @FXML
    private TableColumn<BookingTicket, String> arrivalTimeColumn; // Колонка для времени прибытия
    @FXML
    private TableColumn<BookingTicket, Integer> availableSeatsColumn; // Колонка для количества доступных мест
    @FXML
    private TableColumn<BookingTicket, Void> showBookingColumn; // Колонка для кнопки "Посмотреть"
    @FXML
    private Button backButton; // Кнопка "Назад"

    // Метод для установки токена
    public void setToken(String token) {
        this.token = token;
        // Добавление кнопки "Посмотреть" в каждую строку
        addViewButtonToTable();
    }

    // Метод для установки email
    public void setEmail(String email) {
        this.email = email;
        // Загрузка данных о бронированиях из API после установки email
        loadBookingsByEmail();
    }

    @Override
    // Метод initialize вызывается при загрузке FXML
    public void initialize(URL location, ResourceBundle resources) {
        // Настройка связей колонок таблицы с полями модели BookingTicket
        // Извлекается строковое значение типа транспорта из объекта TransportType
        transportTypeColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getRoute().getTransportType().getTransportType()));

        // Извлекается название города отправления из объекта City
        departureCityColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getRoute().getDepartureCity().getCityName()));

        // Извлекается название города назначения из объекта City
        destinationCityColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getRoute().getDestinationCity().getCityName()));

        // Используется отформатированное время отправления
        departureTimeColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getRoute().getFormattedDepartureTime()));

        // Используется отформатированное время прибытия
        arrivalTimeColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getRoute().getFormattedArrivalTime()));

        // Устанавливается количество доступных мест
        availableSeatsColumn.setCellValueFactory(cellData ->
                new SimpleIntegerProperty(cellData.getValue().getRoute().getNumberAvailableSeats()).asObject());
    }

    // Метод для загрузки бронирований по email
    private void loadBookingsByEmail() {
        try {
            // Создается URL для запроса бронирований по email
            URL url = new URL("http://localhost:8080/booking-tickets/email/" + java.net.URLEncoder.encode(email, StandardCharsets.UTF_8.toString()));
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            // Добавляется заголовок авторизации с токеном
            connection.setRequestProperty("Authorization", "Bearer " + token);

            // Получение кода ответа от сервера
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                // Чтение ответа от сервера
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));
                String inputLine;
                StringBuilder content = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    content.append(inputLine);
                }
                in.close(); // Закрытие потока чтения

                // Преобразование JSON в список объектов BookingTicket
                Gson gson = new Gson();
                List<BookingTicket> bookings = gson.fromJson(content.toString(), new TypeToken<List<BookingTicket>>() {}.getType());

                // Установка данных в таблицу
                ObservableList<BookingTicket> bookingList = FXCollections.observableArrayList(bookings);
                bookingsTable.setItems(bookingList);

                // Настройка placeholder, если список пуст
                if (bookingList.isEmpty()) {
                    Label placeholderLabel = new Label("У вас пока нет бронирований.");
                    placeholderLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: gray;");
                    placeholderLabel.setAlignment(Pos.CENTER);
                    bookingsTable.setPlaceholder(placeholderLabel);
                }
            // Отображения сообщения в таблице, если бронирований нет
            } else if (responseCode == HttpURLConnection.HTTP_NOT_FOUND) {
                bookingsTable.setItems(FXCollections.observableArrayList());
                Label placeholderLabel = new Label("У вас пока нет бронирований.");
                placeholderLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: gray;");
                placeholderLabel.setAlignment(Pos.CENTER);
                bookingsTable.setPlaceholder(placeholderLabel);
            } else {
                // Сообщение об ошибке, если сервер вернул неожиданный код ответа
                showErrorAlert("Не удалось загрузить бронирования: код ошибки " + responseCode);
            }

            connection.disconnect(); // Соединение закрывается после завершения запроса

        } catch (IOException e) {
            // Обработка исключения при недоступности сервера
            showErrorAlert("Ошибка при загрузке бронирований. Сервер недоступен.");
        } catch (Exception e) {
            // Обработка непредвиденных ошибок
            showErrorAlert("Произошла непредвиденная ошибка при загрузке данных.");
        }
    }

    // Метод для добавления кнопки "Посмотреть" в таблицу
    private void addViewButtonToTable() {
        // Создание ячейки для столбца "Посмотреть"
        showBookingColumn.setCellFactory(new Callback<>() {
            @Override
            public TableCell<BookingTicket, Void> call(final TableColumn<BookingTicket, Void> param) {
                return new TableCell<>() {
                    // Создание кнопки "Посмотреть"
                    private final Button btn = new Button("Посмотреть");
                    {
                        // Обработчик нажатия на кнопку
                        btn.setOnAction(event -> {
                            BookingTicket booking = getTableView().getItems().get(getIndex());
                            handleViewButton(booking); // Вызов отдельного метода обработки
                        });
                        // Выравнивание кнопки по центру
                        setAlignment(Pos.CENTER);
                    }

                    @Override
                    // Метод для обновления ячейки
                    public void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        // Если строка пустая, скрыть кнопку
                        if (empty) {
                            setGraphic(null);
                        } else {
                            // Иначе отобразить кнопку
                            setGraphic(btn);
                        }
                    }
                };
            }
        });
    }

    // Метод для обработки нажатия кнопки "Посмотреть"
    private void handleViewButton(BookingTicket booking) {
        try {
            if (!isServerAvailable()) {
                showErrorAlert("Сервер недоступен. Пожалуйста, попробуйте позже.");
                return; // Выход из метода
            }

            // Загружается окно с деталями бронирования
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/frontendpassengertransportation/views/view_booking_details.fxml"));
            Parent root = loader.load();

            // Передаются данные о бронировании, токен и email в контроллер нового окна
            ViewBookingDetailsController detailsController = loader.getController();
            detailsController.setBooking(booking);
            detailsController.setToken(token);
            detailsController.setEmail(email);

            // Создается новая сцена
            Scene scene = new Scene(root, 1040, 740);
            Stage newStage = new Stage();
            newStage.setTitle("Просмотр забронированного билета");
            newStage.setScene(scene);
            newStage.initOwner(bookingsTable.getScene().getWindow());
            newStage.show();
        } catch (IOException e) {
            // Обработка ошибки при загрузке окна деталей
            showErrorAlert("Не удалось открыть окно с деталями бронирования.");
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

    @FXML
    // Обработка нажатия кнопки "Назад"
    private void handleBackButton() {
        try {
            // Закрытие текущего окна
            Stage currentStage = (Stage) backButton.getScene().getWindow();
            currentStage.close();

            // Возврат на главное окно
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/frontendpassengertransportation/views/main.fxml"));
            Parent root = loader.load();

            // Передача токена и email обратно в главное окно
            MainController mainController = loader.getController();
            mainController.setToken(token);
            mainController.setEmail(email);
            // Загрузка данных в ComboBox перед отображением окна
            mainController.loadComboBoxData();

            // Создание новой сцены
            Scene scene = new Scene(root, 1040, 740);
            Stage newStage = new Stage();
            newStage.setTitle("Бронирование билетов");
            newStage.setScene(scene);
            newStage.show();
        } catch (IOException e) {
            // Обработка ошибки при загрузке окна данных пассажира
            showErrorAlert("Не удалось вернуться в окно данных пассажира.");
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
        // Отображается диалоговое окно с сообщением об ошибке
        alert.showAndWait();
    }
}