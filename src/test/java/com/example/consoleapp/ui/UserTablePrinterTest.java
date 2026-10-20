package com.example.consoleapp.ui;

import com.example.consoleapp.entity.User;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.ZoneId;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UserTablePrinterTest {

    private static final String NL = System.lineSeparator();
    private final UserTablePrinter printer = new UserTablePrinter(ZoneId.of("Europe/Moscow"));

    private static User user(long id, String name, String email, int age) {
        User user = new User(name, email, age);
        user.setId(id);
        user.setCreatedCode(Instant.parse("2026-10-20T09:30:00Z"));
        return user;
    }

    @Test
    void render_singleUser_exactTable() {
        String table = printer.render(List.of(user(1, "Ivan", "ivan@example.com", 25)));

        // Полное сравнение: если поменяется формат, тест сразу покажет, как именно
        String expected = String.join(NL,
                "+----+------+------------------+---------+------------------+",
                "| ID | Имя  | Email            | Возраст | Создан           |",
                "+----+------+------------------+---------+------------------+",
                "| 1  | Ivan | ivan@example.com | 25      | 2026-10-20 12:30 |",
                "+----+------+------------------+---------+------------------+",
                "Всего пользователей: 1") + NL;
        assertEquals(expected, table);
    }

    @Test
    void render_allLinesHaveSameWidth() {
        String table = printer.render(List.of(
                user(1, "Ivan", "i@x.io", 5),
                user(12345, "Анна-Мария Петрова-Водкина", "anna.maria.long.address@example.com", 101)));

        List<String> tableLines = table.lines()
                .filter(line -> line.startsWith("+") || line.startsWith("|"))
                .toList();
        int width = tableLines.get(0).length();
        tableLines.forEach(line -> assertEquals(width, line.length(), "строка не выровнена: " + line));
    }

    @Test
    void render_keepsOrderOfList() {
        String table = printer.render(List.of(
                user(2, "Petr", "petr@example.com", 40),
                user(1, "Ivan", "ivan@example.com", 25)));

        assertTrue(table.indexOf("Petr") < table.indexOf("Ivan"));
    }

    @Test
    void render_emptyOrNull_printsEmptyMessage() {
        assertEquals("Список пользователей пуст." + NL, printer.render(List.of()));
        assertEquals("Список пользователей пуст." + NL, printer.render(null));
    }

    @Test
    void render_nullFields_areShownAsEmpty() {
        User user = new User(null, null, null);   // ещё не сохранён: нет id и даты

        String table = assertDoesNotThrow(() -> printer.render(List.of(user)));

        assertFalse(table.contains("null"));
    }

    @Test
    void render_dateIsShownInConfiguredZone() {
        User u = user(1, "Ivan", "ivan@example.com", 25);

        assertTrue(new UserTablePrinter(ZoneId.of("UTC")).render(List.of(u)).contains("2026-10-20 09:30"));
        assertTrue(new UserTablePrinter(ZoneId.of("Asia/Tokyo")).render(List.of(u)).contains("2026-10-20 18:30"));
    }

    @Test
    void renderOne_containsAllFields() {
        String card = printer.renderOne(user(7, "Ivan", "ivan@example.com", 25));

        String expected = String.join(NL,
                "ID:      7",
                "Имя:     Ivan",
                "Email:   ivan@example.com",
                "Возраст: 25",
                "Создан:  2026-10-20 12:30") + NL;
        assertEquals(expected, card);
    }
}
