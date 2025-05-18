package userInterface;

import util.graph.GraphGenerator;

import java.util.Scanner;

public class UserControl {
    Scanner scanner = new Scanner(System.in);
    GraphGenerator graphGenerator;
    public UserControl(GraphGenerator graphGenerator){
        this.graphGenerator = graphGenerator;
    }

    public void start(){
        while(true){
            displayCommands();
        }
    }

    private void displayCommands(){
        System.out.println("Welcome to the London Underground Routing system!\n");
        System.out.println("Please select an option from the menu below: \n");
        System.out.println("1. View all stations");
        System.out.println("2. Plan a route");
        System.out.println("3. Exit");

        String input = scanner.nextLine();
        if(input != null || input.length() > 0){
            if(input.equals("1")){
                viewStationMap();
            }
            if(input.equals("2")){
                planRoute();
            }
            if(input.equals("3")){
                System.exit(0);
            }
        }
    }

    private void viewStationMap(){
        graphGenerator.printEntireMap();
    }

    private void planRoute(){
        System.out.println("Please enter the name of the starting station: ");
        String start = scanner.nextLine();
        System.out.println("Please enter the name of the destination station: ");
        String end = scanner.nextLine();
        //graphGenerator.planRoute(start, end);
    }
}
