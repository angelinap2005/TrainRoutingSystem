import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

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

        System.out.println("Root element: " + doc.getDocumentElement().getNodeName());

        traverse(doc.getDocumentElement());
    }

    private static void traverse(Node node) {
        if (node.getNodeType() == Node.ELEMENT_NODE) {
            Element element = (Element) node;
            System.out.println("Element: " + element.getTagName());

            if (element.hasAttributes()) {
                for (int i = 0; i < element.getAttributes().getLength(); i++) {
                    Node attr = element.getAttributes().item(i);
                    System.out.println("  Attribute: " + attr.getNodeName() + " = " + attr.getNodeValue());
                }
            }

            String text = element.getTextContent().trim();
            if (!text.isEmpty()) {
                System.out.println("  Text Content: " + text);
            }
        }

        NodeList nodeList = node.getChildNodes();
        for (int i = 0; i < nodeList.getLength(); i++) {
            traverse(nodeList.item(i));
        }
    }
}
