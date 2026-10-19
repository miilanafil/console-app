package main.java.com.example.consoleapp.ui;


public static void main (String []args){

    Menu menu = new Menu ();
    menu.addItem("1" , "Создать" , new CreateUser());
    menu.addItem("2" , "Прочитать" , );
    menu.addItem("3" , "Изменить" , );
    menu.addItem("4" , "Удалить" , );
    menu.addItem("5" , "Выход" , new ExitAction ());




}