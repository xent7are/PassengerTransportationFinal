package com.example.backendpassengertransportation.service;

import com.example.backendpassengertransportation.model.User;
import com.example.backendpassengertransportation.repository.UserRepository;
import com.example.backendpassengertransportation.util.ValidationUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;

// Сервис для управления пользователями, реализует аутентификацию и хеширование паролей с использованием BCrypt
@Service
public class UserService implements UserDetailsService {

    // Репозиторий для работы с данными пользователей
    @Autowired
    private UserRepository userRepository;

    // Компонент для хеширования паролей с использованием BCrypt
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    // Загрузка пользователя по электронной почте для аутентификации
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByPassengerEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь с email " + email + " не найден."));
        return new org.springframework.security.core.userdetails.User(
                user.getPassengerEmail(), user.getPassword(), new ArrayList<>());
    }

    // Метод для загрузки пользователя по email
    public UserDetails loadUserByEmail(String email) {
        return loadUserByUsername(email);
    }

    // Поиск пользователя по ФИО и email
    public User getUserByFullNameAndEmail(String passengerFullName, String passengerEmail) {
        return userRepository.findByPassengerFullNameAndPassengerEmail(passengerFullName, passengerEmail)
                .orElseThrow(() -> new IllegalArgumentException("Неверные учетные данные."));
    }

    // Получение всех пользователей
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // Получение пользователя по ID
    public User getUserById(String idUser) {
        return userRepository.findById(idUser).orElse(null);
    }

    // Получение пользователя по email
    public User getUserByEmail(String email) {
        return userRepository.findByPassengerEmail(email)
                .orElseThrow(() -> new NoSuchElementException("Пользователь с email " + email + " не найден."));
    }

    // Создание нового пользователя с хешированием пароля через BCrypt
    public User createUser(String passengerFullName, String passengerPhone, String passengerEmail,
                           Date dateOfBirth, String password) {
        // Проверка на пустые значения
        if (passengerFullName == null || passengerFullName.isEmpty() ||
                passengerPhone == null || passengerPhone.isEmpty() ||
                passengerEmail == null || passengerEmail.isEmpty() ||
                dateOfBirth == null || password == null || password.isEmpty()) {
            throw new IllegalArgumentException("Все поля должны быть заполнены.");
        }

        // Проверка формата телефона
        if (!ValidationUtil.isValidPhoneFormat(passengerPhone)) {
            throw new IllegalArgumentException("Неверный формат телефона. Используйте формат: +7 XXX XXX-XX-XX");
        }

        // Проверка формата электронной почты
        if (!ValidationUtil.isValidEmailFormat(passengerEmail)) {
            throw new IllegalArgumentException("Неверный формат электронной почты. " +
                    "Используйте формат: имя_пользователя@домен. " +
                    "Допустимые домены: mail.ru, inbox.ru, yandex.ru, gmail.com.");
        }

        // Проверка возраста (для регистрации или создания пользователя)
        ValidationUtil.validateAge(dateOfBirth);

        // Проверка уникальности номера телефона
        if (userRepository.findByPassengerPhone(passengerPhone).isPresent()) {
            throw new IllegalArgumentException("Пользователь с таким номером телефона уже существует.");
        }

        // Проверка уникальности email
        if (userRepository.findByPassengerEmail(passengerEmail).isPresent()) {
            throw new IllegalArgumentException("Пользователь с таким email уже существует.");
        }

        // Хеширование пароля с использованием BCrypt
        String hashedPassword = passwordEncoder.encode(password);

        String newIdUser = generateNewId("u");
        return userRepository.save(
                new User(newIdUser, passengerFullName, passengerPhone,
                        passengerEmail, dateOfBirth, hashedPassword)
        );
    }

    // Обновление пользователя с хешированием нового пароля
    public User updateUser(String idUser, String passengerFullName, String passengerPhone,
                           String passengerEmail, Date dateOfBirth, String password) {
        User user = userRepository.findById(idUser)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден."));

        if (passengerFullName != null && !passengerFullName.isEmpty()) {
            user.setPassengerFullName(passengerFullName);
        }
        if (passengerPhone != null && !passengerPhone.isEmpty()) {
            if (!ValidationUtil.isValidPhoneFormat(passengerPhone)) {
                throw new IllegalArgumentException("Неверный формат телефона. Используйте формат: +7 XXX XXX-XX-XX");
            }
            user.setPassengerPhone(passengerPhone);
        }
        if (passengerEmail != null && !passengerEmail.isEmpty()) {
            if (!ValidationUtil.isValidEmailFormat(passengerEmail)) {
                throw new IllegalArgumentException("Неверный формат электронной почты. " +
                        "Используйте формат: имя_пользователя@домен. " +
                        "Допустимые домены: mail.ru, inbox.ru, yandex.ru, gmail.com.");
            }
            user.setPassengerEmail(passengerEmail);
        }
        if (dateOfBirth != null) {
            // Проверка возраста при обновлении
            ValidationUtil.validateAge(dateOfBirth);
            user.setDateOfBirth(dateOfBirth);
        }
        if (password != null && !password.isEmpty()) {
            // Хеширование нового пароля
            user.setPassword(passwordEncoder.encode(password));
        }

        return userRepository.save(user);
    }

    // Проверка пароля
    public boolean verifyPassword(String providedPassword, String storedPassword) {
        return passwordEncoder.matches(providedPassword, storedPassword);
    }

    // Генерация нового ID в формате "u + число"
    private String generateNewId(String prefix) {
        List<User> allUsers = userRepository.findAll();
        int maxId = 0;
        for (User user : allUsers) {
            String id = user.getIdUser().substring(1);
            int num = Integer.parseInt(id);
            maxId = Math.max(maxId, num);
        }
        return prefix + (maxId + 1);
    }

    // Удаление пользователя
    public void deleteUser(String idUser) {
        User user = userRepository.findById(idUser)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден."));
        userRepository.deleteById(idUser);
    }
}