package com.yellowbkpk.osm.output;

import com.yellowbkpk.osm.OSMFile;
import com.yellowbkpk.osm.primitive.node.Node;
import com.yellowbkpk.osm.primitive.way.Way;
import com.yellowbkpk.osm.relation.Relation;


public interface OSMOutputter {

    /**
     * @param maxPerFile The maximum number of changes per file to allow.
     */
    void setMaxElementsPerFile(int maxPerFile);

    /**
     * Called when the output OSM file should be created.
     */
    void start();

    /**
     * @param way The way to add.
     */
    void addWay(Way way);

    /**
     * @param relation The relation to add.
     */
    void addRelation(Relation relation);

    /**
     * @param node The node to add.
     */
    void addNode(Node node);

    /**
     * Called when the output OSM file should be completed.
     */
    void finish();

    /**
     * @param out
     */
    void write(OSMFile out);

}
