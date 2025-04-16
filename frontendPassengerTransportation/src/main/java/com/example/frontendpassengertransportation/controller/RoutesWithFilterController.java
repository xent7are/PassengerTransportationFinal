package com.example.frontendpassengertransportation.controller;

import com.example.frontendpassengertransportation.model.Route;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

// Класс управляет окном с отфильтрованными маршрутами
public class RoutesWithFilterController {

    // Элементы интерфейса
    @FXML
    private TableView<Route> routesTable; // Таблица для отображения списка маршрутов
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

    // Поля для хранения токена, почты и данных для фильтрации
    private String token; // Хранится JWT-токен для авторизации запросов к API
    private String email; // Хранится email пользователя для передачи в другие окна
    private String transportType; // Хранится тип транспорта для фильтрации
    private String departureCity; // Хранится город отправления для фильтрации
    private String destinationCity; // Хранится город назначения для фильтрации
    private String startDate; // Хранится начальная дата для фильтрации
    private String endDate; // Хранится конечная дата для фильтрации

    // Устанавливается токен для авторизации запросов
    public void setToken(String token) {
        this.token = token;
    }

    // Устанавливается email пользователя
    public void setEmail(String email) {
        this.email = email;
    }

    // Устанавливаются данные для фильтрации маршрутов
    public void setFilterData(String transportType, String departureCity, String destinationCity, LocalDate startDate, LocalDate endDate) {
        // Игнорируется тип транспорта, если выбрано "Микс"
        this.transportType = "Микс".equals(transportType) ? null : transportType;
        // Сохраняется город отправления
        this.departureCity = departureCity;
        // Сохраняется город назначения
        this.destinationCity = destinationCity;
        // Форматируется начальная дата, если указана
        this.startDate = startDate != null ? formatDate(startDate) : null;
        // Форматируется конечная дата, если указана
        this.endDate = endDate != null ? formatDate(endDate) : null;
        // Вызывается метод для загрузки отфильтрованных маршрутов
        loadFilteredRoutes();
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

    // Форматируется дата в строку для передачи на сервер
    private String formatDate(LocalDate date) {
        // Создание форматтера для дат в формате дд.мм.гггг
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        // Преобразование даты в строку
        return date.format(formatter);
    }

    // Загружаются и отображаются отфильтрованные маршруты
    private void loadFilteredRoutes() {
        // Хранятся маршруты по idRoute
        Map<String, Route> routeMap = new HashMap<>();
        // Считаются совпадения фильтров
        Map<String, Integer> filterCountMap = new HashMap<>();

        try {
            // Получаются все маршруты с сервера
            List<Route> allRoutes = fetchRoutes("http://localhost:8080/routes");
            // Заполняется карта маршрутов и счетчик фильтров
            for (Route route : allRoutes) {
                routeMap.put(route.getIdRoute(), route);
                filterCountMap.put(route.getIdRoute(), 0);
            }

            // Счетчик активных фильтров
            int totalFilters = 0;

            // Фильтр по типу транспорта
            if (transportType != null) {
                // Увеличение счетчика фильтров
                totalFilters++;
                // Формирование URL для запроса маршрутов по типу транспорта
                String url = "http://localhost:8080/routes/transport/" + URLEncoder.encode(transportType, StandardCharsets.UTF_8);
                // Получение маршрутов, соответствующих типу транспорта
                List<Route> transportRoutes = fetchRoutes(url);
                // Обновление счетчика совпадений для маршрутов
                for (Route route : transportRoutes) {
                    if (routeMap.containsKey(route.getIdRoute())) {
                        filterCountMap.put(route.getIdRoute(), filterCountMap.get(route.getIdRoute()) + 1);
                    }
                }
            }

            // Фильтр по городам отправления и назначения
            if (departureCity != null || destinationCity != null) {
                // Увеличение счетчика фильтров
                totalFilters++;
                if (departureCity != null && destinationCity != null) {
                    // Формирование URL для запроса маршрутов по городам отправления и назначения
                    String url = "http://localhost:8080/routes/points?departureCity=" + URLEncoder.encode(departureCity, StandardCharsets.UTF_8) +
                            "&destinationCity=" + URLEncoder.encode(destinationCity, StandardCharsets.UTF_8);
                    // Получение маршрутов, соответствующих городам
                    List<Route> cityRoutes = fetchRoutes(url);
                    // Обновление счетчика совпадений для маршрутов
                    for (Route route : cityRoutes) {
                        if (routeMap.containsKey(route.getIdRoute())) {
                            filterCountMap.put(route.getIdRoute(), filterCountMap.get(route.getIdRoute()) + 1);
                        }
                    }
                } else if (departureCity != null) {
                    // Формирование URL для запроса маршрутов по городу отправления
                    String url = "http://localhost:8080/routes/departure/" + URLEncoder.encode(departureCity, StandardCharsets.UTF_8);
                    // Получение маршрутов, соответствующих городу отправления
                    List<Route> departureRoutes = fetchRoutes(url);
                    // Обновление счетчика совпадений для маршрутов
                    for (Route route : departureRoutes) {
                        if (routeMap.containsKey(route.getIdRoute())) {
                            filterCountMap.put(route.getIdRoute(), filterCountMap.get(route.getIdRoute()) + 1);
                        }
                    }
                } else if (destinationCity != null) {
                    // Формирование URL для запроса маршрутов по городу назначения
                    String url = "http://localhost:8080/routes/destination/" + URLEncoder.encode(destinationCity, StandardCharsets.UTF_8);
                    // Получение маршрутов, соответствующих городу назначения
                    List<Route> destinationRoutes = fetchRoutes(url);
                    // Обновление счетчика совпадений для маршрутов
                    for (Route route : destinationRoutes) {
                        if (routeMap.containsKey(route.getIdRoute())) {
                            filterCountMap.put(route.getIdRoute(), filterCountMap.get(route.getIdRoute()) + 1);
                        }
                    }
                }
            }

            // Фильтр по датам отправления
            if (startDate != null) {
                // Увеличение счетчика фильтров
                totalFilters++;
                if (endDate == null) {
                    // Формирование URL для запроса маршрутов по точной дате
                    String url = "http://localhost:8080/routes/exactDate?exactDate=" + startDate;
                    // Получение маршрутов, соответствующих точной дате
                    List<Route> exactDateRoutes = fetchRoutes(url);
                    // Обновление счетчика совпадений для маршрутов
                    for (Route route : exactDateRoutes) {
                        if (routeMap.containsKey(route.getIdRoute())) {
                            filterCountMap.put(route.getIdRoute(), filterCountMap.get(route.getIdRoute()) + 1);
                        }
                    }
                } else {
                    // Формирование URL для запроса маршрутов по диапазону дат
                    String url = "http://localhost:8080/routes/dateRange?startDate=" + startDate + "&endDate=" + endDate;
                    // Получение маршрутов, соответствующих диапазону дат
                    List<Route> dateRangeRoutes = fetchRoutes(url);
                    // Обновление счетчика совпадений для маршрутов
                    for (Route route : dateRangeRoutes) {
                        if (routeMap.containsKey(route.getIdRoute())) {
                            filterCountMap.put(route.getIdRoute(), filterCountMap.get(route.getIdRoute()) + 1);
                        }
                    }
                }
            }

            // Отбираются маршруты, соответствующие всем фильтрам
            List<Route> filteredRoutes = new ArrayList<>();
            // Получение текущего времени для фильтрации
            LocalDateTime now = LocalDateTime.now();

            // Проверка маршрутов на соответствие всем фильтрам
            for (Map.Entry<String, Integer> entry : filterCountMap.entrySet()) {
                if (entry.getValue() == totalFilters) {
                    Route route = routeMap.get(entry.getKey());
                    // Получение времени отправления маршрута
                    LocalDateTime departureTime = route.getDepartureTimeAsLocalDateTime();
                    // Добавление маршрута, если время отправления позже текущего + 30 минут
                    if (departureTime != null && departureTime.isAfter(now.plusMinutes(30))) {
                        filteredRoutes.add(route);
                    }
                }
            }

            // Если фильтры не заданы, отображаются все маршруты с учетом времени
            if (totalFilters == 0) {
                // Фильтрация маршрутов по времени отправления (позже текущего + 30 минут)
                filteredRoutes = allRoutes.stream()
                        .filter(route -> route.getDepartureTimeAsLocalDateTime().isAfter(now.plusMinutes(30)))
                        .collect(Collectors.toList());
            }

            // Сортировка маршрутов по дате отправления
            filteredRoutes = sortRoutesByDepartureTime(filteredRoutes);

            // Отображение маршрутов в таблице
            displayRoutes(filteredRoutes);
        } catch (Exception e) {
            // Отображение сообщения об ошибке при загрузке маршрутов
            showErrorAlert("Произошла ошибка при загрузке маршрутов: " + e.getMessage());
        }
    }

    // Сортируются маршруты по дате отправления
    private List<Route> sortRoutesByDepartureTime(List<Route> routes) {
        // Создается новый список для сортировки, чтобы не изменять исходный
        List<Route> sortedRoutes = new ArrayList<>(routes);
        // Сортировка по полю departureTime с использованием LocalDateTime
        sortedRoutes.sort(Comparator.comparing(route -> route.getDepartureTimeAsLocalDateTime()));
        // Возврат отсортированного списка
        return sortedRoutes;
    }

    // Выполняется запрос к серверу для получения маршрутов
    private List<Route> fetchRoutes(String apiUrl) {
        try {
            // Создание URL для запроса
            URL url = new URL(apiUrl);
            // Создание HTTP-соединения
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            // Добавление заголовка авторизации с токеном
            connection.setRequestProperty("Authorization", "Bearer " + token);

            // Получение кода ответа сервера
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                // Чтение ответа от сервера
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();
                // Парсинг JSON в список маршрутов
                Gson gson = new Gson();
                List<Route> routes = gson.fromJson(response.toString(), new TypeToken<List<Route>>() {}.getType());
                // Возврат списка маршрутов или пустого списка, если данные отсутствуют
                return routes != null ? routes : Collections.emptyList();
            } else if (responseCode == HttpURLConnection.HTTP_NOT_FOUND || responseCode == HttpURLConnection.HTTP_NO_CONTENT) {
                // Возврат пустого списка при отсутствии данных
                return Collections.emptyList();
            } else {
                // Отображение сообщения об ошибке сервера
                showErrorAlert("Сервер вернул код ошибки: " + responseCode);
                // Возврат пустого списка при ошибке
                return Collections.emptyList();
            }
        } catch (IOException e) {
            // Отображение сообщения о невозможности подключения к серверу
            showErrorAlert("Не удалось подключиться к серверу: " + e.getMessage());
            // Возврат пустого списка при ошибке
            return Collections.emptyList();
        }
    }

