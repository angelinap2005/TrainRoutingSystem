package util.graph;

import dto.Station;

import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.sql.Timestamp;
import java.util.*;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import org.graphstream.graph.*;
import org.graphstream.algorithm.Dijkstra;
import org.graphstream.ui.view.View;
import org.graphstream.ui.view.Viewer;
import org.graphstream.ui.view.camera.Camera;

/*For shortest path calculations, code taken from:
 * https://graphstream-project.org/doc/Algorithms/Shortest-path/Dijkstra/
 *
 * For BFS algorithm, code taken from:
 * https://graphstream-project.org/doc/Tutorials/Storing-retrieving-and-displaying-data-in-graphs/#an-example-using-attributes-and-the-viewer
 * https://favtutor.com/blogs/breadth-first-search-java
 * https://www.geeksforgeeks.org/breadth-first-search-or-bfs-for-a-graph/
 *
 * For A* algorithm, code taken from:
 * https://www.geeksforgeeks.org/a-search-algorithm/
 * https://www.baeldung.com/java-a-star-pathfinding
 * https://codegym.cc/groups/posts/a-search-algorithm-in-java
 * https://www.geeksforgeeks.org/euclidean-distance
 *
 * Displaying the graph and user controls code taken from:
 * https://stackoverflow.com/questions/44675827/how-to-zoom-into-a-graphstream-view
 */

@Setter
@Getter
public class RouteGenerator{
    private List<Station> stations;
    private Station startStation;
    private Station endStation;
    private Graph graph;

    public RouteGenerator(List<Station> stations, Station startStation, Station endStation, Graph graph) {
        this.stations = stations;
        this.startStation = startStation;
        this.endStation = endStation;
        this.graph = graph;
        System.setProperty("org.graphstream.ui", "swing");
        System.setProperty("org.graphstream.ui.renderer", "org.graphstream.ui.j2dviewer.J2DGraphRenderer");
    }

    //normal calculation methods

    public boolean calculateShortestRoute() {
        Dijkstra dijkstra = new Dijkstra(Dijkstra.Element.EDGE, null, "length");
        Timestamp start = new Timestamp(System.currentTimeMillis());
        String startNodeName = startStation.getRailStation().getName();
        Node startNode = graph.getNode(startNodeName);
        String endNodeName = endStation.getRailStation().getName();
        Node endNode = graph.getNode(endNodeName);

        resetGraphEdges();

        dijkstra.init(graph);
        if (startNode == null || endNode == null) {
            System.err.println("No nodes found");
            return false;
        }

        dijkstra.setSource(startNode);
        //calculate the shortest path
        dijkstra.compute();

        Path path = dijkstra.getPath(endNode);
        if (path != null) {
            setNodeStyle(startNode, endNode, path);
            Timestamp end = new Timestamp(System.currentTimeMillis());
            //print the shortest path
            System.out.println("Calculation completed in " + (end.getTime() - start.getTime()) + " ms");
            System.out.println("\nShortest path found:");
            System.out.printf("Total distance: %.2f km%n", dijkstra.getPathLength(endNode));
            System.out.println("Route: ");
            path.nodes().forEach(node -> System.out.println("  -> " + node.getId()));
        } else {
            System.out.println("No path found between " + startNodeName + " and " + endNodeName);
        }
        return path != null;
    }


    public boolean calculateLeastStationStops(){
        Timestamp start = new Timestamp(System.currentTimeMillis());
        String startNodeName = startStation.getRailStation().getName();
        Node startNode = graph.getNode(startNodeName);
        String endNodeName = endStation.getRailStation().getName();
        Node endNode = graph.getNode(endNodeName);

        resetGraphEdges();
        List<Node> shortestPath = explore(startNode, endNode);

        if (shortestPath != null && shortestPath.size() > 0) {
            //create path object
            Path path = new Path();

            //initialize path with start node
            path.setRoot(shortestPath.get(0));
            //add edges to the path
            for (int i = 0; i < shortestPath.size() - 1; i++) {
                Node current = shortestPath.get(i);
                Node next = shortestPath.get(i + 1);

                //find edge connecting the two nodes
                Edge connectingEdge = null;
                for (Edge edge : current.edges().toList()) {
                    if (edge.getOpposite(current).equals(next)) {
                        connectingEdge = edge;
                        break;
                    }
                }

                if (connectingEdge != null) {
                    //add edge to path
                    path.add(connectingEdge);
                }
            }

            setNodeStyle(startNode, endNode, path);

            Timestamp end = new Timestamp(System.currentTimeMillis());
            System.out.println("Calculation completed in " + (end.getTime() - start.getTime()) + " ms");
            System.out.println("\nRoute with least amount of stops found:");
            System.out.println("Number of stops: " + (shortestPath.size() - 1));
            System.out.println("Route: ");
            shortestPath.forEach(node -> System.out.println("  -> " + node.getId()));
            return shortestPath != null;
        } else {
            System.out.println("No path found between " + startNodeName + " and " + endNodeName);
            return shortestPath == null;
        }
    }

