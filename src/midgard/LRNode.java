package midgard;

import yggdrasil.TagPriority;
import yggdrasil.TagRecord;
import yggdrasil.Yggdrasil;

import java.util.*;

public class LRNode {
    //States and outEdges
    private Set<LRState> ruleStates;
    private Map<Integer, LRNode> outEdges;
    private Yggdrasil parent;
    //Simply create block without closure or continuing
    public LRNode(LRState state, Yggdrasil parent) {
        this.parent = parent;
        ruleStates = new HashSet<>();
        outEdges = new HashMap<>();
        ruleStates.add(state);
    }
    public LRNode(Set<LRState> LRStates, Yggdrasil parent) {
        this.parent = parent;
        ruleStates = new HashSet<>();
        ruleStates.addAll(LRStates);
        outEdges = new HashMap<>();
    }
    //Node modification and data retrieval
    public LRNode fetchTransition(Integer input) {
        return outEdges.get(input);
    }
    public void addTransition(Integer input, LRNode destination) {
        outEdges.put(input, destination);
    }
    public Set<LRState> fetchStates() {
        return ruleStates;
    }
    public void addRuleState(LRState state) {
        ruleStates.add(state);
    }
    public Map<Integer, LRNode> getOutEdges() {
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
        if (ruleStates.iterator().next().getRule().getLeft() == parent.tagEncode(TagRecord.START_LABEL, TagPriority.SUB) &&
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
        for (Integer transition : outEdges.keySet()) {
            total.append("\tTransition: ").append(parent.tagDecode(transition, TagPriority.SUB)).append("\n");
        }
        total.setLength(total.length() - 1);
        return total.toString();
    }
}
