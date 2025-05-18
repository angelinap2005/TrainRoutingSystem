package dto;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Objects;

@Getter
@Setter
public class RailLine extends RailObject {

    public RailLine(){}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RailLine railLine = (RailLine) o;
        return Objects.equals(getName(), railLine.getName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName());
    }

}
