
package dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Route {
    private RailLine railLine;
    private RailStation destination;
    private double weight;

    public Route(RailLine railLine) {
        this.railLine = railLine;
    }

    public Route(RailLine railLine, RailStation destination, double weight) {
        this.railLine = railLine;
        this.destination = destination;
        this.weight = weight;
    }
}