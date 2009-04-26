package osm;

import util.IDGenerator;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author Ian Dees
 *
 */
public class Way extends Primitive {

    private List<Node> nodeList = new ArrayList<Node>();
    
    public void addNode(Node node) {
        node.setID(IDGenerator.nextNodeID());
        
        nodeList.add(node);
    }

    public Iterator<Node> getNodeIterator() {
        return nodeList.iterator();
    }

    public PrimitiveTypeEnum getType() {
        return PrimitiveTypeEnum.way;
    }

}
