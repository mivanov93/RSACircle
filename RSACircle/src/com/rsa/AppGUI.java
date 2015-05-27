package com.rsa;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

public class AppGUI extends JFrame {

    private DrawPanel drawPanel;

    private JButton btnZoomIn;
    private JButton btnZoomOut;
    private JTextArea textArea;

    private static int count = 1;
    
    public AppGUI() {
        initUI();
    }

    private void initUI() {

        Form form = new Form();
        getContentPane().add(form);

        drawPanel = new DrawPanel();
        drawPanel.setPreferredSize(new Dimension(8000, 8000));
        drawPanel.setBackground(new Color(1,255,3));
        
        JScrollPane scrollPane = form.getjScrollPane3();
        scrollPane.add(drawPanel);
        scrollPane.setViewportView(drawPanel);

        btnZoomIn = form.getjButton1();
        btnZoomIn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        drawPanel.setZoomScale(drawPanel.getZoomScale() * 1.1);
                    }
                });
            }
        });

        btnZoomOut = form.getjButton2();
        btnZoomOut.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        drawPanel.setZoomScale(drawPanel.getZoomScale() / 1.1);
                    }
                });
            }
        });
        
        textArea = form.getjTextArea1();
        log("Initializing...");

        setTitle("Minimal enclosing circle");
        setSize(800, 550);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }

    public void setData(List<Point> points, Circle circle) {
        drawPanel.setData(points, circle);
        log("Done.");
    }
    
    public void log(final String msg) {
        
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                textArea.append(count++ + ". " + msg + "\n");
            }
        });
    }
}

class DrawPanel extends javax.swing.JPanel {

    private List<Point> mPoints;
    private Circle mCircle;
    private double radScale, zoomScale;

    public DrawPanel() {
        mPoints = null;
        mCircle = null;
        zoomScale = 1.0;
        setBackground(Color.WHITE);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        doDrawing(g);
    }

    public void setZoomScale(double newScale) {
        zoomScale = Math.min(Math.max(0.01, newScale), 10.0);
        repaint();
    }

    public double getZoomScale() {
        return zoomScale;
    }
    
    private double toRadScale(Double param) {
        return radScale * param;
    }

    private void doDrawing(Graphics g) {
        if (mPoints == null || mCircle == null) {
            return;
        }

        Graphics2D g2d = (Graphics2D) g;
        
        //antialiasing, smooth figures
        g2d.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(
                RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        

        g2d.scale(zoomScale, zoomScale);
        radScale = 200 / mCircle.getRadius();

        Double dispConst=50.0;
        //disp to move circle to a better position
        Double xDisp = dispConst / zoomScale + toRadScale(mCircle.getRadius() - mCircle.getCenter().getX());
        Double yDisp = dispConst / zoomScale + toRadScale(mCircle.getRadius() - mCircle.getCenter().getY());
        
        Double szConst = 50.0;
        Double pointDispSize = szConst * Math.pow(zoomScale, 1 / 8) / radScale / 10;

        
        for (Point p : mPoints) {
            g2d.setColor(Color.RED);
            
            //draw circle around point
            Ellipse2D myEl = new Ellipse2D.Double(
                    toRadScale(p.getX() - pointDispSize / 2) + xDisp,
                    toRadScale(p.getY() - pointDispSize / 2) + yDisp,
                    toRadScale(pointDispSize),
                    toRadScale(pointDispSize));
            g2d.fill(myEl);
            g2d.setColor(Color.BLACK);
            
            //draw point
            g2d.draw(new Line2D.Double(
                    toRadScale(p.getX()) + xDisp,
                    toRadScale(p.getY()) + yDisp,
                    toRadScale(p.getX()) + xDisp,
                    toRadScale(p.getY()) + yDisp));
        }

        //draw the circle
        g2d.draw(new Ellipse2D.Double(
                toRadScale(mCircle.getCenter().getX() - mCircle.getRadius()) + xDisp,
                toRadScale(mCircle.getCenter().getY() - mCircle.getRadius()) + yDisp,
                toRadScale(mCircle.getDiameter()),
                toRadScale(mCircle.getDiameter())
        )
        );
    }

    public void setData(List<Point> points, Circle circle) {
        mPoints = points;
        mCircle = circle;

        repaint();
        updateUI();
    }
}
