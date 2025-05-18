
package util.Graph;

import java.util.List;
import java.util.HashSet;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import org.graphstream.graph.*;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.ui.view.Viewer;
import dto.Route;
import dto.Station;

/* code from https://graphstream-project.org/doc/ */
@Getter
@Setter
public class GraphGenerator {
    private Graph graph;
    private Set<String> processedEdges;

    public GraphGenerator() {
        System.setProperty("org.graphstream.ui", "swing");
        System.setProperty("org.graphstream.ui.renderer", "org.graphstream.ui.j2dviewer.J2DGraphRenderer");
        this.graph = new SingleGraph("Train Graph");
        this.processedEdges = new HashSet<>();

        // Add default styling
        graph.setAttribute("ui.quality");
        graph.setAttribute("ui.antialias");
        graph.setAttribute("ui.stylesheet",
                "node { size: 10px; fill-color: #666666; text-size: 14; }" +
                        "edge { size: 2px; fill-color: #333333; }");
    }

    public Graph generateGraph(List<Station> stations) {
        if (stations == null) {
            throw new IllegalArgumentException("Stations list cannot be null");
        }

        try {
            // Clear existing graph
            graph.clear();
            processedEdges.clear();

            // Add nodes first
            addNodes(stations);
            // Then add edges
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
                    node.setAttribute("ui.label", nodeName);
                } catch (IdAlreadyInUseException e) {
                    // Node already exists, skip
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
                    System.err.println("Error processing route from " + sourceStation +
                            ": " + e.getMessage());
                }
            }
        }
    }

    private boolean isValidStation(Station station) {
        return station != null &&
                station.getRailStation() != null &&
                station.getRailStation().getName() != null &&
                station.getRoutes() != null;
    }

    private boolean isValidRoute(Route route) {
        return route != null &&
                route.getRailLine() != null &&
                route.getRailLine().getName() != null &&
                route.getDestination() != null &&
                route.getDestination().getName() != null;
    }

    private void addEdgeSafely(String sourceStation, String destStation) {
        // Create unique edge identifiers for both directions
        String edgeId = sourceStation + "--" + destStation;
        String reverseEdgeId = destStation + "--" + sourceStation;

        // Check if we've already processed this edge in either direction
        if (processedEdges.contains(edgeId) || processedEdges.contains(reverseEdgeId)) {
            return;
        }

        try {
            // Add the edge with a unique ID
            Edge edge = graph.addEdge(edgeId, sourceStation, destStation, false); // false for undirected edge
            if (edge != null) {
                processedEdges.add(edgeId);
            }
        } catch (Exception e) {
            // Log error but don't throw exception to allow processing to continue
            System.err.println("Warning: Could not create edge between " +
                    sourceStation + " and " + destStation);
        }
    }

    public void printGraph() {
        try {
            Viewer viewer = graph.display();
            viewer.setCloseFramePolicy(Viewer.CloseFramePolicy.HIDE_ONLY);
        } catch (Exception e) {
            System.err.println("Error displaying graph. Make sure GraphStream UI is properly configured: " +
                    e.getMessage());
        }
    }
}