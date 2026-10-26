package com.example.consoleapp.ui;

import com.example.consoleapp.exception.DaoException;
import org.junit.jupiter.api.Test;

import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.*;


class ReadAndFindUsersTest {

    private final InMemoryUserService service = new InMemoryUserService();
    private final UserTablePrinter printer = new UserTablePrinter(ZoneId.of("UTC"));

    @Test
    void readUsers_printsAllUsers() {
        service.given("Ivan", "ivan@example.com", 25);
        service.given("Petr", "petr@example.com", 40);
        TestConsole console = TestConsole.withInput();

        assertTrue(new ReadUsers(service, printer).execute(console.io()));

        String out = console.output();
        assertTrue(out.contains("ivan@example.com"));
        assertTrue(out.contains("petr@example.com"));
        assertTrue(out.contains("Всего пользователей: 2"));
    }

    @Test
    void readUsers_emptyDatabase_printsEmptyMessage() {
        TestConsole console = TestConsole.withInput();

        new ReadUsers(service, printer).execute(console.io());

        assertTrue(console.output().contains("Список пользователей пуст."));
    }

    @Test
    void readUsers_daoFails_propagatesDaoException() {
        service.failNextCallWith(new DaoException("connection refused"));

        assertThrows(DaoException.class,
                () -> new ReadUsers(service, printer).execute(TestConsole.withInput().io()));
    }

    @Test
    void findUser_existing_printsCard() {
        long id = service.given("Ivan", "ivan@example.com", 25);
        TestConsole console = TestConsole.withInput(String.valueOf(id));

        assertTrue(new FindUser(service, printer).execute(console.io()));

        String out = console.output();
        assertTrue(out.contains("Имя:     Ivan"));
        assertTrue(out.contains("Email:   ivan@example.com"));
        assertTrue(out.contains("Возраст: 25"));
        assertTrue(out.contains("Создан:  2026-10-20 09:30"));
    }

    @Test
    void findUser_missing_printsNotFound() {
        TestConsole console = TestConsole.withInput("7");

        new FindUser(service, printer).execute(console.io());

        assertTrue(console.output().contains("Пользователь с id = 7 не найден"));
    }

    @Test
    void findUser_invalidId_isAskedAgain() {
        long id = service.given("Ivan", "ivan@example.com", 25);
        TestConsole console = TestConsole.withInput("-1", String.valueOf(id));

        new FindUser(service, printer).execute(console.io());

        assertTrue(console.output().contains("положительным"));
        assertTrue(console.output().contains("Имя:     Ivan"));
    }

    @Test
    void constructors_nullArguments_throwNpe() {
        assertAll(
                () -> assertThrows(NullPointerException.class, () -> new ReadUsers(null, printer)),
                () -> assertThrows(NullPointerException.class, () -> new ReadUsers(service, null)),
                () -> assertThrows(NullPointerException.class, () -> new FindUser(null, printer)),
                () -> assertThrows(NullPointerException.class, () -> new FindUser(service, null)),
                () -> assertThrows(NullPointerException.class, () -> new UpdateUser(null)),
                () -> assertThrows(NullPointerException.class, () -> new DeleteUser(null))
        );
    }
}
