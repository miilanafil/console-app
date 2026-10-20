package com.example.consoleapp.ui;

public class ExitAction implements MenuAction {

    @Override
    public boolean execute(ConsoleIO io) {
        io.println("До свидания!");
        return false;
    }
}
