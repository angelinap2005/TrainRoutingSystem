package dto;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
@Getter
@Setter
public class RailStation extends RailObject{
    ArrayList<RailLine> railLines;

    public RailStation(String name, String styleUrl, String[] coordinates, ArrayList<Integer> lineString, String placemark) {
        super(name, styleUrl, coordinates, lineString, placemark);
        railLines = new ArrayList<>(); // Initialize the railLines array list
    }

    public RailStation() {}
}
