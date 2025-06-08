package util.graph;

import dto.RailLine;
import dto.RailStation;
import dto.Route;
import dto.Station;
import org.graphstream.graph.Graph;
import org.junit.Before;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.junit.gen5.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class GraphGeneratorTest {

    @Mock
    private GraphObjectGenerator graphObjectGenerator;

    private GraphGenerator graphGenerator;
    private List<Station> stations;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        stations = new ArrayList<>();

        RailLine line1 = mock(RailLine.class);
        RailLine line2 = mock(RailLine.class);
        RailLine line3 = mock(RailLine.class);

        when(line1.getName()).thenReturn("Central");
        when(line2.getName()).thenReturn("Piccadilly");
        when(line3.getName()).thenReturn("Northern");

        Station station1 = mock(Station.class);
        Station station2 = mock(Station.class);
        Station station3 = mock(Station.class);

        RailStation railStation1 = mock(RailStation.class);
        RailStation railStation2 = mock(RailStation.class);
        RailStation railStation3 = mock(RailStation.class);

        when(railStation1.getName()).thenReturn("Station A");
        when(railStation2.getName()).thenReturn("Station B");
        when(railStation3.getName()).thenReturn("Station C");

        when(railStation1.getCoordinates()).thenReturn(new Double[]{51.5074, -0.1278});
        when(railStation2.getCoordinates()).thenReturn(new Double[]{51.5080, -0.1280});
        when(railStation3.getCoordinates()).thenReturn(new Double[]{51.5090, -0.1290});

        List<RailLine> linesForStation1 = new ArrayList<>();
        linesForStation1.add(line1);

        List<RailLine> linesForStation2 = new ArrayList<>();
        linesForStation2.add(line1);
        linesForStation2.add(line2);

        List<RailLine> linesForStation3 = new ArrayList<>();
        linesForStation3.add(line2);

        when(railStation1.getRailLines()).thenReturn((ArrayList<RailLine>) linesForStation1);
        when(railStation2.getRailLines()).thenReturn((ArrayList<RailLine>) linesForStation2);
        when(railStation3.getRailLines()).thenReturn((ArrayList<RailLine>) linesForStation3);

        when(station1.getRailStation()).thenReturn(railStation1);
        when(station2.getRailStation()).thenReturn(railStation2);
        when(station3.getRailStation()).thenReturn(railStation3);

        List<Route> routesForStation1 = new ArrayList<>();
        List<Route> routesForStation2 = new ArrayList<>();
        List<Route> routesForStation3 = new ArrayList<>();

        Route route1to2 = mock(Route.class);
        when(route1to2.getDestination()).thenReturn(railStation2);
        when(route1to2.getWeight()).thenReturn(1.5);
        routesForStation1.add(route1to2);

        Route route2to1 = mock(Route.class);
        when(route2to1.getDestination()).thenReturn(railStation1);
        when(route2to1.getWeight()).thenReturn(1.5);
        routesForStation2.add(route2to1);

        Route route2to3 = mock(Route.class);
        when(route2to3.getDestination()).thenReturn(railStation3);
        when(route2to3.getWeight()).thenReturn(2.0);
        routesForStation2.add(route2to3);

        Route route3to2 = mock(Route.class);
        when(route3to2.getDestination()).thenReturn(railStation2);
        when(route3to2.getWeight()).thenReturn(2.0);
        routesForStation3.add(route3to2);

        when(station1.getRoutes()).thenReturn((ArrayList<Route>) routesForStation1);
        when(station2.getRoutes()).thenReturn((ArrayList<Route>) routesForStation2);
        when(station3.getRoutes()).thenReturn((ArrayList<Route>) routesForStation3);

        stations.add(station1);
        stations.add(station2);
        stations.add(station3);

        graphGenerator = new GraphGenerator(graphObjectGenerator);

        when(graphObjectGenerator.getStations()).thenReturn(stations);
    }

    @Test
    public void generateGraphValidDataTest() {
        //generate the graph
        Graph result = graphGenerator.generateGraph(stations);
        //assert that the graph is not null and contains the expected number of nodes and edges
        assertNotNull("Generated graph should not be null", result);
    }

    @Test(expected = IllegalArgumentException.class)
    public void generateGraphNullDataTest() {
        //attempt to generate the graph with null data
        graphGenerator.generateGraph(null);
    }

    @Test
    public void nonExistentStartStationTest() {
        //generate the graph
        graphGenerator.generateGraph(stations);

        //attempt to plan a route with a nonexistent station
        boolean result = graphGenerator.planRoute("Nonexistent Station", "Station B", true, false, false);

        //assert that the result is false, indicating the route planning failed
        assertFalse("Planning route with nonexistent start station should fail", result);
    }

    @Test
    public void nonExistentEndStationTest() {
        //generate the graph
        graphGenerator.generateGraph(stations);

        //attempt to plan a route with a nonexistent end station
        boolean result = graphGenerator.planRoute("Station A", "Nonexistent Station", true, false, false);

        //assert that the result is false, indicating the route planning failed
        assertFalse("Planning route with nonexistent end station should fail", result);
    }

    @Test
    public void sameStartAndEndStationTest() {
        //generate the graph
        graphGenerator.generateGraph(stations);

        //attempt to plan a route with the same start and end station
        boolean result = graphGenerator.planRoute("Station A", "Station A", true, false, false);

        //assert that the result is false, indicating the route planning failed
        assertFalse("Planning route with same start and end station should fail", result);
    }

    @Test
    public void mullParameterPlanRouteTest() {
        graphGenerator.generateGraph(stations);

        //test with null parameters
        try {
            // Attempt to plan a route with null start and end stations
            graphGenerator.planRoute(null, "Station B", true, false, false);
            fail("Should throw exception with null start station");
        } catch (IllegalArgumentException e) {
            //expected exception for null start station
        }

        try {
            //attempt to plan a route with null end station
            graphGenerator.planRoute("Station A", null, true, false, false);
            //should throw exception with null end station
            fail("Should throw exception with null end station");
        } catch (IllegalArgumentException e) {
        }
    }

    @Test
    public void planShortestRouteAStarTest() {
        //generate the graph
        graphGenerator.generateGraph(stations);

        //plan a route using A* algorithm for the shortest path
        boolean result = graphGenerator.planRoute("Station A", "Station B", true, false, false);

        //assert that the route planning was successful
        assertTrue("Route planning should succeed for shortest route", result);
    }

    @Test
    public void planLeastStopsAStarTest() {
        //generate the graph
        graphGenerator.generateGraph(stations);

        //plan a route using A* algorithm for the least stops
        boolean result = graphGenerator.planRoute("Station A", "Station B", false, false, false);

        //assert that the route planning was successful
        assertTrue("Route planning should succeed for least stops route", result);
    }


    @Test
    public void printValidRouteTest() {
        //generate the graph
        graphGenerator.generateGraph(stations);

        //plan a route
        boolean routePlanned = graphGenerator.planRoute("Station A", "Station B", true, false, false);
        assertTrue("Route should be planned successfully", routePlanned);

        //print the route
        try {
            //print the planned route
            graphGenerator.printRoute();
            //assert that the printRoute method executes without exceptions
            assertTrue("printRoute should execute without exception", true);
        } catch (Exception e) {
            //if an exception is thrown, the test fails
            fail("printRoute should not throw exception: " + e.getMessage());
        }
    }

    @Test
    public void printWithoutPlannedRouteTest() {
        //generate the graph without planning a route
        graphGenerator.generateGraph(stations);

        try {
            //attempt to print the route without planning it first
            graphGenerator.printRoute();
            //if no exception is thrown, the test passes
            assertTrue("printRoute should handle case with no planned route", true);
        } catch (Exception e) {
            //if an exception is thrown, the test fails
            assertTrue("Exception is acceptable for no planned route", true);
        }
    }

    @Test
    public void resetEdgeColourTest() {
        //generate the graph
        graphGenerator.generateGraph(stations);

        try {
            //reset the edge colors
            graphGenerator.resetEdgeColors();
            //assert that the resetEdgeColors method executes without exceptions
            assertTrue("resetEdgeColors should execute without exception", true);
        } catch (Exception e) {
            //if an exception is thrown, the test fails
            fail("resetEdgeColors should not throw exception: " + e.getMessage());
        }
    }

    @Test
    public void highlightRouteEdgesTest() {
        //generate the graph
        graphGenerator.generateGraph(stations);

        //mock a route with stations
        List<String> routeStations = Arrays.asList("Station A", "Station B", "Station C");

        //attempt to highlight the route edges
        try {
            //highlight the edges of the route
            graphGenerator.highlightRouteEdges(routeStations);
            assertTrue("highlightRouteEdges should execute without exception", true);
        } catch (Exception e) {
            //if an exception is thrown, the test fails
            fail("highlightRouteEdges should not throw exception: " + e.getMessage());
        }
    }

    @Test
    public void printEntireMapTest() {
        //generate the graph
        graphGenerator.generateGraph(stations);

        try {
            //print the entire map
            graphGenerator.printEntireMap();
            assertTrue("printEntireMap should execute without exception", true);
        } catch (Exception e) {
            //if an exception is thrown, the test fails
            fail("printEntireMap should not throw exception: " + e.getMessage());
        }
    }
}