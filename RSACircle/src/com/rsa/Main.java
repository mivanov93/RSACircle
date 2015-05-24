package com.rsa;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class Main {

    private static Options sOptions;
    private static int maxThreads;
    private static Mode runMode;
    private static int randPointsNum;
    private static boolean quietFlag;
    private static String filePath;

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
                runMode = Mode.RANDOM;
            } else if (cmd.hasOption("i")) {
                runMode = Mode.FILE;
                filePath = cmd.getOptionValue("i");
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

    private static void run(Mode runMode, int maxThreads, int randPointNum, String filePath, boolean quietFlag) throws InterruptedException, ExecutionException {
        Point[] points = null;
        if (runMode == Mode.RANDOM) {
            points = new Point[randPointNum];
            for (int i = 0; i < points.length; i++) {
                points[i] = new Point(randomWithRange(0, 10024), randomWithRange(0, 10024));
            }

            if (points.length == 1) {
                System.out.println("Single point " + new Circle(points[0], 0));
                return;
            }
        } else {
            System.exit(1);
        }
        int threadNum = 1;
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
            for (Point point : points) {
                System.out.println(point);
            }
            System.exit(1);
        }

        System.out.println("Debug: Multi-thread result " + solution);
        System.out.println("Debug: Single thread check " + singleSol);

        //this is buggy, so just for testing:
        //Circle recurAlgoResult=new FindSecWorker(points, 0, points.length).recurAlgo(points.length,points,0);
        //System.out.println("Recur algo result " +recurAlgoResult);
    }
}