    // Отображаются маршруты в таблице
    private void displayRoutes(List<Route> routes) {
        // Создание наблюдаемого списка маршрутов
        ObservableList<Route> observableRoutes = FXCollections.observableArrayList(routes);

        // Настройка колонок таблицы для отображения данных
        transportTypeColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getTransportType().getTransportType()));
        departureCityColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDepartureCity().getCityName()));
        destinationCityColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDestinationCity().getCityName()));
        departureTimeColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getFormattedDepartureTime()));
        arrivalTimeColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getFormattedArrivalTime()));
        availableSeatsColumn.setCellValueFactory(new PropertyValueFactory<>("numberAvailableSeats"));

        // Добавление кнопки "Забронировать" в таблицу
        addBookingButtonToTable();
        // Установка данных в таблицу
        routesTable.setItems(observableRoutes);

        // Настройка сообщения для пустой таблицы
        if (routes.isEmpty()) {
            // Создание метки для отображения при отсутствии маршрутов
            Label placeholderLabel = new Label("По данному фильтру не нашлось подходящих маршрутов");
            // Настройка стиля метки
            placeholderLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: gray;");
            // Выравнивание метки по центру
            placeholderLabel.setAlignment(Pos.CENTER);
            // Установка метки как заполнителя таблицы
            routesTable.setPlaceholder(placeholderLabel);
        }
    }

    // Добавляется кнопка "Забронировать" в таблицу
    private void addBookingButtonToTable() {
        // Создание ячейки для столбца "Бронирование"
        bookingColumn.setCellFactory(new Callback<>() {
            @Override
            public TableCell<Route, Void> call(final TableColumn<Route, Void> param) {
                return new TableCell<>() {
                    // Создание кнопки "Забронировать"
                    private final Button btn = new Button("Забронировать");
                    {
                        // Обработчик нажатия на кнопку
                        btn.setOnAction(event -> {
                            // Получение маршрута из текущей строки таблицы
                            Route route = getTableView().getItems().get(getIndex());
                            // Вызов метода для обработки бронирования
                            handleBookingButton(route);
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

    // Обрабатывается нажатие кнопки "Забронировать"
    private void handleBookingButton(Route route) {
        try {
            // Проверка доступности сервера
            if (!isServerAvailable()) {
                // Отображение сообщения об ошибке при недоступности сервера
                showErrorAlert("Сервер недоступен. Пожалуйста, попробуйте позже.");
                // Выход из метода
                return;
            }

            // Закрытие текущего окна
            Stage currentStage = (Stage) routesTable.getScene().getWindow();
            currentStage.close();

            // Загрузка окна бронирования
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/frontendpassengertransportation/views/booking_ticket.fxml"));
            Parent root = loader.load();

            // Получение контроллера окна бронирования
            BookingTicketController bookingController = loader.getController();
            // Передача данных о маршруте, токене и email
            bookingController.setRoute(route);
            bookingController.setToken(token);
            bookingController.setEmail(email);

            // Создание новой сцены
            Scene scene = new Scene(root, 1040, 740);
            // Создание нового окна
            Stage newStage = new Stage();
            newStage.setTitle("Бронирование билета");
            newStage.setScene(scene);
            // Отображение окна
            newStage.show();
        } catch (IOException e) {
            // Отображение сообщения об ошибке при загрузке окна
            showErrorAlert("Не удалось открыть окно бронирования.");
        }
    }

    // Обрабатывается нажатие кнопки "Назад" с передачей токена и email
    @FXML
    private void handleBackButton() {
        try {
            // Закрытие текущего окна
            Stage stage = (Stage) routesTable.getScene().getWindow();
            stage.close();

            // Загрузка главного окна
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
            // Установка сцены в текущее окно
            stage.setScene(scene);
            // Отображение окна
            stage.show();
        } catch (IOException e) {
            // Отображение сообщения об ошибке при загрузке окна
            showErrorAlert("Не удалось открыть главное окно.");
        }
    }

    // Отображается сообщение об ошибке
    private void showErrorAlert(String message) {
        // Создание диалогового окна ошибки
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Ошибка");
        alert.setHeaderText(null);
        alert.setContentText(message);
        // Отображение и ожидание закрытия окна
        alert.showAndWait();
    }
}