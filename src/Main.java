import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * @author Ian Dees
 * 
 */
public class Main {

    /**
     * @param args
     */
    public static void main(String[] args) {

        File shpFile;
        File osmFile;
        RuleSet rules;
        try {
            shpFile = new File(args[0]);
            rules = readFileToRulesSet(new File(args[1]));
            osmFile = new File(args[2]);
            
            boolean keepOnlyTaggedWays = false;
            if(args.length == 4 && "-t".equals(args[3])) {
                keepOnlyTaggedWays  = true;
            }

            ShpToOsmConverter conv = new ShpToOsmConverter(shpFile, rules, osmFile, keepOnlyTaggedWays);
            conv.go();
        } catch (IOException e) {
            e.printStackTrace();
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
            
            // Skip comments
            if(line.startsWith("#")) {
                continue;
            }
            
            String[] splits = line.split(",", 5);
            if (splits.length == 5) {
                String type = splits[0];
                String srcKey = splits[1];
                String srcValue = splits[2];
                String targetKey = splits[3];
                String targetValue = splits[4];

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

                System.err.println("Adding " + r);
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
                throw new IllegalArgumentException("Could not parse line " + lineCount + ". Had " + splits.length
                        + " pieces and expected 5.");
            }
        }
        
        return rules;
    }
}