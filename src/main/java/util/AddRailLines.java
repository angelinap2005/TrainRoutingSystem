package util;

import dto.RailLine;
import dto.RailStation;
import java.util.ArrayList;

public class AddRailLines {
    public AddRailLines() {}

    public void addLines(ArrayList<RailLine> railLines, ArrayList<RailStation> railStations) {
        for (RailStation station : railStations) {
            ArrayList<RailLine> lines = new ArrayList<>();
            String stationName = station.getName();

            for (RailLine line : railLines) {
                //check if the name of the line contains the name of the station
                if (line.getName() != null && line.getName().contains(stationName)) {
                    lines.add(line);
                }
            }
            station.setRailLines(lines);
        }
    }
}