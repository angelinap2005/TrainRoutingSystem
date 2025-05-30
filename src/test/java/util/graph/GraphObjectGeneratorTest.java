package util.graph;

import dto.RailLine;
import dto.RailStation;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;

public class GraphObjectGeneratorTest {
    @Mock
    GraphObjectGenerator graphObjectGenerator;

    private ArrayList<RailLine> railLines;
    private ArrayList<RailStation> railStations;

    @Before
    public void before(){
        railLines = new ArrayList<>();
        railStations = new ArrayList<>();
        RailLine railLine1 = new RailLine();
        railLine1.setName("Line 1");
        RailLine railLine2 = new RailLine();
        railLine2.setName("Line 2");
        railLines.add(railLine1);
        railLines.add(railLine2);
        RailStation railStation1 = new RailStation();
        railStation1.setName("Station 1");
        RailStation railStation2 = new RailStation();
        railStation2.setName("Station 2");
        railStations.add(railStation1);
        railStations.add(railStation2);
        graphObjectGenerator = new GraphObjectGenerator(railLines, railStations);
    }

    @Test
    public void testController(){
        graphObjectGenerator.controller();
        assertEquals(2, graphObjectGenerator.getLineToStationsMap().size());
    }
}