    private List<Node> explore(Node source, Node destination) {
        Queue<Node> queue = new LinkedList<>();
        Set<Node> visited = new HashSet<>();
        Map<Node, Node> predecessors = new HashMap<>();

        //start BFS from the source node
        queue.add(source);
        visited.add(source);

        while (!queue.isEmpty()) {
            Node current = queue.poll();

            //check if destination has been reached
            if (current.equals(destination)) {
                return reconstructPath(predecessors, source, destination);
            }

            //explore all neighbors of the current node
            for (Edge edge : current) {
                Node neighbor = edge.getOpposite(current);
                if (!visited.contains(neighbor)) {
                    visited.add(neighbor);
                    queue.add(neighbor);
                    predecessors.put(neighbor, current);
                }
            }
        }

        return null;
    }

    private List<Node> reconstructPath(Map<Node, Node> predecessors, Node source, Node destination) {
        List<Node> path = new ArrayList<>();
        Node current = destination;

        //build path from destination to source
        while (current != null && !current.equals(source)) {
            path.add(0, current);
            current = predecessors.get(current);
        }

        //add source to path
        if (current != null) {
            path.add(0, source);
        }

        return path;
    }

    //A* methods

    public boolean calculateShortestRouteAStar() {
        Timestamp start = new Timestamp(System.currentTimeMillis());
        String startNodeName = startStation.getRailStation().getName();
        Node startNode = graph.getNode(startNodeName);
        String endNodeName = endStation.getRailStation().getName();
        Node endNode = graph.getNode(endNodeName);
        //A* starting setup
        //cost from start to current node (gScore)
        Map<Node, Double> gScore = new HashMap<>();
        //estimated cost from start to end through current node (fScore)
        Map<Node, Double> fScore = new HashMap<>();
        Map<Node, Node> cameFrom = new HashMap<>();
        Set<Node> closedSet = new HashSet<>();
        //priority queue for open set (nodes to be evaluated)
        PriorityQueue<Node> openSet = new PriorityQueue<>(Comparator.comparingDouble(node -> fScore.getOrDefault(node, Double.MAX_VALUE)));

        resetGraphEdges();

        if (startNode == null || endNode == null) {
            System.err.println("No nodes found");
            return false;
        }

        //initialise scores for all nodes
        for (Node node : graph) {
            gScore.put(node, Double.MAX_VALUE);
            fScore.put(node, Double.MAX_VALUE);
        }
        gScore.put(startNode, 0.0);
        fScore.put(startNode, heuristicCost(startNode, endNode));
        openSet.add(startNode);

        while (!openSet.isEmpty()) {
            Node current = openSet.poll();

            if (current.equals(endNode)) {
                //path found, construct and display it
                //reconstruct the path from end to start to get the correct order
                Path path = reconstructPathAStar(cameFrom, startNode, endNode);
                setNodeStyle(startNode, endNode, path);
                Timestamp end = new Timestamp(System.currentTimeMillis());
                System.out.println("Calculation completed in " + (end.getTime() - start.getTime()) + " ms");
                System.out.println("\nShortest path found:");
                System.out.printf("Total distance: %.2f km%n", gScore.get(endNode));
                System.out.println("Route: ");
                path.nodes().forEach(node -> System.out.println("  -> " + node.getId()));
                return path != null;
            }

            closedSet.add(current);

            for (Edge edge : current.edges().toList()) {
                Node neighbor = edge.getOpposite(current);

                if (closedSet.contains(neighbor)) {
                    //skip already evaluated nodes
                    continue;
                }

                //distance from current to neighbor
                double edgeWeight = edge.getAttribute("length", Double.class);
                double tentativeGScore = gScore.get(current) + edgeWeight;

                if (tentativeGScore < gScore.get(neighbor)) {
                    //this path to neighbor is better
                    cameFrom.put(neighbor, current);
                    gScore.put(neighbor, tentativeGScore);
                    fScore.put(neighbor, tentativeGScore + heuristicCost(neighbor, endNode));

                    if (!openSet.contains(neighbor)) {
                        openSet.add(neighbor);
                    } else {
                        //update the priority queue
                        openSet.remove(neighbor);
                        openSet.add(neighbor);
                    }
                }
            }
        }

        System.out.println("No path found between " + startNodeName + " and " + endNodeName);
        return false;
    }

