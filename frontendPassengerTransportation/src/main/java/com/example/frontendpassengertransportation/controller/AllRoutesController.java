package com.example.frontendpassengertransportation.controller;

import com.example.frontendpassengertransportation.model.Route;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import javafx.util.Callback;
import javafx.beans.property.SimpleStringProperty;

// Управляется окно со списком всех доступных маршрутов, отображаемых в таблице с пагинацией
public class AllRoutesController implements Initializable {

    // Элементы интерфейса для отображения данных и управления навигацией
    @FXML
    private TableView<Route> routesTable; // Таблица для отображения маршрутов
    @FXML
    private TableColumn<Route, String> transportTypeColumn; // Колонка для отображения типа транспорта
    @FXML
    private TableColumn<Route, String> departureCityColumn; // Колонка для отображения города отправления
    @FXML
    private TableColumn<Route, String> destinationCityColumn; // Колонка для отображения города назначения
    @FXML
    private TableColumn<Route, String> departureTimeColumn; // Колонка для отображения времени отправления
    @FXML
    private TableColumn<Route, String> arrivalTimeColumn; // Колонка для отображения времени прибытия
    @FXML
    private TableColumn<Route, Integer> availableSeatsColumn; // Колонка для отображения количества доступных мест
    @FXML
    private TableColumn<Route, Void> bookingColumn; // Колонка с кнопкой "Забронировать" для каждого маршрута
    @FXML
    private Button backButton; // Кнопка для возврата на главное окно
    @FXML
    private Button prevPageButton; // Кнопка для перехода на предыдущую страницу
    @FXML
    private Button nextPageButton; // Кнопка для перехода на следующую страницу
    @FXML
    private Label pageLabel; // Надпись для отображения текущей страницы и общего количества страниц

    // Переменные для хранения данных и состояния пагинации
    private String token; // Хранится JWT-токен для авторизации запросов к API
    private String email; // Хранится email пользователя для передачи в другие окна
    private int currentPage = 0; // Хранится номер текущей страницы (начинается с 0)
    private int totalPages = 0; // Хранится общее количество страниц, полученное от API

    // Метод для установки JWT-токена и выполнения начальной загрузки данных
    public void setToken(String token) {
        this.token = token;
    }

    // Метод для установки email пользователя
    public void setEmail(String email) {
        this.email = email;
    }

    // Метод для загрузки маршрутов и настройки таблицы
    public void loadInitialRoutes() {
        // Добавление кнопки "Забронировать" в таблицу
        addBookingButtonToTable();
        // Загрузка маршрутов для первой страницы
        loadRoutes(0);
    }

