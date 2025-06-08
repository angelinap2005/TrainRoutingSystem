import dto.RailLine;
import dto.RailStation;
import org.junit.Test;
import org.junit.Before;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import util.DocumentParser;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import static org.junit.Assert.*;

public class RailSystemTest {

    private File validLinesFile;
    private File validStationsFile;
    private File invalidFile;

    @Before
    public void setUp() {
        validLinesFile = new File("src/test/resources/testLinesFile.kml");
        validStationsFile = new File("src/test/resources/testStationFile.kml");
        invalidFile = new File("src/test/resources/invalidFile.kml");
    }

    @Test
    public void parseDocWithValidFileTest(){
        Document doc;
        try {
            doc = DocumentParser.parseDocument(validLinesFile);
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (SAXException e) {
            throw new RuntimeException(e);
        }
        assertNotNull("Document should not be null for valid file", doc);
        assertEquals("kml", doc.getDocumentElement().getNodeName());
    }

    @Test(expected = RuntimeException.class)
    public void parseDocWithNoExistentFileTest(){
        try {
            DocumentParser.parseDocument(new File("nonexistent.kml"));
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (SAXException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void setRailLinesTest() {
        //create empty lists for rail lines and stations
        ArrayList<RailLine> railLines = new ArrayList<>();
        ArrayList<RailStation> railStations = new ArrayList<>();

        //create a rail line
        RailLine line = new RailLine();
        line.setName("Weaver - Stoke Newington to Stamford Hill");
        railLines.add(line);

        //create a rail station and assign the rail line to it
        RailStation station = new RailStation();
        station.setName("Stoke Newington");
        station.setRailLines(new ArrayList<>());
        railStations.add(station);

        //call the method to set rail lines
        RailSystem.setRailLines(railLines, railStations);

       //verify that the rail line was added to the station
        assertEquals(1, station.getRailLines().size());
        assertEquals("Weaver - Stoke Newington to Stamford Hill", station.getRailLines().get(0).getName());
    }

    @Test
    public void setRailLinesWithNoLinesTest() {
        ArrayList<RailLine> railLines = new ArrayList<>();
        ArrayList<RailStation> railStations = new ArrayList<>();

        //call the method to set rail lines with empty lists
        RailSystem.setRailLines(railLines, railStations);
    }

    @Test
    public void mainMethodArgumentValidationTest() {
        //test the main method with no arguments
        String[] emptyArgs = {};
        try {
            RailSystem.main(emptyArgs);
            //expect an exception to be thrown
        } catch (Exception e) {
            //expect an ArrayIndexOutOfBoundsException
            assertTrue(e instanceof ArrayIndexOutOfBoundsException);
        }
    }
}