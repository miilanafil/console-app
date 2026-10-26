package com.example.consoleapp.ui;

import com.example.consoleapp.entity.User;
import com.example.consoleapp.exception.DaoException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CreateUserTest {

    private final InMemoryUserService service = new InMemoryUserService();
    private final CreateUser action = new CreateUser(service);

    @Test
    void execute_validInput_savesUserAndPrintsId() {
        TestConsole console = TestConsole.withInput("Ivan", "ivan@example.com", "25");

        boolean keepRunning = action.execute(console.io());

        assertTrue(keepRunning, "после создания нужно вернуться в меню");
        assertEquals(1, service.size());
        User saved = service.stored(1).orElseThrow();
        assertAll(
                () -> assertEquals("Ivan", saved.getName()),
                () -> assertEquals("ivan@example.com", saved.getEmail()),
                () -> assertEquals(25, saved.getAge())
        );
        assertTrue(console.output().contains("Пользователь создан, id = 1"));
    }

    @Test
    void execute_savesNormalizedValues() {
        TestConsole console = TestConsole.withInput("  Иван   Петров ", " Ivan@Example.COM ", " 30 ");

        action.execute(console.io());

        User saved = service.stored(1).orElseThrow();
        assertEquals("Иван Петров", saved.getName());
        assertEquals("ivan@example.com", saved.getEmail());
        assertEquals(30, saved.getAge());
    }

    @Test
    void execute_invalidInput_isAskedAgainAndNotSaved() {
        TestConsole console = TestConsole.withInput(
                "", "Ivan1", "Ivan",               // имя: пусто, цифра, ок
                "ivan@", "ivan@example.com",       // email: без домена, ок
                "abc", "200", "25");               // возраст: не число, вне диапазона, ок

        action.execute(console.io());

        assertEquals(1, service.size(), "сохранён ровно один пользователь, с корректными данными");
        assertEquals("Ivan", service.stored(1).orElseThrow().getName());
        String out = console.output();
        assertTrue(out.contains("не может быть пустым"));
        assertTrue(out.contains("только буквы"));
        assertTrue(out.contains("Некорректный email"));
        assertTrue(out.contains("целым числом"));
        assertTrue(out.contains("от 1 до 100"));
    }

    @Test
    void execute_duplicateEmail_propagatesDaoException() {
        service.given("Petr", "ivan@example.com", 40);
        TestConsole console = TestConsole.withInput("Ivan", "IVAN@example.com", "25");

        // Действие не глотает ошибку: её показывает Menu (см. MenuTest и тест ниже)
        assertThrows(DaoException.class, () -> action.execute(console.io()));
        assertEquals(1, service.size());
    }

    @Test
    void execute_duplicateEmail_insideMenu_showsErrorAndContinues() {
        service.given("Petr", "ivan@example.com", 40);
        TestConsole console = TestConsole.withInput("1", "Ivan", "ivan@example.com", "25", "0");
        Menu menu = new Menu(console.io());
        menu.addItem("1", "Создать", action);
        menu.addItem("0", "Выход", new ExitAction());

        assertDoesNotThrow(menu::run);

        assertTrue(console.output().contains("Ошибка при работе с базой данных"));
        assertTrue(console.output().contains("duplicate key"));
        assertTrue(console.output().contains("До свидания!"), "меню продолжило работу и дошло до выхода");
    }

    @Test
    void execute_serviceRejectsData_insideMenu_showsReasonAndContinues() {
        service.failNextCallWith(new IllegalArgumentException("Некорректный формат email"));
        TestConsole console = TestConsole.withInput("1", "Ivan", "ivan@example.com", "25", "0");
        Menu menu = new Menu(console.io());
        menu.addItem("1", "Создать", action);
        menu.addItem("0", "Выход", new ExitAction());

        assertDoesNotThrow(menu::run);

        assertEquals(0, service.size());
        assertTrue(console.output().contains("Ошибка: Некорректный формат email"));
        assertTrue(console.output().contains("До свидания!"));
    }

    @Test
    void execute_inputEndsBeforeAllFieldsEntered_savesNothing() {
        TestConsole console = TestConsole.withInput("Ivan", "ivan@example.com");

        assertThrows(InputClosedException.class, () -> action.execute(console.io()));
        assertEquals(0, service.size());
    }

    @Test
    void constructor_nullDao_throwsNpe() {
        assertThrows(NullPointerException.class, () -> new CreateUser(null));
    }
}
