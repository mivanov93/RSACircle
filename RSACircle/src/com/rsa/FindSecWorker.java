package com.rsa;

import com.rsa.Circle;
import java.util.concurrent.Callable;

public class FindSecWorker implements Callable {

    private final Point[] mpoints;
    private final int mstartIndex, mendIndex;
    private Circle solution;

    public FindSecWorker(Point[] points, int startIndex, int endIndex) {
        mpoints = points;
        mstartIndex = startIndex;
        mendIndex = endIndex;
    }

    public Circle getSolution() {
        return solution;
    }

    @Override
    public Circle call() {
        System.out.println("New thread call() started");
        Circle testPointSol;

        for (int i = mstartIndex; i < mendIndex; i++) {
            //System.out.println(mpoints[i].printVal());
            for (int j = i+1; j < mpoints.length; j++) {
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

                for (int k = j+1; k < mpoints.length; k++) {
                     //System.out.println(mpoints[i].printVal()+" "+mpoints[j].printVal()+" "+mpoints[k].printVal());
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
                        //System.out.println("skipping");
                    }
                }
            }
        }
//       if (solution != null) {
//            System.out.println(solution);
//        } else {
//            System.out.println("no solution");
//        }
        return solution;
    }

}
