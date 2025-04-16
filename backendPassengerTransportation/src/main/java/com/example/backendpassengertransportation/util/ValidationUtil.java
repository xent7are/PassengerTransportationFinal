package com.example.backendpassengertransportation.util;

import java.time.LocalDate;
import java.time.Period;
import java.util.Date;
import java.util.regex.Pattern;

// Утилита для валидации форматов телефона, электронной почты и возраста
public class ValidationUtil {

    // Регулярное выражение для проверки формата телефона (+7 XXX XXX-XX-XX)
    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\+7 \\d{3} \\d{3}-\\d{2}-\\d{2}$");

    // Регулярное выражение для проверки формата электронной почты
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

    // Список допустимых доменов для электронной почты
    private static final String[] ALLOWED_DOMAINS = {"mail.ru", "inbox.ru", "yandex.ru", "gmail.com"};

    // Проверка формата телефона
    public static boolean isValidPhoneFormat(String phone) {
        return PHONE_PATTERN.matcher(phone).matches();
    }

    // Проверка формата электронной почты и допустимых доменов
    public static boolean isValidEmailFormat(String email) {
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            return false;
        }
        String domain = email.substring(email.lastIndexOf("@") + 1);
        for (String allowedDomain : ALLOWED_DOMAINS) {
            if (domain.equalsIgnoreCase(allowedDomain)) {
                return true;
            }
        }
        return false;
    }

    // Проверка возраста на основе даты рождения
    public static void validateAge(Date dateOfBirth) {
        LocalDate birthDate = ((java.sql.Date) dateOfBirth).toLocalDate();
        LocalDate currentDate = LocalDate.now();

        // Проверка, если дата рождения в будущем
        if (birthDate.isAfter(currentDate)) {
            throw new IllegalArgumentException("Дата рождения не может быть позже текущей даты.");
        }

        // Вычисление возраста
        int age = Period.between(birthDate, currentDate).getYears();

        // Проверка, если возраст больше 120 лет
        if (age > 120) {
            throw new IllegalArgumentException("Наша транспортная компания не уверена, что Вам " + age + " лет, введите пожалуйста корректную дату рождения.");
        }

        // Проверка, если возраст меньше 14 лет
        if (age < 14) {
            throw new IllegalArgumentException("У вас прекрасный возраст! К сожалению, вы еще не можете самостоятельно бронировать билеты на поездки. Пусть ваш родитель забронирует Вам билет.");
        }
    }
}