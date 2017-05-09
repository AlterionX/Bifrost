package bragi.bragi.skaldparts;

import bragi.Lausavisa;
import bragi.SkaldComponent;

public class SkaldPositiveLookahead implements SkaldComponent {
    SkaldComponent internal;

    public SkaldPositiveLookahead(SkaldComponent regex) {
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
        System.out.println("NEGATIVE_LOOKAHEAD");
        internal.printStructure(level + 1);
    }
    public StringBuilder generateString() {
        return (new StringBuilder()).append("(?!(").append(internal.generateString()).append("))");
    }
    public Lausavisa generateNFA() {
        return new Lausavisa(false, false, internal.generateNFA().tablify().generateDFA());
    }
    public SkaldComponent reverse() {
        return new SkaldPositiveLookbehind(internal.reverse());
    }
}
