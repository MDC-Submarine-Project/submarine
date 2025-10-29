package dev.senj.common;

import java.io.Serializable;

public class Position implements Serializable {

    public float x, y, z;

    private static final Position INSTANCE = new Position();

    public static Position getInstance() {
        return INSTANCE;
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
