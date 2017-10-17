package midgard;

import tagtable.Tag;
import tagtable.TagTable;
import tagtable.TagPriority;

import java.util.*;

public class LRNode {
    //States and outEdges
    private final Set<LRState> ruleStates;
    private final Map<Tag, LRNode> outEdges;
    private final TagTable tagTable;
    //Simply create block without closure or continuing
    public LRNode(LRState state, TagTable tagTable) {
        this.tagTable = tagTable;
        ruleStates = new HashSet<>();
        outEdges = new HashMap<>();
        ruleStates.add(state);
    }
    public LRNode(Set<LRState> LRStates, TagTable tagTable) {
        this.tagTable = tagTable;
        ruleStates = new HashSet<>();
        ruleStates.addAll(LRStates);
        outEdges = new HashMap<>();
    }
    //Node modification and data retrieval
    public LRNode fetchTransition(Tag input) {
        return outEdges.get(input);
    }
    public void addTransition(Tag input, LRNode destination) {
        outEdges.put(input, destination);
    }
    public Set<LRState> fetchStates() {
        return ruleStates;
    }
    public void addRuleState(LRState state) {
        ruleStates.add(state);
    }
    public Map<Tag, LRNode> getOutEdges() {
        return outEdges;
    }
    //Fetching rule states and the block state
    public void addRuleStates(Set<LRState> states) {
        this.ruleStates.addAll(states);
    }
    public boolean hasRuleState(LRState state) {
        return ruleStates.contains(state);
    }
    //Determines end states
    public boolean isEndNode() {
        //Cleanup
        if (ruleStates.iterator().next().getRule().getLeft() == tagTable.START_TAG &&
                ruleStates.iterator().next().isAtEnd()) {
            System.out.println("The final death. But this is a benign death. A proper death. " +
                    "The death of a warrior, bound for Valhalla. " +
                    "Unless it more than once. Accursed necromancy.");
            return true;
        }
        return false;
    }
    //General object overrides
    public boolean equals(Object o) {
        return o instanceof LRNode &&
                this.ruleStates.containsAll(((LRNode) o).ruleStates) &&
                this.ruleStates.size() == ((LRNode) o).ruleStates.size();
    }
    public int hashCode() {
        return ruleStates.stream().mapToInt(Object::hashCode).sum();
    }
    public String toString() {
        StringBuilder total = new StringBuilder("Node block: \n");
        for (LRState state : ruleStates) {
            total.append("\tMember: ").append(state).append("\n");
        }
        for (Tag transition : outEdges.keySet()) {
            total.append("\tTransition: ").append(transition).append("\n");
        }
        total.setLength(total.length() - 1);
        return total.toString();
    }
}
