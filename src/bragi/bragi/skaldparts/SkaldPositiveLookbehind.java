package bragi.bragi.skaldparts;

import bragi.Lausavisa;
import bragi.NFA;
import bragi.RegEx;

public class SkaldPositiveLookbehind implements RegEx {
    private RegEx internal;
    public SkaldPositiveLookbehind(RegEx regex) {
        internal = regex;
    }
    public RegEx reduce() {
        internal = internal.reduce();
        return internal == null ? null : this;
    }

    public void printStructure(int level) {
        for (int i = 0; i < level; i++) {
            System.out.print("\t");
        }
        System.out.println("POSITIVE LOOKBEHIND");
        internal.printStructure(level + 1);
    }
    public StringBuilder generateString() {
        return (new StringBuilder()).append("(?<=(").append(internal.generateString()).append("))");
    }
    public NFA generateNFA() {
        return new Lausavisa(false, true, internal.reverse().generateNFA().tablify().generateDFA());
    }
    public RegEx reverse() {
        return new SkaldPositiveLookahead(internal.reverse());
    }
}
