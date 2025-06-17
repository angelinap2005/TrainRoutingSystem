
package util.graph;

import dto.Station;

import java.awt.*;
import java.sql.Timestamp;
import java.util.*;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import org.graphstream.graph.*;
import org.graphstream.algorithm.Dijkstra;
import org.graphstream.ui.geom.Point3;
import org.graphstream.ui.layout.springbox.implementations.SpringBox;
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
 *
 * Least line changes algorithm code taken from:
 * https://stackoverflow.com/questions/3137548/how-to-find-minimum-number-of-transfers-for-a-metro-or-railway-network
 * https://github.com/wlxiong/k_shortest_bus_routes
 * https://www.geeksforgeeks.org/dijkstras-shortest-path-algorithm-in-java-using-priorityqueue
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

    private NodesResult getAndValidateNodes() {
        //check if start and end stations are set
        String startNodeName = startStation.getRailStation().getName();
        Node startNode = graph.getNode(startNodeName);
        String endNodeName = endStation.getRailStation().getName();
        Node endNode = graph.getNode(endNodeName);

        if (startNode == null || endNode == null) {
            System.err.println("No nodes found");
            return null;
        }

        return new NodesResult(startNode, endNode, startNodeName, endNodeName);
    }

    private void printRouteResults(Timestamp start, String routeType, Path path, List<Node> pathNodes, Double distance) {
        Timestamp end = new Timestamp(System.currentTimeMillis());
        //print the details of the calculation
        System.out.println("Calculation completed in " + (end.getTime() - start.getTime()) + " ms");
        System.out.println("\n" + routeType + " found:");

        if (distance != null) {
            System.out.printf("Total distance: %.2f km%n", distance);
        }
        if (pathNodes != null && pathNodes.size() > 1) {
            System.out.println("Number of stops: " + (pathNodes.size() - 1));
        }

        System.out.println("Route: ");
        if (path != null) {
            path.nodes().forEach(node -> System.out.println("  -> " + node.getId()));
        } else if (pathNodes != null) {
            pathNodes.forEach(node -> System.out.println("  -> " + node.getId()));
        }
    }

    private void displayNoRouteFound(String startNodeName, String endNodeName) {
        System.out.println("No path found between " + startNodeName + " and " + endNodeName);
    }

    private Path createPathFromNodes(List<Node> nodeList) {
        if (nodeList == null || nodeList.isEmpty()) {
            return null;
        }

        Path path = new Path();
        //initialise path with start node
        path.setRoot(nodeList.get(0));
        //add edges to the path
        for (int i = 0; i < nodeList.size() - 1; i++) {
            Node current = nodeList.get(i);
            Node next = nodeList.get(i + 1);

            //find edge connecting the two nodes
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

        return path;
    }

    public boolean calculateShortestRoute() {
        Dijkstra dijkstra = new Dijkstra(Dijkstra.Element.EDGE, null, "length");
        Timestamp start = new Timestamp(System.currentTimeMillis());

        //get and validate start and end nodes
        NodesResult nodesResult = getAndValidateNodes();
        if (nodesResult == null) {
            return false;
        }

        dijkstra.init(graph);
        dijkstra.setSource(nodesResult.startNode);
        //calculate the shortest path
        dijkstra.compute();

        Path path = dijkstra.getPath(nodesResult.endNode);
        if (path != null) {
            setNodeStyle(nodesResult.startNode, nodesResult.endNode, path);
            printRouteResults(start, "Shortest path", path, null, dijkstra.getPathLength(nodesResult.endNode));
            return true;
        } else {
            displayNoRouteFound(nodesResult.startNodeName, nodesResult.endNodeName);
        }
        return false;
    }

    public boolean calculateLeastStationStops(){
        Timestamp start = new Timestamp(System.currentTimeMillis());

        NodesResult nodesResult = getAndValidateNodes();
        if (nodesResult == null) {
            return false;
        }
        //BFS to find the shortest path with the least number of stops
        List<Node> shortestPath = explore(nodesResult.startNode, nodesResult.endNode);

        if (shortestPath != null && shortestPath.size() > 0) {
            Path path = createPathFromNodes(shortestPath);
            setNodeStyle(nodesResult.startNode, nodesResult.endNode, path);
            printRouteResults(start, "Route with least amount of stops", null, shortestPath, null);
            return true;
        } else {
            displayNoRouteFound(nodesResult.startNodeName, nodesResult.endNodeName);
            return false;
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

            //explore all neighbours of the current node
            for (Edge edge : current) {
                Node neighbour = edge.getOpposite(current);
                if (!visited.contains(neighbour)) {
                    visited.add(neighbour);
                    queue.add(neighbour);
                    predecessors.put(neighbour, current);
                }
            }
        }

        return null;
    }

    private List<Node> reconstructPath(Map<Node, Node> predecessors, Node source, Node destination) {
        List<Node> path = new ArrayList<>();
        Node current = destination;

        //build the path from end to start to ensure the order is correct
        while (current != null && !current.equals(source)) {
            path.add(0, current);
            current = predecessors.get(current);
        }

        if (current != null) {
            path.add(0, source);
        }

        return path;
    }

    public boolean calculateShortestRouteAStar() {
        Timestamp start = new Timestamp(System.currentTimeMillis());

        NodesResult nodesResult = getAndValidateNodes();
        if (nodesResult == null) {
            return false;
        }

        //g and f scores for A* algorithm
        Map<Node, Double> gScore = new HashMap<>();
        Map<Node, Double> fScore = new HashMap<>();
        Map<Node, Node> cameFrom = new HashMap<>();
        Set<Node> closedSet = new HashSet<>();
        //priority queue for an open set (nodes to be evaluated)
        PriorityQueue<Node> openSet = new PriorityQueue<>(Comparator.comparingDouble(node -> fScore.getOrDefault(node, Double.MAX_VALUE)));

        //initialise scores for all nodes
        for (Node node : graph) {
            gScore.put(node, Double.MAX_VALUE);
            fScore.put(node, Double.MAX_VALUE);
        }
        gScore.put(nodesResult.startNode, 0.0);
        fScore.put(nodesResult.startNode, heuristicCost(nodesResult.startNode, nodesResult.endNode));
        openSet.add(nodesResult.startNode);

        while (!openSet.isEmpty()) {
            //get the node with the lowest fScore from the open set
            Node current = openSet.poll();

            if (current.equals(nodesResult.endNode)) {
                //if the end node is reached, reconstruct the path
                Path path = reconstructPathAStar(cameFrom, nodesResult.startNode, nodesResult.endNode);
                setNodeStyle(nodesResult.startNode, nodesResult.endNode, path);
                printRouteResults(start, "Shortest path", path, null, gScore.get(nodesResult.endNode));
                return true;
            }

            closedSet.add(current);

            for (Edge edge : current.edges().toList()) {
                Node neighbour = edge.getOpposite(current);

                if (closedSet.contains(neighbour)) {
                    //skip already evaluated nodes
                    continue;
                }

                //if the neighbour is not in the open set, add it
                double edgeWeight = edge.getAttribute("length", Double.class);
                double tentativeGScore = gScore.get(current) + edgeWeight;

                //if the neighbour is not in the open set or the tentative gScore is better than the current gScore
                if (tentativeGScore < gScore.get(neighbour)) {
                    //update the scores and the path
                    cameFrom.put(neighbour, current);
                    gScore.put(neighbour, tentativeGScore);
                    fScore.put(neighbour, tentativeGScore + heuristicCost(neighbour, nodesResult.endNode));

                    if (!openSet.contains(neighbour)) {
                        openSet.add(neighbour);
                    } else {
                        openSet.remove(neighbour);
                        openSet.add(neighbour);
                    }
                }
            }
        }

        displayNoRouteFound(nodesResult.startNodeName, nodesResult.endNodeName);
        return false;
    }

    private double heuristicCost(Node from, Node to) {
        Station startStation = null;
        Station endStation = null;

        //find the start and end stations in the list of stations
        for(Station station : stations){
            //check if the station matches the start or end node
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

        //calculate Euclidean distance
        if(startStation != null && endStation != null && startStation.getRailStation().getCoordinates() != null && endStation.getRailStation().getCoordinates() != null) {
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
        if((from.getId().equals(startStation != null ? startStation.getRailStation().getName() : "") || to.getId().equals(endStation != null ? endStation.getRailStation().getName() : "")) && (startStation == null || endStation == null)) {
            System.err.println("Station " + from.getId() + " or station " + to.getId() + " not found in the list of stations.");
        }
        return 0.0;
    }

    private Path reconstructPathAStar(Map<Node, Node> cameFrom, Node start, Node end) {
        Path path = new Path();
        Node current = end;

        //reconstruct the path from end to start using the cameFrom map
        List<Edge> edges = new ArrayList<>();
        while (current != start) {
            Node prev = cameFrom.get(current);
            Edge edge = prev.getEdgeBetween(current);
            edges.add(0, edge);
            current = prev;
        }

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

        NodesResult nodesResult = getAndValidateNodes();
        if (nodesResult == null) {
            return false;
        }

        //A* starting setup for fewest stops
        Map<Node, Integer> gScore = new HashMap<>();
        Map<Node, Node> cameFrom = new HashMap<>();
        Set<Node> closedSet = new HashSet<>();

        //priority queue for an open set (nodes to be evaluated)
        PriorityQueue<Node> openSet = new PriorityQueue<>(
                Comparator.comparingInt(node -> gScore.getOrDefault(node, Integer.MAX_VALUE))
        );


        gScore.put(nodesResult.startNode, 0);
        openSet.add(nodesResult.startNode);

        while (!openSet.isEmpty()) {
            Node current = openSet.poll();

            //if the current node is the end node, reconstruct the path
            if (current.equals(nodesResult.endNode)) {
                List<Node> pathNodes = new ArrayList<>();
                Node node = current;

                while (node != null) {
                    pathNodes.add(0, node);
                    node = cameFrom.get(node);
                }

                Path path = createPathFromNodes(pathNodes);

                setNodeStyle(nodesResult.startNode, nodesResult.endNode, path);
                printRouteResults(start, "Route with least amount of stops", null, pathNodes, null);
                return true;
            }

            closedSet.add(current);

            //explore neighbours
            for (Edge edge : current.edges().toList()) {
                Node neighbour = edge.getOpposite(current);

                if (closedSet.contains(neighbour)) {
                    continue;
                }

                int tentativeGScore = gScore.get(current) + 1;

                if (!gScore.containsKey(neighbour) || tentativeGScore < gScore.get(neighbour)) {
                    //update the gScore and cameFrom map
                    cameFrom.put(neighbour, current);
                    gScore.put(neighbour, tentativeGScore);

                    //if the neighbour is not in the open set, add it
                    if (!openSet.contains(neighbour)) {
                        openSet.add(neighbour);
                    } else {
                        openSet.remove(neighbour);
                        openSet.add(neighbour);
                    }
                }
            }
        }
        displayNoRouteFound(nodesResult.startNodeName, nodesResult.endNodeName);
        return false;
    }

    public void displayRoute() {
        try {
            //generate the graph
            configureGraphStyles();
            Viewer viewer = graph.display();
            viewer.setCloseFramePolicy(Viewer.CloseFramePolicy.HIDE_ONLY);

            //set the default view
            SpringBox layout = new SpringBox(false);
            layout.setForce(0.7);
            layout.setQuality(1);
            layout.setStabilizationLimit(0.9);
            viewer.enableAutoLayout(layout);

            new Timer().schedule(new TimerTask() {
                //disable auto layout after 3 seconds to allow user interaction
                @Override
                public void run() {
                    viewer.disableAutoLayout();
                }
            }, 3000);

            System.out.println("\nGraph Controls:");
            System.out.println("- Use mouse wheel to zoom in/out");
            System.out.println("- Use arrow keys to pan the view");
            System.out.println("- Press shift 'r' to reset view");

            new Thread(() -> {
                try {
                    Thread.sleep(1000);
                    View view = viewer.getDefaultView();

                    if (view != null && view instanceof Component) {
                        Component component = (Component) view;

                        //enable mouse wheel zooming
                        component.addMouseWheelListener(e -> {
                            Camera camera = view.getCamera();
                            double currentZoom = camera.getViewPercent();
                            double zoomFactor = 0.05;
                            Point3 center = camera.getViewCenter();

                            //check if the mouse wheel rotation is negative (zooming in) or positive (zooming out)
                            if (e.getWheelRotation() < 0) {
                                double newZoom = currentZoom * (1.0 - zoomFactor);
                                newZoom = Math.max(newZoom, 0.05);

                                camera.setAutoFitView(false);
                                camera.setViewPercent(newZoom);

                                //set the viewport size based on the new zoom level
                                double viewportSize = Math.max(20, 10/newZoom);
                                camera.setGraphViewport(-viewportSize, -viewportSize, viewportSize, viewportSize);

                                double finalNewZoom = newZoom;
                                graph.nodes().forEach(node -> {
                                    double scale = Math.min(1.5, 1.0/ finalNewZoom * 0.3);
                                    node.setAttribute("ui.size", 10 * scale);
                                    node.setAttribute("ui.style", "text-size: 12px; z-index: 1000;");
                                });
                            } else {
                                double newZoom = currentZoom * (1.0 + zoomFactor);
                                newZoom = Math.min(newZoom, 3.0);
                                camera.setViewPercent(newZoom);

                                //set the viewport size based on the new zoom level
                                graph.nodes().forEach(node -> {
                                    node.setAttribute("ui.size", 10);
                                    node.setAttribute("ui.style", "text-size: 12px; z-index: 100;");
                                });
                            }

                            //recenter the camera after zooming
                            camera.setViewCenter(center.x, center.y, 0);
                        });
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

    private void configureGraphStyles() {
        //set graph attributes
        graph.setAttribute("ui.quality");
        graph.setAttribute("ui.antialias");

        //set the stylesheet for the graph
        String stylesheet =
                "graph { padding: 50px; } " +
                        "node { " +
                        "size: 10px; " +
                        "fill-color: #999999; " +
                        "text-style: bold; " +
                        "text-color: black; " +
                        "text-size: 15px; " +
                        "text-offset: 5px, 5px; " +
                        "text-padding: 3px; " +
                        "} " +
                        "edge { " +
                        "arrow-size: 5px, 4px; " +
                        "size-mode: dyn-size; " +
                        "size: 2px; " +
                        "}";

        graph.setAttribute("ui.stylesheet", stylesheet);

        //set default edge attributes
        graph.edges().forEach(edge -> {
            edge.setAttribute("weight", 2.0);
        });
    }

    private void resetGraphEdges() {
        //reset all edges to their original colour
        graph.edges().forEach(edge -> {
            String originalColour = edge.getAttribute("original.color").toString();
            if (originalColour != null) {
                edge.setAttribute("ui.style", "fill-color: " + originalColour + ";");
            } else {
                edge.setAttribute("ui.style", "fill-color: #0000FF;");
            }
        });
    }

    private void setNodeStyle(Node startNode, Node endNode, Path path) {
        resetGraphEdges();

        //style start and end nodes
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

        //style nodes in the path
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
        //highlight edges between stations in the route
        for (int i = 0; i < routeStations.size() - 1; i++) {
            String station1 = routeStations.get(i);
            String station2 = routeStations.get(i + 1);

            Edge edge = graph.getEdge(station1 + "--" + station2);
            if (edge == null) {
                edge = graph.getEdge(station2 + "--" + station1);
            }

            if (edge != null) {
                //store original color for reset
                edge.setAttribute("ui.style", "fill-color: #FF4500; size: 3px;");
            }
        }
    }

    public boolean calculateLeastChanges() {
        Timestamp start = new Timestamp(System.currentTimeMillis());

        NodesResult nodesResult = getAndValidateNodes();
        if (nodesResult == null) {
            return false;
        }

        //find path with least line changes
        List<Node> pathWithLeastChanges = findPathWithLeastLineChanges(nodesResult.startNode, nodesResult.endNode);

        //if a path is found, create a Path object and display it
        if (pathWithLeastChanges != null && pathWithLeastChanges.size() > 0) {
            Path path = createPathFromNodes(pathWithLeastChanges);

            setNodeStyle(nodesResult.startNode, nodesResult.endNode, path);

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
            displayNoRouteFound(nodesResult.startNodeName, nodesResult.endNodeName);
            return false;
        }
    }

    private List<Node> findPathWithLeastLineChanges(Node start, Node end) {
        //use a priority queue to explore paths with the least line changes first
        PriorityQueue<PathWithChanges> queue = new PriorityQueue<>(Comparator.comparingInt((PathWithChanges p) -> p.lineChanges).thenComparingInt(p -> p.path.size()));

        Set<Node> visited = new HashSet<>();
        PathWithChanges initialPath = new PathWithChanges();
        initialPath.path.add(start);
        initialPath.lineChanges = 0;
        initialPath.currentLines = getLinesForStation(start.getId());

        //add the initial path to the queue
        queue.offer(initialPath);

        while (!queue.isEmpty()) {
            //get the path with the least line changes
            PathWithChanges current = queue.poll();
            Node currentNode = current.path.get(current.path.size() - 1);

            //check if the end node is reached
            if (currentNode.equals(end)) {
                return current.path;
            }

            //if the current node has already been visited, skip it
            if (visited.contains(currentNode)) {
                continue;
            }
            visited.add(currentNode);

            for (Edge edge : currentNode.edges().toList()) {
                Node neighbour = edge.getOpposite(currentNode);

                if (!visited.contains(neighbour)) {
                    //create a new path with the current node and the neighbour
                    PathWithChanges newPath = new PathWithChanges();
                    newPath.path.addAll(current.path);
                    newPath.path.add(neighbour);

                    Set<String> neighbourLines = getLinesForStation(neighbour.getId());
                    Set<String> commonLines = new HashSet<>(current.currentLines);
                    commonLines.retainAll(neighbourLines);

                    //if there are no common lines, a line change is needed
                    if (commonLines.isEmpty() && current.path.size() > 1) {
                        newPath.lineChanges = current.lineChanges + 1;
                        newPath.currentLines = neighbourLines;
                    } else {
                        newPath.lineChanges = current.lineChanges;
                        newPath.currentLines = commonLines.isEmpty() ? neighbourLines : commonLines;
                    }

                    queue.offer(newPath);
                }
            }
        }

        return null;
    }

    private Set<String> getLinesForStation(String stationName) {
        Set<String> lines = new HashSet<>();

        //find the station by name
        for (Station station : stations) {
            if (station.getRailStation().getName().equals(stationName)) {
                //if the station has rail lines, get the names of the lines
                if (station.getRailStation().getRailLines() != null) {
                    station.getRailStation().getRailLines().forEach(line -> {
                        if (line.getName() != null) {
                            String lineName = getLineName(line.getName());
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

    private String getLineName(String fullLineName) {
        if (fullLineName.contains(" - ")) {
            return fullLineName.split(" - ")[0];
        }
        return fullLineName;
    }

    private int calculateLineChanges(List<Node> path) {
        if (path.size() <= 2) return 0;

        //calculate the number of line changes in the path
        int changes = 0;
        Set<String> currentLines = getLinesForStation(path.get(0).getId());

        for (int i = 1; i < path.size(); i++) {
            //get lines for the next station
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

        //display detailed route with line information
        System.out.println("\nDetailed route with line change information:");
        Set<String> currentLines = getLinesForStation(path.get(0).getId());

        System.out.println("Start at: " + path.get(0).getId());
        System.out.println("Available lines: " + currentLines);

        //iterate through the path and display line changes
        for (int i = 1; i < path.size(); i++) {
            Set<String> nextLines = getLinesForStation(path.get(i).getId());
            Set<String> commonLines = new HashSet<>(currentLines);
            commonLines.retainAll(nextLines);

            if (commonLines.isEmpty() && i > 1) {
                System.out.println("\nLine Change at: " + path.get(i-1).getId());
                System.out.println("From lines: " + currentLines);
                System.out.println("To lines: " + nextLines);
                currentLines = nextLines;
            } else if (!commonLines.isEmpty()) {
                currentLines = commonLines;
            }

            //display the current station and available lines
            System.out.println("-> " + path.get(i).getId() + " (Lines: " + currentLines + ")");
        }
    }

    private record NodesResult(Node startNode, Node endNode, String startNodeName, String endNodeName) {}

    private static class PathWithChanges {
        //stores the path and the number of line changes
        List<Node> path = new ArrayList<>();
        int lineChanges = 0;
        Set<String> currentLines = new HashSet<>();
    }
}