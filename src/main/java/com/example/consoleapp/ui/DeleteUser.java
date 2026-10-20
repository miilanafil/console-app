package com.example.consoleapp.ui;

import java.util.Scanner;

public class DeleteUser implements MenuAction{


    @Override
    public boolean execute(Scanner scanner) {
        com.example.consoleapp.dao.UserDAOImpl userdel = new  com.example.consoleapp.dao.UserDAOImpl();
        com.example.consoleapp.entity.User user = new com.example.consoleapp.entity.User();

        System.out.print("id:  ");
        String UserID = scanner.nextLine();
        user.setId(Long.valueOf(UserID));

        userdel.delete(user.getId());
        return false;
    }
}
