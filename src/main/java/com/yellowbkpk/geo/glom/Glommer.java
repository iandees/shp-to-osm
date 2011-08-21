package com.yellowbkpk.geo.glom;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.yellowbkpk.osm.OSMFile;
import com.yellowbkpk.osm.primitive.node.LatLon;
import com.yellowbkpk.osm.primitive.node.Node;
import com.yellowbkpk.osm.primitive.way.Way;


public class Glommer {
    
    private final String key;

    public Glommer(String keyToGlomOn) {
        key = keyToGlomOn;
    }

    public OSMFile glom(OSMFile data) {
        Map<LatLon, List<Node>> locToNodes = new HashMap<LatLon, List<Node>>();
        Map<Node, List<Way>> nodeToWays = new HashMap<Node, List<Way>>();
        OSMFile out = new OSMFile();
        
        // Iterate over the entire file creating reverse mappings
        Iterator<Way> wayIterator = data.getWayIterator();
        while (wayIterator.hasNext()) {
            Way way = wayIterator.next();
            String value = way.getTagValue(key);
            
            if(value == null) {
                // No matching key, so just add it as-is to the file
                out.addWay(way);
                wayIterator.remove();
                continue;
            }

            List<Node> nodeList = way.getNodes();
            for (int i = 0; i < nodeList.size(); i++) {
                Node node = nodeList.get(i);
                LatLon point = node.getPoint();

                // Gather all the ways that share this node
                List<Way> waysWithNode = nodeToWays.get(node);
                if(waysWithNode == null) {
                    waysWithNode = new LinkedList<Way>();
                    nodeToWays.put(node, waysWithNode);
                }
                waysWithNode.add(way);

                // Gather all the nodes that share this location
                List<Node> nodesAtPoint = locToNodes.get(point);
                if(nodesAtPoint == null) {
                    nodesAtPoint = new LinkedList<Node>();
                    locToNodes.put(point, nodesAtPoint);
                }
                nodesAtPoint.add(node);
            }
        }
        
        // Ditch the entries that only have one element lists
        // TODO Maybe we don't need to do this
        Iterator<List<Way>> iterator1 = nodeToWays.values().iterator();
        while (iterator1.hasNext()) {
            List<Way> ways = iterator1.next();
            if(ways.size() < 2) {
                iterator1.remove();
            }
        }
        
        Iterator<List<Node>> iterator2 = locToNodes.values().iterator();
        while (iterator2.hasNext()) {
            List<Node> nodes = iterator2.next();
            if(nodes.size() < 2) {
                iterator2.remove();
            }
        }
        
        // Iterate over the ways again, but this time remove duplicate nodes
        // that are part of ways with matching key values.
        wayIterator = data.getWayIterator();
        while (wayIterator.hasNext()) {
            Way originalWay = wayIterator.next();
            String value = originalWay.getTagValue(key);
            
            Way newWay = new Way();
            newWay.copyTags(originalWay);

            Iterator<Node> originalNodeIterator = originalWay.getNodeIterator();
            while(originalNodeIterator.hasNext()) {
                Node node = originalNodeIterator.next();
                LatLon point = node.getPoint();
                
                // Find other nodes that use this node's location
                List<Node> suspectNodes = locToNodes.get(point);
                if (suspectNodes != null) {
                    Iterator<Node> suspectNodesIter = suspectNodes.iterator();
                    while(suspectNodesIter.hasNext()) {
                        Node suspectNode = suspectNodesIter.next();
                        
                        // Find the ways that use this other node
                        List<Way> suspectWays = nodeToWays.get(suspectNode);
                        if(suspectWays != null) {
                            for (Way suspectWay : suspectWays) {
                                if (!suspectWay.equals(originalWay) && suspectWay.getTagValue(key).equals(value)) {
                                    // The suspect way has the same tag value as the
                                    // way we're looking at, so the current way
                                    // should have the suspect way's node added
                                    // instead of the current way's.
                                }
                            }
                        }
                    }
                }
            }
            
            out.addWay(newWay);
        }

        return out;
    }

}
