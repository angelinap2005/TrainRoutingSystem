package util.graph;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.Timer;

import dto.RailLine;
import dto.RailStation;
import org.graphstream.ui.geom.Point3;
import org.graphstream.ui.layout.springbox.implementations.SpringBox;
import lombok.Getter;
import lombok.Setter;
import org.graphstream.graph.*;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.ui.view.View;
import org.graphstream.ui.view.Viewer;
import dto.Route;
import dto.Station;
import org.graphstream.ui.view.camera.Camera;

/*Code references for graph generation:
 * https://graphstream-project.org/doc/
 * https://graphstream-project.org/doc/Tutorials/Storing-retrieving-and-displaying-data-in-graphs/#an-example-using-attributes-and-the-viewer
 */

@Getter
@Setter
public class GraphGenerator {
    private Graph graph;
    private Set<String> processedEdges;
    private GraphObjectGenerator graphObjectGenerator;
    private List<Station> stations;
    private RouteGenerator routeGenerator;

    public GraphGenerator(GraphObjectGenerator graphObjectGenerator) {
        this.graphObjectGenerator = graphObjectGenerator;
        setSystemProperties();
    }

    private void setSystemProperties() {
        this.graph = new SingleGraph("Train Graph");
        this.processedEdges = new HashSet<>();

        System.setProperty("org.graphstream.ui", "swing");
        System.setProperty("org.graphstream.ui.renderer", "org.graphstream.ui.j2dviewer.J2DGraphRenderer");
    }

    public Graph generateGraph(List<Station> stations) {
        this.stations = stations;
        //check if stations list is null
        if (stations == null) {
            throw new IllegalArgumentException("Stations list cannot be null");
        }
        Graph graph = new SingleGraph("RailNetwork");

        graph.setAttribute("ui.quality");
        graph.setAttribute("ui.antialias");

        for (Station station : stations) {
            if (isValidStation(station)) {
                RailStation railStation = station.getRailStation();
                Node node = graph.addNode(railStation.getName());

                //set node attributes
                Double[] coords = railStation.getCoordinates();
                if (coords != null && coords.length >= 2) {
                    double scalingFactor = 1.5;
                    node.setAttribute("x", coords[0] * scalingFactor);
                    node.setAttribute("y", -coords[1] * scalingFactor);
                    node.setAttribute("layout.frozen");
                }

                node.setAttribute("ui.label", railStation.getName());
            }
        }
        try {
            addNodes(stations);
            addEdges(stations);
        } catch (Exception e) {
            System.err.println("Error generating graph: " + e.getMessage());
        }

        return graph;
    }

    private void addNodes(List<Station> stations) {
        for (Station station : stations) {
            if (isValidStation(station)) {
                String nodeName = station.getRailStation().getName();
                try {
                    //check if node already exists
                    Node node = graph.addNode(nodeName);
                    node.setAttribute("ui.label", nodeName);
                } catch (IdAlreadyInUseException e) {
                    System.err.println("Node " + nodeName + " already exists");
                }
            }
        }
    }

    private void addEdges(List<Station> stations) {
        for (Station station : stations) {
            if (!isValidStation(station)) continue;

            String sourceStation = station.getRailStation().getName();

            for (Route route : station.getRoutes()) {
                try {
                    //check if route is valid
                    if (isValidRoute(route)) {
                        //check if destination station is null
                        String destStation = route.getDestination().getName();
                        addEdge(sourceStation, destStation);
                    }
                } catch (Exception e) {
                    System.err.println("Error processing route from " + sourceStation + ": " + e.getMessage());
                }
            }
        }
    }

    private boolean isValidStation(Station station) {
        //check if station is null and has valid attributes
        return station != null && station.getRailStation() != null && station.getRailStation().getName() != null && station.getRoutes() != null;
    }

    private boolean isValidRoute(Route route) {
        //check if route is null and has valid attributes
        return route != null && route.getRailLine() != null && route.getRailLine().getName() != null && route.getDestination() != null && route.getDestination().getName() != null;
    }


