package com.rsa;

import java.io.FileReader;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.apache.commons.cli.CommandLine;

public class Algo {
    
    private static final int maxCoord = 500;
    private static final int minCoord = 0;

    private enum Mode { FILE, RANDOM }
    
    private Mode    runMode;
    private int     randPointsNum;
    private int     maxThreads;
    private String  filePath;
    private boolean quietFlag;
    
    private List<Point> points;
    
    public Circle run(CommandLine cmd) throws Exception {
        
        if (cmd.hasOption("n")) {
            randPointsNum = Integer.parseInt(cmd.getOptionValue("n"));
            runMode = Mode.RANDOM;
        } else if (cmd.hasOption("i")) {
            filePath = cmd.getOptionValue("i");
            runMode = Mode.FILE;
        } else {
            System.err.println("Verify validation...");
            System.exit(1);
        }

        maxThreads = Integer.parseInt(cmd.getOptionValue("t"));
        quietFlag  = cmd.hasOption("q");
        
        System.out.println("Debug: " + runMode + " mode, " + maxThreads + " threads, " + randPointsNum + " rand points, " + filePath + " file");
        
        final Point[] points;
        if (runMode == Mode.RANDOM) {
            points = new Point[randPointsNum];
            for (int i = 0; i < points.length; i++) {
                points[i] = new Point(randomWithRange(minCoord, maxCoord), randomWithRange(minCoord, maxCoord));
            }

            if (points.length == 1) {
                System.out.println("Single point " + new Circle(points[0], 0));
                return null;
            }

        } else {
            points = fileToPoints(filePath);
        }

        GrahamScan test = new GrahamScan(points);
        Point[] hullPoints = test.hull();

        int threadNum = 1;
        int pointsPerThread = hullPoints.length;
        int minPointsPerThread = 2;

        if (hullPoints.length > minPointsPerThread * 2) {
            threadNum = Math.min(maxThreads, hullPoints.length / minPointsPerThread);
            pointsPerThread = hullPoints.length / threadNum;
        }

        ExecutorService executorService = Executors.newFixedThreadPool(threadNum);
        Set<Callable<Circle>> callables = new HashSet<>();

        int start = 0, end = 0;
        for (int i = 0; i < threadNum; i++) {
            start = end;
            end = (i == threadNum - 1 ? hullPoints.length : start + minPointsPerThread + pointsPerThread);
            callables.add(new FindSecWorker(hullPoints, start, end, quietFlag));
        }

        List<Future<Circle>> futures = executorService.invokeAll(callables);

        Circle solution = null;
        for (Future<Circle> future : futures) {
            if (solution == null || (future.get() != null && future.get().getRadius() < solution.getRadius())) {
                solution = future.get();
            }
        }

        executorService.shutdown();

        //Circle singleSol = new FindSecWorker(points, 0, points.length, quietFlag).call();
        Circle grahamSingleSol = new FindSecWorker(hullPoints, 0, hullPoints.length, quietFlag).call();
        if (grahamSingleSol == null || solution == null) {
            showPoints(points);
            System.exit(1);
        }

        System.out.println("Debug: Multi-thread result " + solution);
        //System.out.println("Debug: Single thread check " + singleSol);
        System.out.println("Debug: Single thread check " + grahamSingleSol);

        //this is buggy, so just for testing:
        //Circle recurAlgoResult=new FindSecWorker(points, 0, points.length).recurAlgo(points.length,points,0);
        //System.out.println("Recur algo result " +recurAlgoResult);
        
        this.points = Arrays.asList(points);
        
        return solution;
    }
    
    private int randomWithRange(int min, int max) {
        int range = (max - min) + 1;
        return (int) (Math.random() * range) + min;
    }

    private void showPoints(Point[] points) {
        for (Point point : points) {
            System.out.println(point);
        }
    }
    
    public List<Point> getPoints() {
        return points;
    }

    private Point[] fileToPoints(String filePath) throws Exception {
        Point[] points = null;
        if (filePath == null) {
            throw new Exception("null");
        }

        FileReader fin = new FileReader(filePath);

        Scanner src = new Scanner(fin);
        int i, count = 0, k = 0, lastI = 0, len = 0;
        while (src.hasNext()) {
            if (src.hasNextInt()) {
                i = src.nextInt();
                if (count == 0) {
                    len = i;
                    points = new Point[i];
                } else {
                    if (i > maxCoord || i < minCoord) {
                        throw new Exception("Points in file not in the correct range");
                    }

                    if (count != 1 && (count == 2 || count % 2 == 1)) {
                        if (k > points.length - 1) {
                            throw new Exception("Too many points in file");
                        }
                        points[k++] = new Point(lastI, i);
                    }
                }
                lastI = i;
            } else {
                throw new Exception("Points in file with incorrect coords.");
            }
            count++;
        }
        fin.close();
        // System.out.println("int: " + i);
        if (len == 0 || points.length != len) {
            throw new Exception("Not enough points in file");
        }
        return points;
    }
    
    private void log(String msg) {
        
        
        
        if (!quietFlag) {
            
        }
    }
}