    //heuristic function for A* algorithm
    private double heuristicCost(Node from, Node to) {
        Station startStation = null;
        Station endStation = null;

        for(Station station : stations){
            if(station.getRailStation().getName().equals(from.getId())) {
                startStation = station;
            }
            if(station.getRailStation().getName().equals(to.getId())) {
                endStation = station;
            }

            //early exit if both stations are found
            if(startStation != null && endStation != null) {
                break;
            }
        }

        //if nodes have coordinates, calculate Euclidean distance
        if(startStation != null && endStation != null){
            Double[] startCoords = startStation.getRailStation().getCoordinates();
            Double[] endCoords = endStation.getRailStation().getCoordinates();

            if (startCoords != null && startCoords.length >= 2 && endCoords != null && endCoords.length >= 2 && startCoords[0] != null && startCoords[1] != null && endCoords[0] != null && endCoords[1] != null) {
                double x1 = startCoords[0];
                double y1 = startCoords[1];
                double x2 = endCoords[0];
                double y2 = endCoords[1];
                return Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
            }
        }

        //only print error for missing stations if they are the actual start/end stations
        if((from.getId().equals(startStation != null ? startStation.getRailStation().getName() : "") || to.getId().equals(endStation != null ? endStation.getRailStation().getName() : "")) && (startStation == null || endStation == null)) {
            System.err.println("Station " + from.getId() + " or station " + to.getId() + " not found in the list of stations.");
        }
        //fallback to 0.0 if coordinates are not available
        return 0.0;
    }

    private Path reconstructPathAStar(Map<Node, Node> cameFrom, Node start, Node end) {
        Path path = new Path();
        Node current = end;

        //build back the path from end to start
        List<Edge> edges = new ArrayList<>();
        while (current != start) {
            Node prev = cameFrom.get(current);
            Edge edge = prev.getEdgeBetween(current);
            edges.add(0, edge);
            current = prev;
        }

        //create a path object with the edges
        if (!edges.isEmpty()) {
            path.setRoot(start);
            for (Edge edge : edges) {
                path.add(edge);
            }
        }

        return path;
    }

