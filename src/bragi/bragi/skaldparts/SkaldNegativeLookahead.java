package bragi.bragi.skaldparts;

import bragi.Lausavisa;
import bragi.NFA;
import bragi.RegEx;

public class SkaldNegativeLookahead implements RegEx {
    private RegEx internal;
    public SkaldNegativeLookahead(RegEx regex) {
        internal = regex;
    }

    public RegEx reduce() {
        internal = internal.reduce();
        return (internal == null) ? (null) : (this);
    }

    public void printStructure(int level) {
        for (int i = 0; i < level; i++) {
            System.out.print("\t");
        }
        System.out.println("NEGATIVE_LOOKAHEAD");
        internal.printStructure(level + 1);
    }
    public StringBuilder generateString() {
        return (new StringBuilder()).append("(?!(").append(internal.generateString()).append(")");
    }
    public NFA generateNFA() {
        return new Lausavisa(true, false, internal.generateNFA().tablify().generateDFA());
    }
    public RegEx reverse() {
        return new SkaldNegativeLookbehind(internal.reverse());
    }

}
