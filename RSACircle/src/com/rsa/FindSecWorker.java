package com.rsa;

import java.util.concurrent.Callable;

public class FindSecWorker implements Callable {

    private final Algo algo;
    private final Point[] mpoints;
    private final int mstartIndex, mendIndex;
    private Circle solution;
    private Boolean mquiet;

    public FindSecWorker(Algo algo, Point[] points, int startIndex, int endIndex, boolean quiet) {
        this.algo = algo;
        mpoints = points;
        mstartIndex = startIndex;
        mendIndex = endIndex;
        mquiet = quiet;
    }

    public Circle getSolution() {
        return solution;
    }

    @Override
    public Circle call() {
        if (!mquiet) {
            algo.log("New thread call() started - from " + mstartIndex + " to " + mendIndex);
        }
        long startTime = System.nanoTime();
        
        Circle testPointSol;

        for (int i = mstartIndex; i < mendIndex; i++) {
            for (int j = i + 1; j < mpoints.length; j++) {
                testPointSol = new Circle(mpoints[i], mpoints[j]);
                if (testPointSol.containAll(mpoints)) {
                    if (solution == null || testPointSol.getRadius() < solution.getRadius()) {
                        solution = testPointSol;
                    }
                }

                for (int k = j + 1; k < mpoints.length; k++) {
                    try {
                        testPointSol = new Circle(mpoints[i], mpoints[j], mpoints[k]);
                        if (testPointSol.containAll(mpoints)) {
                            if (solution == null || testPointSol.getRadius() < solution.getRadius()) {
                                solution = testPointSol;
                            }
                        }
                    } catch (Exception ex) {
                        // skip
                    }
                }
            }
        }
        long duration = (System.nanoTime() - startTime) / 1000; //in us
        if (!mquiet) {
            algo.log("Thread from " + mstartIndex + " to " + mendIndex + " finished in " + duration + "us");
        }
        return solution;
    }
}