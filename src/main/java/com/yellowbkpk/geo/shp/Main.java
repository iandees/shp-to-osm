package com.yellowbkpk.geo.shp;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang.StringEscapeUtils;

import com.yellowbkpk.geo.glom.GlommingFilter;
import com.yellowbkpk.osm.output.OSMChangeOutputter;
import com.yellowbkpk.osm.output.OSMOldOutputter;
import com.yellowbkpk.osm.output.OSMOutputter;
import com.yellowbkpk.osm.output.OutputFilter;
import com.yellowbkpk.osm.output.SaveEverything;
import com.yellowbkpk.osm.primitive.PrimitiveTypeEnum;

/**
 * @author Ian Dees
 * 
 */
public class Main {
    
    private static Logger log = Logger.getLogger(Main.class.getName());

    private static final String GENERATOR_STRING = "shp-to-osm 0.7";

    /**
     * @param args
     */
    public static void main(String[] args) {
        
        CommandLineParser parser = new GnuParser();
        Options options = new Options();
        options.addOption(OptionBuilder.withLongOpt("shapefile")
                .withDescription("Path to the input shapefile.")
                .withArgName("SHPFILE")
                .hasArg()
                .isRequired()
                .create());
        options.addOption(OptionBuilder.withLongOpt("rulesfile")
                .withDescription("Path to the input rules file.")
                .withArgName("RULESFILE")
                .isRequired()
                .hasArg()
                .create());
        options.addOption(OptionBuilder.withLongOpt("osmfile")
                .withDescription("Prefix of the output file name.")
                .withArgName("OSMFILE")
                .hasArg()
                .isRequired()
                .create());
        options.addOption(OptionBuilder.withLongOpt("outdir")
                .withDescription("Directory to output to. Default is working dir.")
                .withArgName("OUTDIR")
                .hasArg()
                .create());
        options.addOption(OptionBuilder.withLongOpt("maxnodes")
                .withDescription("Maximum elements per OSM file.")
                .withArgName("nodes")
                .hasArg()
                .create());
        options.addOption(OptionBuilder.withLongOpt("outputFormat")
                .withDescription("The output format ('osm' or 'osmc' (default)).")
                .withArgName("format")
                .hasArg()
                .create());
        options.addOption(OptionBuilder.withLongOpt("glomKey")
                .withDescription("The key to 'glom' on. Read the README for more info.")
                .withArgName("key")
                .hasArg()
                .create());
        options.addOption(OptionBuilder.withLongOpt("copyTags")
                .withDescription("Copy all shapefile attributes to OSM tags verbatim, with an optional prefix.")
                .withArgName("prefix")
                .hasOptionalArg()
                .create());
        
        boolean keepOnlyTaggedWays = false;
        try {
            CommandLine line = parser.parse(options, args, false);
            
            if(line.hasOption("t")) {
                keepOnlyTaggedWays = true;
            }
            
            if(!line.hasOption("shapefile") || !line.hasOption("rulesfile") || !line.hasOption("osmfile")) {
                System.out.println("Missing one of the required file paths.");
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp("java -cp shp-to-osm.jar", options, true);
                System.exit(-1);
            }
            
            File shpFile = new File(line.getOptionValue("shapefile"));
            if(!shpFile.canRead()) {
                System.out.println("Could not read the input shapefile.");
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp("java -cp shp-to-osm.jar", options, true);
                System.exit(-1);
            }
            
            final String filePrefix = line.getOptionValue("osmfile");
            String rootDirStr;
            if (line.hasOption("outdir")) {
                rootDirStr = line.getOptionValue("outdir");
            } else {
                rootDirStr = ".";
            }
            File rootDirFile = new File(rootDirStr);
            if (rootDirFile.exists() && rootDirFile.isDirectory()) {
            } else {
                System.err.println("Specified outdir is not a directory: \"" + rootDirStr + "\".");
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp("java -cp shp-to-osm.jar", options, true);
                System.exit(-1);
            }
            
            RuleSet rules = new RuleSet();
            
            boolean useAllTags = line.hasOption("copyTags");
            if (useAllTags) {
                String allTagsPrefix = line.getOptionValue("copyTags", "");
                rules.setUseAllTags(allTagsPrefix);
            }

            if (line.hasOption("rulesfile")) {
                File rulesFile = new File(line.getOptionValue("rulesfile"));
                if (!rulesFile.canRead()) {
                    System.out.println("Could not read the input rulesfile.");
                    HelpFormatter formatter = new HelpFormatter();
                    formatter.printHelp("java -cp shp-to-osm.jar", options, true);
                    System.exit(-1);
                }
                rules.appendRules(readFileToRulesSet(rulesFile));
            }
            
            boolean shouldGlom = false;
            String glomKey = null;
            if(line.hasOption("glomKey")) {
                glomKey = line.getOptionValue("glomKey");
                shouldGlom = true;
                System.out.println("Will attempt to glom on key \'" + glomKey + "\'.");
            }

            OSMOutputter outputter = new OSMChangeOutputter(rootDirFile, filePrefix, GENERATOR_STRING);
            if(line.hasOption("outputFormat")) {
                String type = line.getOptionValue("outputFormat");
                if("osm".equals(type)) {
                    outputter = new OSMOldOutputter(rootDirFile, filePrefix, GENERATOR_STRING);
                }
                
                if(shouldGlom) {
                    OutputFilter glomFilter = new GlommingFilter(glomKey);
                    outputter = new SaveEverything(outputter).withFilter(glomFilter);
                }
            } else {
                System.err.println("No output format specified. Defaulting to osmChange format.");
            }
            
            int maxNodesPerFile = 50000;
            if(line.hasOption("maxnodes")) {
                String maxNodesString = line.getOptionValue("maxnodes");
                try {
                    maxNodesPerFile = Integer.parseInt(maxNodesString);
                } catch(NumberFormatException e) {
                    System.err.println("Error parsing max nodes value of \"" + maxNodesString
                            + "\". Defaulting to 50000.");
                }
            }
            outputter.setMaxElementsPerFile(maxNodesPerFile);
            
            ShpToOsmConverter conv = new ShpToOsmConverter(shpFile, rules, keepOnlyTaggedWays, outputter);
            conv.convert();
        } catch (IOException e) {
            log.log(Level.WARNING, "Error reading rules file.", e);
        } catch (ParseException e) {
            System.err.println("Could not parse command line: " + e.getMessage());
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("java -cp shp-to-osm.jar", options, true);
        } catch (ShpToOsmException e) {
            log.log(Level.SEVERE, "Error creating OSM data from shapefile.", e);
        }
        
    }

