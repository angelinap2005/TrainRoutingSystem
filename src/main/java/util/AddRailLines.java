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
                // Check if the station is on this line
                if (line.getName() != null &&
                        line.getName().contains(stationName)) {
                    lines.add(line);
                }
            }

            station.setRailLines(lines);
        }
    }
}