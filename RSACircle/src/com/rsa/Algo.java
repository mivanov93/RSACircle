package com.rsa;

import java.io.FileReader;
import java.util.ArrayList;
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

    private static final int maxCoord = 10240;
    private static final int minCoord = 0;

    private enum Mode {
        FILE, RANDOM
    }

    private Mode runMode;
    private int randPointsNum;
    private int maxThreads;
    private String filePath;
    private boolean quietFlag;

    private List<Point> points;
    
    private AppGUI appGUI;

    private class StartEnd {

        public int start, end;

        public StartEnd(int pstart, int pend) {
            this.start = pstart;
            this.end = pend;
        }
    };
    
    public Algo() {
        this(null);
    }
    
    public Algo(AppGUI appGUI) {
        this.appGUI = appGUI;
    }

    public Circle run(CommandLine cmd) throws Exception {
        
        log("Running algorithm...");

        if (cmd.hasOption("n")) {
            randPointsNum = Integer.parseInt(cmd.getOptionValue("n"));
            runMode = Mode.RANDOM;
        } else if (cmd.hasOption("i")) {
            filePath = cmd.getOptionValue("i");
            runMode = Mode.FILE;
        } else {
            log("Verify validation...");
            System.exit(1);
        }

        maxThreads = Integer.parseInt(cmd.getOptionValue("t"));
        quietFlag = cmd.hasOption("q");

        log("Mode:" + runMode + ", " + maxThreads + " threads, " + randPointsNum + " rand points, " + filePath + " file");

        final Point[] points;
        if (runMode == Mode.RANDOM) {
            log("Generating random points...");
            
            points = new Point[randPointsNum];
            for (int i = 0; i < points.length; i++) {
                points[i] = new Point(randomWithRange(minCoord, maxCoord), randomWithRange(minCoord, maxCoord));
            }

            if (points.length == 1) {
                log("Single point: " + new Circle(points[0], 0));
                return null;
            }

        } else {
            log("Parsing points from file...");
            
            points = fileToPoints(filePath);
        }

        log("Searching potential points using convex hull...");
        GrahamScan test = new GrahamScan(points);
        Point[] hullPoints = test.hull();

        int threadNum = maxThreads;
        int n = hullPoints.length;

        int sum = 0, startPoint = 0;
        //black magic :D
        int allTriplesDoubles = n * (n - 1) * (n - 2) / 6 + n * (n - 1) / 2;
        int triplesDoublesPerThread = allTriplesDoubles / threadNum;
        ArrayList<StartEnd> stEndList = new ArrayList<>();
        
        for (int i = 0; i < n; i++) {
            //more black magic :D
            sum += (int) (0.5 * (n - i - 1) * (n - i - 2) + (n - 1 - i));
            if (sum >= triplesDoublesPerThread) {
                stEndList.add(new StartEnd(startPoint, i + 1));

                sum = 0;
                startPoint = i + 1;
            } else if (i == n - 1) {
                stEndList.add(new StartEnd(startPoint, i + 1));
            }
        }
        threadNum = stEndList.size();

        log("Constructing executor service...");
        ExecutorService executorService = Executors.newFixedThreadPool(threadNum);
        Set<Callable<Circle>> callables = new HashSet<>();

        for (int i = 0; i < threadNum; i++) {
            callables.add(new FindSecWorker(this, hullPoints, stEndList.get(i).start, stEndList.get(i).end, quietFlag));
        }

        log("Executing tasks...");
        List<Future<Circle>> futures = executorService.invokeAll(callables);

        Circle solution = null;
        for (Future<Circle> future : futures) {
            if (solution == null || (future.get() != null && future.get().getRadius() < solution.getRadius())) {
                solution = future.get();
            }
        }
        
        log("Ready!");
        executorService.shutdown();

        //Circle singleSol = new FindSecWorker(points, 0, points.length, quietFlag).call();
        Circle grahamSingleSol = new FindSecWorker(this, hullPoints, 0, hullPoints.length, quietFlag).call();
        if (grahamSingleSol == null || solution == null) {
            showPoints(points);
            System.exit(1);
        }

        log("Multi-thread result " + solution);
        log("Single thread check " + grahamSingleSol);

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
        log(Arrays.toString(points));
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

    public void log(String msg) {

        if (!quietFlag && appGUI != null) {
            appGUI.log(msg);
        }
        
        System.out.println(msg);
    }
}
