package dev.senj.common;

public class Position {
    
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
