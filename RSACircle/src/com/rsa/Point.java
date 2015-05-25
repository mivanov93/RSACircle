package com.rsa;

import java.awt.geom.Point2D;
import java.util.Comparator;

public class Point implements Comparable<Point> {

    // The x corrdinate of a point
    private double x;
    // The y corrdinate of a point
    private double y;

    // Construct a point at the origin
    public Point() {
        x = 0;
        y = 0;
    }

    public String getVals() {
        return "x: " + getX() + " y: " + getY();
    }

    // Construct a point at the specified location (xVal, yVal)
    public Point(double xVal, double yVal) {
        x = xVal;
        y = yVal;
    }

    // Construct a point with the same location as the specified point
    public Point(Point point) {
        x = point.x;
        y = point.y;
    }

    // Get the x corrdinate
    public double getX() {
        return x;
    }

    // Get the y corrdinate
    public double getY() {
        return y;
    }

    // Set the x corrdinate
    public void setX(double xVal) {
        x = xVal;
    }

    // Set the y corrdinate
    public void setY(double yVal) {
        y = yVal;
    }

    // Translate a point to the specified location
    public void translate(Point point) {
        translate(point.x, point.y);
    }

    // Translate a point to the specified location (newX, newY)
    public void translate(double newX, double newY) {
        x = newX;
        y = newY;
    }

    // Offset a point along the x and y axes by dx and dy, respectively
    public void offset(double dx, double dy) {
        x += dx;
        y += dy;
    }

    // Calcualte the distance between two points
    public double distance(Point point) {
        double dx = x - point.x;
        double dy = y - point.y;
        //System.err.println(y+" "+point.y+" "+dx+" "+dy+" "+(dx * dx + dy * dy));
        return Math.sqrt(dx * dx + dy * dy);
    }

    // Calcualte the square of the distance between two points
    public double distance2(Point point) {
        double dx = x - point.x;
        double dy = y - point.y;
        return (dx * dx + dy * dy);
    }

    // Calculate the middle point between two points
    public Point midPoint(Point point) {
        return new Point((x + point.x) / 2, (y + point.y) / 2);
    }

    // Determine whether two points are equal
    public boolean equals(Point point) {
        return (x == point.x) && (y == point.y);
    }

    // Return a representation of a point as a string
    @Override
    public String toString() {
        return "point = (" + x + "," + y + ")";
    }

    public static int ccw(Point a, Point b, Point c) {
        double area2 = (b.x - a.x) * (c.y - a.y) - (b.y - a.y) * (c.x - a.x);
        if (area2 < 0) {
            return -1;
        } else if (area2 > 0) {
            return +1;
        } else {
            return 0;
        }
    }

    @Override
        public int compareTo(Point that) {
        if (this.y < that.y) return -1;
        if (this.y > that.y) return +1;
        if (this.x < that.x) return -1;
        if (this.x > that.x) return +1;
        return 0;
    }
    
    
    public final Comparator<Point> POLAR_ORDER = new PolarOrder();

    private class PolarOrder implements Comparator<Point> {

        public int compare(Point q1, Point q2) {
            double dx1 = q1.x - x;
            double dy1 = q1.y - y;
            double dx2 = q2.x - x;
            double dy2 = q2.y - y;

            if (dy1 >= 0 && dy2 < 0) {
                return -1;    // q1 above; q2 below
            } else if (dy2 >= 0 && dy1 < 0) {
                return +1;    // q1 below; q2 above
            } else if (dy1 == 0 && dy2 == 0) {            // 3-collinear and horizontal
                if (dx1 >= 0 && dx2 < 0) {
                    return -1;
                } else if (dx2 >= 0 && dx1 < 0) {
                    return +1;
                } else {
                    return 0;
                }
            } else {
                return -ccw(Point.this, q1, q2);     // both above or below
            }
            // Note: ccw() recomputes dx1, dy1, dx2, and dy2
        }

    }
}