    private void addEdge(String sourceStation, String destStation) {
        String edgeId = sourceStation + "--" + destStation;
        String reverseEdgeId = destStation + "--" + sourceStation;

        //check if the edge has already been processed
        if (processedEdges.contains(edgeId) || processedEdges.contains(reverseEdgeId)) {
            return;
        }

        try {
            Edge edge = graph.addEdge(edgeId, sourceStation, destStation, false);
            if (edge != null) {
                //set edge attributes
                Double weight = graphObjectGenerator.getStationDistances().getOrDefault(sourceStation, new HashMap<>()).getOrDefault(destStation, 1.0);
                edge.setAttribute("length", weight);

                String lineColour = getEdgeColour(sourceStation, destStation);
                if (lineColour != null) {
                    edge.setAttribute("ui.style", "fill-color: " + lineColour + ";");
                    edge.setAttribute("original.color", lineColour);
                }

                processedEdges.add(edgeId);
            }
        } catch (Exception e) {
            System.err.println("Warning: Could not create edge between " + sourceStation + " and " + destStation);
        }
    }

    public void resetEdgeColours() {
        graph.edges().forEach(edge -> {
            //reset edge colour to its original value
            String originalColour = edge.getAttribute("original.color").toString();
            if (originalColour != null) {
                //set the edge style to its original colour
                edge.setAttribute("ui.style", "fill-color: " + originalColour + ";");
            }
        });
    }

    private String getEdgeColour(String sourceStation, String destStation) {
        for (Station station : stations) {
            //check if station is null
            if (station.getRailStation().getName().equals(sourceStation)) {
                for (Route route : station.getRoutes()) {
                    //check if route is null
                    if (route.getDestination().getName().equals(destStation)) {
                        RailLine railLine = route.getRailLine();
                        if (railLine != null && railLine.getColour() != null) {
                            //return the colour of the rail line
                            return railLine.getColour();
                        }
                    }
                }
            }
        }
        return "#0000FF";
    }

    public void printEntireMap() {
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

    public boolean planRoute(String start, String end, boolean shortestRoute, boolean aStar, boolean leastChanges) {
        if (start == null || end == null) {
            throw new IllegalArgumentException("Start and end station names cannot be null");
        }
        resetEdgeColours();

        Station startStation = null;
        Station endStation = null;

        //iterate through the stations to find the start and end stations
        for (Station station : getGraphObjectGenerator().getStations()) {
            if (station.getRailStation() != null) {
                String stationName = station.getRailStation().getName();
                if (stationName != null) {
                    if (stationName.equalsIgnoreCase(start)) {
                        startStation = station;
                    }
                    if (stationName.equalsIgnoreCase(end)) {
                        endStation = station;
                    }
                }
            }
        }

        if (startStation == null) {
            System.err.println("Start station '" + start + "' not found in the system");
            return false;
        }

        if (endStation == null) {
            System.err.println("End station '" + end + "' not found in the system");
            return false;
        }

        if (startStation.equals(endStation)) {
            System.err.println("Start and end stations cannot be the same");
            return false;
        }

        try {
            routeGenerator = new RouteGenerator(stations, startStation, endStation, graph);
            if(leastChanges){
                return routeGenerator.calculateLeastChanges();
            }
            if (aStar) {
                //use A* algorithm for route calculation
                return shortestRoute ? routeGenerator.calculateShortestRouteAStar() : routeGenerator.calculateLeastStationStopsAStar();
            }else{
                //use Dijkstra's or BFS algorithm for route calculation
                return shortestRoute ? routeGenerator.calculateShortestRoute() : routeGenerator.calculateLeastStationStops();
            }
        } catch (Exception e) {
            System.err.println("Error calculating route: " + e.getMessage());
            return false;
        }
    }

    public void printRoute() {
        routeGenerator.displayRoute();
    }
}