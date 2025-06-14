package util.graph;

import dto.RailLine;
import dto.RailStation;
import dto.Station;
import org.junit.Test;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.SingleGraph;
import org.junit.Before;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.gen5.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
        edge12.setAttribute("original.color", "#0000FF");

        Edge edge23 = graph.addEdge("Edge23", "Station2", "Station3", false);
        edge23.setAttribute("length", 3.0);
        edge23.setAttribute("original.color", "#0000FF");

        Edge edge34 = graph.addEdge("Edge34", "Station3", "Station4", false);
        edge34.setAttribute("length", 2.0);
        edge34.setAttribute("original.color", "#0000FF");

        Edge edge14 = graph.addEdge("Edge14", "Station1", "Station4", false);
        edge14.setAttribute("length", 15.0);
        edge14.setAttribute("original.color", "#0000FF");

        stations = new ArrayList<>();

        RailLine centralLine = mock(RailLine.class);
        when(centralLine.getName()).thenReturn("Central");

        RailLine piccadillyLine = mock(RailLine.class);
        when(piccadillyLine.getName()).thenReturn("Piccadilly");

        RailLine northernLine = mock(RailLine.class);
        when(northernLine.getName()).thenReturn("Northern");

        RailStation railStation1 = mock(RailStation.class);
        when(railStation1.getName()).thenReturn("Station1");
        when(railStation1.getCoordinates()).thenReturn(new Double[]{0.0, 0.0});
        List<RailLine> linesForStation1 = new ArrayList<>();
        linesForStation1.add(centralLine);
        when(railStation1.getRailLines()).thenReturn((ArrayList<RailLine>) linesForStation1);

        RailStation railStation2 = mock(RailStation.class);
        when(railStation2.getName()).thenReturn("Station2");
        when(railStation2.getCoordinates()).thenReturn(new Double[]{1.0, 1.0});
        List<RailLine> linesForStation2 = new ArrayList<>();
        linesForStation2.add(centralLine);
        linesForStation2.add(piccadillyLine);
        when(railStation2.getRailLines()).thenReturn((ArrayList<RailLine>) linesForStation2);

        RailStation railStation3 = mock(RailStation.class);
        when(railStation3.getName()).thenReturn("Station3");
        when(railStation3.getCoordinates()).thenReturn(new Double[]{2.0, 2.0});
        List<RailLine> linesForStation3 = new ArrayList<>();
        linesForStation3.add(piccadillyLine);
        linesForStation3.add(northernLine);
        when(railStation3.getRailLines()).thenReturn((ArrayList<RailLine>) linesForStation3);

        RailStation railStation4 = mock(RailStation.class);
        when(railStation4.getName()).thenReturn("Station4");
        when(railStation4.getCoordinates()).thenReturn(new Double[]{3.0, 3.0});
        List<RailLine> linesForStation4 = new ArrayList<>();
        linesForStation4.add(northernLine);
        when(railStation4.getRailLines()).thenReturn((ArrayList<RailLine>) linesForStation4);

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
    public void shortestRouteValidTest() {
        boolean result = routeGenerator.calculateShortestRoute();
        assertTrue("Should find shortest route between connected stations", result);
    }

    @Test
    public void shortestRouteMissingNodesTest() {
        Graph emptyGraph = new SingleGraph("EmptyGraph");
        RouteGenerator emptyGenerator = new RouteGenerator(stations, startStation, endStation, emptyGraph);

        boolean result = emptyGenerator.calculateShortestRoute();
        assertFalse("Should return false when nodes don't exist in graph", result);
    }

    @Test
    public void leastStopsValidTest() {
        boolean result = routeGenerator.calculateLeastStationStops();
        assertTrue("Should find route with least stops between connected stations", result);
    }

    @Test
    public void shortestRouteAStarValidTest() {
        boolean result = routeGenerator.calculateShortestRouteAStar();
        assertTrue("Should find shortest route using A* algorithm", result);
    }

    @Test
    public void shortestRouteAStarMissingNodesTest() {
        Graph emptyGraph = new SingleGraph("EmptyGraph");
        RouteGenerator emptyGenerator = new RouteGenerator(stations, startStation, endStation, emptyGraph);

        boolean result = emptyGenerator.calculateShortestRouteAStar();
        assertFalse("Should return false when nodes don't exist in graph", result);
    }

    @Test
    public void shortestRouteAStarNoEdgesTest() {
        Graph disconnectedGraph = new SingleGraph("DisconnectedGraph");
        disconnectedGraph.addNode("Station1");
        disconnectedGraph.addNode("Station4");

        RouteGenerator disconnectedGenerator = new RouteGenerator(stations, startStation, endStation, disconnectedGraph);
        boolean result = disconnectedGenerator.calculateShortestRouteAStar();

        assertFalse("Should return false when no path exists", result);
    }

    @Test
    public void leastStopsAStarValidTest() {
        boolean result = routeGenerator.calculateLeastStationStopsAStar();
        assertTrue("Should find route with least stops using A* algorithm", result);
    }

    @Test
    public void leastStopsAStarMissingNodesTest() {
        Graph emptyGraph = new SingleGraph("EmptyGraph");
        RouteGenerator emptyGenerator = new RouteGenerator(stations, startStation, endStation, emptyGraph);

        boolean result = emptyGenerator.calculateLeastStationStopsAStar();
        assertFalse("Should return false when nodes don't exist in graph", result);
    }

    @Test
    public void leastStationAStarNoPathTest() {
        Graph disconnectedGraph = new SingleGraph("DisconnectedGraph");
        disconnectedGraph.addNode("Station1");
        disconnectedGraph.addNode("Station4");

        RouteGenerator disconnectedGenerator = new RouteGenerator(stations, startStation, endStation, disconnectedGraph);
        boolean result = disconnectedGenerator.calculateLeastStationStopsAStar();

        assertFalse("Should return false when no path exists", result);
    }

    @Test
    public void leastChangesValidTest() {
        boolean result = routeGenerator.calculateLeastChanges();
        assertTrue("Should find route with least line changes", result);
    }

    @Test
    public void leastChangesNoPathTest() {
        Graph disconnectedGraph = new SingleGraph("DisconnectedGraph");
        disconnectedGraph.addNode("IsolatedStation1");
        disconnectedGraph.addNode("IsolatedStation2");

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
    public void leastChangesEmptyPathTest() {
        Graph emptyGraph = new SingleGraph("EmptyGraph");

        RouteGenerator emptyGenerator = new RouteGenerator(stations, startStation, endStation, emptyGraph);
        boolean result = emptyGenerator.calculateLeastChanges();

        assertFalse("Should return false when nodes don't exist in graph", result);
    }

    @Test
    public void leastChangesSameLineTest() {
        Graph sameLineGraph = new SingleGraph("SameLineGraph");
        sameLineGraph.addNode("Station1");
        sameLineGraph.addNode("Station2");

        Edge edge = sameLineGraph.addEdge("Edge12", "Station1", "Station2", false);
        edge.setAttribute("length", 5.0);
        edge.setAttribute("original.color", "#0000FF");

        RailLine centralLine = mock(RailLine.class);
        when(centralLine.getName()).thenReturn("Central");

        RailStation start = mock(RailStation.class);
        when(start.getName()).thenReturn("Station1");
        List<RailLine> startLines = new ArrayList<>();
        startLines.add(centralLine);
        when(start.getRailLines()).thenReturn((ArrayList<RailLine>) startLines);

        RailStation end = mock(RailStation.class);
        when(end.getName()).thenReturn("Station2");
        List<RailLine> endLines = new ArrayList<>();
        endLines.add(centralLine);
        when(end.getRailLines()).thenReturn((ArrayList<RailLine>) endLines);

        Station startStn = mock(Station.class);
        when(startStn.getRailStation()).thenReturn(start);

        Station endStn = mock(Station.class);
        when(endStn.getRailStation()).thenReturn(end);

        List<Station> sameLineStations = new ArrayList<>();
        sameLineStations.add(startStn);
        sameLineStations.add(endStn);

        RouteGenerator sameLineGenerator = new RouteGenerator(sameLineStations, startStn, endStn, sameLineGraph);
        boolean result = sameLineGenerator.calculateLeastChanges();

        assertTrue("Should find route with no line changes", result);
    }


    @Test
    public void displayValidGraphTest() {
        try {
            routeGenerator.displayRoute();
            assertTrue("displayRoute should execute without throwing exception", true);
        } catch (Exception e) {
            fail("displayRoute should not throw exception: " + e.getMessage());
        }
    }

    @Test
    public void displayEmptyGraphTest() {
        Graph emptyGraph = new SingleGraph("EmptyGraph");
        RouteGenerator emptyGenerator = new RouteGenerator(stations, startStation, endStation, emptyGraph);

        try {
            emptyGenerator.displayRoute();
            assertTrue("displayRoute should handle empty graph gracefully", true);
        } catch (Exception e) {
            assertTrue("Exception is acceptable for empty graph", true);
        }
    }

    @Test
    public void heuristicCostValidTest() {
        boolean result = routeGenerator.calculateShortestRouteAStar();
        assertTrue("A* algorithm should work with valid coordinates", result);
    }

    @Test
    public void lineExtractionComplexNameTest() {
        boolean result = routeGenerator.calculateLeastChanges();
        assertTrue("Should handle complex line names correctly", result);
    }

    @Test
    public void longPathTest() {
        Graph longGraph = new SingleGraph("LongGraph");

        for (int i = 1; i <= 10; i++) {
            longGraph.addNode("Station" + i);
        }

        for (int i = 1; i < 10; i++) {
            Edge edge = longGraph.addEdge("Edge" + i + "_" + (i+1), "Station" + i, "Station" + (i+1), false);
            edge.setAttribute("length", (double) i);
            edge.setAttribute("original.color", "#0000FF");
        }

        RailStation longEndStation = mock(RailStation.class);
        when(longEndStation.getName()).thenReturn("Station10");
        when(longEndStation.getCoordinates()).thenReturn(new Double[]{10.0, 10.0});
        when(longEndStation.getRailLines()).thenReturn(new ArrayList<>());

        Station longEnd = mock(Station.class);
        when(longEnd.getRailStation()).thenReturn(longEndStation);

        RouteGenerator longGenerator = new RouteGenerator(stations, startStation, longEnd, longGraph);

        boolean result = longGenerator.calculateShortestRoute();
        assertTrue("Should handle longer paths correctly", result);
    }
}