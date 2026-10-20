package com.example.consoleapp.ui;

import com.example.consoleapp.exception.DaoException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;


public class Menu {

    private static final Logger log = LoggerFactory.getLogger(Menu.class);
    private static final String SEPARATOR = "------------------";


    private record Item(String label, MenuAction action) {
    }


    private final Map<String, Item> items = new LinkedHashMap<>();
    private final ConsoleIO io;

    public Menu(ConsoleIO io) {
        this.io = Objects.requireNonNull(io, "io");
    }


    public void addItem(String key, String label, MenuAction action) {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(label, "label");
        Objects.requireNonNull(action, "action");

        String normalizedKey = key.strip();
        if (normalizedKey.isEmpty()) {
            throw new IllegalArgumentException("Ключ пункта меню не может быть пустым");
        }
        if (items.containsKey(normalizedKey)) {
            throw new IllegalArgumentException("Пункт меню с ключом '" + normalizedKey + "' уже добавлен");
        }
        items.put(normalizedKey, new Item(label, action));
    }

    /**
     * Цикл меню. Завершается, когда действие вернёт {@code false} или закончится ввод.
     */
    public void run() {
        if (items.isEmpty()) {
            throw new IllegalStateException("В меню нет ни одного пункта");
        }
        try {
            boolean running = true;
            while (running) {
                printMenu();
                String choice = io.readLine("Выберите пункт: ").strip();

                Item item = items.get(choice);
                if (item == null) {
                    io.println("Неизвестный пункт: '" + choice + "'. Введите номер из списка.");
                    continue;
                }
                running = execute(item);
            }
        } catch (InputClosedException e) {
            io.println("Ввод завершён. Выход из программы.");
        }
    }

    private boolean execute(Item item) {
        try {
            return item.action().execute(io);
        } catch (InputClosedException e) {

            throw e;
        } catch (DaoException e) {
            log.warn("Ошибка БД при выполнении пункта '{}'", item.label(), e);
            io.println("Ошибка при работе с базой данных: " + e.getMessage());
            return true;
        } catch (RuntimeException e) {

            log.error("Непредвиденная ошибка при выполнении пункта '{}'", item.label(), e);
            io.println("Непредвиденная ошибка: " + e.getMessage());
            return true;
        }
    }

    private void printMenu() {
        io.println(SEPARATOR);
        items.forEach((key, item) -> io.printf("%s. %s%n", key, item.label()));
        io.println(SEPARATOR);
    }
}