    public boolean calculateLeastStationStopsAStar() {
        Timestamp start = new Timestamp(System.currentTimeMillis());
        String startNodeName = startStation.getRailStation().getName();
        Node startNode = graph.getNode(startNodeName);
        String endNodeName = endStation.getRailStation().getName();
        Node endNode = graph.getNode(endNodeName);

        resetGraphEdges();

        if (startNode == null || endNode == null) {
            System.err.println("No nodes found");
            return false;
        }
        //A* starting setup for fewest stops
        Map<Node, Integer> gScore = new HashMap<>();
        Map<Node, Node> cameFrom = new HashMap<>();
        Set<Node> closedSet = new HashSet<>();

        //priority queue for an open set (nodes to be evaluated)
        PriorityQueue<Node> openSet = new PriorityQueue<>((a, b) -> {
            int fScoreA = gScore.getOrDefault(a, Integer.MAX_VALUE) + estimateStopsToGoal(a, endNode);
            int fScoreB = gScore.getOrDefault(b, Integer.MAX_VALUE) + estimateStopsToGoal(b, endNode);
            return Integer.compare(fScoreA, fScoreB);
        });

        gScore.put(startNode, 0);
        openSet.add(startNode);

        //initialise gScore for all nodes
        while (!openSet.isEmpty()) {
            Node current = openSet.poll();

            if (current.equals(endNode)) {
                List<Node> pathNodes = new ArrayList<>();
                Node node = current;

                while (node != null) {
                    pathNodes.add(0, node);
                    node = cameFrom.get(node);
                }

                Path path = new Path();
                path.setRoot(pathNodes.get(0));

                for (int i = 0; i < pathNodes.size() - 1; i++) {
                    Node currentNode = pathNodes.get(i);
                    Node nextNode = pathNodes.get(i + 1);

                    for (Edge edge : currentNode.edges().toList()) {
                        if (edge.getOpposite(currentNode).equals(nextNode)) {
                            path.add(edge);
                            break;
                        }
                    }
                }

                //style start and end nodes
                setNodeStyle(startNode, endNode, path);
                Timestamp end = new Timestamp(System.currentTimeMillis());
                System.out.println("Calculation completed in " + (end.getTime() - start.getTime()) + " ms");
                System.out.println("\nRoute with least amount of stops found:");
                System.out.println("Number of stops: " + (pathNodes.size() - 1));
                System.out.println("Route: ");
                pathNodes.forEach(n -> System.out.println("  -> " + n.getId()));
                return path != null;
            }

            closedSet.add(current);

            //explore neighbors
            for (Edge edge : current.edges().toList()) {
                Node neighbor = edge.getOpposite(current);

                if (closedSet.contains(neighbor)) {
                    continue;
                }

                //calculate tentative gScore
                int tentativeGScore = gScore.get(current) + 1;

                if (!gScore.containsKey(neighbor) || tentativeGScore < gScore.get(neighbor)) {
                    cameFrom.put(neighbor, current);
                    gScore.put(neighbor, tentativeGScore);

                    if (!openSet.contains(neighbor)) {
                        openSet.add(neighbor);
                    } else {
                        openSet.remove(neighbor);
                        openSet.add(neighbor);
                    }
                }
            }
        }

        //if no path found
        System.out.println("No path found between " + startNodeName + " and " + endNodeName);
        return false;
    }

    private int estimateStopsToGoal(Node current, Node goal) {
        //use heuristic cost to estimate the number of stops to the goal
        double distance = heuristicCost(current, goal);
        return (int)(distance * 100);
    }

    //general methods

