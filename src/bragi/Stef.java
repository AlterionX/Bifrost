package bragi;

import java.util.*;

public class Stef {
    Map<SkaldPrim, Set<Stef>> shiftMatrix = new HashMap<>();
    int index = 0;
    int mark = 0;

    private String nfaTerminationRegExString;

    ArrayList<Boolean> negateLookAround = new ArrayList<>();
    ArrayList<Boolean> reverseLookAround = new ArrayList<>();
    ArrayList<Drottkvaett> visur = new ArrayList<>();

    Stef() {}
    Stef(Stef next, SkaldPrim prim) {
        this.registerConnection(next, prim);
    }
    void registerConnection(Stef next, SkaldPrim prim) {
        shiftMatrix.putIfAbsent(prim, new HashSet<>());
        shiftMatrix.get(prim).add(next);
    }
    public String toString() {
        return "NODE" + index;
    }

    public void addRegExString(String regexString) {
        nfaTerminationRegExString = regexString;
    }
    public String regExString() {
        return nfaTerminationRegExString;
    }

    public Set<Stef> fetchNext(SkaldPrim input) {
        if (input.equals(new SkaldPrim(true, false, false))) {
            return shiftMatrix.get(input);
        }
        boolean valid = false;
        Set<Stef> other = new HashSet<>();
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
    public Stef fetchSingleNext(SkaldPrim input) {
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

    public boolean lookaroundCheck(String stream, int currLoc) {
        //System.out.println(visur);
        for (int i = 0; i < visur.size(); i++) {
            if (negateLookAround.get(i)) { //False if exists
                if (reverseLookAround.get(i) ?
                        (visur.get(i).processFirstReverse(stream, currLoc - 2) != -1) :
                        (visur.get(i).processFirst(stream, currLoc) != -1)
                ) {
                    //System.out.println("FALSE-N");
                    return false;
                }
            } else { //False if doesn't exist
                if (reverseLookAround.get(i) ?
                        (visur.get(i).processFirstReverse(stream, currLoc - 2) == -1) :
                        (visur.get(i).processFirst(stream, currLoc) == -1)
                ) {
                    //System.out.println("FALSE-P");
                    return false;
                }
            }
        }
        //System.out.println("TRUE");
        return true;
    }
    public void mergeLookArounds(Stef nfaSubNode) {
        if (!nfaSubNode.visur.isEmpty()) {
            this.negateLookAround.addAll(nfaSubNode.negateLookAround);
            this.reverseLookAround.addAll(nfaSubNode.reverseLookAround);
            this.visur.addAll(nfaSubNode.visur);
        }
    }
}