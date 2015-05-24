package com.rsa;

public class Circle {

    // The center of a circle
    private Point p;
    // The radius of a circle
    private double r;

    // Construct a circle without any specification
    public Circle() {
        p = new Point(0, 0);
        r = 0;
    }

    // Construct a circle with the specified circle
    public Circle(Circle circle) {
        p = new Point(circle.p);
        r = circle.r;
    }

    // Construct a circle with the specified center and radius
    public Circle(Point center, double radius) {
        p = new Point(center);
        r = radius;
    }

    // Construct a circle based on one point
    public Circle(Point center) {
        p = new Point(center);
        r = 0;
    }

    // Construct a circle based on two points
    public Circle(Point p1, Point p2) {
        p = p1.midPoint(p2);
        r = p1.distance(p);
    }

    // Construct a circle based on three points
    public Circle(Point p1, Point p2, Point p3) throws Exception {

        double dxc = p3.getX() - p1.getX();
        double dyc = p3.getY() - p1.getY();

        double dxl = p2.getX() - p1.getX();
        double dyl = p2.getY() - p1.getY();

        double cross = dxc * dyl - dyc * dxl;
        //should this be equal
        if (cross > 0.0000001 && cross < 0.0000001) {
            throw new Exception("points on the same line");
        }
        double x = (p3.getX() * p3.getX() * (p1.getY() - p2.getY()) + (p1.getX() * p1.getX() + (p1.getY() - p2.getY()) * (p1.getY() - p3.getY()))
                * (p2.getY() - p3.getY()) + p2.getX() * p2.getX() * (-p1.getY() + p3.getY()))
                / (2 * (p3.getX() * (p1.getY() - p2.getY()) + p1.getX() * (p2.getY() - p3.getY()) + p2.getX() * (-p1.getY() + p3.getY())));
        double y = (p2.getY() + p3.getY()) / 2 - (p3.getX() - p2.getX()) / (p3.getY() - p2.getY()) * (x - (p2.getX() + p3.getX()) / 2);
        if (!Double.isFinite(x) || !Double.isFinite(y)) {
            throw new Exception("not real points");
        }
        p = new Point(x, y);
        r = p.distance(p1);
    }

    // Get the center
    public Point getCenter() {
        return p;
    }

    // Get the radius
    public double getRadius() {
        return r;
    }

    // Set the center
    public void setCenter(Point center) {
        p.translate(center);
    }

    // Set the radius
    public void setRadius(double radius) {
        r = radius;
    }

    // Translate the center of a circle to a specified point
    public void translate(Point newCenter) {
        p.translate(newCenter);
    }

    // Offset a circle along its radius by dr
    public void offset(double dr) {
        r += dr;
    }

    // Scale a circle along its radius by a factor
    public void scale(double factor) {
        r *= factor;
    }

    // Calculate the diameter of a circle
    public double getDiameter() {
        return (2 * r);
    }

    // Calculate the circumference of a circle
    public double getCircumference() {
        return (Math.PI * 2 * r);
    }

    // Calcualte the area of a circle
    public double getArea() {
        return (Math.PI * r * r);
    }

    // Is a point in the circle
    public int contain(Point point) {
        int answer = 0;
        double d = p.distance(point);
        if (d == r || (r+0.01 > d && r > d-0.01) ) {
            //System.out.println(point.printVal()+" not in center "+this.getCenter()+" rad "+this.getRadius());
            answer = 0;		// The point is on the circumference of the circle
        } else if (d < r) {
            answer = -1;	// The point is inside the circle
        } else {
            answer = 1;		// The point is outside the circle
        }
        return answer;
    }

    public boolean containAll(Point[] points) {
        for (int i = 0; i < points.length; i++) {
            if (this.contain(points[i]) == 1) {
                return false;
            }
        }
        return true;
    }

    // Determine whether two points are equal
    public boolean equals(Circle circle) {
        return p.equals(circle.p) && (r == circle.r);
    }

    // Return a representation of a point as a string
    @Override
    public String toString() {
        //System.out.println("dd");
        return "Center = (" + p.getX() + ", " + p.getY() + "); " + "Radius = " + r;
    }
}
