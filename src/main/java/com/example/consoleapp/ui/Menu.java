package main.java.com.example.consoleapp.ui;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;

public class Menu {

private final Map<String , MenuAction > actions = new LinkedHashMap<>();
private final Map<String , String > labels = new LinkedHashMap<>();


//метод для отрисовки меню
public void addItem (String key , String label , MenuAction action){

    labels.put (key , label );
    actions.put (key , action);

}
public void run() {
    Scanner scanner = new Scanner(System.in);

    while (true){
        printMenu();

        System.out.print("выберите пункт:");
        String ch = scanner.nextLine().trim();

        MenuAction action = actions.get (ch);
        if (action == null){ System.out.println("неверный пункт"); continue;}

        boolean keepRun = action.execute (scanner);
        if (!keepRun){break;}
    }
scanner.close();

}

public void printMenu(){
    System.out.println("------------------");
    labels.forEach ((key , lable)->System.out.printf("%s. %s%n", key, label));
    System.out.println("------------------");
}

}