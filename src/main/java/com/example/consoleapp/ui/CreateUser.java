package com.example.consoleapp.ui;



import java.util.Scanner;

public class CreateUser implements MenuAction {

    @Override
    public  boolean execute (Scanner scanner) {

        clearConsole ();
        com.example.consoleapp.entity.User user = new com.example.consoleapp.entity.User();
        com.example.consoleapp.dao.UserDAOImpl saveUs = new com.example.consoleapp.dao.UserDAOImpl();

        System.out.print("Имя:  ");
        String UserName = scanner.nextLine();
        user.setName(UserName);

        System.out.print("Почта:  ");
        String UserEmail = scanner.nextLine();
        user.setEmail(UserEmail);

        System.out.print("Возраст:  ");
        int UserAge = scanner.nextInt();
        user.setAge(UserAge);


        saveUs.create(user);









        return true;


    }
    public static void clearConsole() {
        for (int i = 0; i < 50; i++) {
            System.out.println();
        }
    }
}