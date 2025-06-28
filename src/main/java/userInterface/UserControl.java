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
        System.out.println("Welcome to the London Transport System!");
        while(true){
            displayCommands();
        }
    }

    private void displayCommands() {
        System.out.println("Please select an option from the menu below: \n");
        System.out.println("1. View map of stations");
        System.out.println("2. Plan a route");
        System.out.println("3. Plan a route with the A* algorithm");
        System.out.println("4. Plan a route with the least amount of line changes");
        System.out.println("5. Exit");

        String input = scanner.nextLine();

        if (input == null || input.isEmpty() || (!input.equals("1") && !input.equals("2") && !input.equals("3") && !input.equals("4")  && !input.equals("5"))) {
            //error handling for invalid input
            System.out.println("Please enter a valid option (1-5)");
            return;
        }

        try {
            switch (input) {
                case "1":
                    viewStationMap();
                    break;
                case "2":
                    planRoute(false);
                    break;
                case "3":
                    planRoute(true);
                    break;
                case "4":
                    boolean success = planRoute();
                    if (success) {
                        viewMapOfRoute();
                    }
                    break;

                case "5":
                    System.out.println("Thank you for using the London Transport System!");
                    System.exit(0);
                    break;
                default:
                    System.out.println("Please enter a valid option (1-5).");
            }
        } catch (Exception e) {
            System.out.println("An error occurred: " + e.getMessage());
        }
    }

    private void viewStationMap(){
        graphGenerator.printEntireMap();
    }

    private void planRoute(boolean aStar) {
        try {
            System.out.println("Please enter the name of the starting station: ");
            String start = scanner.nextLine();
            if (start == null || start.trim().isEmpty()) {
                System.out.println("Starting station cannot be empty.");
                return;
            }

            System.out.println("Please enter the name of the destination station: ");
            String end = scanner.nextLine();
            if (end == null || end.trim().isEmpty()) {
                System.out.println("Destination station cannot be empty.");
                return;
            }

            if (start.equalsIgnoreCase(end)) {
                System.out.println("Start and destination stations cannot be the same.");
                return;
            }

            System.out.println("Would you like to view the shortest route or route with the least amount of stops? (shortest/least) ");
            String decision = scanner.nextLine();
            if (decision == null || (!decision.equalsIgnoreCase("shortest") && !decision.equalsIgnoreCase("least"))) {
                System.out.println("Please enter either 'shortest' or 'least'.");
                return;
            }

            boolean isShortestRoute = decision.equalsIgnoreCase("shortest");
            boolean success = graphGenerator.planRoute(start, end, isShortestRoute, aStar, false);
            //if the route planning was successful, ask if the user wants to view the map
            if (success) {
                viewMapOfRoute();
            }
        } catch (Exception e) {
            System.out.println("Error planning route: " + e.getMessage());
        }
    }

    private boolean planRoute(){
        try {
            System.out.println("Please enter the name of the starting station: ");
            String start = scanner.nextLine();
            if (start == null || start.trim().isEmpty()) {
                System.out.println("Starting station cannot be empty.");
                return false;
            }

            System.out.println("Please enter the name of the destination station: ");
            String end = scanner.nextLine();
            if (end == null || end.trim().isEmpty()) {
                System.out.println("Destination station cannot be empty.");
                return false;
            }

            if (start.equalsIgnoreCase(end)) {
                System.out.println("Start and destination stations cannot be the same.");
                return false;
            }

            boolean success = graphGenerator.planRoute(start, end, false, false, true);
            return success;

        } catch (Exception e) {
            System.out.println("Error planning route: " + e.getMessage());
        }
        return false;
    }

    private void viewMapOfRoute() {
        System.out.println("Would you like to view the map of the route? (y/n) ");
        String input = scanner.nextLine();

        if (input != null && input.equalsIgnoreCase("y")) {
            graphGenerator.printRoute();
        } else if (input != null && input.equalsIgnoreCase("n")) {
            System.out.println("Thank you for using the London Transport System!");
        } else {
            System.out.println("Invalid input. Please enter 'y' or 'n'.");
            viewMapOfRoute();
        }
    }
}
