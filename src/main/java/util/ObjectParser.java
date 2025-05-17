package util;

import dto.RailLines;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;

public class ObjectParser {
    ArrayList<RailLines> railLines;
    public ObjectParser() {
        railLines = new ArrayList<>();
    }
    public void traverse(Document doc) {
        NodeList nodeList = doc.getElementsByTagName("Placemark");
        for (int i = 0; i < nodeList.getLength(); ++i) {
            Node node = nodeList.item(i);
            //System.out.println("\nNode Name :" + node.getNodeName());
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                RailLines railLine = new RailLines();
                Element tElement = (Element) node;
                //System.out.println("Name: " + tElement.getElementsByTagName("name").item(0).getTextContent());
                railLine.setName(tElement.getElementsByTagName("name").item(0).getTextContent());
                railLines.add(railLine);
            }
        }
        System.out.println(railLines.get(0).getName());
    }
}
