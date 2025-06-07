package util.graph;

import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import dto.RailLine;
import dto.RailStation;
import dto.Station;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.SingleGraph;

import java.util.ArrayList;
import java.util.List;

public class RouteGeneratorTest {

    private RouteGenerator routeGenerator;
    private Graph graph;
    private Station startStation;
    private Station endStation;
    private List<Station> stations;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        graph = new SingleGraph("TestGraph");

        graph.addNode("Station1");
        graph.addNode("Station2");
        graph.addNode("Station3");
        graph.addNode("Station4");

        Edge edge12 = graph.addEdge("Edge12", "Station1", "Station2", false);
        edge12.setAttribute("length", 5.0);
        Edge edge23 = graph.addEdge("Edge23", "Station2", "Station3", false);
        edge23.setAttribute("length", 3.0);
        Edge edge34 = graph.addEdge("Edge34", "Station3", "Station4", false);
        edge34.setAttribute("length", 2.0);
        Edge edge14 = graph.addEdge("Edge14", "Station1", "Station4", false);
        edge14.setAttribute("length", 15.0);

        stations = new ArrayList<>();

        // Create mock rail lines for testing line changes
        RailLine centralLine = mock(RailLine.class);
        when(centralLine.getName()).thenReturn("Central");

        RailLine piccadillyLine = mock(RailLine.class);
        when(piccadillyLine.getName()).thenReturn("Piccadilly");

        RailLine northernLine = mock(RailLine.class);
        when(northernLine.getName()).thenReturn("Northern");

        // Station 1: Only Central line
        RailStation railStation1 = mock(RailStation.class);
        when(railStation1.getName()).thenReturn("Station1");
        when(railStation1.getCoordinates()).thenReturn(new Double[]{0.0, 0.0});
        List<RailLine> linesForStation1 = new ArrayList<>();
        linesForStation1.add(centralLine);

        // Station 2: Central and Piccadilly lines (interchange)
        RailStation railStation2 = mock(RailStation.class);
        when(railStation2.getName()).thenReturn("Station2");
        when(railStation2.getCoordinates()).thenReturn(new Double[]{1.0, 1.0});
        List<RailLine> linesForStation2 = new ArrayList<>();
        linesForStation2.add(centralLine);
        linesForStation2.add(piccadillyLine);

        // Station 3: Piccadilly and Northern lines (interchange)
        RailStation railStation3 = mock(RailStation.class);
        when(railStation3.getName()).thenReturn("Station3");
        when(railStation3.getCoordinates()).thenReturn(new Double[]{2.0, 2.0});
        List<RailLine> linesForStation3 = new ArrayList<>();
        linesForStation3.add(piccadillyLine);
        linesForStation3.add(northernLine);

        // Station 4: Only Northern line
        RailStation railStation4 = mock(RailStation.class);
        when(railStation4.getName()).thenReturn("Station4");
        when(railStation4.getCoordinates()).thenReturn(new Double[]{3.0, 3.0});
        List<RailLine> linesForStation4 = new ArrayList<>();
        linesForStation4.add(northernLine);

        startStation = mock(Station.class);
        when(startStation.getRailStation()).thenReturn(railStation1);

        Station station2 = mock(Station.class);
        when(station2.getRailStation()).thenReturn(railStation2);

        Station station3 = mock(Station.class);
        when(station3.getRailStation()).thenReturn(railStation3);

        endStation = mock(Station.class);
        when(endStation.getRailStation()).thenReturn(railStation4);

        stations.add(startStation);
        stations.add(station2);
        stations.add(station3);
        stations.add(endStation);

        routeGenerator = new RouteGenerator(stations, startStation, endStation, graph);
    }

    @Test
    public void testCalculateLeastChanges_NoPath() {
        // Create a disconnected graph to test no path scenario
        Graph disconnectedGraph = new SingleGraph("DisconnectedGraph");
        disconnectedGraph.addNode("IsolatedStation1");
        disconnectedGraph.addNode("IsolatedStation2");
        // No edges between nodes - they're disconnected

        RailStation isolatedRailStation1 = mock(RailStation.class);
        when(isolatedRailStation1.getName()).thenReturn("IsolatedStation1");
        when(isolatedRailStation1.getRailLines()).thenReturn(new ArrayList<>());

        RailStation isolatedRailStation2 = mock(RailStation.class);
        when(isolatedRailStation2.getName()).thenReturn("IsolatedStation2");
        when(isolatedRailStation2.getRailLines()).thenReturn(new ArrayList<>());

        Station isolatedStart = mock(Station.class);
        when(isolatedStart.getRailStation()).thenReturn(isolatedRailStation1);

        Station isolatedEnd = mock(Station.class);
        when(isolatedEnd.getRailStation()).thenReturn(isolatedRailStation2);

        List<Station> isolatedStations = new ArrayList<>();
        isolatedStations.add(isolatedStart);
        isolatedStations.add(isolatedEnd);

        RouteGenerator isolatedGenerator = new RouteGenerator(isolatedStations, isolatedStart, isolatedEnd, disconnectedGraph);
        boolean result = isolatedGenerator.calculateLeastChanges();

        assertFalse("Should return false when no path exists", result);
    }

    @Test
    public void testCalculateLeastChanges_EmptyPath() {
        // Test with missing nodes in graph
        Graph emptyGraph = new SingleGraph("EmptyGraph");

        RouteGenerator emptyGenerator = new RouteGenerator(stations, startStation, endStation, emptyGraph);
        boolean result = emptyGenerator.calculateLeastChanges();

        assertFalse("Should return false when nodes don't exist in graph", result);
    }
}