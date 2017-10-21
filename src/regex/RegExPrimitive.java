package regex;

import java.util.HashMap;
import java.util.Map;

public class RegExPrimitive implements RegEx {
    private static Map<Character, Map<Byte, RegExPrimitive>> pool = new HashMap<>();

    private static final byte MASK_EMP = 0;
    private static final byte MASK_DOT = 1;
    private static final byte MASK_NEG = 2;
    private static final byte MASK_EOF = 4;
    private static final byte MASK_SOF = 8;

    private final char CHARA;
    private final byte STATE;

    public static RegExPrimitive getRegExPrim(char prim) {
        return getRegExPrim(prim, (byte) 0);
    }
    public static RegExPrimitive getRegExPrim(boolean emp, boolean dot, boolean eof) {
        byte temp = (byte) ((dot ? MASK_DOT : 0) + (emp ? MASK_EMP : 0) + (eof ? MASK_EOF : 0));
        return getRegExPrim((char) 0, temp);
    }
    public static RegExPrimitive getRegExPrim(char primitive, boolean negate) {
        return getRegExPrim(primitive, negate ? MASK_NEG : 0);
    }
    public static RegExPrimitive getRegExPrim(char prim, byte state) {
        if (!pool.containsKey(prim)) {
            pool.put(prim, new HashMap<>());
        }
        Map<Byte, RegExPrimitive> intern = pool.get(prim);
        if (!intern.containsKey(state)) {
            intern.put(state, new RegExPrimitive(prim, state));
        }
        return intern.get(state);
    }
    private RegExPrimitive(char prim, byte state) {
        this.CHARA = prim;
        this.STATE = state;
    }

    public RegEx reduce() {
        return this;
    }
    public NFA generateNFA() {
        NFA nfa = new Lausavisa(this);
        nfa.addStringToTerminal(this.generateString().toString());
        return nfa;
    }
    public RegEx reverse() {
        return this;
    }

    public void printStructure(int level) {
        for (int i = 0; i < level; i++) {
            System.out.print("\t");
        }
        System.out.println("CHARA: " + CHARA
                + ", EMP: " + ((STATE & MASK_EMP) == 0)
                + ", DOT: " + ((STATE & MASK_DOT) == 0)
                + ", NEGATE: " + ((STATE & MASK_NEG) == 0));
    }
    public StringBuilder generateString() {
        StringBuilder sb = new StringBuilder();
        if ((STATE & MASK_DOT) != 0) {
            return sb.append("\\.");
        }
        if ((STATE & MASK_EMP) != 0) {
            return sb.append("[:EMPTY:]");
        }
        if ((STATE & MASK_SOF) != 0) {
            return sb.append("^");
        }
        if ((STATE & MASK_EOF) != 0) {
            return sb.append("$");
        }
        switch (CHARA) {
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
                sb.append(CHARA);
                break;
        }
        return sb;
    }

    public boolean equals(Object o) {
        return o instanceof RegExPrimitive && STATE == ((RegExPrimitive) o).STATE && CHARA == ((RegExPrimitive) o).CHARA;
    }
    public int hashCode() {
        return (STATE == 0 ? 0 : (STATE + 256)) + CHARA;
    }

    public char getChar() {
        return CHARA;
    }
}
