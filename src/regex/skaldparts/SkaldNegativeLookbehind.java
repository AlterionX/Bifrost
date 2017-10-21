package regex.skaldparts;

import regex.Lausavisa;
import regex.NFA;
import regex.RegEx;

public class SkaldNegativeLookbehind implements RegEx {
    private RegEx internal;
    public SkaldNegativeLookbehind(RegEx regex) {
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
        System.out.println("NEGATIVE_LOOKBEHIND");
        internal.printStructure(level + 1);
    }
    public StringBuilder generateString() {
        return (new StringBuilder()).append("(?<!(").append(internal.generateString()).append("))");
    }
    public NFA generateNFA() {
        return new Lausavisa(true, true, internal.reverse().generateNFA().tablify().generateDFA());
    }
    public RegEx reverse() {
        return new SkaldNegativeLookahead(internal.reverse());
    }
}
