import osm.OSMFile;
import osm.primitive.Primitive;
import osm.primitive.Tag;
import osm.primitive.node.Node;
import osm.primitive.relation.Member;
import osm.primitive.relation.Relation;
import osm.primitive.way.Way;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;


public class OSMOldOutputter implements OSMOutputter {

    public void write(OSMFile osmOut, File fileOut) {
        saveOsmOut(osmOut, fileOut);
    }

    private void saveOsmOut(OSMFile osmOut, File actualOutput) {
        System.err.println("Writing out to file " + actualOutput.getAbsolutePath() + ".");
    
        // Now write out the file
        try {
            FileWriter bos = new FileWriter(actualOutput);
    
            bos.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
            bos.write("<osm version=\"0.5\" generator=\"shp-to-osm 0.5\">\n");
    
            Iterator<Node> nodeIter = osmOut.getNodeIterator();
            outputNodes(bos, nodeIter);
    
            Iterator<Way> wayIter = osmOut.getWayIterator();
            outputWays(bos, wayIter);
    
            Iterator<Relation> relationIter = osmOut.getRelationIterator();
            outputRelations(bos, relationIter);
    
            bos.write("</osm>\n");
    
            bos.flush();
            bos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        System.err.println("Done.");
    }

    private static void outputRelations(Writer out, Iterator<Relation> relationIter) throws IOException {
        while (relationIter.hasNext()) {
            Relation way = (Relation) relationIter.next();
    
            out.write("  <relation id=\"");
            out.write(Integer.toString(way.getID()));
            out.write("\">\n");
    
            Iterator<Member> memberIter = way.getMemberIterator();
            outputMembers(out, memberIter);
    
            Iterator<Tag> tagIter = way.getTagIterator();
            outputTags(out, tagIter);
    
            out.write("  </relation>\n");
        }
    }

    private static void outputMembers(Writer out, Iterator<Member> memberIter) throws IOException {
        while (memberIter.hasNext()) {
            Member member = (Member) memberIter.next();
    
            out.write("    <member type=\"");
            out.write(member.getMember().getType().toString());
            out.write("\" ref=\"");
            out.write(Integer.toString(member.getMember().getID()));
            out.write("\" role=\"");
            out.write(member.getRole());
            out.write("\"/>\n");
        }
    }

    private static void outputWays(Writer out, Iterator<Way> wayIter) throws IOException {
        while (wayIter.hasNext()) {
            Way way = (Way) wayIter.next();
    
            out.write("  <way id=\"");
            out.write(Integer.toString(way.getID()));
            out.write("\">\n");
    
            Iterator<Node> nodeIter = way.getNodeIterator();
            outputWayRefs(out, nodeIter);
    
            Iterator<Tag> tagIter = way.getTagIterator();
            outputTags(out, tagIter);
    
            out.write("  </way>\n");
        }
    }

    private static void outputWayRefs(Writer out, Iterator<Node> nodeIter) throws IOException {
        while (nodeIter.hasNext()) {
            Primitive node = (Primitive) nodeIter.next();
    
            out.write("    <nd ref=\"");
            out.write(Integer.toString(node.getID()));
            out.write("\"/>\n");
        }
    }

    private static void outputNodes(Writer out, Iterator<Node> nodeIter) throws IOException {
        while (nodeIter.hasNext()) {
            Node node = (Node) nodeIter.next();
    
            out.write("  <node id=\"");
            out.write(Integer.toString(node.getID()));
            out.write("\" lat=\"");
            out.write(Double.toString(node.getLat()));
            out.write("\" lon=\"");
            out.write(Double.toString(node.getLon()));
            out.write("\"");
    
            if (node.hasTags()) {
                out.write(">\n");
    
                Iterator<Tag> tagIter = node.getTagIterator();
                outputTags(out, tagIter);
    
                out.write("  </node>\n");
            } else {
                out.write("/>\n");
            }
    
        }
    }

    private static void outputTags(Writer out, Iterator<Tag> tagIter) throws IOException {
        while (tagIter.hasNext()) {
            Tag tag = (Tag) tagIter.next();
    
            out.write("    <tag k=\"");
            out.write(tag.getKey());
            out.write("\" v=\"");
            out.write(tag.getValue());
            out.write("\"/>\n");
        }
    }

}
