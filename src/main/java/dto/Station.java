package dto;

import java.util.ArrayList;

public class Station {
    RailStation railStation;
    ArrayList<Route> routes;

    public Station(RailStation railStation, ArrayList<Route> routes) {
        this.railStation = railStation;
        this.routes = routes;
    }
}
