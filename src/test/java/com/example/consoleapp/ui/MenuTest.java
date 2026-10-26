package com.example.consoleapp.ui;

import com.example.consoleapp.exception.DaoException;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;


class MenuTest {

    private final ByteArrayOutputStream output = new ByteArrayOutputStream();
    private final List<String> calls = new ArrayList<>();

    private Menu menuWithInput(String input) {
        ConsoleIO io = new ConsoleIO(
                new Scanner(input),
                new PrintStream(output, true, StandardCharsets.UTF_8));
        return new Menu(io);
    }

    private String output() {
        return output.toString(StandardCharsets.UTF_8);
    }

    private MenuAction recording(String name) {
        return io -> {
            calls.add(name);
            return true;
        };
    }

    private static final MenuAction EXIT = io -> false;

    @Test
    void run_selectedKey_executesMatchingAction() {
        Menu menu = menuWithInput("2\n9\n");
        menu.addItem("1", "Первый", recording("first"));
        menu.addItem("2", "Второй", recording("second"));
        menu.addItem("9", "Выход", EXIT);

        menu.run();

        assertEquals(List.of("second"), calls);
    }

    @Test
    void run_actionReturnsTrue_menuContinues() {
        Menu menu = menuWithInput("1\n1\n1\n9\n");
        menu.addItem("1", "Действие", recording("a"));
        menu.addItem("9", "Выход", EXIT);

        menu.run();

        assertEquals(3, calls.size());
    }

    @Test
    void run_actionReturnsFalse_menuStops() {
        // после выхода на входе ещё есть "1", но меню не должно его читать
        Menu menu = menuWithInput("9\n1\n");
        menu.addItem("1", "Действие", recording("a"));
        menu.addItem("9", "Выход", EXIT);

        menu.run();

        assertTrue(calls.isEmpty());
    }

    @Test
    void run_unknownKey_showsMessageAndContinues() {
        Menu menu = menuWithInput("7\nabc\n\n1\n9\n");
        menu.addItem("1", "Действие", recording("a"));
        menu.addItem("9", "Выход", EXIT);

        menu.run();

        assertEquals(List.of("a"), calls);
        assertTrue(output().contains("Неизвестный пункт: '7'"));
        assertTrue(output().contains("Неизвестный пункт: 'abc'"));
        assertTrue(output().contains("Неизвестный пункт: ''"));
    }

    @Test
    void run_keyWithSurroundingSpaces_isRecognized() {
        Menu menu = menuWithInput("  1  \n9\n");
        menu.addItem("1", "Действие", recording("a"));
        menu.addItem("9", "Выход", EXIT);

        menu.run();

        assertEquals(List.of("a"), calls);
    }

    @Test
    void run_printsItemsInInsertionOrder() {
        Menu menu = menuWithInput("9\n");
        menu.addItem("2", "Второй", recording("b"));
        menu.addItem("1", "Первый", recording("a"));
        menu.addItem("9", "Выход", EXIT);

        menu.run();

        String out = output();
        int second = out.indexOf("2. Второй");
        int first = out.indexOf("1. Первый");
        int exit = out.indexOf("9. Выход");
        assertTrue(second >= 0 && first > second && exit > first,
                "пункты должны печататься в порядке добавления:\n" + out);
    }

    @Test
    void run_daoException_showsMessageAndReturnsToMenu() {
        Menu menu = menuWithInput("1\n2\n9\n");
        menu.addItem("1", "Сломанный", io -> {
            throw new DaoException("duplicate key value violates unique constraint");
        });
        menu.addItem("2", "Рабочий", recording("ok"));
        menu.addItem("9", "Выход", EXIT);

        assertDoesNotThrow(menu::run);

        assertEquals(List.of("ok"), calls, "после ошибки меню должно продолжить работу");
        assertTrue(output().contains("Ошибка при работе с базой данных: duplicate key"));
    }

    @Test
    void run_illegalArgumentFromService_showsReasonAndReturnsToMenu() {
        Menu menu = menuWithInput("1\n2\n9\n");
        menu.addItem("1", "Отклонено сервисом", io -> {
            throw new IllegalArgumentException("Возраст не может превышать 100 лет");
        });
        menu.addItem("2", "Рабочий", recording("ok"));
        menu.addItem("9", "Выход", EXIT);

        assertDoesNotThrow(menu::run);

        assertEquals(List.of("ok"), calls);
        assertTrue(output().contains("Ошибка: Возраст не может превышать 100 лет"));
        assertFalse(output().contains("Непредвиденная ошибка"),
                "отказ сервиса из-за данных — не баг, и сообщение должно быть обычным");
    }

    @Test
    void run_unexpectedRuntimeException_showsMessageAndReturnsToMenu() {
        Menu menu = menuWithInput("1\n9\n");
        menu.addItem("1", "С багом", io -> {
            throw new IllegalStateException("что-то сломалось");
        });
        menu.addItem("9", "Выход", EXIT);

        assertDoesNotThrow(menu::run);

        assertTrue(output().contains("Непредвиденная ошибка: что-то сломалось"));
    }

    @Test
    void run_inputEndsAtMenuPrompt_exitsGracefully() {
        Menu menu = menuWithInput("1\n");   // после первого действия ввод заканчивается
        menu.addItem("1", "Действие", recording("a"));
        menu.addItem("9", "Выход", EXIT);

        assertDoesNotThrow(menu::run);

        assertEquals(List.of("a"), calls);
        assertTrue(output().contains("Ввод завершён"));
    }

    @Test
    void run_inputEndsInsideAction_exitsGracefully() {
        Menu menu = menuWithInput("1\n");
        menu.addItem("1", "Спрашивает имя", io -> {
            io.readLine("Имя: ");          // ввода больше нет
            calls.add("не должно выполниться");
            return true;
        });

        assertDoesNotThrow(menu::run);

        assertTrue(calls.isEmpty());
        assertTrue(output().contains("Ввод завершён"));
    }

    @Test
    void addItem_duplicateKey_throwsIllegalArgumentException() {
        Menu menu = menuWithInput("");
        menu.addItem("1", "Первый", EXIT);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> menu.addItem(" 1 ", "Дубликат", EXIT));
        assertTrue(ex.getMessage().contains("'1'"));
    }

    @Test
    void addItem_blankKey_throwsIllegalArgumentException() {
        Menu menu = menuWithInput("");

        assertThrows(IllegalArgumentException.class, () -> menu.addItem("  ", "Пустой", EXIT));
    }

    @Test
    void addItem_nullArguments_throwNpe() {
        Menu menu = menuWithInput("");

        assertAll(
                () -> assertThrows(NullPointerException.class, () -> menu.addItem(null, "a", EXIT)),
                () -> assertThrows(NullPointerException.class, () -> menu.addItem("1", null, EXIT)),
                () -> assertThrows(NullPointerException.class, () -> menu.addItem("1", "a", null))
        );
    }

    @Test
    void run_emptyMenu_throwsIllegalStateException() {
        Menu menu = menuWithInput("1\n");

        assertThrows(IllegalStateException.class, menu::run);
    }

    @Test
    void exitAction_returnsFalse() {
        ConsoleIO io = new ConsoleIO(new Scanner(""), new PrintStream(output, true, StandardCharsets.UTF_8));

        assertFalse(new ExitAction().execute(io));
    }
}
