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
        Point[] points = new Point[15];
        for (int i = 0; i < points.length; i++) {
            points[i] = new Point(1.0 + 3 * i + randomWithRange(1, 100), 1.7 + i + randomWithRange(1, 100));
            // new Point((double)randomWithRange(1,100), (double)randomWithRange(1,100));

        }
        ExecutorService executorService = Executors.newFixedThreadPool(10);

        Set<Callable<Circle>> callables = new HashSet<>();
        callables.add(new FindSecWorker(points, 0, 15));
        List<Future<Circle>> futures = executorService.invokeAll(callables);

        for (Future<Circle> future : futures) {
            System.out.println("future.get = " + future.get());
        }

        
        executorService.shutdown();

        // new Thread(new FindSecWorker(points, 5, 15)).start();
        //new Thread(new FindSecWorker(points, 0, 4)).start();
    }
}
