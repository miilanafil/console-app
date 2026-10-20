package com.example.consoleapp.ui;

@FunctionalInterface
interface MenuAction {

    boolean execute(ConsoleIO io);
}
