package com.cmput402w2016.t1;

import com.cmput402w2016.t1.importer.Importer;
import com.cmput402w2016.t1.simulator.Simulator;
import org.apache.commons.cli.*;

import javax.xml.stream.XMLStreamException;

public class Main {
    private static Options options = null;

    public static void main(String[] args) {
        options = new Options();
        options.addOption("h", "help", false, "display this help message");
        options.addOption("v", "version", false, "display the version number");
        options.addOption("i", "importer", true, "run the importer");
        options.addOption("w", "webapi", true, "run the web api server");
        options.addOption("s", "simulator", true, "run the simulator");
        options.addOption("u", "usage", false, "display heap usage");

        try {
            CommandLineParser parser = new BasicParser();
            CommandLine line = parser.parse(options, args);

            if (line.hasOption("h")) {
                print_help();
                return;
            }

            if (line.hasOption("u")) {
                print_heap_usage();
                return;
            }

            // Check if any one of importer, webapi, or simulator params are set
            boolean has_proper_option = line.hasOption("i") || line.hasOption("w") || line.hasOption("s");
            if (!has_proper_option) {
                print_help();
                return;
            }

            // Check that only one of the importer, webapi, or simulator params are set
            boolean[] proper_options = {line.hasOption("i"), line.hasOption("w"), line.hasOption("s")};
            int proper_option_count = 0;
            for (boolean proper_option : proper_options) {
                if (proper_option) {
                    proper_option_count += 1;
                }
            }
            if (proper_option_count != 1) {
                print_help();
                return;
            }

            if (line.hasOption("i")) {
                // Run the importer
                try {
                    Importer.import_from_file(line.getOptionValue("i"));
                } catch (XMLStreamException e) {
                    e.printStackTrace();
                }
            } else if (line.hasOption("w")) {
                // Run the webapi
                System.out.println("Run the webapi placeholder");
            } else if (line.hasOption("s")) {
                // Run the simulator
                Simulator.run(line.getOptionValues('i'));
                System.out.println("Run the simulator placeholder");
            }

        } catch (ParseException exp) {
            System.err.println("Parsing failed.  Reason: " + exp.getMessage());
        }
    }

    private static void print_help() {
        String header = "MapCore application. CMPUT 402 Winter 2016 Project.\n" +
                "Must run as either importer, simulator, or webapi.\n\n";
        String footer = "\nPlease report issues at\nhttps://github.com/cmput402w2016/CMPUT402W16T1/issues";
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("MapCore", header, options, footer, true);
    }

    public static void print_heap_usage() {
        int mb = 1024 * 1024;
        // get Runtime instance
        Runtime instance = Runtime.getRuntime();
        System.out.println("***** Heap utilization statistics [MB] *****");
        // available memory
        System.out.println("Total Memory: " + instance.totalMemory() / mb);
        // free memory
        System.out.println("Free Memory: " + instance.freeMemory() / mb);
        // used memory
        System.out.println("Used Memory: "
                + (instance.totalMemory() - instance.freeMemory()) / mb);
        // Maximum available memory
        System.out.println("Max Memory: " + instance.maxMemory() / mb);
    }
}
