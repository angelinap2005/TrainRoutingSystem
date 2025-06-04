package util.graph;

import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.*;
import java.util.List;

import dto.RailStation;
import lombok.Getter;
import lombok.Setter;
import org.graphstream.graph.*;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.ui.view.View;
import org.graphstream.ui.view.Viewer;
import dto.Route;
import dto.Station;
import org.graphstream.ui.view.camera.Camera;

import javax.swing.*;

/*For graph generation, code was taken from
* https://graphstream-project.org/doc/Tutorials/Getting-Started/
* https://graphstream-project.org/doc/Tutorials/Graph-Visualisation/
* https://stackoverflow.com/questions/67331322/show-the-names-of-nodes-and-edges-using-graphstream-in-scala
*
* Displaying the graph and user controls code taken from:
* https://stackoverflow.com/questions/44675827/how-to-zoom-into-a-graphstream-view
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
        //set graph attributes
        this.graph = new SingleGraph("Train Graph");
        this.processedEdges = new HashSet<>();

        System.setProperty("org.graphstream.ui", "swing");
        System.setProperty("org.graphstream.ui.renderer", "org.graphstream.ui.j2dviewer.J2DGraphRenderer");
    }


    public Graph generateGraph(List<Station> stations) {
        this.stations = stations;
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

                Double[] coords = railStation.getCoordinates();
                if (coords != null && coords.length >= 2) {
                    node.setAttribute("x", coords[0]);
                    node.setAttribute("y", -coords[1]);
                    node.setAttribute("layout.frozen");
                }

                node.setAttribute("ui.label", railStation.getName());
            }
        }
        try {
            //clear the graph
            graph.clear();
            processedEdges.clear();
            //add graph nodes
            addNodes(stations);
            //add graph edges
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
                    Node node = graph.addNode(nodeName);
                    //set the node label to station name
                    node.setAttribute("ui.label", nodeName);
                } catch (IdAlreadyInUseException e) {
                    //if node already exists, ignore
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
                    if (isValidRoute(route)) {
                        String destStation = route.getDestination().getName();
                        addEdgeSafely(sourceStation, destStation);
                    }
                } catch (Exception e) {
                    System.err.println("Error processing route from " + sourceStation + ": " + e.getMessage());
                }
            }
        }
    }

    private boolean isValidStation(Station station) {
        return station != null && station.getRailStation() != null && station.getRailStation().getName() != null && station.getRoutes() != null;
    }

    private boolean isValidRoute(Route route) {
        return route != null && route.getRailLine() != null && route.getRailLine().getName() != null && route.getDestination() != null && route.getDestination().getName() != null;
    }

    private void addEdgeSafely(String sourceStation, String destStation) {
        String edgeId = sourceStation + "--" + destStation;
        String reverseEdgeId = destStation + "--" + sourceStation;

        if (processedEdges.contains(edgeId) || processedEdges.contains(reverseEdgeId)) {
            //if edge already exists, ignore
            return;
        }

        try {
            Edge edge = graph.addEdge(edgeId, sourceStation, destStation, false);
            if (edge != null) {
                //get weight from distance map
                Double weight = graphObjectGenerator.getStationDistances().getOrDefault(sourceStation, new HashMap<>()).getOrDefault(destStation, 1.0);

                //set length as weight attribute
                edge.setAttribute("length", weight);
                processedEdges.add(edgeId);
            }
        } catch (Exception e) {
            System.err.println("Warning: Could not create edge between " + sourceStation + " and " + destStation);
        }
    }



    public void printEntireMap() {
        try {
            setGraphStyles();
            //display the graph
            Viewer viewer = graph.display();
            viewer.setCloseFramePolicy(Viewer.CloseFramePolicy.HIDE_ONLY);
            viewer.enableAutoLayout();

            //user controls
            System.out.println("\nGraph Controls:");
            System.out.println("- Use mouse wheel to zoom in/out");
            System.out.println("- Use arrow keys to pan the view");
            System.out.println("- Press 'r' to reset view");
            //create a thread to handle user input for zooming and panning
            new Thread(() -> {
                try {
                    Thread.sleep(1000);
                    View view = viewer.getDefaultView();
                    if (view != null && view instanceof Component) {
                        Component component = (Component) view;

                        //when the component is focused, set up mouse wheel and key listeners
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
                            //when r is pressed, reset the camera view
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


    private void setGraphStyles(){
        //set default node and edge styles
        graph.edges().forEach(edge -> {
            edge.setAttribute("ui.style", "fill-color: blue;");
        });
        graph.nodes().forEach(node -> {
            node.setAttribute("ui.style", "fill-color: #888888; " + "text-style: bold; " + "text-color: black;" + "text-size: 15px;");
        });
    }


    public boolean planRoute(String start, String end, boolean shortestRoute, boolean aStar) {
        //validate input
        if (start == null || end == null) {
            throw new IllegalArgumentException("Start and end station names cannot be null");
        }

        Station startStation = null;
        Station endStation = null;

        //find the start and end stations in the graph
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

        //check if start and end stations were found
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
            if (aStar) {
                return shortestRoute ? routeGenerator.calculateShortestRouteAStar() : routeGenerator.calculateLeastStationStopsAStar();
            }else{
                return shortestRoute ? routeGenerator.calculateShortestRoute() : routeGenerator.calculateLeastStationStops();

            }
        } catch (Exception e) {
            //error handling for route calculation
            System.err.println("Error calculating route: " + e.getMessage());
            return false;
        }
    }

    public void printRoute(){
        routeGenerator.displayRoute();
    }

}