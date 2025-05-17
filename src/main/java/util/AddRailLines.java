package util;

import dto.RailLines;
import dto.RailStations;

import java.util.ArrayList;

public class AddRailLines {
    public AddRailLines() {}

    public void addLines(ArrayList<RailLines> railLines, ArrayList<RailStations> railStations) {
        for(int i = 0; i < railStations.size(); i++) {
            ArrayList<RailLines> lines = new ArrayList<>();
            for (RailLines line : railLines) {
                if(line.getName().contains(railStations.get(i).getName())) {
                    lines.add(line);
                }
            }
            railStations.get(i).setRailLines(lines);
        }
        System.out.println(railStations.get(0).getRailLines().size());
    }
}
