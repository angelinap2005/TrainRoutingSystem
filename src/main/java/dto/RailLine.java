package dto;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;

@Getter
@Setter
public class RailLine extends RailObject {
    public RailLine(String name, String styleUrl, String[] coordinates, ArrayList<Integer> lineString, String placemark) {
        super(name, styleUrl, coordinates, lineString, placemark);
    }

    public RailLine(){}
}
