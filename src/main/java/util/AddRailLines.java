package util;

import dto.RailLine;
import dto.RailStation;

import java.util.ArrayList;

public class AddRailLines {
    public AddRailLines() {}

    public void addLines(ArrayList<RailLine> railLines, ArrayList<RailStation> railStations) {
        for(int i = 0; i < railStations.size(); i++) {
            ArrayList<RailLine> lines = new ArrayList<>();
            for (RailLine line : railLines) {
                if(line.getName().contains(railStations.get(i).getName())) {
                    lines.add(line);
                }
            }
            railStations.get(i).setRailLines(lines);
        }
        System.out.println(railStations.get(0).getRailLines().size());
    }
}
