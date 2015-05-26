package com.rsa;

import com.rsa.Circle;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FindSecWorker implements Callable {

    private final Point[] mpoints;
    private final int mstartIndex, mendIndex;
    private Circle solution;
    private Point[] bRecurAlgo;
    private Boolean mquiet;

    public FindSecWorker(Point[] points, int startIndex, int endIndex, boolean quiet) {
        mpoints = points;
        mstartIndex = startIndex;
        mendIndex = endIndex;
        bRecurAlgo = new Point[3];
        mquiet = quiet;
    }

    public Circle getSolution() {
        return solution;
    }

    public Circle recurAlgo(int n, Point[] p, int m) {
        Circle sec = new Circle();

        // Compute the Smallest Enclosing Circle defined by B
        if (m == 1) {
            sec = new Circle(bRecurAlgo[0]);
        } else if (m == 2) {
            sec = new Circle(bRecurAlgo[0], bRecurAlgo[1]);
        } else if (m == 3) {
            try {
                return new Circle(bRecurAlgo[0], bRecurAlgo[1], bRecurAlgo[2]);
            } catch (Exception ex) {
                Logger.getLogger(FindSecWorker.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        // Check if all the points in p are enclosed
        for (int i = 0; i < n; i++) {
            if (sec.contain(p[i]) == 1) {
                // Compute B <--- B union P[i].
                bRecurAlgo[m] = new Point(p[i]);
                // Recurse
                sec = recurAlgo(i, p, m + 1);
            }
        }

        return sec;
    }

    @Override
    public Circle call() {
        if (!mquiet) {
            System.out.println("New thread call() started - from " + mstartIndex + " to " + mendIndex);
        }
        long startTime = System.nanoTime();
        Circle testPointSol;

        for (int i = mstartIndex; i < mendIndex; i++) {
            //System.out.println(mpoints[i].printVal());
            //System.out.println(i+" as first ");
            for (int j = i + 1; j < mpoints.length; j++) {
                //System.out.println(j);
                testPointSol = new Circle(mpoints[i], mpoints[j]);
                //System.out.println(i+" "+j+testPointSol.getCenter()+" "+testPointSol.getRadius());
                //System.out.println(mpoints[i].printVal()+" "+mpoints[j].printVal());
                if (testPointSol.containAll(mpoints)) {
                    //System.out.println("ff3");
                    if (solution == null || testPointSol.getRadius() < solution.getRadius()) {
                        solution = testPointSol;
                        // System.out.println("ff1");
                    }
                }

                for (int k = j + 1; k < mpoints.length; k++) {
                    //System.out.println(i+" "+j+" "+k);
                    try {
                        testPointSol = new Circle(mpoints[i], mpoints[j], mpoints[k]);
                        //System.out.println("go "+testPointSol+
                        //        mpoints[i].getVals()+mpoints[j].getVals()+mpoints[k].getVals());
                        if (testPointSol.containAll(mpoints)) {
                            if (solution == null || testPointSol.getRadius() < solution.getRadius()) {
                                solution = testPointSol;
                                //System.out.println("ff");
                            }
                        }
                    } catch (Exception ex) {
                        //System.out.println("skipping" + ex.getMessage());
                    }
                }
            }
        }
        long duration = (System.nanoTime() - startTime) / 1000;//in us
        if (!mquiet) {
            System.out.println("Thread from " + mstartIndex + " to " + mendIndex + " finished in " + duration + "us");
        }
        return solution;
    }

}
