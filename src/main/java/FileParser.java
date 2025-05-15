import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
//code from https://stackoverflow.com/questions/2310139/how-to-read-xml-response-from-a-url-in-java
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

        readTrainLines(railLinesPath);
    }

    private static void readTrainLines(File fileKML) throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(fileKML);
        doc.getDocumentElement().normalize();
        System.out.println("root element: " + doc.getDocumentElement().getNodeName());
        NodeList nList = doc.getElementsByTagName("placemark");
        for (int temp = 0; temp < nList.getLength(); temp++) {
            Node nNode = nList.item(temp);
            if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                Element eElement = (Element) nNode;
                System.out.println("Line: " + eElement.getElementsByTagName("name").item(0).getTextContent());
            }
        }
    }


}
