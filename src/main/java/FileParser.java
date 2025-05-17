import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import util.ObjectParser;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
/*code from: https://stackoverflow.com/questions/2310139/how-to-read-xml-response-from-a-url-in-java, https://www.geeksforgeeks.org/java-program-to-extract-content-from-a-xml-document/ */
public class FileParser {

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

        readDocuments(railLinesPath);
        readDocuments(railStationsPath);
    }

    private static void readDocuments(File fileKML) throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(fileKML);
        doc.getDocumentElement().normalize();
        ObjectParser objectParser = new ObjectParser();

        objectParser.traverse(doc);
    }
}
