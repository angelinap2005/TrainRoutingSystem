package util.graph;

import dto.Station;

import java.util.List;

import lombok.Getter;
import lombok.Setter;
import org.graphstream.graph.*;
import org.graphstream.algorithm.Dijkstra;

/*For shortest path calculations, code taken from:
* https://graphstream-project.org/doc/Algorithms/Shortest-path/Dijkstra/
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

    public void calculateShortestRoute() {
        String startNodeName = startStation.getRailStation().getName();
        Node startNode = graph.getNode(startNodeName);
        String endNodeName = endStation.getRailStation().getName();
        Node endNode = graph.getNode(endNodeName);

        resetGraphEdges();

        dijkstra.init(graph);
        if (startNode == null || endNode == null) {
            System.err.println("No nodes found");
            return;
        }

        dijkstra.setSource(startNode);
        //calculate the shortest path
        dijkstra.compute();

        Path path = dijkstra.getPath(endNode);
        if (path != null) {
            setNodeStyle(startNode, endNode, path);
            //print the shortest path
            System.out.println("\nShortest path found:");
            System.out.printf("Total distance: %.2f km%n", dijkstra.getPathLength(endNode));
            System.out.println("Route: ");
            path.nodes().forEach(node -> System.out.println("  -> " + node.getId()));
        } else {
            System.out.println("No path found between " + startNodeName + " and " + endNodeName);
        }
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
}
