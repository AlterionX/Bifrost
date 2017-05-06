package bragi;

public class SkaldKleene implements SkaldComponent {
    private SkaldComponent internal;
    public SkaldKleene(SkaldComponent regex) {
        internal = regex;
    }

    public SkaldComponent reduce() {
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
    public Lausavisa generateNFA() {
        Lausavisa nfa = internal.generateNFA().kleeneWrap();
        nfa.addStringToTerminal(this.generateString().toString());
        return nfa;
    }
    public SkaldComponent reverse() {
        return new SkaldKleene(internal.reverse());
    }
}
