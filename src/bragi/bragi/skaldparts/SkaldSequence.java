package bragi.bragi.skaldparts;

import bragi.Lausavisa;
import bragi.NFA;
import bragi.RegEx;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class SkaldSequence implements RegEx {
    private ArrayList<RegEx> seq;
    public SkaldSequence() {
        seq = new ArrayList<>();
    }
    private SkaldSequence(List<RegEx> reversed) {
        seq.addAll(reversed);
    }

    public void add(RegEx regex) {
        seq.add(regex);
    }

    public RegEx reduce() {
        for (int i = 0; i < seq.size(); ++i) {
            seq.set(i, seq.get(i).reduce());
        }
        seq.remove(null);
        ArrayList<RegEx> temp = new ArrayList<>();
        for (RegEx regex : seq) {
            if (regex instanceof SkaldSequence) temp.addAll(((SkaldSequence)regex).seq);
            else temp.add(regex);
        }
        this.seq = temp;
        return (seq.size() == 0) ? (null) : ((seq.size() == 1) ? (seq.get(0)) : (this));
    }

    public void printStructure(int level) {
        for (int i = 0; i < level; i++) {
            System.out.print("\t");
        }
        System.out.println("SEQUENCE");
        for (RegEx regex: seq) {
            regex.printStructure(level + 1);
        }
    }
    public StringBuilder generateString() {
        StringBuilder sb = new StringBuilder();
        for (RegEx regex: seq) {
            sb.append("(").append(regex.generateString()).append(")");
        }
        return sb;
    }
    public NFA generateNFA() {
        if (seq.size() == 0) new Lausavisa();
        NFA nfa = seq.get(0).generateNFA();
        for (int i = 1; i < seq.size(); i++) {
            nfa.merge(seq.get(i).generateNFA(), Lausavisa.SIMPLE_CONCAT);
        }
        nfa.addStringToTerminal(this.generateString().toString());
        return nfa;
    }
    public RegEx reverse() {
        LinkedList<RegEx> reversed = new LinkedList<>();
        for (RegEx regex : seq) {
            reversed.addFirst(regex.reverse());
        }
        return new SkaldSequence(reversed);
    }
}
