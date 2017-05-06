package midgard;

import yggdrasil.TagPriority;
import yggdrasil.TagRecord;
import yggdrasil.Yggdrasil;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class LRState {
    private Yggdrasil parent;
    private Integer pos;
    private CFGRule rule;
    private Set<Integer> follows;
    //Initialization
    public LRState(int pos, CFGRule rule, Yggdrasil parent, Integer... follows) {
        this.parent = parent;
        this.pos = pos;
        this.rule = rule;
        this.follows = new HashSet<>();
        this.follows.addAll(Arrays.asList(follows));
    }
    //Simpler copying
    public LRState duplicate() {
        return new LRState(pos, rule, parent);
    }
    //Shift state
    public int advance() {
        return ++pos;
    }
    //Fetch state
    public int getPos() {
        return pos;
    }
    public int getLast() {
        return rule.getRightElement(pos - 1);
    }
    public int getNext() {
        return rule.getRightElement(pos);
    }
    public CFGRule getRule() {
        return rule;
    }
    public boolean isAtEnd() {
        return pos == rule.getRightCount();
    }
    //Object class method override
    public String toString() {
        StringBuilder sb = new StringBuilder(" state ").append(parent.tagDecode(rule.getLeft(), TagPriority.SUB)).append(" -> ");
        for (int i = 0; i < rule.getRightCount(); i++) {
            if (pos == i) {
                sb.append(" ^ ");
            }
            sb.append(" ").append(parent.tagDecode(rule.getRightElement(i), TagPriority.SUB));
        }
        if (pos == rule.getRightCount()) {
            sb.append(" ^");
        }
        return sb.toString();
    }
    public boolean equals(Object o) {
        return o instanceof LRState && this.rule.equals(((LRState) o).rule) && this.pos == (int) ((LRState) o).pos;
    }
    public int hashCode() {
        return rule.hashCode() + pos;
    }
}
