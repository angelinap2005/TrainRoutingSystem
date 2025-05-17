package util;

import dto.RailLines;
import dto.RailStations;
import org.ejml.ops.DConvertArrays;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;

public class ObjectParser {
    ArrayList<RailLines> railLines;
    ArrayList<RailStations> railStations;
    public ObjectParser() {
        railLines = new ArrayList<>();
        railStations = new ArrayList<>();
    }
    public void traverse(Document doc) {
        Element kmlElement = (Element) doc.getElementsByTagName("kml").item(0);
        Element documentElement = (Element) kmlElement.getElementsByTagName("Document").item(0);
        String docName = documentElement.getElementsByTagName("name").item(0).getTextContent();

        if ("London Train Lines".equals(docName)) {
            railLineParser(doc);
        } else if ("London stations".equals(docName)) {
            railStationParser(doc);
        } else {
            System.out.println("Unknown document name: " + docName);
        }
    }
    private void railLineParser(Document doc) {
        NodeList nodeList = doc.getElementsByTagName("Placemark");
        for (int i = 0; i < nodeList.getLength(); ++i) {
            Node node = nodeList.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                RailLines railLine = new RailLines();
                Element tElement = (Element) node;
                railLine.setName(tElement.getElementsByTagName("name").item(0).getTextContent());
                railLine.setStyleUrl(tElement.getElementsByTagName("styleUrl").item(0).getTextContent());
                String coordinates = tElement.getElementsByTagName("coordinates").item(0).getTextContent();
                coordinates.substring(0, coordinates.length() - 1);
                String[] coordinatesArray = coordinates.split(",");
                railLine.setCoordinates(coordinatesArray);
                railLines.add(railLine);
            }
        }
        System.out.println(railLines.get(0).getName());
    }

    private void railStationParser(Document doc) {
        NodeList nodeList = doc.getElementsByTagName("Placemark");
        for (int i = 0; i < nodeList.getLength(); ++i) {
            Node node = nodeList.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                RailStations railStation = new RailStations();
                Element tElement = (Element) node;
                railStation.setName(tElement.getElementsByTagName("name").item(0).getTextContent());
                railStation.setStyleUrl(tElement.getElementsByTagName("styleUrl").item(0).getTextContent());
                String coordinates = tElement.getElementsByTagName("coordinates").item(0).getTextContent();
                coordinates.substring(0, coordinates.length() - 1);
                String[] coordinatesArray = coordinates.split(",");
                railStation.setCoordinates(coordinatesArray);
                railStations.add(railStation);
            }
        }
        System.out.println(railStations.get(0).getName());
    }
}

