package com.yellowbkpk.geo.shp;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.opengis.feature.Property;
import org.opengis.feature.simple.SimpleFeature;

import osm.primitive.Primitive;
import osm.primitive.Tag;

public class RuleSet {

    private List<Rule> inner = new LinkedList<Rule>();
    private List<Rule> outer = new LinkedList<Rule>();
    private List<Rule> point = new LinkedList<Rule>();
    private List<Rule> line = new LinkedList<Rule>();
    private List<ExcludeRule> excludeRules = new LinkedList<ExcludeRule>();
    private String allTagsPrefix = null;
    
    public void addInnerPolygonRule(Rule r) {
        inner.add(r);
    }
    public void addOuterPolygonRule(Rule r) {
        outer.add(r);
    }
    public void addPointRule(Rule r) {
        point.add(r);
    }
    public void addLineRule(Rule r) {
        line.add(r);
    }
    
    public List<Rule> getInnerPolygonRules() {
        return inner;
    }
    public List<Rule> getOuterPolygonRules() {
        return outer;
    }
    public List<Rule> getPointRules() {
        return point;
    }
    public List<Rule> getLineRules() {
        return line;
    }
    public void addFilter(ExcludeRule rule) {
        excludeRules.add(rule);
    }
    public boolean includes(Primitive w) {
        for (ExcludeRule rule : excludeRules) {
            if(!rule.allows(w)) {
                return false;
            }
        }
        return true;
    }
    public void setUseAllTags(String allTagsPrefix) {
        this.allTagsPrefix = allTagsPrefix;
    }
    public void appendRules(RuleSet existingRules) {
        inner.addAll(existingRules.inner);
        outer.addAll(existingRules.outer);
        point.addAll(existingRules.point);
        line.addAll(existingRules.line);
        excludeRules.addAll(existingRules.excludeRules);
    }
    public void applyLineRules(SimpleFeature feature, String geometryType, List<? extends Primitive> primitives) {
        applyRules(feature, geometryType, primitives, line);
    }
    public void applyOuterPolygonRules(SimpleFeature feature, String geometryType, List<? extends Primitive> primitives) {
        applyRules(feature, geometryType, primitives, outer);
    }
    public void applyInnerPolygonRules(SimpleFeature feature, String geometryType, List<? extends Primitive> primitives) {
        applyRules(feature, geometryType, primitives, inner);
    }
    public void applyPointRules(SimpleFeature feature, String geometryType, List<? extends Primitive> primitives) {
        applyRules(feature, geometryType, primitives, point);
    }

    public void applyRules(SimpleFeature feature, String geometryType, List<? extends Primitive> primitives, List<Rule> rules) {
        if(allTagsPrefix != null) {
            for (Primitive primitive : primitives) {
                applyOriginalTagsTo(feature, geometryType, primitive, allTagsPrefix);
            }
        }
        
        Collection<Property> properties = feature.getProperties();
        for (Property property : properties) {
            String srcKey = property.getType().getName().toString();
            if (!geometryType.equals(srcKey)) {

                Object value = property.getValue();
                if (value != null) {
                    String dirtyOriginalValue = getDirtyValue(value);
                    
                    if (!StringUtils.isEmpty(dirtyOriginalValue)) {
                        String escapedOriginalValue = StringEscapeUtils.escapeXml(dirtyOriginalValue);

                        for (Rule rule : rules) {
                            Tag t = rule.createTag(srcKey, escapedOriginalValue);
                            if (t != null) {
                                for (Primitive primitive : primitives) {
                                    primitive.addTag(t);
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    private static String getDirtyValue(Object value) {
        String dirtyOriginalValue;
        if (value instanceof Double) {
            double asDouble = (Double) value;
            double floored = Math.floor(asDouble);
            if(floored == asDouble) {
                dirtyOriginalValue = Integer.toString((int) asDouble);
            } else {
                dirtyOriginalValue = Double.toString(asDouble);
            }
        } else {
            dirtyOriginalValue = value.toString().trim();
        }
        return dirtyOriginalValue;
    }

    private static void applyOriginalTagsTo(SimpleFeature feature, String geometryType, Primitive w, String prefix) {
        String prefixPlusColon = prefix + ":";
        
        Collection<Property> properties = feature.getProperties();
        for (Property property : properties) {
            String name = property.getType().getName().toString();
            if (!geometryType.equals(name)) {
                Object value = property.getValue();
                if (value != null) {
                    String dirtyOriginalValue = getDirtyValue(value);

                    if (!StringUtils.isEmpty(dirtyOriginalValue)) {
                        String escapedOriginalValue = StringEscapeUtils.escapeXml(dirtyOriginalValue);

                        w.addTag(new Tag(prefixPlusColon + name, escapedOriginalValue));
                    }
                }
            }
        }
    }
    
}
