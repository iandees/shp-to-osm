import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.FeatureSource;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.opengis.feature.Property;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;

import osm.Member;
import osm.Node;
import osm.OSMFile;
import osm.Primitive;
import osm.Relation;
import osm.Tag;
import osm.Way;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.io.Writer;
import java.net.MalformedURLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


public class ShpToOsmConverter {

    private File inputFile;
    private File outputFile;
    private RuleSet ruleset;
    private boolean onlyIncludeTaggedPrimitives;

    /**
     * @param shpFile
     * @param rules
     * @param osmFile
     */
    public ShpToOsmConverter(File shpFile, RuleSet rules, File osmFile, boolean onlyIncludeTaggedPrim) {
        inputFile = shpFile;
        outputFile = osmFile;
        
        ruleset = rules;
        onlyIncludeTaggedPrimitives = onlyIncludeTaggedPrim;
    }

    /**
     * 
     */
    public void go() {

        CoordinateReferenceSystem targetCRS = null;
        try {
            targetCRS = CRS
            .parseWKT("GEOGCS[\"WGS 84\",DATUM[\"WGS_1984\",SPHEROID[\"WGS 84\",6378137,298.257223563,AUTHORITY[\"EPSG\",\"7030\"]],AUTHORITY[\"EPSG\",\"6326\"]],PRIMEM[\"Greenwich\",0,AUTHORITY[\"EPSG\",\"8901\"]],UNIT[\"degree\",0.01745329251994328,AUTHORITY[\"EPSG\",\"9122\"]],AUTHORITY[\"EPSG\",\"4326\"]]");
        } catch (NoSuchAuthorityCodeException e1) {
            e1.printStackTrace();
        } catch (FactoryException e1) {
            e1.printStackTrace();
        }

        OSMFile osmOut = new OSMFile();

        try {
            // Connection parameters
            Map<String, Serializable> connectParameters = new HashMap<String, Serializable>();

            connectParameters.put("url", inputFile.toURI().toURL());
            ShapefileDataStore dataStore = (ShapefileDataStore) DataStoreFinder.getDataStore(connectParameters);

            CoordinateReferenceSystem sourceCRS = dataStore.getSchema().getCoordinateReferenceSystem();
            System.err.println("Converting from " + sourceCRS + " to " + targetCRS);

            // we are now connected
            String[] typeNames = dataStore.getTypeNames();
            for (String typeName : typeNames) {
                System.err.println(typeName);

                FeatureSource<SimpleFeatureType, SimpleFeature> featureSource;
                FeatureCollection<SimpleFeatureType, SimpleFeature> collection;
                FeatureIterator<SimpleFeature> iterator;

                featureSource = dataStore.getFeatureSource(typeName);
                collection = featureSource.getFeatures();
                iterator = collection.features();

                try {
                    while (iterator.hasNext()) {
                        SimpleFeature feature = iterator.next();

                        Geometry rawGeom = (Geometry) feature.getDefaultGeometry();
                        String geometryType = rawGeom.getGeometryType();

                        // Transform to spherical mercator
                        Geometry geometry = null;
                        try {
                            MathTransform transform = CRS.findMathTransform(sourceCRS, targetCRS, true);
                            geometry = JTS.transform(rawGeom, transform);
                        } catch (FactoryException e) {
                            e.printStackTrace();
                        } catch (TransformException e) {
                            e.printStackTrace();
                        }
                        // geometry = rawGeom;

                        System.err.println("Geometry type: " + geometryType);

                        if ("MultiLineString".equals(geometryType)) {

                            for (int i = 0; i < geometry.getNumGeometries(); i++) {
                                LineString geometryN = (LineString) geometry.getGeometryN(i);

                                Way w = linestringToWay(geometryN);
                                applyRulesList(feature, geometryType, w, ruleset.getLineRules());
                                if (shouldInclude(w)) {
                                    osmOut.addWay(w);
                                }
                            }

                        } else if ("MultiPolygon".equals(geometryType)) {

                            for (int i = 0; i < geometry.getNumGeometries(); i++) {
                                Polygon geometryN = (Polygon) geometry.getGeometryN(i);

                                // Get the outer ring of the polygon
                                LineString outerLine = geometryN.getExteriorRing();

                                Way w = polygonToWay(outerLine);

                                if (geometryN.getNumInteriorRing() > 0) {
                                    // Tags go on the outer way for
                                    // multipolygons

                                    applyRulesList(feature, geometryType, w, ruleset.getOuterPolygonRules());

                                    if (shouldInclude(w)) {
                                        Relation r = new Relation();

                                        r.addTag(new Tag("type", "multipolygon"));

                                        r.addMember(new Member(w, "outer"));

                                        // Then the inner ones, if any
                                        for (int j = 0; j < geometryN.getNumInteriorRing(); j++) {
                                            LineString innerLine = geometryN.getInteriorRingN(j);

                                            w = polygonToWay(innerLine);

                                            applyRulesList(feature, geometryType, w, ruleset.getInnerPolygonRules());

                                            r.addMember(new Member(w, "inner"));

                                        }

                                        osmOut.addRelation(r);
                                    }
                                } else {
                                    // If there aren't any inner lines, then
                                    // just use the outer one as a way.
                                    applyRulesList(feature, geometryType, w, ruleset.getOuterPolygonRules());

                                    if (shouldInclude(w)) {
                                        osmOut.addWay(w);
                                    }
                                }
                            }
                        } else if ("Point".equals(geometryType)) {
                            for (int i = 0; i < geometry.getNumGeometries(); i++) {
                                Point geometryN = (Point) geometry.getGeometryN(i);

                                Node n = pointToNode(geometryN);

                                applyRulesList(feature, geometryType, n, ruleset.getPointRules());

                                if (shouldInclude(n)) {
                                    osmOut.addNode(n);
                                }
                            }
                        }
                    }
                } catch (MismatchedDimensionException e) {
                    e.printStackTrace();
                } finally {
                    if (iterator != null) {
                        // YOU MUST CLOSE THE ITERATOR!
                        iterator.close();
                    }
                }

            }

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.err.println("Writing out to file.");

        // Now write out the file
        try {
            FileWriter bos = new FileWriter(outputFile);

            bos.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
            bos.write("<osm version=\"0.5\" generator=\"SHP to OSM 0.3\">\n");

            Iterator<Node> nodeIter = osmOut.getNodeIterator();
            outputNodes(bos, nodeIter);

            Iterator<Way> wayIter = osmOut.getWayIterator();
            outputWays(bos, wayIter);

            Iterator<Relation> relationIter = osmOut.getRelationIterator();
            outputRelations(bos, relationIter);

            bos.write("</osm>\n");

            bos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        System.err.println("Done.");
    }

    private boolean shouldInclude(Primitive w) {
        if(onlyIncludeTaggedPrimitives) {
            return w.hasTags();
        } else {
            return true;
        }
    }

    private Node pointToNode(Point geometryN) {
        Coordinate coord = geometryN.getCoordinate();
        return new Node(coord.y, coord.x);
    }

    /**
     * @param feature
     * @param geometryType
     * @param w
     * @param rulelist
     */
    private void applyRulesList(SimpleFeature feature, String geometryType, Primitive w, List<Rule> rulelist) {
        Collection<Property> properties = feature.getProperties();
        for (Property property : properties) {
            String srcKey = property.getType().getName().toString();
            if (!geometryType.equals(srcKey)) {

                Object value = property.getValue();
                if (value != null) {
                    final String dirtyOriginalValue = value.toString().trim();

                    if (!StringUtils.isEmpty(dirtyOriginalValue)) {
                        String escapedOriginalValue = StringEscapeUtils.escapeXml(dirtyOriginalValue);

                        for (Rule rule : rulelist) {
                            Tag t = rule.createTag(srcKey, escapedOriginalValue);
                            if (t != null) {
                                w.addTag(t);
                            }
                        }
                    }
                }
            }
        }
    }

    private static void applyOriginalTagsTo(SimpleFeature feature, String geometryType, Primitive w) {
        Collection<Property> properties = feature.getProperties();
        for (Property property : properties) {
            String name = property.getType().getName().toString();
            if (!geometryType.equals(name)) {
                String value = property.getValue().toString();
                value = StringEscapeUtils.escapeXml(value);

                w.addTag(new Tag(name, value));
            }
        }
    }
    
    private static Way linestringToWay(LineString geometryN) {
        Coordinate[] coordinates = geometryN.getCoordinates();
        Way w = new Way();

        for (Coordinate coord : coordinates) {
            Node node = new Node(coord.y, coord.x);
            w.addNode(node);
        }

        return w;
    }

    private static Way polygonToWay(LineString geometryN) {
        Coordinate[] coordinates = geometryN.getCoordinates();
        if(coordinates.length < 2) {
            throw new IllegalArgumentException("Way with less than 2 nodes.");
        }
        Way w = new Way();
        
        // First node for the polygon
        Coordinate firstCoord = coordinates[0];
        Node firstNode = new Node(firstCoord.y, firstCoord.x);
        w.addNode(firstNode);

        // "middle" nodes
        for (int i = 1; i < coordinates.length-1; i++) {
            Coordinate coord = coordinates[i];

            Node node = new Node(coord.y, coord.x);
            w.addNode(node);
        }
        
        // Last node should be the same ID as the first one
        Coordinate lastCoord = coordinates[coordinates.length-1];
        if(lastCoord.x == firstCoord.x && lastCoord.y == firstCoord.y) {
            w.addNode(firstNode);
        }
        
        return w;
    }

    private static void outputRelations(Writer out, Iterator<Relation> relationIter) throws IOException {
        while (relationIter.hasNext()) {
            Relation way = (Relation) relationIter.next();

            out.write("<relation id=\"");
            out.write(Integer.toString(way.getID()));
            out.write("\">\n");

            Iterator<Member> memberIter = way.getMemberIterator();
            outputMembers(out, memberIter);

            Iterator<Tag> tagIter = way.getTagIterator();
            outputTags(out, tagIter);

            out.write("</relation>\n");
        }
    }

    private static void outputMembers(Writer out, Iterator<Member> memberIter) throws IOException {
        while (memberIter.hasNext()) {
            Member member = (Member) memberIter.next();

            out.write("<member type=\"");
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

            out.write("<way id=\"");
            out.write(Integer.toString(way.getID()));
            out.write("\">\n");

            Iterator<Node> nodeIter = way.getNodeIterator();
            outputWayRefs(out, nodeIter);

            Iterator<Tag> tagIter = way.getTagIterator();
            outputTags(out, tagIter);

            out.write("</way>\n");
        }
    }

    private static void outputWayRefs(Writer out, Iterator<Node> nodeIter) throws IOException {
        while (nodeIter.hasNext()) {
            Primitive node = (Primitive) nodeIter.next();

            StringBuilder buf = new StringBuilder();

            buf.append("<nd ref=\"");
            buf.append(node.getID());
            buf.append("\"/>\n");

            out.write(buf.toString());
        }
    }

    private static void outputNodes(Writer out, Iterator<Node> nodeIter) throws IOException {
        while (nodeIter.hasNext()) {
            Node node = (Node) nodeIter.next();

            StringBuilder buf = new StringBuilder();

            buf.append("<node id=\"");
            buf.append(node.getID());
            buf.append("\" lat=\"");
            buf.append(Double.toString(node.getLat()));
            buf.append("\" lon=\"");
            buf.append(Double.toString(node.getLon()));
            buf.append("\"");

            if (node.hasTags()) {
                buf.append(">\n");

                out.write(buf.toString());

                Iterator<Tag> tagIter = node.getTagIterator();
                outputTags(out, tagIter);

                out.write("</node>\n");
            } else {
                buf.append("/>\n");
                out.write(buf.toString());
            }

        }
    }

    private static void outputTags(Writer out, Iterator<Tag> tagIter) throws IOException {
        while (tagIter.hasNext()) {
            Tag tag = (Tag) tagIter.next();

            StringBuilder buf = new StringBuilder();

            buf.append("<tag k=\"");
            buf.append(tag.getKey());
            buf.append("\" v=\"");
            buf.append(tag.getValue());
            buf.append("\"/>\n");

            out.write(buf.toString());
        }
    }

}
