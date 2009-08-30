import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**
 * @author Ian Dees
 * 
 */
public class Main {

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
                .create());
        options.addOption(OptionBuilder.withLongOpt("rulesfile")
                .withDescription("Path to the input rules file.")
                .withArgName("RULESFILE")
                .hasArg()
                .create());
        options.addOption(OptionBuilder.withLongOpt("osmfile")
                .withDescription("Prefix to the output OSM file path.")
                .withArgName("OSMFILE")
                .hasArg()
                .create());
        options.addOption("t", false, "Keep only tagged elements.");
        options.addOption(OptionBuilder.withLongOpt("maxnodes")
                .withDescription("Maximum elements per OSM file.")
                .withArgName("nodes")
                .hasArg()
                .create());
        
        boolean keepOnlyTaggedWays = false;
        try {
            CommandLine line = parser.parse(options, args, false);
            
            if(line.hasOption("t")) {
                keepOnlyTaggedWays = true;
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
            
            File rulesFile = new File(line.getOptionValue("rulesfile"));
            if(!rulesFile.canRead()) {
                System.out.println("Could not read the input rulesfile.");
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp("java -cp shp-to-osm.jar", options, true);
                System.exit(-1);
            }
            RuleSet rules = readFileToRulesSet(rulesFile);
            
            File osmFile = new File(line.getOptionValue("osmfile"));
            
            ShpToOsmConverter conv = new ShpToOsmConverter(shpFile, rules, osmFile, keepOnlyTaggedWays, maxNodesPerFile);
            conv.go();
        } catch (IOException e) {
            System.err.println("Error reading rules file.");
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
            System.err.println("Could not parse command line.");
        }
        
        // ShpToOsmGUI g = new ShpToOsmGUI();
        // g.start();

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

                System.err.println("Adding rule " + r);
                if ("inner".equals(type)) {
                    rules.addInnerPolygonRule(r);
                } else if ("outer".equals(type)) {
                    rules.addOuterPolygonRule(r);
                } else if ("line".equals(type)) {
                    rules.addLineRule(r);
                } else if ("point".equals(type)) {
                    rules.addPointRule(r);
                } else {
                    System.err.println("Line " + lineCount + ": Unknown type " + type);
                }
            } else {
                System.err.println("Skipped line " + lineCount + ": \"" + line + "\". Had " + splits.length
                        + " pieces and expected 5.");
                continue;
            }
        }
        
        return rules;
    }
}