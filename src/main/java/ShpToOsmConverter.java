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

import osm.OSMFile;
import osm.primitive.Primitive;
import osm.primitive.Tag;
import osm.primitive.node.Node;
import osm.primitive.relation.Member;
import osm.primitive.relation.Relation;
import osm.primitive.way.Way;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShpToOsmConverter {

    private static final int MAX_NODES_IN_WAY = 2000;
    private static final int MAX_ELEMENTS = 50000;
    private static final double LOADING_FACTOR = 1.25;
    private File inputFile;
    private File outputFile;
    private RuleSet ruleset;
    private boolean onlyIncludeTaggedPrimitives;
    private int filesCreated = 0;
    private int maxElements = MAX_ELEMENTS;
    private OSMOutputter outputter;

    public ShpToOsmConverter(File shpFile, RuleSet rules, File osmFile, boolean onlyIncludeTaggedPrim, int maxNodesPerFile, OSMOutputter out) {
        inputFile = shpFile;
        outputFile = osmFile;
        maxElements = maxNodesPerFile;
        outputter = out;
        
        ruleset = rules;
        onlyIncludeTaggedPrimitives = onlyIncludeTaggedPrim;
    }

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
            if (sourceCRS == null) {
                System.err
                        .println("Could not determine the shapefile's projection. More than likely, the .prj file was not included.");
                System.exit(-1);
            } else {
                System.err.println("Converting from " + sourceCRS + " to " + targetCRS);
            }

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
                        try {
                            SimpleFeature feature = iterator.next();

                            Geometry rawGeom = (Geometry) feature.getDefaultGeometry();
                            
                            int approxElementsThatWillBeAdded = (int) (rawGeom.getNumPoints() * LOADING_FACTOR);
                            int changes = osmOut.getChangeCount();
                            System.err.println("Approx new points: " + approxElementsThatWillBeAdded + "  Total so far: " + changes);
                            if(changes > 0 && changes + approxElementsThatWillBeAdded > maxElements) {
                                File actualOutput = new File(outputFile.getAbsolutePath() + filesCreated + ".osm");
                                outputter.write(osmOut, actualOutput);
                                osmOut = new OSMFile();
                                filesCreated++;
                                changes = 0;
                            }
                            
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
									LineString geometryN = (LineString) geometry
											.getGeometryN(i);

									List<Way> ways = linestringToWays(geometryN);
									applyRulesList(feature, geometryType, ways,
											ruleset.getLineRules());
									for (Way way : ways) {

										if (shouldInclude(way)) {
											osmOut.addWay(way);
										}
									}
								}

                            } else if ("MultiPolygon".equals(geometryType)) {

                                for (int i = 0; i < geometry.getNumGeometries(); i++) {
                                    Polygon geometryN = (Polygon) geometry.getGeometryN(i);

                                    // Get the outer ring of the polygon
                                    LineString outerLine = geometryN.getExteriorRing();

                                    List<Way> outerWays = polygonToWays(outerLine);

                                    if (geometryN.getNumInteriorRing() > 0) {
                                        Relation r = new Relation();
                                        r.addTag(new Tag("type", "multipolygon"));
                                        
                                        // Tags go on the relation for multipolygons

                                        applyRulesList(feature, geometryType, Arrays.asList(r), ruleset.getOuterPolygonRules());

                                        for (Primitive outerWay : outerWays) {

                                            if (shouldInclude(outerWay)) {

                                                r.addMember(new Member(outerWay, "outer"));
                                            }
                                        }

                                        // Then the inner ones, if any
                                        for (int j = 0; j < geometryN.getNumInteriorRing(); j++) {
                                            LineString innerLine = geometryN.getInteriorRingN(j);

                                            List<Way> innerWays = polygonToWays(innerLine);

                                            applyRulesList(feature, geometryType, innerWays, ruleset
                                                    .getInnerPolygonRules());
                                            
                                            for (Way innerWay : innerWays) {
                                                r.addMember(new Member(innerWay, "inner"));
                                            }

                                        }

                                        osmOut.addRelation(r);

                                    } else {
                                        // If there's more than one way, then it
                                        // needs to be a multipolygon and the
                                        // tags need to be applied to the
                                        // relation
                                        if(outerWays.size() > 1) {
                                            Relation r = new Relation();
                                            r.addTag(new Tag("type", "multipolygon"));

                                            applyRulesList(feature, geometryType, r, ruleset
                                                    .getOuterPolygonRules());

                                            for (Way outerWay : outerWays) {
                                                if (shouldInclude(outerWay)) {
                                                    r.addMember(new Member(outerWay, "outer"));
                                                }
                                            }
                                            
                                            osmOut.addRelation(r);
                                        } else {
                                            // If there aren't any inner lines, then
                                            // just use the outer one as a way.
                                            applyRulesList(feature, geometryType, outerWays, ruleset
                                                    .getOuterPolygonRules());

                                            for (Way outerWay : outerWays) {
                                                if (shouldInclude(outerWay)) {
                                                    osmOut.addWay(outerWay);
                                                }
                                            }
                                        }
                                    }
                                }
                            } else if ("Point".equals(geometryType)) {
                                List<Node> nodes = new ArrayList<Node>(geometry.getNumGeometries());
                                for (int i = 0; i < geometry.getNumGeometries(); i++) {
                                    Point geometryN = (Point) geometry.getGeometryN(i);

                                    Node n = pointToNode(geometryN);

                                    nodes.add(n);
                                }

                                applyRulesList(feature, geometryType, nodes, ruleset.getPointRules());

                                for (Node node : nodes) {
                                    if (shouldInclude(node)) {
                                        osmOut.addNode(node);
                                    }
                                }
                            }
                        } catch (IllegalArgumentException e) {
                            System.err.println("Skipping a geometry becase:");
                            e.printStackTrace();
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

        File actualOutput = new File(outputFile.getName() + filesCreated + ".osm");
        outputter.write(osmOut, actualOutput);
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

    private void applyRulesList(SimpleFeature feature, String geometryType, List<? extends Primitive> features,
            List<Rule> rulelist) {
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
                            	for (Primitive primitive : features) {
									primitive.addTag(t);
								}
                            }
                        }
                    }
                }
            }
        }
    }
    
    private void applyRulesList(SimpleFeature feature, String geometryType, Primitive features, List<Rule> rulelist) {
        applyRulesList(feature, geometryType, Arrays.asList(features), rulelist);
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
    
    private static List<Way> linestringToWays(LineString geometryN) {
        Coordinate[] coordinates = geometryN.getCoordinates();
        
        // Follow the 2000 nodes per way max rule
        int waysToCreate = coordinates.length / MAX_NODES_IN_WAY;
        waysToCreate += (coordinates.length % MAX_NODES_IN_WAY == 0) ? 0 : 1;

        List<Way> ways = new ArrayList<Way>(waysToCreate);
        
        Way way = new Way();

        int nodeCount = 0;
        for (Coordinate coord : coordinates) {
            Node node = new Node(coord.y, coord.x);
            way.addNode(node);
            
            if(++nodeCount % MAX_NODES_IN_WAY == 0) {
            	ways.add(way);
            	way = new Way();
            	way.addNode(node);
            }
        }
        
        // Add the last way to the list of ways
        if(way.nodeCount() > 0) {
        	ways.add(way);
        }

        return ways;
    }

    private static List<Way> polygonToWays(LineString geometryN) {
        Coordinate[] coordinates = geometryN.getCoordinates();
        if(coordinates.length < 2) {
            throw new IllegalArgumentException("Way with less than 2 nodes.");
        }

        // Follow the 2000 max nodes per way rule
        int waysToCreate = coordinates.length / MAX_NODES_IN_WAY;
        waysToCreate += (coordinates.length % MAX_NODES_IN_WAY == 0) ? 0 : 1;

        List<Way> ways = new ArrayList<Way>(waysToCreate);
        
        Way way = new Way();
        
        // First node for the polygon
        Coordinate firstCoord = coordinates[0];
        Node firstNode = new Node(firstCoord.y, firstCoord.x);
        way.addNode(firstNode);

        // "middle" nodes
        for (int i = 1; i < coordinates.length-1; i++) {
            Coordinate coord = coordinates[i];

            Node node = new Node(coord.y, coord.x);
            way.addNode(node);
            
            if(i % (MAX_NODES_IN_WAY - 1) == 0) {
            	ways.add(way);
            	way = new Way();
            	way.addNode(node);
            }
        }
        
        // Last node should be the same ID as the first one
        Coordinate lastCoord = coordinates[coordinates.length-1];
        if(lastCoord.x == firstCoord.x && lastCoord.y == firstCoord.y) {
            way.addNode(firstNode);
        }
        
        // Add the last way to the list of ways
        if(way.nodeCount() > 0) {
        	ways.add(way);
        }
        
        return ways;
    }

}