    public void displayRoute() {
        try {
            Viewer viewer = graph.display();
            viewer.setCloseFramePolicy(Viewer.CloseFramePolicy.HIDE_ONLY);
            viewer.enableAutoLayout();

            //user controls for the graph
            System.out.println("\nGraph Controls:");
            System.out.println("- Use mouse wheel to zoom in/out");
            System.out.println("- Use arrow keys to pan the view");
            System.out.println("- Press 'r' to reset view");

            //create a new thread to handle user input for zooming and panning
            new Thread(() -> {
                try {
                    Thread.sleep(1000);
                    View view = viewer.getDefaultView();
                    if (view != null && view instanceof Component) {
                        Component component = (Component) view;

                        component.addMouseWheelListener(e -> {
                            Camera camera = view.getCamera();
                            double currentZoom = camera.getViewPercent();

                            double zoomFactor = 0.05;

                            //zoom in or out based on mouse wheel rotation
                            if (e.getWheelRotation() < 0) {
                                double newZoom = currentZoom * (1.0 - zoomFactor);
                                camera.setViewPercent(Math.max(newZoom, 0.05));
                            } else {
                                double newZoom = currentZoom * (1.0 + zoomFactor);
                                camera.setViewPercent(Math.min(newZoom, 3.0));
                            }
                        });

                        component.addKeyListener(new KeyAdapter() {
                            @Override
                            public void keyPressed(KeyEvent e) {
                                Camera camera = view.getCamera();

                                switch (e.getKeyCode()) {
                                    case KeyEvent.VK_R:
                                        camera.resetView();
                                        camera.setViewPercent(1.0);
                                        break;
                                }
                            }
                        });

                        //request focus for the component to capture key events
                        component.setFocusable(true);
                        component.requestFocus();
                    }
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            }).start();

        } catch (Exception e) {
            System.err.println("Error displaying graph: " + e.getMessage());
        }

    }

    private void resetGraphEdges() {
        graph.edges().forEach(edge -> {
            String originalColor = edge.getAttribute("original.color").toString();
            if (originalColor != null) {
                edge.setAttribute("ui.style", "fill-color: " + originalColor + ";");
            } else {
                edge.setAttribute("ui.style", "fill-color: #0000FF;");
            }
        });
    }


    private void setNodeStyle(Node startNode, Node endNode, Path path) {
        resetGraphEdges();

        startNode.setAttribute("ui.style",
                "fill-color: green; size: 15px; " +
                        "text-style: bold; text-color: black; text-size: 15px; " +
                        "text-offset: 5px, 5px; text-background-mode: rounded-box; " +
                        "text-background-color: rgba(240, 240, 240, 200); text-padding: 3px;");

        endNode.setAttribute("ui.style",
                "fill-color: red; size: 15px; " +
                        "text-style: bold; text-color: black; text-size: 15px; " +
                        "text-offset: 5px, 5px; text-background-mode: rounded-box; " +
                        "text-background-color: rgba(240, 240, 240, 200); text-padding: 3px;");

        List<String> routeStations = new ArrayList<>();

        List<Node> pathNodes = path.getNodePath();
        for (Node node : pathNodes) {
            if (!node.equals(startNode) && !node.equals(endNode)) {
                node.setAttribute("ui.style",
                        "fill-color: orange; size: 12px; " +
                                "text-style: bold; text-color: black; text-size: 15px; " +
                                "text-offset: 5px, 5px; text-background-mode: rounded-box; " +
                                "text-background-color: rgba(240, 240, 240, 200); text-padding: 3px;");
            }
            routeStations.add(node.getId());
        }

        highlightRouteEdges(routeStations);
    }

    private void highlightRouteEdges(List<String> routeStations) {
        for (int i = 0; i < routeStations.size() - 1; i++) {
            String station1 = routeStations.get(i);
            String station2 = routeStations.get(i + 1);

            Edge edge = graph.getEdge(station1 + "--" + station2);
            if (edge == null) {
                edge = graph.getEdge(station2 + "--" + station1);
            }

            if (edge != null) {
                edge.setAttribute("ui.style", "fill-color: #FF4500; size: 3px;"); // Bright orange
            }
        }
    }

    public boolean calculateLeastChanges() {
        Timestamp start = new Timestamp(System.currentTimeMillis());
        String startNodeName = startStation.getRailStation().getName();
        Node startNode = graph.getNode(startNodeName);
        String endNodeName = endStation.getRailStation().getName();
        Node endNode = graph.getNode(endNodeName);

        resetGraphEdges();

        if (startNode == null || endNode == null) {
            System.err.println("No nodes found");
            return false;
        }

        List<Node> pathWithLeastChanges = findPathWithLeastLineChanges(startNode, endNode);

        if (pathWithLeastChanges != null && pathWithLeastChanges.size() > 0) {
            Path path = new Path();
            path.setRoot(pathWithLeastChanges.get(0));

            for (int i = 0; i < pathWithLeastChanges.size() - 1; i++) {
                Node current = pathWithLeastChanges.get(i);
                Node next = pathWithLeastChanges.get(i + 1);

                Edge connectingEdge = null;
                for (Edge edge : current.edges().toList()) {
                    if (edge.getOpposite(current).equals(next)) {
                        connectingEdge = edge;
                        break;
                    }
                }

                if (connectingEdge != null) {
                    path.add(connectingEdge);
                }
            }

            setNodeStyle(startNode, endNode, path);

            int lineChanges = calculateLineChanges(pathWithLeastChanges);

            Timestamp end = new Timestamp(System.currentTimeMillis());
            System.out.println("Calculation completed in " + (end.getTime() - start.getTime()) + " ms");
            System.out.println("\nRoute with least line changes found:");
            System.out.println("Number of line changes: " + lineChanges);
            System.out.println("Number of stops: " + (pathWithLeastChanges.size() - 1));
            System.out.println("Route: ");
            pathWithLeastChanges.forEach(node -> System.out.println("  -> " + node.getId()));

            displayLineChanges(pathWithLeastChanges);
            return true;
        } else {
            System.out.println("No path found between " + startNodeName + " and " + endNodeName);
            return false;
        }
    }

    private List<Node> findPathWithLeastLineChanges(Node start, Node end) {
        PriorityQueue<PathWithChanges> queue = new PriorityQueue<>(
                Comparator.comparingInt((PathWithChanges p) -> p.lineChanges)
                        .thenComparingInt(p -> p.path.size())
        );

        Set<Node> visited = new HashSet<>();

        PathWithChanges initialPath = new PathWithChanges();
        initialPath.path.add(start);
        initialPath.lineChanges = 0;
        initialPath.currentLines = getLinesForStation(start.getId());

        queue.offer(initialPath);

        while (!queue.isEmpty()) {
            PathWithChanges current = queue.poll();
            Node currentNode = current.path.get(current.path.size() - 1);

            if (currentNode.equals(end)) {
                return current.path;
            }

            if (visited.contains(currentNode)) {
                continue;
            }
            visited.add(currentNode);

            for (Edge edge : currentNode.edges().toList()) {
                Node neighbor = edge.getOpposite(currentNode);

                if (!visited.contains(neighbor)) {
                    PathWithChanges newPath = new PathWithChanges();
                    newPath.path.addAll(current.path);
                    newPath.path.add(neighbor);

                    Set<String> neighborLines = getLinesForStation(neighbor.getId());
                    Set<String> commonLines = new HashSet<>(current.currentLines);
                    commonLines.retainAll(neighborLines);

                    if (commonLines.isEmpty() && current.path.size() > 1) {
                        newPath.lineChanges = current.lineChanges + 1;
                        newPath.currentLines = neighborLines;
                    } else {
                        newPath.lineChanges = current.lineChanges;
                        newPath.currentLines = commonLines.isEmpty() ? neighborLines : commonLines;
                    }

                    queue.offer(newPath);
                }
            }
        }

        return null;
    }

    private Set<String> getLinesForStation(String stationName) {
        Set<String> lines = new HashSet<>();

        for (Station station : stations) {
            if (station.getRailStation().getName().equals(stationName)) {
                if (station.getRailStation().getRailLines() != null) {
                    station.getRailStation().getRailLines().forEach(line -> {
                        if (line.getName() != null) {
                            String lineName = extractLineName(line.getName());
                            if (lineName != null) {
                                lines.add(lineName);
                            }
                        }
                    });
                }
                break;
            }
        }

        return lines;
    }

    private String extractLineName(String fullLineName) {
        if (fullLineName.contains(" - ")) {
            return fullLineName.split(" - ")[0];
        }
        return fullLineName;
    }

    private int calculateLineChanges(List<Node> path) {
        if (path.size() <= 2) return 0;

        int changes = 0;
        Set<String> currentLines = getLinesForStation(path.get(0).getId());

        for (int i = 1; i < path.size(); i++) {
            Set<String> nextLines = getLinesForStation(path.get(i).getId());
            Set<String> commonLines = new HashSet<>(currentLines);
            commonLines.retainAll(nextLines);

            if (commonLines.isEmpty()) {
                changes++;
                currentLines = nextLines;
            } else {
                currentLines = commonLines;
            }
        }

        return changes;
    }

    private void displayLineChanges(List<Node> path) {
        if (path.size() <= 1) return;

        System.out.println("\nDetailed route with line information:");
        Set<String> currentLines = getLinesForStation(path.get(0).getId());

        System.out.println("Start at: " + path.get(0).getId());
        System.out.println("Available lines: " + currentLines);

        for (int i = 1; i < path.size(); i++) {
            Set<String> nextLines = getLinesForStation(path.get(i).getId());
            Set<String> commonLines = new HashSet<>(currentLines);
            commonLines.retainAll(nextLines);

            if (commonLines.isEmpty() && i > 1) {
                System.out.println("\n*** LINE CHANGE at " + path.get(i-1).getId() + " ***");
                System.out.println("From lines: " + currentLines);
                System.out.println("To lines: " + nextLines);
                currentLines = nextLines;
            } else if (!commonLines.isEmpty()) {
                currentLines = commonLines;
            }

            System.out.println("-> " + path.get(i).getId() + " (Lines: " + currentLines + ")");
        }
    }

    private static class PathWithChanges {
        List<Node> path = new ArrayList<>();
        int lineChanges = 0;
        Set<String> currentLines = new HashSet<>();
    }
}