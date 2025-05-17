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
}
