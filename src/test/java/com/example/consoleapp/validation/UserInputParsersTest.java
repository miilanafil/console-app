package com.example.consoleapp.validation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class UserInputParsersTest {

    @Nested
    @DisplayName("Имя")
    class Name {

        @ParameterizedTest(name = "\"{0}\" → \"{1}\"")
        @CsvSource(delimiter = '|', value = {
                "Ivan           | Ivan",
                "Анна           | Анна",
                "Анна-Мария     | Анна-Мария",
                "O'Neil         | O'Neil",
                "Jean Luc       | Jean Luc",
                "'  Ivan  '     | Ivan",
                "'Jean    Luc'  | Jean Luc"
        })
        void validName_isNormalized(String input, String expected) {
            assertEquals(expected, UserInputParsers.parseName(input));
        }

        @ParameterizedTest(name = "пустое значение: [{0}]")
        @NullAndEmptySource
        @ValueSource(strings = {"   ", "\t"})
        void blankName_isRejected(String input) {
            ValidationException ex = assertThrows(ValidationException.class,
                    () -> UserInputParsers.parseName(input));
            assertTrue(ex.getMessage().contains("не может быть пустым"));
        }

        @ParameterizedTest(name = "\"{0}\"")
        @ValueSource(strings = {"Ivan1", "Ivan!", "-Ivan", "Ivan-", "Анна--Мария", "123", "ivan@mail"})
        void nameWithInvalidCharacters_isRejected(String input) {
            assertThrows(ValidationException.class, () -> UserInputParsers.parseName(input));
        }

        @Test
        void nameOfMaxLength_isAccepted() {
            String name = "a".repeat(UserInputParsers.MAX_NAME_LENGTH);
            assertEquals(name, UserInputParsers.parseName(name));
        }

        @Test
        void nameLongerThanMax_isRejected() {
            String name = "a".repeat(UserInputParsers.MAX_NAME_LENGTH + 1);
            ValidationException ex = assertThrows(ValidationException.class,
                    () -> UserInputParsers.parseName(name));
            assertTrue(ex.getMessage().contains("слишком длинное"));
        }
    }

    @Nested
    @DisplayName("Email")
    class Email {

        @ParameterizedTest(name = "\"{0}\" → \"{1}\"")
        @CsvSource(delimiter = '|', value = {
                "ivan@example.com            | ivan@example.com",
                "Ivan.Petrov@Example.COM     | ivan.petrov@example.com",
                "'  ivan@example.com  '      | ivan@example.com",
                "ivan+tag@mail.example.org   | ivan+tag@mail.example.org",
                "i_v-a.n@my-domain.ru        | i_v-a.n@my-domain.ru"
        })
        void validEmail_isNormalized(String input, String expected) {
            assertEquals(expected, UserInputParsers.parseEmail(input));
        }

        @ParameterizedTest(name = "\"{0}\"")
        @ValueSource(strings = {
                "ivan",                 // нет @
                "ivan@",                // нет домена
                "@example.com",         // нет локальной части
                "ivan@example",         // нет зоны
                "ivan@example.c",       // зона из одной буквы
                "ivan@@example.com",    // две @
                "iv an@example.com",    // пробел внутри
                ".ivan@example.com",    // точка в начале
                "ivan.@example.com",    // точка в конце локальной части
                "iv..an@example.com",   // две точки подряд
                "ivan@-example.com",    // дефис в начале метки домена
                "ivan@example..com",    // пустая метка домена
                "иван@example.com"      // кириллица в локальной части
        })
        void invalidEmail_isRejected(String input) {
            assertThrows(ValidationException.class, () -> UserInputParsers.parseEmail(input));
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = "  ")
        void blankEmail_isRejected(String input) {
            assertThrows(ValidationException.class, () -> UserInputParsers.parseEmail(input));
        }

        @Test
        void emailLongerThanMax_isRejected() {
            String email = "a".repeat(UserInputParsers.MAX_EMAIL_LENGTH) + "@example.com";
            ValidationException ex = assertThrows(ValidationException.class,
                    () -> UserInputParsers.parseEmail(email));
            assertTrue(ex.getMessage().contains("слишком длинный"));
        }
    }

    @Nested
    @DisplayName("Возраст")
    class Age {

        @ParameterizedTest(name = "\"{0}\" → {1}")
        @CsvSource({
                "1,     1",       // нижняя граница
                "25,    25",
                "100,   100",     // верхняя граница
                "' 30 ', 30",
                "+18,   18"
        })
        void validAge_isParsed(String input, int expected) {
            assertEquals(expected, UserInputParsers.parseAge(input));
        }

        @ParameterizedTest(name = "\"{0}\"")
        @ValueSource(strings = {"0", "-1", "101", "1000"})
        void ageOutOfRange_isRejected(String input) {
            ValidationException ex = assertThrows(ValidationException.class,
                    () -> UserInputParsers.parseAge(input));
            assertTrue(ex.getMessage().contains("от " + UserInputParsers.MIN_AGE));
        }

        @ParameterizedTest(name = "\"{0}\"")
        @ValueSource(strings = {"abc", "25.5", "25 лет", "2 5", "99999999999"})
        void nonIntegerAge_isRejected(String input) {
            ValidationException ex = assertThrows(ValidationException.class,
                    () -> UserInputParsers.parseAge(input));
            assertTrue(ex.getMessage().contains("целым числом"));
        }
    }

    @Nested
    @DisplayName("Id")
    class Id {

        @ParameterizedTest(name = "\"{0}\" → {1}")
        @CsvSource({"1, 1", "' 42 ', 42", "9223372036854775807, 9223372036854775807"})
        void validId_isParsed(String input, long expected) {
            assertEquals(expected, UserInputParsers.parseId(input));
        }

        @ParameterizedTest(name = "\"{0}\"")
        @ValueSource(strings = {"0", "-5"})
        void nonPositiveId_isRejected(String input) {
            assertThrows(ValidationException.class, () -> UserInputParsers.parseId(input));
        }

        @ParameterizedTest(name = "\"{0}\"")
        @ValueSource(strings = {"abc", "1.0", "9223372036854775808"})
        void nonNumericId_isRejected(String input) {
            assertThrows(ValidationException.class, () -> UserInputParsers.parseId(input));
        }
    }

    @Test
    @DisplayName("Константы-парсеры используют те же правила, что и методы")
    void parserConstants_delegateToMethods() {
        assertEquals("Ivan", UserInputParsers.NAME.parse(" Ivan "));
        assertEquals("ivan@example.com", UserInputParsers.EMAIL.parse("IVAN@example.com"));
        assertEquals(25, UserInputParsers.AGE.parse("25"));
        assertEquals(7L, UserInputParsers.ID.parse("7"));
    }
}
