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
    private static Map<String, String> sMap;

    public static void main(String[] args) {

        configOptions();
        parseOptions(args);
        try {
            run();
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
                //System.exit(1);
            }

            if (!cmd.hasOption("n") && !cmd.hasOption("i")) {
                System.err.println("one of 'n' and 'i' parameters MUST be present.");
                //System.exit(1);
            }

            if (!cmd.hasOption("t") || (cmd.getOptionValue("t") == null)) {
                System.err.println("'t' - max number of threads must be specified");
                //System.exit(1);
            }

            sMap = new HashMap<>(4);
            sMap.put("n", cmd.getOptionValue("n"));
            sMap.put("i", cmd.getOptionValue("i"));
            sMap.put("t", cmd.getOptionValue("t"));
            sMap.put("q", cmd.hasOption("q") + "");

        } catch (ParseException ex) {
            System.err.println("Parsing failed.  Reason: " + ex.getMessage());
        }
    }

    private static int randomWithRange(int min, int max) {
        int range = (max - min) + 1;
        return (int) (Math.random() * range) + min;
    }

    private static void run() throws InterruptedException, ExecutionException {
        Point[] points = new Point[488];
        for (int i = 0; i < points.length; i++) {
            points[i] = new Point(1.0 + 3 * i + randomWithRange(1, 100), 1.7 + i + randomWithRange(1, 100));
            // new Point((double)randomWithRange(1,100), (double)randomWithRange(1,100));

        }
        points[0] = new Point(28.0, 62.7);
        points[1] = new Point(61.0, 45.7);
        points[2] = new Point(53.0, 37.7);
        points[3] = new Point(29.0, 8.7);
        points[4] = new Point(40.0, 41.7);
        points[5] = new Point(113.0, 63.7);
        points[6] = new Point(99.0, 58.7);
        int threadNum = 1;
        int pointsPerThread = points.length;
        int maxThreads = 4;
        int minPointsPerThread = 2;
        if (points.length > minPointsPerThread * 2) {
            threadNum = Math.min(maxThreads, points.length / minPointsPerThread);
            pointsPerThread = points.length / threadNum;
        }
        ExecutorService executorService = Executors.newFixedThreadPool(threadNum);

        Set<Callable<Circle>> callables = new HashSet<>();
        for (int i = 0; i < threadNum; i++) {
            int end = (i == threadNum - 1 ? points.length : (i + 1) * pointsPerThread);
            callables.add(new FindSecWorker(points, i * pointsPerThread, end));
        }

        List<Future<Circle>> futures = executorService.invokeAll(callables);

        Circle solution = null;
        for (Future<Circle> future : futures) {
            if (solution == null || (future.get() != null && future.get().getRadius() < solution.getRadius())) {
                solution = future.get();
            }
        }

        executorService.shutdown();
        
        Circle singleSol = new FindSecWorker(points, 0, points.length).call();
//        if (singleSol == null) {
//            for (Point point : points) {
//                System.out.println(point);
//            }
//        }
        System.out.println("Multi-thread result " + solution);
        System.out.println("Single thread check " + singleSol);
    }
}
