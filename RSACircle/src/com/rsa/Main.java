package com.rsa;

import com.rsa.view.MecGUI;
import java.io.FileReader;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class Main {

    private static Options sOptions;
    private static Mode runMode;
    
    private static int randPointsNum;
    private static String filePath;
    private static int maxThreads;
    private static boolean quietFlag;
    
    private static final int maxCoord = 2024;
    private static final int minCoord = 0;

    private enum Mode {
        FILE, RANDOM
    }

    public static void main(String[] args) {

        configOptions();
        parseOptions(args);
        
        System.out.println("Debug: " + runMode + " mode, " + maxThreads + " threads, " + randPointsNum + " rand points, " + filePath + " file, quiet=" + quietFlag);
        try {
            for (int i = 0; i < 1; i++) {
                run(runMode, maxThreads, randPointsNum, filePath, quietFlag);
            }
        } catch (InterruptedException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ExecutionException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static void configOptions() {
        sOptions = new Options();
        sOptions.addOption("n", true, "number of points");
        sOptions.addOption("i", true, "path to file, which contains points");
        sOptions.addOption("t", true, "max number of threads to be used");
        sOptions.addOption("q", false, "quiet mode; no GUI");
    }

    private static void parseOptions(String[] args) {

        CommandLineParser parser = new DefaultParser();
        try {
            CommandLine cmd = parser.parse(sOptions, args);

            if (cmd.hasOption("n") && cmd.hasOption("i")) {
                System.err.println("'n' and 'i' parameters are mutually exclusive");
                System.exit(1);
            }
            if (!cmd.hasOption("n") && !cmd.hasOption("i")) {
                System.err.println("one of 'n' and 'i' parameters MUST be present.");
                System.exit(1);
            }
            if (!cmd.hasOption("t") || (cmd.getOptionValue("t") == null)) {
                System.err.println("'t' - max number of threads must be specified");
                System.exit(1);
            }

            if (cmd.hasOption("n")) {
                randPointsNum = Integer.parseInt(cmd.getOptionValue("n"));
                runMode       = Mode.RANDOM;
            } else if (cmd.hasOption("i")) {
                filePath      = cmd.getOptionValue("i");
                runMode       = Mode.FILE;
            } else {
                System.err.println("Verify validation...");
                System.exit(1);
            }

            maxThreads = Integer.parseInt(cmd.getOptionValue("t"));
            if (cmd.hasOption("q")) {
                quietFlag = true;
            }

        } catch (ParseException ex) {
            System.err.println("Parsing failed.  Reason: " + ex.getMessage());
            System.exit(1);
        }
    }

    private static int randomWithRange(int min, int max) {
        int range = (max - min) + 1;
        return (int) (Math.random() * range) + min;
    }

    private static void showPoints(Point[] points) {
        for (Point point : points) {
            System.out.println(point);
        }
    }

    private static Point[] fileToPoints(String filePath) throws Exception {
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
                        if(k > points.length-1)
                            throw new Exception("Too many points in file");
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

    private static void run(Mode runMode, int maxThreads, int randPointNum, String filePath, boolean quietFlag) throws Exception {
        
        final Point[] points;
        if (runMode == Mode.RANDOM) {
            points = new Point[randPointNum];
            for (int i = 0; i < points.length; i++) {
                points[i] = new Point(randomWithRange(minCoord, maxCoord), randomWithRange(minCoord, maxCoord));
            }

            if (points.length == 1) {
                System.out.println("Single point " + new Circle(points[0], 0));
                return;
            }

        } else {
            points = fileToPoints(filePath);
        }
        
        int threadNum       = 1;
        int pointsPerThread = points.length;
        int minPointsPerThread = 2;
        
        if (points.length > minPointsPerThread * 2) {
            threadNum = Math.min(maxThreads, points.length / minPointsPerThread);
            pointsPerThread = points.length / threadNum;
        }

        ExecutorService executorService = Executors.newFixedThreadPool(threadNum);
        Set<Callable<Circle>> callables = new HashSet<>();
        
        int start = 0, end = 0;
        for (int i = 0; i < threadNum; i++) {
            start = end;
            end = (i == threadNum - 1 ? points.length : start + minPointsPerThread + pointsPerThread);
            callables.add(new FindSecWorker(points, start, end, quietFlag));
        }

        List<Future<Circle>> futures = executorService.invokeAll(callables);

        Circle solution = null;
        for (Future<Circle> future : futures) {
            if (solution == null || (future.get() != null && future.get().getRadius() < solution.getRadius())) {
                solution = future.get();
            }
        }

        executorService.shutdown();

        Circle singleSol = new FindSecWorker(points, 0, points.length, quietFlag).call();
        if (singleSol == null || solution == null) {
            showPoints(points);
            System.exit(1);
        }
        
        final Circle solutionForUI = solution;
        if (!quietFlag) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    new MecGUI(Arrays.asList(points), solutionForUI);
                }
            });
        }

        System.out.println("Debug: Multi-thread result " + solution);
        System.out.println("Debug: Single thread check " + singleSol);

        //this is buggy, so just for testing:
        //Circle recurAlgoResult=new FindSecWorker(points, 0, points.length).recurAlgo(points.length,points,0);
        //System.out.println("Recur algo result " +recurAlgoResult);
    }
}
