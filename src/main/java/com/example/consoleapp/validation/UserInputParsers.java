package com.example.consoleapp.validation;

import java.util.Locale;
import java.util.regex.Pattern;


public final class UserInputParsers {

    public static final int MAX_NAME_LENGTH = 100;

    public static final int MAX_EMAIL_LENGTH = 254;
    public static final int MIN_AGE = 1;
    public static final int MAX_AGE = 100;

    private static final Pattern NAME_PATTERN =
            Pattern.compile("\\p{L}+(?:[ '-]\\p{L}+)*");


    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "[A-Za-z0-9_+-]+(?:\\.[A-Za-z0-9_+-]+)*"
                    + "@"
                    + "(?:[A-Za-z0-9](?:[A-Za-z0-9-]*[A-Za-z0-9])?\\.)+"
                    + "[A-Za-z]{2,}");

    private static final Pattern WHITESPACE = Pattern.compile("\\s+");

    public static final InputParser<String> NAME = UserInputParsers::parseName;
    public static final InputParser<String> EMAIL = UserInputParsers::parseEmail;
    public static final InputParser<Integer> AGE = UserInputParsers::parseAge;
    public static final InputParser<Long> ID = UserInputParsers::parseId;

    private UserInputParsers() {
        // только статические методы
    }


    public static String parseName(String input) {
        String name = requireNotBlank(input, "Имя");
        name = WHITESPACE.matcher(name).replaceAll(" ");

        if (name.length() > MAX_NAME_LENGTH) {
            throw new ValidationException(
                    "Имя слишком длинное: " + name.length() + " символов, максимум " + MAX_NAME_LENGTH);
        }
        if (!NAME_PATTERN.matcher(name).matches()) {
            throw new ValidationException(
                    "Имя может содержать только буквы, а также пробел, дефис или апостроф между ними");
        }
        return name;
    }


    public static String parseEmail(String input) {
        String email = requireNotBlank(input, "Email").toLowerCase(Locale.ROOT);

        if (email.length() > MAX_EMAIL_LENGTH) {
            throw new ValidationException("Email слишком длинный: максимум " + MAX_EMAIL_LENGTH + " символа");
        }
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new ValidationException("Некорректный email, пример правильного: ivan@example.com");
        }
        return email;
    }

    public static Integer parseAge(String input) {
        String text = requireNotBlank(input, "Возраст");
        int age;
        try {
            age = Integer.parseInt(text);
        } catch (NumberFormatException e) {
            throw new ValidationException("Возраст должен быть целым числом, например 25");
        }
        if (age < MIN_AGE || age > MAX_AGE) {
            throw new ValidationException(
                    "Возраст должен быть от " + MIN_AGE + " до " + MAX_AGE + ", введено: " + age);
        }
        return age;
    }

    public static Long parseId(String input) {
        String text = requireNotBlank(input, "Id");
        long id;
        try {
            id = Long.parseLong(text);
        } catch (NumberFormatException e) {
            throw new ValidationException("Id должен быть целым числом, например 1");
        }
        if (id <= 0) {
            throw new ValidationException("Id должен быть положительным числом, введено: " + id);
        }
        return id;
    }

    private static String requireNotBlank(String input, String fieldName) {
        if (input == null || input.isBlank()) {
            throw new ValidationException(fieldName + " не может быть пустым");
        }
        return input.strip();
    }
}
