package util.graph;

import dto.RailLine;
import dto.RailStation;
import dto.Station;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class GraphObjectGeneratorTest {

    private GraphObjectGenerator graphObjectGenerator;
    private ArrayList<RailLine> railLines;
    private ArrayList<RailStation> railStations;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        railLines = new ArrayList<>();
        railStations = new ArrayList<>();

        RailLine line1 = new RailLine();
        line1.setName("Line 1");

        RailLine line2 = new RailLine();
        line2.setName("Line 2");

        railLines.add(line1);
        railLines.add(line2);

        RailStation station1 = new RailStation();
        station1.setName("Station 1");
        station1.setCoordinates(new Double[]{51.5074, -0.1278});
        ArrayList<RailLine> station1Lines = new ArrayList<>();
        station1Lines.add(line1);
        station1.setRailLines(station1Lines);

        RailStation station2 = new RailStation();
        station2.setName("Station 2");
        station2.setCoordinates(new Double[]{51.5080, -0.1280});
        ArrayList<RailLine> station2Lines = new ArrayList<>();
        station2Lines.add(line1);
        station2.setRailLines(station2Lines);


        RailStation station3 = new RailStation();
        station3.setName("Station 3");
        station3.setCoordinates(new Double[]{48.8570, 2.3530});
        ArrayList<RailLine> station3Lines = new ArrayList<>();
        station3Lines.add(line2);
        station3.setRailLines(station3Lines);

        railStations.add(station1);
        railStations.add(station2);
        railStations.add(station3);
        graphObjectGenerator = new GraphObjectGenerator(railLines, railStations);
    }

    @Test
    public void testController() {
        graphObjectGenerator.controller();
        assertNotNull("Line to stations map should not be null", graphObjectGenerator.getLineToStationsMap());
        assertEquals("Should have correct number of lines", 2, graphObjectGenerator.getLineToStationsMap().size());
        List<Station> stations = graphObjectGenerator.getStations();
        assertNotNull("Stations list should not be null", stations);
        assertEquals("Should have correct number of stations", 3, stations.size());
        Map<String, Map<String, Double>> stationDistances = graphObjectGenerator.getStationDistances();
        assertNotNull("Station distances map should not be null", stationDistances);
        assertEquals("Should have distances for all stations", 3, stationDistances.size());
    }

    @Test
    public void testBuildStationMaps() {
        graphObjectGenerator.controller();

        Map<String, List<RailStation>> lineToStationsMap = graphObjectGenerator.getLineToStationsMap();
        assertNotNull(lineToStationsMap);

        List<RailStation> line1Stations = lineToStationsMap.get("Line 1");
        assertNotNull("Line 1 stations should not be null", line1Stations);
        assertEquals("Line 1 should have 2 stations", 2, line1Stations.size());
        assertTrue("Line 1 should include Station 1", line1Stations.stream().anyMatch(s -> "Station 1".equals(s.getName())));
        assertTrue("Line 1 should include Station 2", line1Stations.stream().anyMatch(s -> "Station 2".equals(s.getName())));

        List<RailStation> line2Stations = lineToStationsMap.get("Line 2");
        assertNotNull("Line 2 stations should not be null", line2Stations);
        assertEquals("Line 2 should have 2 stations", 1, line2Stations.size());
        assertTrue("Line 2 should include Station 3", line2Stations.stream().anyMatch(s -> "Station 3".equals(s.getName())));
    }

    @Test
    public void testDistanceCalculation() {
        graphObjectGenerator.controller();

        Map<String, Map<String, Double>> distances = graphObjectGenerator.getStationDistances();
        Double distance1to2 = distances.get("Station 1").get("Station 2");
        assertNotNull("Distance from Station 1 to 2 should be calculated", distance1to2);
        assertTrue("Distance should be positive", distance1to2 > 0);
        Double distance2to1 = distances.get("Station 2").get("Station 1");
        assertEquals("Distance should be symmetric", distance1to2, distance2to1);
    }

    @Test
    public void testHandlingNullValues() {
        RailStation nullStation = new RailStation();
        nullStation.setName("Null Station");
        nullStation.setCoordinates(new Double[]{0.0, 0.0});
        railStations.add(nullStation);
        railStations.add(null);

        RailLine nullLine = new RailLine();
        railLines.add(nullLine);

        try {
            graphObjectGenerator.controller();
            assertTrue(true);
        } catch (Exception e) {
            fail("Controller should handle null values gracefully: " + e.getMessage());
        }
    }
}