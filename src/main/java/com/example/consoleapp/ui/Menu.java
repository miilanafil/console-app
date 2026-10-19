import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;

public class Menu {

private final Map<String , MenuAction> actions = new LinkHashMap<>();
private final Map<String , Srting > labels = new LinkHashMap<>();


//метод для отрисовки меню
public void addItem (String key , String label , MenyAction action){

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
    libles.forEach ((key , lable)->System.out.printf("%s. %s%n", key, label));
    System.out.println("------------------");
}

}