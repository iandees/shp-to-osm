package osm;

import util.IDGenerator;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author Ian Dees
 * 
 */
public class OSMFile {

    private List<Node> nodes = new ArrayList<Node>();
    private List<Way> ways = new ArrayList<Way>();
    private List<Relation> relations = new ArrayList<Relation>();

    public void addNode(Node n) {
        if (n.getID() > -1) {
            n.setID(IDGenerator.nextNodeID());
        }
        
        nodes.add(n);
    }

    public void addWay(Way w) {
        if (w.getID() > -1) {
            w.setID(IDGenerator.nextWayID());
        }
        
        Iterator<Node> nodeIterator = w.getNodeIterator();
        while (nodeIterator.hasNext()) {
            Node node = (Node) nodeIterator.next();
            addNode(node);
        }
        
        ways.add(w);
    }

    public void addRelation(Relation r) {
        if (r.getID() > -1) {
            r.setID(IDGenerator.nextRelationID());
        }
        
        Iterator<Member> memberIterator = r.getMemberIterator();
        while (memberIterator.hasNext()) {
            Member member = (Member) memberIterator.next();
            
            Primitive primitive = member.getMember();
            PrimitiveTypeEnum type = primitive.getType();
            
            if(PrimitiveTypeEnum.node.equals(type)) {
                addNode((Node) primitive);
            } else if(PrimitiveTypeEnum.way.equals(type)) {
                addWay((Way) primitive);
            } else if(PrimitiveTypeEnum.relation.equals(type)) {
                addRelation((Relation) primitive);
            }
        }
        
        relations.add(r);
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

}
