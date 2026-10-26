package com.example.consoleapp.ui;

import com.example.consoleapp.config.HibernateUtil;
import com.example.consoleapp.dao.UserDAOImpl;
import com.example.consoleapp.service.UserService;
import com.example.consoleapp.service.UserServiceImpl;

import java.util.Scanner;


public class Main {

    public static void main(String[] args) {
        ConsoleIO io = new ConsoleIO(new Scanner(System.in), System.out);

        if (!connectToDatabase(io)) {
            System.exit(1);
        }

        try {

            // UI -> UserService -> UserDAO: интерфейс работает только с сервисом
            UserService userService = new UserServiceImpl(new UserDAOImpl());
            UserTablePrinter printer = new UserTablePrinter();

            Menu menu = new Menu(io);
            menu.addItem("1", "Создать", new CreateUser(userService));
            menu.addItem("2", "Показать всех", new ReadUsers(userService, printer));
            menu.addItem("3", "Найти по id", new FindUser(userService, printer));
            menu.addItem("4", "Изменить", new UpdateUser(userService));
            menu.addItem("5", "Удалить", new DeleteUser(userService));
            menu.addItem("0", "Выход", new ExitAction());
            menu.run();
        } finally {
            HibernateUtil.shutdown();
        }
    }


    private static boolean connectToDatabase(ConsoleIO io) {
        try {
            HibernateUtil.getSessionFactory();
            return true;
        } catch (ExceptionInInitializerError | NoClassDefFoundError e) {
            io.println("Не удалось подключиться к базе данных.");
            io.println("Проверьте, что PostgreSQL запущен: docker compose up -d");
            return false;
        }
    }
}
