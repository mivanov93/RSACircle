package com.rsa;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

public class AppGUI extends JFrame {
    
    private DrawPanel drawPanel;
    
    private JButton btnZoomIn;
    private JButton btnZoomOut;
    private JTextArea textArea;

    public AppGUI() {
        initUI();
    }

    private void initUI() {
        
        Form form = new Form();
        getContentPane().add(form);
        
        btnZoomIn = form.getjButton1();
        btnZoomIn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                
            }
        });
        
        btnZoomOut = form.getjButton2();
        btnZoomOut.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                
            }
        });
        
        // 2) panelDraw
        JScrollPane scrollPane = form.getjScrollPane3(); 

        drawPanel = new DrawPanel();
        drawPanel.setPreferredSize(new Dimension(160000, 160000));
        drawPanel.setBackground(Color.yellow);
        scrollPane.add(drawPanel);
        scrollPane.setViewportView(drawPanel);
        // 3) panelLog
        textArea = form.getjTextArea1();
        textArea.setText("Calculating...");

        setTitle("Minimal enclosing circle");
        setSize(600, 380);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }
    
    public void setData(List<Point> points, Circle circle) {
        drawPanel.setData(points, circle);
        textArea.setText("Done.");
    }
}

class DrawPanel extends javax.swing.JPanel {

    private List<Point> mPoints;
    private Circle mCircle;
    private double radScale;

    public DrawPanel() {
        mPoints = null;
        mCircle = null;
        setBackground(Color.WHITE);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        doDrawing(g);
    }
    
    private  double toSc(Double param)
    {
        return radScale*param;
    }

    private void doDrawing(Graphics g) {
        if (mPoints == null || mCircle == null) {
            return;
        }

        Graphics2D g2d = (Graphics2D) g;
        g2d.setColor(Color.BLACK);
        Double pScale = 130.0 / 30.0;
        g2d.scale(pScale, pScale);  
        radScale = 200/mCircle.getRadius();
 
        Double dispConst = 50.0;
        Double xDisp = dispConst/pScale + toSc(Math.abs(mCircle.getRadius() - mCircle.getCenter().getX()));
        Double yDisp = dispConst/pScale + toSc(Math.abs(mCircle.getRadius() - mCircle.getCenter().getX()));
        Double dispSize = dispConst * Math.pow(pScale,1/8)/radScale/10;
        
        for (Point p : mPoints) {
            //System.out.println("Drawing: " + p.getX() + ", " + p.getY());
            g2d.setColor(Color.RED);  
            Ellipse2D myEl = new Ellipse2D.Double(
                    toSc(p.getX()  - dispSize / 2)+ xDisp, 
                    toSc(p.getY()  - dispSize / 2)+ yDisp, 
                    toSc(dispSize), 
                    toSc(dispSize));
            g2d.fill(myEl);
            g2d.setColor(Color.BLACK);
            g2d.draw(new Line2D.Double(
                    toSc(p.getX() )+ xDisp, 
                    toSc(p.getY() )+ yDisp, 
                    toSc(p.getX() )+ xDisp, 
                    toSc(p.getY() )+ yDisp));
        }
        //System.out.println("Drawing circle... with center: " + mCircle.getCenter() + " d = " + mCircle.getDiameter());
        g2d.draw(new Ellipse2D.Double(
                toSc(mCircle.getCenter().getX() - mCircle.getRadius() )+ xDisp, 
                toSc(mCircle.getCenter().getY()  - mCircle.getRadius())+ yDisp, 
                toSc(mCircle.getDiameter()), 
                toSc(mCircle.getDiameter())
                )
        );
        //System.out.println(mCircle.getCenter().getX() - mCircle.getRadius() + xDisp);
    }
    
    public void setData(List<Point> points, Circle circle) {
        mPoints = points;
        mCircle = circle;
        
        repaint();
        updateUI();
    }
}
