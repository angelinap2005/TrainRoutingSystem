package util.graph;

import dto.RailStation;
import dto.Station;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.SingleGraph;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

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

        RailStation railStation1 = mock(RailStation.class);
        when(railStation1.getName()).thenReturn("Station1");
        when(railStation1.getCoordinates()).thenReturn(new Double[]{0.0, 0.0});

        RailStation railStation2 = mock(RailStation.class);
        when(railStation2.getName()).thenReturn("Station2");
        when(railStation2.getCoordinates()).thenReturn(new Double[]{1.0, 1.0});

        RailStation railStation3 = mock(RailStation.class);
        when(railStation3.getName()).thenReturn("Station3");
        when(railStation3.getCoordinates()).thenReturn(new Double[]{2.0, 2.0});

        RailStation railStation4 = mock(RailStation.class);
        when(railStation4.getName()).thenReturn("Station4");
        when(railStation4.getCoordinates()).thenReturn(new Double[]{3.0, 3.0});

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
    public void testCalculateShortestRoute() {
        boolean result = routeGenerator.calculateShortestRoute();
        assertTrue("Shortest route calculation should succeed", result);
    }

    @Test
    public void testCalculateLeastStationStops() {
        boolean result = routeGenerator.calculateLeastStationStops();
        assertTrue("Least stops route calculation should succeed", result);
    }

    @Test
    public void testCalculateShortestRouteAStar() {
        boolean result = routeGenerator.calculateShortestRouteAStar();
        assertTrue("A* shortest route calculation should succeed", result);
    }

    @Test
    public void testCalculateLeastStationStopsAStar() {
        boolean result = routeGenerator.calculateLeastStationStopsAStar();
        assertTrue("A* least stops route calculation should succeed", result);
    }
}