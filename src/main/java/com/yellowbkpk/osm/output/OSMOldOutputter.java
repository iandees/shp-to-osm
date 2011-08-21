package com.yellowbkpk.osm.output;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;

import com.yellowbkpk.osm.OSMFile;
import com.yellowbkpk.osm.primitive.Primitive;
import com.yellowbkpk.osm.primitive.Tag;
import com.yellowbkpk.osm.primitive.node.Node;
import com.yellowbkpk.osm.primitive.way.Way;
import com.yellowbkpk.osm.relation.Member;
import com.yellowbkpk.osm.relation.Relation;


public class OSMOldOutputter extends AbstractOutputter {

    private File rootDir;
    private String filePre;
    private int count = 0;
    private String generator;

    public OSMOldOutputter(File rootDirFile, String filePrefix, String generatorString) {
        rootDir = rootDirFile;
        filePre = filePrefix;
        generator = generatorString;
    }

    public void write(OSMFile osmOut) {
        File actualOutFile = new File(rootDir, filePre + count + ".xml");
        saveOsmOut(osmOut, actualOutFile);
        count++;
    }

    private void saveOsmOut(OSMFile osmOut, File actualOutput) {
        System.err.println("Writing out to file " + actualOutput.getAbsolutePath() + ".");
    
        // Now write out the file
        try {
            FileWriter bos = new FileWriter(actualOutput);
    
            bos.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
            bos.write("<osm version=\"0.5\" generator=\""+generator+"\">\n");
    
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
            Relation relation = relationIter.next();
    
            out.write("  <relation ");
            writePrimitveAttrs(out, relation);
            out.write(">\n");
    
            Iterator<Member> memberIter = relation.getMemberIterator();
            outputMembers(out, memberIter);
    
            Iterator<Tag> tagIter = relation.getTagIterator();
            outputTags(out, tagIter);
    
            out.write("  </relation>\n");
        }
    }

    private static void outputMembers(Writer out, Iterator<Member> memberIter) throws IOException {
        while (memberIter.hasNext()) {
            Member member = memberIter.next();
    
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
            Way way = wayIter.next();
    
            out.write("  <way ");
            writePrimitveAttrs(out, way);
            out.write(">\n");
    
            Iterator<Node> nodeIter = way.getNodeIterator();
            outputWayRefs(out, nodeIter);
    
            Iterator<Tag> tagIter = way.getTagIterator();
            outputTags(out, tagIter);
    
            out.write("  </way>\n");
        }
    }

    private static void outputWayRefs(Writer out, Iterator<Node> nodeIter) throws IOException {
        while (nodeIter.hasNext()) {
            Node node = nodeIter.next();
    
            out.write("    <nd ref=\"");
            out.write(Integer.toString(node.getID()));
            out.write("\"/>\n");
        }
    }

    private static void outputNodes(Writer out, Iterator<Node> nodeIter) throws IOException {
        while (nodeIter.hasNext()) {
            Node node = nodeIter.next();
    
            out.write("  <node ");
            writePrimitveAttrs(out, node);
            writeAttr(out, "lat", Double.toString(node.getLat()));
            writeAttr(out, "lon", Double.toString(node.getLon()));
    
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

    private static void writePrimitveAttrs(Writer out, Primitive prim) throws IOException {
        writeAttr(out, "id", Integer.toString(prim.getID()));

        if (prim.getVersion() != null) {
            writeAttr(out, "version", prim.getVersion().toString());
        }

        if (prim.getUser() != null) {
            writeAttr(out, "user", prim.getUser().getName());
            writeAttr(out, "uid", Integer.toString(prim.getUser().getId()));
        }
        
        if (prim.isVisible()) {
            writeAttr(out, "visible", "true");
        } else {
            writeAttr(out, "visible", "false");
        }
    }
    
    private static void writeAttr(Writer out, String key, String value) throws IOException {
        if (out != null && key != null && value != null) {
            out.write(key);
            out.write("=\"");
            out.write(value);
            out.write("\" ");
        }
    }

    private static void outputTags(Writer out, Iterator<Tag> tagIter) throws IOException {
        while (tagIter.hasNext()) {
            Tag tag = tagIter.next();
    
            out.write("    <tag k=\"");
            out.write(tag.getKey());
            out.write("\" v=\"");
            out.write(tag.getValue());
            out.write("\"/>\n");
        }
    }

}
