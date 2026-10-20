package com.example.consoleapp.ui;

import java.util.Scanner;

public class UpdateUser implements MenuAction {

    public  boolean execute (Scanner scanner) {
        com.example.consoleapp.dao.UserDAOImpl usercreate = new  com.example.consoleapp.dao.UserDAOImpl();
        com.example.consoleapp.entity.User user = new com.example.consoleapp.entity.User();

        System.out.print("id:  ");
        String UserID = scanner.nextLine();
        user.setId(Long.valueOf(UserID));
        //usercreate.setName(UserName);

        usercreate.update(user);
        return true;
    }
}
