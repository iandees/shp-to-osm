package com.yellowbkpk.geo.shp;
import java.util.LinkedList;
import java.util.List;

import osm.primitive.Primitive;


public class RuleSet {

    private List<Rule> inner = new LinkedList<Rule>();
    private List<Rule> outer = new LinkedList<Rule>();
    private List<Rule> point = new LinkedList<Rule>();
    private List<Rule> line = new LinkedList<Rule>();
    private List<ExcludeRule> excludeRules = new LinkedList<ExcludeRule>();
    
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
    
}
