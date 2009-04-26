import java.util.ArrayList;
import java.util.List;


public class RuleSet {

    private List<Rule> inner = new ArrayList<Rule>();
    private List<Rule> outer = new ArrayList<Rule>();
    private List<Rule> point = new ArrayList<Rule>();
    private List<Rule> line = new ArrayList<Rule>();
    
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
    
}