    /**
     * @param file
     * @return
     * @throws IOException 
     */
    private static RuleSet readFileToRulesSet(File file) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(file));
        
        RuleSet rules = new RuleSet();
        
        String line;
        int lineCount = 0;
        while ((line = br.readLine()) != null) {
            lineCount++;
            
            String trimmedLine = line.trim();
            
            // Skip comments
            if(trimmedLine.startsWith("#")) {
                continue;
            }
            
            // Skip empty lines
            if("".equals(trimmedLine)) {
                continue;
            }
            
            String[] splits = line.split(",", 5);
            if (splits.length == 5) {
                String type = splits[0];
                String srcKey = splits[1];
                String srcValue = splits[2];
                String targetKey = StringEscapeUtils.escapeXml(splits[3]);
                String targetValue = StringEscapeUtils.escapeXml(splits[4]);

                Rule r;

                // If they don't specify a srcValue...
                if ("".equals(srcValue)) {
                    srcValue = null;
                }

                if ("-".equals(targetValue)) {
                    r = new Rule(type, srcKey, srcValue, targetKey);
                } else {
                    r = new Rule(type, srcKey, srcValue, targetKey, targetValue);
                }

                log.log(Level.CONFIG, "Adding rule " + r);
                if ("inner".equals(type)) {
                    rules.addInnerPolygonRule(r);
                } else if ("outer".equals(type)) {
                    rules.addOuterPolygonRule(r);
                } else if ("line".equals(type)) {
                    rules.addLineRule(r);
                } else if ("point".equals(type)) {
                    rules.addPointRule(r);
                } else {
                    log.log(Level.WARNING, "Line " + lineCount + ": Unknown type " + type);
                }
            } else if (splits.length == 4) {
                try {
                    PrimitiveTypeEnum type = PrimitiveTypeEnum.valueOf(splits[0]);
                    String action = splits[1];
                    String key = splits[2];
                    String value = splits[3];
        
                    if ("exclude".equals(action)) {
                        ExcludeRule excludeFilter = new ExcludeRule(type, key, value);
                        rules.addFilter(excludeFilter);
                        log.log(Level.CONFIG, "Adding exclude filter " + excludeFilter);
                    }
                } catch(IllegalArgumentException e) {
                    log.log(Level.WARNING, "Skipped line " + lineCount + ": \""
                        + line + "\". Unknown primtive type specified. Unless you're trying to do" +
                        		" an exclude rule, you probably didn't put enough commas in.");
                }
            } else {
                log.log(Level.WARNING, "Skipped line " + lineCount + ": \""
                        + line + "\". Had " + splits.length
                        + " pieces and expected 5 or 3.");
                continue;
            }
        }
        
        return rules;
    }
}
