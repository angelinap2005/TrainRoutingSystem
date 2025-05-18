import userInterface.UserControl;
import dto.RailLine;
import dto.RailStation;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import util.AddRailLines;
import util.graph.GraphGenerator;
import util.graph.GraphObjectGenerator;
import util.ObjectParser;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/*Code references:
* https://graphstream-project.org/doc/
* https://www.baeldung.com/jaxb
* https://www.geeksforgeeks.org/java-program-to-extract-content-from-a-xml-document/
* https://stackoverflow.com/questions/3694380/calculating-distance-between-two-points-using-latitude-longitude
* https://www.geodatasource.com/resources/tutorials/how-to-calculate-the-distance-between-2-locations-using-java
* https://www.geeksforgeeks.org/haversine-formula-to-find-distance-between-two-points-on-a-sphere/
* */

public class RailSystem {

    public static void main(String[] args) throws IOException, ParserConfigurationException, SAXException {
        File railLinesPath = null;
        File railStationsPath = null;

        //parse arguments
        for (String arg : args) {
            if (arg.startsWith("railLines=")) {
                railLinesPath = new File(arg.substring("railLines=".length()));
            } else if (arg.startsWith("railStations=")) {
                railStationsPath = new File(arg.substring("railStations=".length()));
            }
        }

        if (railLinesPath == null || railStationsPath == null) {
            System.err.println("Files not found");
            return;
        }

        ObjectParser parser = new ObjectParser();
        GraphObjectGenerator graphObjectGenerator = new GraphObjectGenerator();
        GraphGenerator graphGenerator = new GraphGenerator();
        UserControl userControl = new UserControl(graphGenerator);
        parser.traverse(parseDoc(railLinesPath));
        parser.traverse(parseDoc(railStationsPath));

        //set rail lines
        setRailLines(parser.getRailLines(), parser.getRailStations());
        //generate graph objects
        graphObjectGenerator.controller(parser.getRailLines(), parser.getRailStations());
        //generate graph
        graphGenerator.generateGraph(graphObjectGenerator.getStations());
        //pass to user control
        userControl.start();
    }

    private static Document parseDoc(File fileKML) throws ParserConfigurationException, IOException, SAXException {
        //parse kml
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(fileKML);
        doc.getDocumentElement().normalize();
        return doc;
    }

    private static void setRailLines(ArrayList<RailLine> railLines, ArrayList<RailStation> railStations) {
        AddRailLines addRailLines = new AddRailLines();
        addRailLines.addLines(railLines, railStations);
    }
}