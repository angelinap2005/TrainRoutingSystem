package userInterface;

import util.graph.GraphGenerator;

import java.util.Scanner;
public class UserControl {
    private Scanner scanner = new Scanner(System.in);
    private GraphGenerator graphGenerator;

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
        System.out.println("1. View map of stations");
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
        boolean success = false;
        System.out.println("Please enter the name of the starting station: ");
        String start = scanner.nextLine();
        if(start == null || start.length() == 0){
            System.out.println("Please enter a valid station name.");
            return;
        }
        System.out.println("Please enter the name of the destination station: ");
        String end = scanner.nextLine();
        if(end == null || end.length() == 0){
            System.out.println("Please enter a valid station name.");
            return;
        }
        System.out.println("Would you like to view the shortest route or route with the least amount of stops? (shortest/least) ");
        String decision = scanner.nextLine();
        if(decision != null || decision.length() > 0){
            if(decision.equals("shortest")){
                success = graphGenerator.planRoute(start, end,true);
            }
            if(decision.equals("least")){
                success = graphGenerator.planRoute(start, end,false);
            }
        }

        if(success){
            System.out.println("Would you like to view the map of the route? (y/n) ");
            String input = scanner.nextLine();
            if(input != null || input.length() > 0){
                if(input.equals("y")){
                    graphGenerator.printRoute();
                }
            }
        }else{
            System.out.println("No route found between " + start + " and " + end);
        }
    }
}
