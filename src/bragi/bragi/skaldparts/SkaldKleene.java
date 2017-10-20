package bragi.bragi.skaldparts;

import bragi.Lausavisa;
import bragi.NFA;
import bragi.RegEx;

public class SkaldKleene implements RegEx {
    private RegEx internal;
    public SkaldKleene(RegEx regex) {
        internal = regex;
    }

    public RegEx reduce() {
        internal = internal.reduce();
        if (internal instanceof SkaldKleene) {
            return internal;
        }
        return (internal == null) ? null : this;
    }

    public void printStructure(int level) {
        for (int i = 0; i < level; i++) {
            System.out.print("\t");
        }
        System.out.println("KLEENE STAR");
        internal.printStructure(level + 1);
    }
    public StringBuilder generateString() {
        return (new StringBuilder()).append("(").append(internal.generateString()).append(")*");
    }
    public NFA generateNFA() {
        NFA nfa = internal.generateNFA().kleeneWrap();
        nfa.addStringToTerminal(this.generateString().toString());
        return nfa;
    }
    public RegEx reverse() {
        return new SkaldKleene(internal.reverse());
    }
}
