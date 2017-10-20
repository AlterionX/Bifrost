package bragi;

import bragi.bragi.skaldparts.SkaldPrim;

import java.util.*;

public class Stef implements FSANode {
    private int index = 0;
    private final Map<SkaldPrim, Set<FSANode>> shiftMatrix = new HashMap<>();

    private int scratch = 0;
    private String nfaTerminationRegExString;

    private final Set<FSANode.LookAround> visurSet = new HashSet<>();

    //region Constructors
    Stef() {}
    Stef(FSANode next, SkaldPrim prim) {
        this.registerConnection(next, prim);
    }
    //endregion

    //region Simple accessors
    public void addRegExString(String regexString) {
        nfaTerminationRegExString = regexString;
    }
    public String getRegExString() {
        return nfaTerminationRegExString;
    }

    @Override
    public void setIndex(int index) {
        this.index = index;
    }

    @Override
    public int getIndex() {
        return this.index;
    }

    @Override
    public void setScratch(int scratch) {
        this.scratch = scratch;
    }

    @Override
    public int getScratch() {
        return this.scratch;
    }
    //endregion

    //region Connections
    @Override
    public void registerConnection(FSANode next, SkaldPrim prim) {
        shiftMatrix.putIfAbsent(prim, new HashSet<>());
        shiftMatrix.get(prim).add(next);
    }
    @Override
    public Set<SkaldPrim> possibleInputs() {
        return shiftMatrix.keySet();
    }
    @Override
    public Set<FSANode> possibleTransitions(SkaldPrim input) {
        return shiftMatrix.get(input);
    }
    @Override
    public Set<FSANode> fetchNext(SkaldPrim input) {
        if (input.equals(new SkaldPrim(true, false, false))) {
            return shiftMatrix.get(input);
        }
        boolean valid = false;
        Set<FSANode> other = new HashSet<>();
        if (shiftMatrix.containsKey(new SkaldPrim(false, true, false))) {
            valid = true;
            other.addAll(shiftMatrix.get(new SkaldPrim(false, true, false)));
        }
        if (shiftMatrix.containsKey(input)) {
            valid = true;
            other.addAll(shiftMatrix.get(input));
        }
        if (valid) {
            return other;
        }
        return new HashSet<>();
    }
    public FSANode fetchSingleNext(SkaldPrim input) {
        if (input.equals(new SkaldPrim(true, false, false))) {
            throw new RuntimeException("By own definition, cannot have empty pathways in a DFA.");
        }
        if (shiftMatrix.containsKey(input)) {
            return shiftMatrix.get(input).iterator().next();
        }
        if (shiftMatrix.containsKey(new SkaldPrim(false, true, false))) {
            return shiftMatrix.get(new SkaldPrim(false, true, false)).iterator().next();
        }
        return null;
    }
    //endregion

    //region LookArounds
    @Override
    public void addLookAround(FSAutomaton dfa, boolean negate, boolean reverse) {
        visurSet.add(new FSANode.LookAround(dfa, negate, reverse));
    }
    @Override
    public Set<LookAround> getLookArounds() {
        return visurSet;
    }
    @Override
    public int countLookArounds() {
        return visurSet.size();
    }
    @Override
    public boolean checkLookArounds(String stream, int currLoc) {
        //System.out.println(visur);
        for (LookAround k : this.getLookArounds()) {
            if (k.isNegate()) { //False if exists
                if (k.isReverse() ?
                        (k.getBasis().processFirstReverse(stream, currLoc - 2) != -1) :
                        (k.getBasis().processFirst(stream, currLoc) != -1)
                        ) {
                    //System.out.println("FALSE-N");
                    return false;
                }
            } else { //False if doesn't exist
                if (k.isReverse() ?
                        (k.getBasis().processFirstReverse(stream, currLoc - 2) == -1) :
                        (k.getBasis().processFirst(stream, currLoc) == -1)
                        ) {
                    //System.out.println("FALSE-P");
                    return false;
                }
            }
        }
        //System.out.println("TRUE");
        return true;
    }
    @Override
    public void mergeLookArounds(FSANode nfaSubNode) {
        if (nfaSubNode.countLookArounds() != 0) {
            this.visurSet.addAll(nfaSubNode.getLookArounds());
        }
    }
    //endregion

    //region Object overrides
    @Override
    public String toString() {
        return "NODE" + index;
    }
    //endregion
}