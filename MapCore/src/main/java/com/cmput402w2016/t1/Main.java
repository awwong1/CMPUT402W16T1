package com.cmput402w2016.t1;

import com.cmput402w2016.t1.importer.Importer;
import com.cmput402w2016.t1.simulator.Simulator;
import com.cmput402w2016.t1.webapi.WebApi;
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
        options.addOption("c", "converter", true, "run the converter");
        options.addOption("u", "usage", false, "display heap usage");

        // Return value
        int r = 0;

        try {
            CommandLineParser parser = new BasicParser();
            CommandLine line = parser.parse(options, args);

            //// Options that don't need any requirements should be listed first
            if (line.hasOption("h")) {
                // Help
                print_help();
                return;
            }

            if (line.hasOption("u")) {
                // Heap Usage
                print_heap_usage();
                return;
            }

            //// Options that require arguments
            // Check that only one of the importer, webapi, or simulator params are set
            boolean[] proper_options = {line.hasOption("i"), line.hasOption("w"), line.hasOption("s"), line.hasOption("c")};
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
                String raw_port = line.getOptionValue("w");
                WebApi.start_web_api(raw_port);
            } else if (line.hasOption("s")) {
                // Run the simulator
                r  = Simulator.run(line.getOptionValues('s'));
                if(r != 0) {
                    System.exit(r);
                }
            } else if (line.hasOption("c")) {
                if(!line.hasOption("host")) {
                    System.err.println("Run converter with the option --host point to the REST API");
                }
                // Run the converter
                Converter.run(line.getOptionValue('c'), line.getOptionValue("host"));
            }
        } catch (ParseException exp) {
            System.err.println("Parsing failed.  Reason: " + exp.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void print_help() {
        String header = "MapCore application. CMPUT 402 Winter 2016 Project.\n" +
                "Must run as either importer, simulator, or webapi.\n\n";
        String footer = "\nPlease report issues at\nhttps://github.com/cmput402w2016/CMPUT402W16T1/issues";
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("MapCore", header, options, footer, true);
    }


    /**
     * We were having issues with heap usage going over on many runs, so we added this for convenience. If you're
     * running in to heap errors as well, adjust the hadoop settings for heap memory allocation.
     */
    public static void print_heap_usage() {
        int mb = 1024 * 1024;
        Runtime instance = Runtime.getRuntime();
        System.out.println("***** Heap utilization statistics [MB] *****");
        System.out.printf("Total Memory: %d\n", instance.totalMemory() / mb);
        System.out.printf("Free Memory: %d\n", instance.freeMemory() / mb);
        System.out.printf("Used Memory: %d\n", (instance.totalMemory() - instance.freeMemory()) / mb);
        System.out.printf("Max Memory: %d\n", instance.maxMemory() / mb);
    }
}
