import java.io.*;
//code from https://stackoverflow.com/questions/2310139/how-to-read-xml-response-from-a-url-in-java
public class FileParser {

    public static void main(String[] args) throws IOException {
        String railLinesPath = null;
        String stationName = null;

        for (String arg : args) {
            if (arg.startsWith("railLines=")) {
                railLinesPath = arg.substring("railLines=".length());
            } else if (arg.startsWith("railStations=")) {
                stationName = arg.substring("railStations=".length());
            }
        }

        if (railLinesPath == null || stationName == null) {
            System.err.println("Usage: java FileParser railLines=<path> railStations=<placemark name>");
            return;
        }

        readKML(new File(railLinesPath).toURI().toURL().openStream(), stationName);
    }

    private static void readKML(InputStream fileKML, String nameCoordinates) {
        String column = null;
        Boolean folder = Boolean.TRUE;
        Boolean placemark = Boolean.FALSE;
        Boolean placeCorrect = Boolean.FALSE;
        BufferedReader br = new BufferedReader(new InputStreamReader(fileKML));
        try {
            while ((column = br.readLine()) != null) {
                if (folder) {
                    int ifolder = column.indexOf("<Document>");
                    if (ifolder != -1) {
                        folder = Boolean.FALSE;
                        placemark = Boolean.TRUE;
                        continue;
                    }
                }
                if (placemark) {
                    String tmpLine = nameCoordinates;
                    tmpLine = tmpLine.replaceAll("\t", "");
                    tmpLine = tmpLine.replaceAll(" ", "");
                    String tmpColumn = column;
                    tmpColumn = tmpColumn.replaceAll("\t", "");
                    tmpColumn = tmpColumn.replaceAll(" ", "");
                    int name = tmpColumn.indexOf(tmpLine);
                    if (name != -1) {
                        placemark = Boolean.FALSE;
                        placeCorrect = Boolean.TRUE;
                        continue;
                    }
                }
                if (placeCorrect) {
                    int coordin = column.indexOf("<coordinates>");
                    if (coordin != -1) {
                        String tmpCoordin = column;
                        tmpCoordin = tmpCoordin.replaceAll(" ", "");
                        tmpCoordin = tmpCoordin.replaceAll("\t", "");
                        tmpCoordin = tmpCoordin.replaceAll("<coordinates>", "");
                        tmpCoordin = tmpCoordin
                                .replaceAll("</coordinates>", "");
                        String[] coo = tmpCoordin.split(",");
                        System.out.println("LONG: "+coo[0]);
                        System.out.println("LATI: "+coo[1]);
                        break;
                    }
                }

            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
