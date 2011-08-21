package com.yellowbkpk.osm.util;

import java.util.Iterator;

/**
 * @author Ian Dees
 *
 */
public class IDGenerator {
    
    static class PeekableSqn implements Iterator<Integer> {
        int c = -1;
        public boolean hasNext() {
            return true;
        }
        public Integer next() {
            return c--;
        }
        public void remove() {
        }
        public Integer peek() {
            return c;
        }
    }

    private static final PeekableSqn nodeSqn = new PeekableSqn();
    private static final PeekableSqn waySqn = new PeekableSqn();
    private static final PeekableSqn relationSqn = new PeekableSqn();
    
    public static synchronized int nextNodeID() {
        return nodeSqn.next();
    }
    
    public static synchronized int nextWayID() {
        return waySqn.next();
    }

    public static synchronized Integer nextRelationID() {
        return relationSqn.next();
    }

    public static int currentNodeID() {
        return nodeSqn.peek();
    }

    public static int currentWayID() {
        return waySqn.peek();
    }

    public static int currentRelationID() {
        return relationSqn.peek();
    }

}
