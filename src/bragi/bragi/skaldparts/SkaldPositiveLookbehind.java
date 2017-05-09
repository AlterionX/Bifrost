package bragi.bragi.skaldparts;

import bragi.Lausavisa;
import bragi.SkaldComponent;

public class SkaldPositiveLookbehind implements SkaldComponent {
    private SkaldComponent internal;
    public SkaldPositiveLookbehind(SkaldComponent regex) {
        internal = regex;
    }
    public SkaldComponent reduce() {
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
    public Lausavisa generateNFA() {
        return new Lausavisa(false, true, internal.reverse().generateNFA().tablify().generateDFA());
    }
    public SkaldComponent reverse() {
        return new SkaldPositiveLookahead(internal.reverse());
    }
}
