package dto;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
@Getter
@Setter
public class Station {
    private RailStation railStation;
    private ArrayList<Route> routes;
    private String nodeId;

    public Station(RailStation railStation, ArrayList<Route> routes) {
        this.railStation = railStation;
        this.routes = routes;
    }
}
