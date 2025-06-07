package util;

import dto.RailLine;
import dto.RailStation;
import lombok.Getter;
import org.w3c.dom.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/*For file parsing code was taken from:
* https://www.tutorialspoint.com/java_xml/java_dom_parse_document.htm
* https://www.geeksforgeeks.org/java-program-to-extract-content-from-a-xml-document/
*/
@Getter
public class FileParser {
    private ArrayList<RailLine> railLines;
    private ArrayList<RailStation> railStations;

    public FileParser() {
        this.railLines = new ArrayList<>();
        this.railStations = new ArrayList<>();
    }

    public void traverse(Document doc) {
        if(doc == null || doc.getDocumentElement() == null || doc.getElementsByTagName("kml").getLength() == 0 || doc.getElementsByTagName("Document").getLength() == 0 || doc.getElementsByTagName("name").getLength() == 0) {
            System.out.println("Document is invalid or does not contain expected elements.");
            return;
        }
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
        NodeList styleList = doc.getElementsByTagName("Style");
        Map<String, String> styleColorMap = new HashMap<>();

        for (int i = 0; i < styleList.getLength(); i++) {
            Element styleElement = (Element) styleList.item(i);
            String styleId = styleElement.getAttribute("id");

            NodeList lineStyleList = styleElement.getElementsByTagName("LineStyle");
            if (lineStyleList.getLength() > 0) {
                Element lineStyle = (Element) lineStyleList.item(0);
                NodeList colorList = lineStyle.getElementsByTagName("color");
                if (colorList.getLength() > 0) {
                    String kmlColor = colorList.item(0).getTextContent();
                    String hexColor = convertKmlColorToHex(kmlColor);
                    styleColorMap.put(styleId, hexColor);
                }
            }
        }

        NodeList placemarkList = doc.getElementsByTagName("Placemark");
        for (int i = 0; i < placemarkList.getLength(); i++) {
            Element placemark = (Element) placemarkList.item(i);

            NodeList lineStringList = placemark.getElementsByTagName("LineString");
            if (lineStringList.getLength() > 0) {
                RailLine railLine = new RailLine();

                NodeList nameList = placemark.getElementsByTagName("name");
                if (nameList.getLength() > 0) {
                    railLine.setName(nameList.item(0).getTextContent());
                }

                NodeList styleUrlList = placemark.getElementsByTagName("styleUrl");
                if (styleUrlList.getLength() > 0) {
                    String styleUrl = styleUrlList.item(0).getTextContent();
                    String styleId = styleUrl.substring(1);

                    String color = styleColorMap.get(styleId);
                    if (color != null) {
                        railLine.setColor(color);
                    }
                }

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
                String coordinates = tElement.getElementsByTagName("coordinates").item(0).getTextContent().trim();
                String[] coordinatesStringArray = coordinates.split(",");
                Double[] coordinatesArray = new Double[2];
                for(int j = 0; j < coordinatesArray.length; j++) {
                    if(coordinatesStringArray[j].length() > 1){
                        if(coordinatesArray[j] == null){
                            coordinatesArray[j] = Double.parseDouble(coordinatesStringArray[j]);
                        }
                    }
                }
                railStation.setCoordinates(coordinatesArray);
                railStations.add(railStation);
            }
        }
    }

    public static String convertKmlColorToHex(String kmlColor) {
        if (kmlColor == null || kmlColor.length() != 8) {
            return "#000000";
        }

        String blue = kmlColor.substring(2, 4);
        String green = kmlColor.substring(4, 6);
        String red = kmlColor.substring(6, 8);

        return "#" + red + green + blue;
    }
}