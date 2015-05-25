package com.rsa.view;

import com.rsa.Circle;
import com.rsa.Point;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.util.List;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

public class MecGUI extends JFrame {

    public MecGUI(List<Point> points, Circle circle) {
        initUI(points, circle);
    }

    private void initUI(List<Point> points, Circle circle) {

        DrawPanel drawPanel = new DrawPanel(points, circle);
        drawPanel.setPreferredSize(new Dimension(60000, 60000));
        JScrollPane scrollPane = new JScrollPane(drawPanel);

        add(scrollPane, BorderLayout.CENTER);
        setTitle("Minimal enclosing circle");
        setSize(1000, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }
}

class DrawPanel extends JPanel {

    private final List<Point> mPoints;
    private final Circle mCircle;

    public DrawPanel(List<Point> points, Circle circle) {
        mPoints = points;
        mCircle = circle;
        setBackground(Color.GRAY);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        doDrawing(g);
    }

    private void doDrawing(Graphics g) {

        Graphics2D g2d = (Graphics2D) g;
        g2d.setColor(Color.BLACK);
        Double pScale = 8.0 / 30.0;
        g2d.scale(pScale, pScale);
        Double dispConst = 50.0;
        Double xDisp = dispConst / pScale + Math.abs(mCircle.getRadius() - mCircle.getCenter().getX());
        Double yDisp = dispConst / pScale + Math.abs(mCircle.getRadius() - mCircle.getCenter().getX());
        Double dispSize = dispConst * pScale * 1;
        for (Point p : mPoints) {
            System.out.println("Drawing: " + p.getX() + ", " + p.getY());
            g2d.setColor(Color.RED);
            Ellipse2D myEl = new Ellipse2D.Double(p.getX() + xDisp - dispSize / 2, p.getY() + yDisp - dispSize / 2, dispSize, dispSize);
            g2d.fill(myEl);
            g2d.setColor(Color.BLACK);
            g2d.draw(new Line2D.Double(p.getX() + xDisp, p.getY() + yDisp, p.getX() + xDisp, p.getY() + yDisp));

        }
        System.out.println("Drawing circle... with center: " + mCircle.getCenter() + " d = " + mCircle.getDiameter());
        g2d.draw(new Ellipse2D.Double(mCircle.getCenter().getX() - mCircle.getRadius() + xDisp, mCircle.getCenter().getY() + yDisp - mCircle.getRadius(), mCircle.getDiameter(), mCircle.getDiameter()));
        System.out.println(mCircle.getCenter().getX() - mCircle.getRadius() + xDisp);
        //setLocation((int)mCircle.getCenter().getX(), (int)mCircle.getCenter().getY());
    }
}
