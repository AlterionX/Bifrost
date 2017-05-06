package bragi;

import java.util.ArrayList;

public class SkaldChoice implements SkaldComponent {
    private ArrayList<SkaldComponent> opt = new ArrayList<>();

    public SkaldChoice() {}
    public SkaldChoice(ArrayList<SkaldComponent> reversed) {
        opt = reversed;
    }

    public void addChoice(SkaldComponent regex) {
        opt.add(regex);
    }
    public SkaldComponent fetchChoice(int i) {
        return opt.get(i);
    }
    public int choiceCount() {
        return opt.size();
    }

    public SkaldComponent reduce() {
        for (int i = 0; i < opt.size(); i++) {
            opt.set(i, opt.get(i).reduce());
        }
        opt.remove(null);
        ArrayList<SkaldComponent> temp = new ArrayList<>();
        for (SkaldComponent regex : opt) {
            if (regex instanceof SkaldChoice) temp.addAll(((SkaldChoice) regex).opt);
            else temp.add(regex);
        }
        this.opt = temp;
        if (opt.size() == 0) return null;
        if (opt.size() == 1) return opt.get(0);
        return this;
    }

    public void printStructure(int level) {
        for (int i = 0; i < level; i++) {
            System.out.print("\t");
        }
        System.out.println("UNION");
        for (SkaldComponent regex: opt) {
            regex.printStructure(level + 1);
        }
    }
    public StringBuilder generateString() {
        StringBuilder sb = new StringBuilder();
        for (SkaldComponent regex: opt) {
            sb.append("(").append(regex.generateString()).append(")|");
        }
        sb.setLength(sb.length() - 1);
        return sb;
    }
    public Lausavisa generateNFA() {
        if (opt.isEmpty()) return null;
        Lausavisa nfa = opt.get(0).generateNFA();
        for (int i = 1; i < opt.size(); i++) {
            nfa.merge(opt.get(i).generateNFA(), ((i == 1) ? (Lausavisa.SIMPLE_BRANCH) : (Lausavisa.CONCAT_BRANCH)));
        }
        nfa.addStringToTerminal(this.generateString().toString());
        return nfa;
    }
    public SkaldComponent reverse() {
        ArrayList<SkaldComponent> reversed = new ArrayList<>();
        for (SkaldComponent regex : opt) {
            reversed.add(regex.reverse());
        }
        return new SkaldChoice(reversed);
    }
}
