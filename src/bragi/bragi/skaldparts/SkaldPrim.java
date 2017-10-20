package bragi.bragi.skaldparts;

import bragi.Lausavisa;
import bragi.NFA;
import bragi.RegEx;

public class SkaldPrim implements RegEx {
    private final boolean EMP;
    private final boolean DOT;
    public final char PRIMITIVE;
    private final boolean NEGATE;
    private final boolean EOF;
    private final boolean SOF;

    public SkaldPrim(char prim) {
        PRIMITIVE = prim;
        EMP = false;
        DOT = false;
        NEGATE = false;
        EOF = false;
        SOF = false;
    }
    public SkaldPrim(boolean emp, boolean dot, boolean eof) {
        PRIMITIVE = 0;
        DOT = dot;
        EMP = emp;
        NEGATE = false;
        EOF = eof;
        SOF = false;
    }

    public SkaldPrim(char primitive, boolean negate) {
        PRIMITIVE = primitive;
        DOT = false;
        EMP = false;
        NEGATE = negate;
        EOF = false;
        SOF = false;
    }

    public RegEx reduce() {
        return this;
    }

    public void printStructure(int level) {
        for (int i = 0; i < level; i++) {
            System.out.print("\t");
        }
        System.out.println("PRIMITIVE: " + PRIMITIVE + ", EMP: " + EMP + ", DOT: " + DOT + ", NEGATE: " + NEGATE);
    }
    public StringBuilder generateString() {
        StringBuilder sb = new StringBuilder();
        if (DOT) {
            return sb.append("\\.");
        }
        if (EMP) {
            return sb.append("[:EMPTY:]");
        }
        if (SOF) {
            return sb.append("^");
        }
        if (EOF) {
            return sb.append("$");
        }
        switch (PRIMITIVE) {
            case '\n':
                sb.append("\\n");
                break;
            case '\r':
                sb.append("\\r");
                break;
            case '\t':
                sb.append("\\t");
                break;
            case '(':
                sb.append("\\(");
                break;
            case ')':
                sb.append("\\)");
                break;
            case '[':
                sb.append("\\]");
                break;
            case ']':
                sb.append("\\]");
                break;
            case '{':
                sb.append("\\{");
                break;
            case '}':
                sb.append("\\}");
                break;
            default:
                sb.append(PRIMITIVE);
                break;
        }
        return sb;
    }
    public NFA generateNFA() {
        Lausavisa nfa = new Lausavisa(this);
        nfa.addStringToTerminal(this.generateString().toString());
        return nfa;
    }
    public RegEx reverse() {
        return this;
    }

    public boolean equals(Object o) {
        //TODO utilize this to match parallel streams in the RegEx
        return o instanceof RegEx && o instanceof SkaldPrim && EMP == ((SkaldPrim) o).EMP && DOT == ((SkaldPrim) o).DOT && EOF == ((SkaldPrim) o).EOF && SOF == ((SkaldPrim) o).SOF && PRIMITIVE == ((SkaldPrim) o).PRIMITIVE;
    }
    public int hashCode() {
        return EOF ? 258 : (SOF ? 257 : (EMP ? 256 : (DOT ? 257 : PRIMITIVE)));
    }
}
