package parser;

import tagtable.Tag;
import tagtable.TagTable;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class LRState {
    private final TagTable tagTable;
    private Integer pos;
    private final CFGProduction rule;

    //Initialization
    public LRState(int pos, CFGProduction rule, TagTable tagTable, Tag... follows) {
        this.pos = pos;
        this.rule = rule;
        this.tagTable = tagTable;
        Set<Tag> follows1 = new HashSet<>(Arrays.stream(follows).collect(Collectors.toList()));
    }
    //Simpler copying
    public LRState duplicate() {
        return new LRState(pos, rule, tagTable);
    }
    //Shift state
    public void advance() {
        ++pos;
    }
    //Fetch state
    public int getPos() {
        return pos;
    }
    public Tag getLast() {
        return rule.getRightElement(pos - 1);
    }
    public Tag getNext() {
        return rule.getRightElement(pos);
    }
    public CFGProduction getRule() {
        return rule;
    }
    public boolean isAtEnd() {
        return pos == rule.getRightCount();
    }
    //Object class method override
    public String toString() {
        StringBuilder sb = new StringBuilder(" state ").append(rule.getLeft()).append(" -> ");
        for (int i = 0; i < rule.getRightCount(); i++) {
            if (pos == i) {
                sb.append(" ^ ");
            }
            sb.append(" ").append(rule.getRightElement(i));
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
