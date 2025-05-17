package dto;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;

@Getter
@Setter
public class RailLines extends RailObject {
    String name;
    String styleUrl;
    String[] coordinates;
    ArrayList<Integer> lineString;
    String placemark;
}
