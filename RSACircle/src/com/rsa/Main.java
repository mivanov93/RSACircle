package com.rsa;

import com.computation.algo.GiftWrapping;
import com.computation.algo.GrahamScanParallel;
import com.computation.algo.QuickHull;
import com.computation.common.Point2DCloud;
import com.computation.common.Utils;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class Main {

    public static void main(String[] args) {

        final Point2DCloud point2DCloud = new Point2DCloud(100 /* points */,
                Utils.WIDTH = 700,
                Utils.HEIGHT = 700, true);
        new QuickHull(point2DCloud, 4, true, 4);
          point2DCloud.show();

        Options opts = configOptions();
        CommandLine cmd = parseOptions(opts, args);

        initApp(cmd);
    }

    private static Options configOptions() {
        return new Options()
                .addOption("n", true, "number of points")
                .addOption("i", true, "path to file, which contains points")
                .addOption("t", true, "max number of threads to be used")
                .addOption("q", false, "quiet mode; no GUI");
    }

    private static CommandLine parseOptions(Options opts, String[] args) {

        CommandLine cmd = null;
        try {
            cmd = new DefaultParser().parse(opts, args);

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
        } catch (ParseException ex) {
            System.err.println("Parsing failed.  Reason: " + ex.getMessage());
            System.exit(1);
        }

        return cmd;
    }

    private static void initApp(CommandLine cmd) {

        if (!cmd.hasOption("q")) {
            initAppWithGUI(cmd);
        } else {
            initAppWithCLI(cmd);
        }

    }

    private static void initAppWithGUI(final CommandLine cmd) {

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                // Construct the UI components on the EDT thread
                final AppGUI appGUI = new AppGUI();
                final Algo algo = new Algo(appGUI);

                // Run the consuming operation on a worker thread
                SwingWorker worker = new SwingWorker<Circle, Void>() {

                    @Override
                    protected Circle doInBackground() throws Exception {
                        return algo.run(cmd);
                    }

                    @Override
                    protected void done() {

                        Circle circle = null;
                        List<Point> points = null;

                        try {
                            circle = get();
                            points = algo.getPoints();
                        } catch (InterruptedException ex) {
                            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (ExecutionException ex) {
                            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                        }

                        appGUI.setData(points, circle);
                    }
                };

                worker.execute();
            }
        });
    }

    private static void initAppWithCLI(final CommandLine cmd) {
        try {
            Algo algo = new Algo();
            algo.run(cmd);
        } catch (InterruptedException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ExecutionException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
