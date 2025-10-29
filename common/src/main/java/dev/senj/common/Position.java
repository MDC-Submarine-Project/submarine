package dev.senj.common;

import java.io.Serializable;

public class Position implements Serializable {

    public float x, y, z;

    public static Position getInstance() {
        return new Position();
    }

    @Override
    public String toString() {
        return "Position{" +
                "x=" + x +
                ", y=" + y +
                ", z=" + z +
                '}';
    }
}
