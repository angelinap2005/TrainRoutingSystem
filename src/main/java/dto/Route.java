package dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Route {
    RailLine railLine;
    RailStation destination;

    public Route(RailLine railLine) {
        this.railLine = railLine;
    }
}
