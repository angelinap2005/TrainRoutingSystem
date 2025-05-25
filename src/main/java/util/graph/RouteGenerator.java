package util.graph;

import dto.Station;

import java.sql.Timestamp;
import java.util.*;

import lombok.Getter;
import lombok.Setter;
import org.graphstream.graph.*;
import org.graphstream.algorithm.Dijkstra;

/*For shortest path calculations, code taken from:
* https://graphstream-project.org/doc/Algorithms/Shortest-path/Dijkstra/
*
* For BFS algorithm, code taken from:
* https://graphstream-project.org/doc/Tutorials/Storing-retrieving-and-displaying-data-in-graphs/#an-example-using-attributes-and-the-viewer
* https://favtutor.com/blogs/breadth-first-search-java
* https://www.geeksforgeeks.org/breadth-first-search-or-bfs-for-a-graph/
*
* For A* shortest path algorithm, code taken from:
* https://www.geeksforgeeks.org/a-search-algorithm/
* https://www.baeldung.com/java-a-star-pathfinding
* https://codegym.cc/groups/posts/a-search-algorithm-in-java
*/

@Setter
@Getter
public class RouteGenerator{
    private List<Station> stations;
    private Station startStation;
    private Station endStation;
    private Graph graph;
    private Dijkstra dijkstra;

    public RouteGenerator(List<Station> stations, Station startStation, Station endStation, Graph graph) {
        this.stations = stations;
        this.startStation = startStation;
        this.endStation = endStation;
        this.graph = graph;
        dijkstra = new Dijkstra(Dijkstra.Element.EDGE, null, "length");
        System.setProperty("org.graphstream.ui", "swing");
        System.setProperty("org.graphstream.ui.renderer", "org.graphstream.ui.j2dviewer.J2DGraphRenderer");
        graph.setAttribute("ui.quality");
        graph.setAttribute("ui.antialias");
        graph.setAttribute("ui.stylesheet", "node { size: 10px; fill-color: #666666; text-size: 14; }" + "edge { size: 2px; fill-color: #333333; }");
    }

    public boolean calculateShortestRoute() {
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

    public void displayRoute(){
        graph.display();
    }

    private void resetGraphEdges(){
        graph.edges().forEach(edge -> {
            edge.setAttribute("ui.style", "fill-color: #333333;");
        });
        graph.nodes().forEach(node -> {
            node.setAttribute("ui.style", "fill-color: #666666;");
        });
    }

    private void setNodeStyle(Node startNode, Node endNode, Path path) {
        startNode.setAttribute("ui.style", "fill-color: green;");
        endNode.setAttribute("ui.style", "fill-color: red;");

        path.edges().forEach(edge -> {edge.setAttribute("ui.style", "fill-color: #FF0000; size: 3px;");});
        path.nodes().forEach(node -> {
            if (node != startNode && node != endNode) {
                node.setAttribute("ui.style", "fill-color: #FFA500;");
            }
        });
    }


    public boolean calculateLeastStationStops(){
        String startNodeName = startStation.getRailStation().getName();
        Node startNode = graph.getNode(startNodeName);
        String endNodeName = endStation.getRailStation().getName();
        Node endNode = graph.getNode(endNodeName);

        resetGraphEdges();
        List<Node> shortestPath = explore(startNode, endNode);

        if (shortestPath != null && shortestPath.size() > 0) {
            //create path object
            Path leastStops = new Path();

            //intialise path with start node
            leastStops.setRoot(shortestPath.get(0));

            //style start and end nodes
            startNode.setAttribute("ui.style", "fill-color: green;");
            endNode.setAttribute("ui.style", "fill-color: red;");

            //add each node to the path
            for (int i = 0; i < shortestPath.size() - 1; i++) {
                Node current = shortestPath.get(i);
                Node next = shortestPath.get(i + 1);

                //find edge connecting the two nodes
                Edge connectingEdge = null;
                for (Edge edge : current.edges().toList()) {
                    if (edge.getOpposite(current).equals(next)) {
                        connectingEdge = edge;
                        //highlight the edge
                        edge.setAttribute("ui.style", "fill-color: #FF0000; size: 3px;");
                        break;
                    }
                }

                if (connectingEdge != null) {
                    //add edge to path
                    leastStops.add(connectingEdge);

                    //style the nodes
                    if (next != endNode) {
                        next.setAttribute("ui.style", "fill-color: #FFA500;");
                    }
                }
            }

            System.out.println("\nShortest path found:");
            System.out.println("Number of stops: " + (shortestPath.size() - 1));
            System.out.println("Route: ");
            shortestPath.forEach(node -> System.out.println("  -> " + node.getId()));
        } else {
            System.out.println("No path found between " + startNodeName + " and " + endNodeName);
        }
        return shortestPath != null;
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

    public boolean calculateShortestRouteAStar() {
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

        //A* starting setup
        //cost from start to current node (gScore)
        Map<Node, Double> gScore = new HashMap<>();
        //estimated cost from start to end through current node (fScore)
        Map<Node, Double> fScore = new HashMap<>();
        Map<Node, Node> cameFrom = new HashMap<>();
        Set<Node> closedSet = new HashSet<>();
        //priority queue for open set (nodes to be evaluated)
        PriorityQueue<Node> openSet = new PriorityQueue<>(Comparator.comparingDouble(node -> fScore.getOrDefault(node, Double.MAX_VALUE)));

        //intialize scores for all nodes
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
                Path path = reconstructPathAStar(cameFrom, startNode, endNode);
                setNodeStyle(startNode, endNode, path);
                Timestamp end = new Timestamp(System.currentTimeMillis());
                System.out.println("A* calculation completed in " + (end.getTime() - start.getTime()) + " ms");
                System.out.println("\nShortest path found:");
                System.out.printf("Total distance: %.2f km%n", gScore.get(endNode));
                System.out.println("Route: ");
                path.nodes().forEach(node -> System.out.println("  -> " + node.getId()));
                return true;
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
        //if nodes have x and y attributes, calculate Euclidean distance
        if (from.hasAttribute("x") && from.hasAttribute("y") && to.hasAttribute("x") && to.hasAttribute("y")) {
            double x1 = from.getAttribute("x", Double.class);
            double y1 = from.getAttribute("y", Double.class);
            double x2 = to.getAttribute("x", Double.class);
            double y2 = to.getAttribute("y", Double.class);
            return Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
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

    public boolean calculateLeastStationStopsAStar(){
        return false;
    }
}
