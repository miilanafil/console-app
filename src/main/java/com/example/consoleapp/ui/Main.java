package com.example.consoleapp.ui;            // 1. первой строкой


public class Main {                           // 3. затем типы
    public static void main(String[] args) {
        Menu menu = new Menu();
        menu.addItem("1", "Создать",  new CreateUser());
        menu.addItem("2", "Прочитать", new ReadUsers());   // пока не реализованы
        menu.addItem("3", "Изменить",  new UpdateUser());
        menu.addItem("4", "Удалить",   new DeleteUser());
        menu.addItem("5", "Выход",    new ExitAction());
        menu.run();
    }
}