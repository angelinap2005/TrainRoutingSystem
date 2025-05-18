package dto;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
@Setter
@Getter
public class RailObject {
    String name;
    String styleUrl;
    String[] coordinates;
    ArrayList<Integer> lineString;
    String placemark;

    public RailObject(String name, String styleUrl, String[] coordinates, ArrayList<Integer> lineString, String placemark) {
        this.name = name;
        this.styleUrl = styleUrl;
        this.coordinates = coordinates;
        this.lineString = lineString;
        this.placemark = placemark;
    }

    public RailObject() {}
}
