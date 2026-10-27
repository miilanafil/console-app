package com.example.consoleapp.ui;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DeleteUserTest {

    private final InMemoryUserService service = new InMemoryUserService();
    private final DeleteUser action = new DeleteUser(service);

    @Test
    void execute_confirmed_deletesUserAndReturnsToMenu() {
        long id = service.given("Ivan", "ivan@example.com", 25);
        TestConsole console = TestConsole.withInput(String.valueOf(id), "д");

        boolean keepRunning = action.execute(console.io());

        assertTrue(keepRunning, "после удаления приложение не должно завершаться");
        assertTrue(service.stored(id).isEmpty());
        assertTrue(console.output().contains("Пользователь с id = " + id + " удалён"));
    }

    @Test
    void execute_notConfirmed_doesNotDelete() {
        long id = service.given("Ivan", "ivan@example.com", 25);
        TestConsole console = TestConsole.withInput(String.valueOf(id), "н");

        action.execute(console.io());

        assertTrue(service.stored(id).isPresent());
        assertEquals(0, service.deleteCalls(), "сервис не должен вызываться при отмене");
        assertTrue(console.output().contains("Удаление отменено"));
    }

    @Test
    void execute_userNotFound_printsMessage() {
        TestConsole console = TestConsole.withInput("42", "да");

        assertTrue(action.execute(console.io()));

        assertTrue(console.output().contains("Пользователь с id = 42 не найден"));
    }

    @Test
    void execute_invalidIdAndAnswer_areAskedAgain() {
        long id = service.given("Ivan", "ivan@example.com", 25);
        TestConsole console = TestConsole.withInput("abc", "0", String.valueOf(id), "наверное", "y");

        action.execute(console.io());

        assertTrue(service.stored(id).isEmpty());
        String out = console.output();
        assertTrue(out.contains("целым числом"));
        assertTrue(out.contains("положительным"));
        assertTrue(out.contains("Введите «д» или «н»"));
    }

    @Test
    void execute_deletesOnlyRequestedUser() {
        long ivan = service.given("Ivan", "ivan@example.com", 25);
        long petr = service.given("Petr", "petr@example.com", 40);
        TestConsole console = TestConsole.withInput(String.valueOf(ivan), "д");

        action.execute(console.io());

        assertTrue(service.stored(ivan).isEmpty());
        assertTrue(service.stored(petr).isPresent());
    }
}
