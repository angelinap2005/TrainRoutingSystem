package dto;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
@Getter
@Setter
public class RailStation extends RailObject{
    private ArrayList<RailLine> railLines;

    public RailStation() {}
}
