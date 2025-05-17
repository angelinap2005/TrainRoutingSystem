import dto.RailLine;
import dto.RailStation;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import util.AddRailLines;
import util.ObjectParser;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class RailSystem {

    public static void main(String[] args) throws IOException, ParserConfigurationException, SAXException {
        File railLinesPath = null;
        File railStationsPath = null;

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
        parser.traverse(parseDoc(railLinesPath));
        parser.traverse(parseDoc(railStationsPath));

        setRailLines(parser.getRailLines(), parser.getRailStations());
    }

    private static Document parseDoc(File fileKML) throws ParserConfigurationException, IOException, SAXException {
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