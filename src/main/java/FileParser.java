import java.io.*;
//code from https://stackoverflow.com/questions/2310139/how-to-read-xml-response-from-a-url-in-java
public class FileParser {

    public static void main(String[] args) throws IOException {
        String railLinesPath = null;
        String railStationsPath = null;

        for (String arg : args) {
            if (arg.startsWith("railLines=")) {
                railLinesPath = arg.substring("railLines=".length());
            } else if (arg.startsWith("railStations=")) {
                railStationsPath = arg.substring("railStations=".length());
            }
        }

        if (railLinesPath == null || railStationsPath == null) {
            System.err.println("Files not found");
            return;
        }

        readTrainLines(new File(railLinesPath).toURI().toURL().openStream());
    }

    private static void readTrainLines(InputStream fileKML) {
        String column;
        Boolean folder = true;
        Boolean placemark = false;
        Boolean placeCorrect = false;
        BufferedReader br = new BufferedReader(new InputStreamReader(fileKML));
        try {
            while ((column = br.readLine()) != null) {
                if (folder) {
                    int ifolder = column.indexOf("<Document>");
                    if (ifolder != -1) {
                        folder = false;
                        placemark = true;
                        continue;
                    }
                }
                if (placemark) {
                    String tmpLine = column;
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
