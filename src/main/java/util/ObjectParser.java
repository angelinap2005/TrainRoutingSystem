package util;

import dto.RailLine;
import dto.RailStation;
import lombok.Getter;
import org.w3c.dom.*;

import java.util.ArrayList;

@Getter
public class ObjectParser {
    private ArrayList<RailLine> railLines = new ArrayList<>();
    private ArrayList<RailStation> railStations = new ArrayList<>();

    public void traverse(Document doc) {
        Element kmlElement = (Element) doc.getElementsByTagName("kml").item(0);
        Element documentElement = (Element) kmlElement.getElementsByTagName("Document").item(0);
        String docName = documentElement.getElementsByTagName("name").item(0).getTextContent();
        //check document name
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
                RailLine railLine = new RailLine();
                Element tElement = (Element) node;
                railLine.setName(tElement.getElementsByTagName("name").item(0).getTextContent());
                railLine.setStyleUrl(tElement.getElementsByTagName("styleUrl").item(0).getTextContent());
                String coordinates = tElement.getElementsByTagName("coordinates").item(0).getTextContent().trim();
                String[] coordinatesArray = coordinates.split(",");
                railLine.setCoordinates(coordinatesArray);
                railLines.add(railLine);
            }
        }
    }

    private void railStationParser(Document doc) {
        NodeList nodeList = doc.getElementsByTagName("Placemark");
        for (int i = 0; i < nodeList.getLength(); ++i) {
            Node node = nodeList.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                RailStation railStation = new RailStation();
                Element tElement = (Element) node;
                railStation.setName(tElement.getElementsByTagName("name").item(0).getTextContent());
                railStation.setStyleUrl(tElement.getElementsByTagName("styleUrl").item(0).getTextContent());
                String coordinates = tElement.getElementsByTagName("coordinates").item(0).getTextContent().trim();
                String[] coordinatesArray = coordinates.split(",");
                railStation.setCoordinates(coordinatesArray);
                railStations.add(railStation);
            }
        }
    }
}