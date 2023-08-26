package chelly.utils;

public class Point {
    private double x;
    private double y;
    private double risk;

    public Point(double x, double y, double risk) {
        this.x = x;
        this.y = y;
        this.risk = risk;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getRisk() {
        return risk;
    }
}