    // Метод для инициализации контроллера после загрузки FXML-файла
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Настраиваются колонки таблицы для отображения данных из объекта Route
        // Для вложенных объектов (TransportType, City) извлекаются строковые значения
        transportTypeColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getTransportType().getTransportType()));
        departureCityColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getDepartureCity().getCityName()));
        destinationCityColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getDestinationCity().getCityName()));
        // Для времени отправления и прибытия используются отформатированные строки
        departureTimeColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getFormattedDepartureTime()));
        arrivalTimeColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getFormattedArrivalTime()));
        // Для количества доступных мест используется прямое значение из объекта Route
        availableSeatsColumn.setCellValueFactory(new PropertyValueFactory<>("numberAvailableSeats"));
    }

    // Метод для настройки колонки с кнопкой "Забронировать"
    private void addBookingButtonToTable() {
        // Создается фабрика ячеек для колонки bookingColumn
        bookingColumn.setCellFactory(new Callback<>() {
            @Override
            public TableCell<Route, Void> call(final TableColumn<Route, Void> param) {
                return new TableCell<>() {
                    // Создается кнопка "Забронировать" для каждой строки
                    private final Button btn = new Button("Забронировать");
                    {
                        // Назначается обработчик события для кнопки
                        btn.setOnAction(event -> {
                            // Получение маршрута из текущей строки таблицы
                            Route route = getTableView().getItems().get(getIndex());
                            // Вызывается метод для обработки бронирования
                            handleBookingButton(route);
                        });
                        // Выравнивание кнопки по центру
                        setAlignment(Pos.CENTER);
                    }

                    @Override
                    // Обновляется содержимое ячейки при изменении данных
                    public void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        // Если строка пустая, кнопка скрывается
                        if (empty) {
                            setGraphic(null);
                        } else {
                            // Иначе отображается кнопка "Забронировать"
                            setGraphic(btn);
                        }
                    }
                };
            }
        });
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

    // Метод для обработки нажатия кнопки "Забронировать"
    private void handleBookingButton(Route route) {
        try {
            if (!isServerAvailable()) {
                showErrorAlert("Сервер недоступен. Пожалуйста, попробуйте позже.");
                return; // Выход из метода
            }

            // Закрывается текущее окно
            Stage currentStage = (Stage) backButton.getScene().getWindow();
            currentStage.close();

            // Загружается FXML-файл для окна бронирования
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/frontendpassengertransportation/views/booking_ticket.fxml"));
            Parent root = loader.load();
            // Получение контроллера окна бронирования
            BookingTicketController controller = loader.getController();
            // Передаются данные о маршруте, токене и email в контроллер
            controller.setRoute(route);
            controller.setToken(token);
            controller.setEmail(email);
            // Создается новая сцена с заданными размерами
            Scene scene = new Scene(root, 1040, 740);
            // Создается новое окно для бронирования
            Stage stage = new Stage();
            stage.setTitle("Бронирование билета");
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            // Отображается сообщение об ошибке, если окно бронирования не удалось открыть
            showErrorAlert("Не удалось открыть окно бронирования");
        }
    }

    // Метод для выполнения HTTP-запроса к API для получения данных о маршрутах
    private Map<String, Object> fetchRoutesPage(int page) throws IOException {
        // Формируется время, отстоящее на 30 минут от текущего, для фильтрации маршрутов
        LocalDateTime nowPlus30 = LocalDateTime.now().plusMinutes(30);
        String minDepartureTime = nowPlus30.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        // Формируется URL для запроса с параметрами страницы, размера (16 маршрутов) и минимального времени отправления
        URL url = URI.create("http://localhost:8080/routes/paginated?page=" + page + "&size=16&minDepartureTime=" + minDepartureTime).toURL();
        // Создается HTTP-соединение для выполнения GET-запроса
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        // Добавляется заголовок авторизации с JWT-токеном
        connection.setRequestProperty("Authorization", "Bearer " + token);

        // Получение кода ответа сервера
        int responseCode = connection.getResponseCode();
        String responseMessage;

        // Чтение тела ответа
        if (responseCode >= 400) {
            // Чтение сообщения об ошибке из error stream
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getErrorStream()))) {
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                responseMessage = response.toString();
            }
        } else {
            // Чтение ответа для успешного запроса
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                responseMessage = response.toString();
            }
        }

        connection.disconnect();

        if (responseCode == HttpURLConnection.HTTP_OK) {
            // Данные ответа преобразуются из JSON в Map с помощью Gson
            Gson gson = new Gson();
            java.lang.reflect.Type mapType = new TypeToken<Map<String, Object>>() {}.getType();
            return gson.fromJson(responseMessage, mapType);
        } else if (responseCode == HttpURLConnection.HTTP_NOT_FOUND) {
            throw new IOException(responseMessage.isEmpty() ? "Маршруты не найдены." : responseMessage);
        } else if (responseCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
            throw new IOException(responseMessage.isEmpty() ? "Недействительный или отсутствующий токен." : responseMessage);
        } else if (responseCode == HttpURLConnection.HTTP_FORBIDDEN) {
            throw new IOException(responseMessage.isEmpty() ? "Нет доступа для выполнения этой операции." : responseMessage);
        } else {
            throw new IOException(responseMessage.isEmpty() ? "Не удалось загрузить маршруты: код ошибки " + responseCode : responseMessage);
        }
    }

    // Метод для загрузки маршрутов с указанной страницы и обновления таблицы
    private void loadRoutes(int page) {
        try {
            // Выполняется запрос к API для получения данных о маршрутах
            Map<String, Object> responseMap = fetchRoutesPage(page);
            Gson gson = new Gson();
            // Извлекается список маршрутов из ответа и преобразуется в List<Route>
            List<Route> routes = gson.fromJson(gson.toJson(responseMap.get("content")), new TypeToken<List<Route>>() {}.getType());
            // Обновляется текущая страница и общее количество страниц на основе ответа API
            currentPage = ((Double) responseMap.get("number")).intValue();
            totalPages = ((Double) responseMap.get("totalPages")).intValue();

            // Создается ObservableList для отображения маршрутов в таблице
            ObservableList<Route> routeList = FXCollections.observableArrayList(routes);
            // Данные загружаются в таблицу
            routesTable.setItems(routeList);

            // Обновляется состояние кнопок пагинации и надпись страницы
            updatePaginationButtons();
        } catch (IOException e) {
            // Отображается сообщение об ошибке, переданное из fetchRoutesPage
            showErrorAlert(e.getMessage());
        } catch (Exception e) {
            // Отображается сообщение об ошибке при непредвиденных проблемах
            showErrorAlert("Произошла непредвиденная ошибка при загрузке данных");
        }
    }

    // Метод для обновления видимости кнопок пагинации и текста надписи страницы
    private void updatePaginationButtons() {
        // Кнопка "Предыдущая страница" отображается, если текущая страница не первая
        prevPageButton.setVisible(currentPage > 0);
        // Кнопка "Следующая страница" отображается, если текущая страница не последняя
        nextPageButton.setVisible(currentPage < totalPages - 1);
        // Обновляется текст надписи страницы
        pageLabel.setText(String.format("Страница %d/%d", currentPage + 1, totalPages));
    }

    // Метод для обработки нажатия кнопки "Предыдущая страница"
    @FXML
    private void handlePrevPage() {
        if (!isServerAvailable()) {
            showErrorAlert("Сервер недоступен. Пожалуйста, попробуйте позже.");
            return; // Выход из метода
        }

        // Если текущая страница не первая, загружается предыдущая страница
        if (currentPage > 0) {
            loadRoutes(currentPage - 1);
        }
    }

    // Метод для обработки нажатия кнопки "Следующая страница"
    @FXML
    private void handleNextPage() {
        if (!isServerAvailable()) {
            showErrorAlert("Сервер недоступен. Пожалуйста, попробуйте позже.");
            return; // Выход из метода
        }

        // Если текущая страница не последняя, загружается следующая страница
        if (currentPage < totalPages - 1) {
            loadRoutes(currentPage + 1);
        }
    }

    // Метод для обработки нажатия кнопки "Назад"
    @FXML
    private void handleBackButton() {
        try {
            // Закрывается текущее окно
            Stage currentStage = (Stage) backButton.getScene().getWindow();
            currentStage.close();

            // Загружается FXML-файл главного окна
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/frontendpassengertransportation/views/main.fxml"));
            Parent root = loader.load();

            // Получение контроллера главного окна
            MainController mainController = loader.getController();
            // Передаются токен и email в контроллер главного окна
            mainController.setToken(token);
            mainController.setEmail(email);
            // Загрузка данных в ComboBox перед отображением окна
            mainController.loadComboBoxData();

            // Создается новая сцена с заданными размерами
            Scene scene = new Scene(root, 1040, 740);

            // Создается новое окно для главного меню
            Stage newStage = new Stage();
            newStage.setTitle("Бронирование билетов на транспорт");
            newStage.setScene(scene);
            newStage.show();
        } catch (IOException e) {
            // Отображается сообщение об ошибке, если главное окно не удалось открыть
            showErrorAlert("Не удалось открыть главное окно");
        }
    }

    // Метод для отображения диалогового окна с сообщением об ошибке
    private void showErrorAlert(String message) {
        // Создается Alert типа ERROR
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Ошибка");
        alert.setHeaderText(null);
        alert.setContentText(message);
        // Устанавливаются размеры диалогового окна
        alert.getDialogPane().setPrefSize(400, 180);
        // Отображается окно и ожидается его закрытие
        alert.showAndWait();
    }
}