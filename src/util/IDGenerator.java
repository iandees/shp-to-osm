package util;

import java.util.Iterator;

/**
 * @author Ian Dees
 *
 */
public class IDGenerator {

    private static final Iterator<Integer> waySqn = new Iterator<Integer>() {
        int c = -1;
        public boolean hasNext() {
            return true;
        }
        public Integer next() {
            return c--;
        }
        public void remove() {
        }
    };
    private static final Iterator<Integer> nodeSqn = new Iterator<Integer>() {
        int c = -1;
        public boolean hasNext() {
            return true;
        }
        public Integer next() {
            return c--;
        }
        public void remove() {
        }
    };
    private static final Iterator<Integer> relationSqn = new Iterator<Integer>() {
        int c = -1;
        public boolean hasNext() {
            return true;
        }
        public Integer next() {
            return c--;
        }
        public void remove() {
        }
    };
    
    public static int nextNodeID() {
        return nodeSqn.next();
    }
    
    public static int nextWayID() {
        return waySqn.next();
    }

    public static Integer nextRelationID() {
        return relationSqn.next();
    }

}
