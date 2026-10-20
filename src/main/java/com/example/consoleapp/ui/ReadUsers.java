package com.example.consoleapp.ui;

import com.example.consoleapp.dao.UserDAO;

import java.util.Objects;


public class ReadUsers implements MenuAction {

    private final UserDAO userDao;
    private final UserTablePrinter printer;

    public ReadUsers(UserDAO userDao, UserTablePrinter printer) {
        this.userDao = Objects.requireNonNull(userDao, "userDao");
        this.printer = Objects.requireNonNull(printer, "printer");
    }

    @Override
    public boolean execute(ConsoleIO io) {
        io.print(printer.render(userDao.findAll()));
        return true;
    }
}
