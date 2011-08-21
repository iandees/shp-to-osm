package com.yellowbkpk.geo.glom;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import com.yellowbkpk.osm.OSMFile;
import com.yellowbkpk.osm.output.OSMOldOutputter;
import com.yellowbkpk.osm.output.OSMOutputter;

public class Main {

    public static void main(String[] args) {
        CommandLineParser parser = new GnuParser();
        Options options = new Options();
        options.addOption(OptionBuilder.withLongOpt("osmfile")
                .withDescription("Path to the input OSM file(s).")
                .withArgName("OSMFILE")
                .hasArgs()
                .isRequired()
                .create());
        options.addOption(OptionBuilder.withLongOpt("glomkey")
                .withDescription("The key to glom on.")
                .withArgName("KEY")
                .hasArg()
                .isRequired()
                .create());
        
        try {
            CommandLine line = parser.parse(options, args, false);
            
            String keyToGlomOn = null;
            if(line.hasOption("glomkey")) {
                keyToGlomOn = line.getOptionValue("glomkey");
            } else {
                System.out.println("Missing the key to glom with.");
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp("java -cp glomifier.jar", options, true);
                System.exit(-1);
            }
            
            List<File> files = new ArrayList<File>();
            String[] filePaths = line.getOptionValues("osmfile");
            for (String path : filePaths) {
                File file = new File(path);
                if(!file.exists()) {
                    System.err.println("Skipping " + path + " because it does not exist.");
                    continue;
                }
                
                files.add(file);
            }
            
            OSMFile aggregate = OSMFile.fromFiles(files);
            
            Glommer g = new Glommer(keyToGlomOn);
            OSMFile out = g.glom(aggregate);
            
            OSMOutputter outputter = new OSMOldOutputter(new File("."), "glommed", "glomifier 0.1");
            outputter.write(out);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

}
