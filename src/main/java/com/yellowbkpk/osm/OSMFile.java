package com.yellowbkpk.osm;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXException;

import com.yellowbkpk.osm.parser.OSMSaxParser;
import com.yellowbkpk.osm.primitive.Primitive;
import com.yellowbkpk.osm.primitive.PrimitiveTypeEnum;
import com.yellowbkpk.osm.primitive.node.Node;
import com.yellowbkpk.osm.primitive.way.Way;
import com.yellowbkpk.osm.relation.Member;
import com.yellowbkpk.osm.relation.Relation;
import com.yellowbkpk.osm.util.IDGenerator;

/**
 * @author Ian Dees
 * 
 */
public class OSMFile {

    private LinkedHashSet<Node> nodes = new LinkedHashSet<Node>();
    private LinkedHashSet<Way> ways = new LinkedHashSet<Way>();
    private LinkedHashSet<Relation> relations = new LinkedHashSet<Relation>();

    public void addNode(Node n) {
        if (n.getID() == 0) {
            n.setID(IDGenerator.nextNodeID());
        }

        addPrimitive(nodes, n);
    }

    private <M extends Primitive> void addPrimitive(LinkedHashSet<M> list, M n) {
        list.add(n);
    }

    public void addWay(Way w) {
        if (w.getID() == 0) {
            w.setID(IDGenerator.nextWayID());
        }

        for (Node n : w.getNodes()) {
            addNode(n);
        }

        addPrimitive(ways, w);
    }

    public void addRelation(Relation r) {
        if (r.getID() == 0) {
            r.setID(IDGenerator.nextRelationID());
        }

        for (Member member : r.getMembers()) {
            Primitive primitive = member.getMember();
            PrimitiveTypeEnum type = primitive.getType();

            if (PrimitiveTypeEnum.node.equals(type)) {
                addNode((Node) primitive);
            } else if (PrimitiveTypeEnum.way.equals(type)) {
                addWay((Way) primitive);
            } else if (PrimitiveTypeEnum.relation.equals(type)) {
                addRelation((Relation) primitive);
            }
        }

        addPrimitive(relations, r);
    }

    public int getChangeCount() {
        return getNodeCount() + getWayCount() + getRelationCount();
    }

    public Iterator<Node> getNodeIterator() {
        return nodes.iterator();
    }

    public Iterator<Way> getWayIterator() {
        return ways.iterator();
    }

    public Iterator<Relation> getRelationIterator() {
        return relations.iterator();
    }

    public int getNodeCount() {
        return nodes.size();
    }

    /**
     * @param file
     * @return
     */
    public static OSMFile fromFile(File file) {
        OSMSaxParser osmHandler = new OSMSaxParser();

        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();
            saxParser.parse(file, osmHandler);
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return osmHandler.getOSMFile();
    }

    /**
     * @param id
     * @return
     */
    public Node findNodeById(int id) {
        // TODO Need a better data structure for id => primitive
        for (Node node : nodes) {
            if (node.getID() == id) {
                return node;
            }
        }
        return null;
    }

    /**
     * @param refId
     * @return
     */
    public Primitive findRelationById(int refId) {
        // TODO Need a better data structure for id => primitive
        for (Relation relation : relations) {
            if (relation.getID() == refId) {
                return relation;
            }
        }
        return null;
    }

    /**
     * @param refId
     * @return
     */
    public Primitive findWayById(int refId) {
        // TODO Need a better data structure for id => primitive
        for (Way way : ways) {
            if (way.getID() == refId) {
                return way;
            }
        }
        return null;
    }

    /**
     * @return
     */
    public int getWayCount() {
        return ways.size();
    }

    /**
     * @return
     */
    public int getRelationCount() {
        return relations.size();
    }

    /**
     * Read in a list of OSM files and return an aggregate of all of them
     * together.
     * 
     * @param files The list of files to read.
     * @return The combined data from the list of OSM files.
     */
    public static OSMFile fromFiles(List<File> files) {
        if (files == null) {
            throw new IllegalArgumentException("Files cannot be null.");
        }

        OSMFile aggregate = new OSMFile();
        for (File file : files) {
            if (file.exists()) {
                OSMFile f = OSMFile.fromFile(file);
                aggregate.appendTo(f);
            }
        }
        return aggregate;
    }

    public void appendTo(OSMFile f) {
        if (f == null) {
            throw new IllegalArgumentException("File cannot be null.");
        }

        this.nodes.addAll(f.nodes);
        this.ways.addAll(f.ways);
        this.relations.addAll(f.relations);
    }

}
