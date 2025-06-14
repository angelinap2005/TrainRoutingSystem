
package dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RailLine extends RailObject {
    private String colour;

    public RailLine(){
        super();
    }
}