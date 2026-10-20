package com.example.consoleapp.ui;

import com.example.consoleapp.config.HibernateUtil;
import com.example.consoleapp.dao.UserDAO;
import com.example.consoleapp.dao.UserDAOImpl;

import java.util.Scanner;


public class Main {

    public static void main(String[] args) {
        ConsoleIO io = new ConsoleIO(new Scanner(System.in), System.out);

        if (!connectToDatabase(io)) {
            System.exit(1);
        }

        try {

            UserDAO userDao = new UserDAOImpl();
            UserTablePrinter printer = new UserTablePrinter();

            Menu menu = new Menu(io);
            menu.addItem("1", "Создать", new CreateUser(userDao));
            menu.addItem("2", "Показать всех", new ReadUsers(userDao, printer));
            menu.addItem("3", "Найти по id", new FindUser(userDao, printer));
            menu.addItem("4", "Изменить", new UpdateUser(userDao));
            menu.addItem("5", "Удалить", new DeleteUser(userDao));
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
