package dto;

import lombok.Getter;
import lombok.Setter;
import org.locationtech.jts.geom.Point;

import java.util.ArrayList;

@Getter
@Setter
public class RailLines {
    String name;
    String styleUrl;
    ArrayList<Point> coordinates;
}
