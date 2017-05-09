package bragi.bragi.skaldparts;

import bragi.Lausavisa;
import bragi.SkaldComponent;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class SkaldSequence implements SkaldComponent {
    private ArrayList<SkaldComponent> seq;
    public SkaldSequence() {
        seq = new ArrayList<>();
    }
    public SkaldSequence(List<SkaldComponent> reversed) {
        seq.addAll(reversed);
    }

    public void add(SkaldComponent regex) {
        seq.add(regex);
    }

    public SkaldComponent reduce() {
        for (int i = 0; i < seq.size(); ++i) {
            seq.set(i, seq.get(i).reduce());
        }
        seq.remove(null);
        ArrayList<SkaldComponent> temp = new ArrayList<>();
        for (SkaldComponent regex : seq) {
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
        for (SkaldComponent regex: seq) {
            regex.printStructure(level + 1);
        }
    }
    public StringBuilder generateString() {
        StringBuilder sb = new StringBuilder();
        for (SkaldComponent regex: seq) {
            sb.append("(").append(regex.generateString()).append(")");
        }
        return sb;
    }
    public Lausavisa generateNFA() {
        if (seq.size() == 0) new Lausavisa();
        Lausavisa nfa = seq.get(0).generateNFA();
        for (int i = 1; i < seq.size(); i++) {
            nfa.merge(seq.get(i).generateNFA(), Lausavisa.SIMPLE_CONCAT);
        }
        nfa.addStringToTerminal(this.generateString().toString());
        return nfa;
    }
    public SkaldComponent reverse() {
        LinkedList<SkaldComponent> reversed = new LinkedList<>();
        for (SkaldComponent regex : seq) {
            reversed.addFirst(regex.reverse());
        }
        return new SkaldSequence(reversed);
    }
}
