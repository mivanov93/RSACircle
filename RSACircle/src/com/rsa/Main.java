package com.rsa;

import java.util.HashMap;
import java.util.Map;
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
        run();
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

    private static void run() {
        Point[] points = new Point[10];
        for (int i = 0; i < points.length; i++) {
            points[i] = new Point(1.0, 1.0+(double)i);
                   // new Point((double)randomWithRange(1,100), (double)randomWithRange(1,100));
                    
        }
        new Thread(new FindSecWorker(points, 0, points.length)).start();

    }
}
