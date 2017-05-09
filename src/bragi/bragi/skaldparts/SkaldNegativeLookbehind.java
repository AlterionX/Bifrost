package bragi.bragi.skaldparts;

import bragi.Lausavisa;
import bragi.SkaldComponent;
import bragi.bragi.skaldparts.SkaldNegativeLookahead;

public class SkaldNegativeLookbehind implements SkaldComponent {
    private SkaldComponent internal;
    public SkaldNegativeLookbehind(SkaldComponent regex) {
        internal = regex;
    }

    public SkaldComponent reduce() {
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
    public Lausavisa generateNFA() {
        return new Lausavisa(true, true, internal.reverse().generateNFA().tablify().generateDFA());
    }
    public SkaldComponent reverse() {
        return new SkaldNegativeLookahead(internal.reverse());
    }
}
