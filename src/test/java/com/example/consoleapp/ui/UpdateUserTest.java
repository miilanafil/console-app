package com.example.consoleapp.ui;

import com.example.consoleapp.entity.User;
import com.example.consoleapp.exception.DaoException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UpdateUserTest {

    private final InMemoryUserService service = new InMemoryUserService();
    private final UpdateUser action = new UpdateUser(service);
    private long ivanId;

    @BeforeEach
    void setUp() {
        ivanId = service.given("Ivan", "ivan@example.com", 25);
    }

    @Test
    void execute_changeAllFields_updatesUser() {
        TestConsole console = TestConsole.withInput(
                String.valueOf(ivanId), "Petr", "petr@example.com", "40");

        assertTrue(action.execute(console.io()));

        User updated = service.stored(ivanId).orElseThrow();
        assertAll(
                () -> assertEquals("Petr", updated.getName()),
                () -> assertEquals("petr@example.com", updated.getEmail()),
                () -> assertEquals(40, updated.getAge())
        );
        assertEquals(1, service.updateCalls());
        assertTrue(console.output().contains("обновлён"));
    }

    @Test
    void execute_enterKeepsField_onlyChangedFieldIsUpdated() {
        // Enter для имени и возраста, меняется только email
        TestConsole console = TestConsole.withInput(String.valueOf(ivanId), "", "new@example.com", "");

        action.execute(console.io());

        User updated = service.stored(ivanId).orElseThrow();
        assertEquals("Ivan", updated.getName(), "имя не должно стать null или пустым");
        assertEquals("new@example.com", updated.getEmail());
        assertEquals(25, updated.getAge());
        assertEquals(InMemoryUserService.CREATED_AT, updated.getCreatedCode(), "дата создания не меняется");
    }

    @Test
    void execute_promptsShowCurrentValues() {
        TestConsole console = TestConsole.withInput(String.valueOf(ivanId), "", "", "");

        action.execute(console.io());

        String out = console.output();
        assertTrue(out.contains("Имя [Ivan]: "));
        assertTrue(out.contains("Email [ivan@example.com]: "));
        assertTrue(out.contains("Возраст [25]: "));
    }

    @Test
    void execute_nothingChanged_doesNotCallUpdate() {
        // Enter везде, а также ввод того же значения, что уже стоит
        TestConsole console = TestConsole.withInput(String.valueOf(ivanId), "", " IVAN@example.com ", "25");

        action.execute(console.io());

        assertEquals(0, service.updateCalls());
        assertTrue(console.output().contains("Изменений нет"));
    }

    @Test
    void execute_userNotFound_doesNotAskFieldsAndDoesNotUpdate() {
        TestConsole console = TestConsole.withInput("999");

        assertTrue(action.execute(console.io()));

        assertEquals(0, service.updateCalls());
        assertTrue(console.output().contains("Пользователь с id = 999 не найден"));
        assertFalse(console.output().contains("Имя ["));
    }

    @Test
    void execute_invalidValues_areAskedAgain() {
        TestConsole console = TestConsole.withInput(
                "abc", String.valueOf(ivanId),   // id: не число, ок
                "Petr2", "Petr",                  // имя: цифра, ок
                "",                               // email: оставить
                "-3", "26");                      // возраст: отрицательный, ок

        action.execute(console.io());

        User updated = service.stored(ivanId).orElseThrow();
        assertEquals("Petr", updated.getName());
        assertEquals(26, updated.getAge());
    }

    @Test
    void execute_emailTakenByAnotherUser_propagatesDaoException() {
        service.given("Petr", "petr@example.com", 40);
        TestConsole console = TestConsole.withInput(String.valueOf(ivanId), "", "petr@example.com", "");

        assertThrows(DaoException.class, () -> action.execute(console.io()));
        assertEquals("ivan@example.com", service.stored(ivanId).orElseThrow().getEmail(), "данные не изменились");
    }

    @Test
    void execute_daoFailsOnFind_propagatesDaoException() {
        service.failNextCallWith(new DaoException("connection refused"));
        TestConsole console = TestConsole.withInput(String.valueOf(ivanId));

        assertThrows(DaoException.class, () -> action.execute(console.io()));
    }
}
