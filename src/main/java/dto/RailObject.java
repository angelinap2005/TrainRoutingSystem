package dto;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
@Setter
@Getter
public class RailObject {
    private String name;
    private String styleUrl;
    private String[] coordinates;
    private ArrayList<Integer> lineString;
    private String placemark;

    public RailObject() {}
}
