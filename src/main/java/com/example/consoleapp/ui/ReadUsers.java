package com.example.consoleapp.ui;

import com.example.consoleapp.dao.UserDAOImpl;
import com.example.consoleapp.entity.User;
import com.example.consoleapp.dao.UserDAOImpl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class ReadUsers implements MenuAction {

    private Object List;

    public  boolean execute (Scanner scanner) {
        com.example.consoleapp.dao.UserDAOImpl userlist = new  com.example.consoleapp.dao.UserDAOImpl();
        com.example.consoleapp.entity.User user = new com.example.consoleapp.entity.User();
        List<User> users = new ArrayList<>();


        UserTablePrinter.print(users);
        return true;

    }
    public class UserTablePrinter {

        public static void print(List<User> users) {
            if (users == null || users.isEmpty()) {
                System.out.println("Список пользователей пуст.");
                return;
            }

            // Определяем ширину колонок
            int idWidth = 5;
            int nameWidth = Math.max("Имя".length(), maxLength(users, User::getName));
            int emailWidth = Math.max("Email".length(), maxLength(users, User::getEmail));

            String format = "| %-" + idWidth + "s | %-" + nameWidth + "s | %-" + emailWidth + "s |%n";
            String separator = "+" + "-".repeat(idWidth + 2)
                    + "+" + "-".repeat(nameWidth + 2)
                    + "+" + "-".repeat(emailWidth + 2) + "+";

            // Шапка
            System.out.println(separator);
            System.out.printf(format, "ID", "Имя", "Email");
            System.out.println(separator);

            // Данные
            for (User user : users) {
                System.out.printf(format,
                        user.getId(),
                        safe(user.getName()),
                        safe(user.getEmail()));
            }
            System.out.println(separator);
            System.out.println("Всего пользователей: " + users.size());
        }

        private static int maxLength(List<User> users, java.util.function.Function<User, String> getter) {
            return users.stream()
                    .map(getter)
                    .filter(s -> s != null)
                    .mapToInt(String::length)
                    .max()
                    .orElse(0);
        }

        private static String safe(String s) {
            return s != null ? s : "";
        }
    }
}
