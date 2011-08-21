package com.yellowbkpk.osm.primitive.node;

/**
 * A node that has not been downloaded yet, so we don't know its tags or
 * location, just its ID.
 * 
 * @author Ian Dees
 * 
 */
public class NodeByRef extends Node {

    public NodeByRef(int id) {
        super(-1, -1);
        setID(id);
    }

}
