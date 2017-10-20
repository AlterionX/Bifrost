package bragi;

import bragi.bragi.skaldparts.*;

import java.util.List;

public class Skald implements RegEx {
    private RegEx core;
    private final String pattern;
    private NFA nfa;
    private DFA dfa;

    private final String alph;

    public Skald(String input, String alph) {
        this.alph = alph;
        this.input = input;
        pattern = input;
        mark = 0;

        core = regex();
        core = reduce();
        if (core == null) {
            throw new IllegalStateException("Provided regex reduces to nothing.");
        }
        setNFA(generateNFA().tablify());
        setDFA(getNFA().generateDFA().minimize());
        dfa = dfa.minimize(); //TODO

        //System.out.println(generateString());
        //nfa.printTable();
        //dfa.printTable();
    }

    /* Regex generation primitives and storage variables */
    private final String input;
    private int mark;
    private char peek() {
        return input.charAt(mark);
    }
    private char eat(char expect) {
        assert input.charAt(mark) == expect : "Incorrect regex. Expected " + expect + " received " + peek() + ".";
        mark++;
        return expect;
    }
    private char next() {
        return eat(peek());
    }
    private boolean hasMore() {
        return mark != input.length()   ;
    }

    /* Reducing potentially overly complex regex */
    public RegEx reduce() {
        return core.reduce();
    }

    /* General down-recurse regex structure building */
    private RegEx regex() {
        SkaldChoice opt = new SkaldChoice();
        opt.addChoice(term());
        while (hasMore() && peek() == '|') {
            eat('|');
            opt.addChoice(regex());
        }
        return opt;
    }
    //regex|regex...
    private RegEx term() {
        SkaldSequence sequence = new SkaldSequence();
        while (hasMore() && !(peek() == '|') && !(peek() == ')')) {
            sequence.add(factor());
        }
        return sequence;
    }
    //regex*, regex+,{num,num}
    private RegEx factor() {
        RegEx base = base(true);
        SkaldChoice tempChoice;
        SkaldSequence tempSeq;
        while (hasMore() && (peek() == '*' || peek() == '+' || peek() == '?' || peek() == '{')) {
            switch (peek()) {
                case '*':
                    eat('*');
                    base = new SkaldKleene(base);
                    break;
                case '+':
                    eat('+');
                    tempSeq = new SkaldSequence();
                    tempSeq.add(base);
                    tempSeq.add(new SkaldKleene(base));
                    base = tempSeq;
                    tempSeq = null;
                    break;
                case '{':
                    eat('{');
                    int numOne = 0;
                    while (peek() != '}' && peek() != ',') {
                        if (peek() > '9' || peek() < '0') {
                            throw new RuntimeException("Curly brackets... Something's wrong with the numbers.");
                        }
                        numOne *= 10;
                        numOne += next() - '0';
                    }
                    if (peek() == '}') {
                        eat('}');
                        //Fixed quantity
                        tempSeq = new SkaldSequence();
                        for (int i = 0; i < numOne; ++i) tempSeq.add(base);
                        base = tempSeq;
                        tempSeq = null;
                        break;
                    }
                    eat(',');
                    if (peek() == '}') {
                        //At least
                        tempSeq = new SkaldSequence();
                        for (int i = 0; i < numOne; ++i) tempSeq.add(base);
                        tempSeq.add(new SkaldKleene(base));
                        base = tempSeq;
                        tempSeq = null;
                        break;
                    }
                    int numTwo = 0;
                    while (peek() != '}') {
                        if (peek() > '9' || peek() < '0') {
                            throw new RuntimeException("Curly brackets... Something's wrong with the numbers.");
                        }
                        numTwo *= 10;
                        numTwo += next() - '0';
                    }
                    eat('}');
                    tempSeq = new SkaldSequence();
                    for (int i = 0; i < numOne; ++i) tempSeq.add(base);
                    SkaldChoice segment = new SkaldChoice();
                    if (numTwo < numOne) {
                        throw new RuntimeException("Exceptions aglore. indexes off.");
                    }
                    for (int i = 0; i < numTwo - numOne + 1; i++) {
                        if (i != 0) {
                            SkaldSequence extension = new SkaldSequence();
                            for (int j = 0; j < i; j++) {
                                extension.add(base);
                            }
                            segment.addChoice(extension);
                        } else {
                            segment.addChoice(new SkaldPrim(true, false, false));
                        }
                    }
                    tempSeq.add(segment);
                    base = tempSeq;
                    tempSeq = null;
                    break;
                case '?':
                    eat('?');
                    tempChoice = new SkaldChoice();
                    tempChoice.addChoice(new SkaldPrim(true, false, false));
                    tempChoice.addChoice(base);
                    base = tempChoice;
                    tempChoice = null;
                    break;
            }
        }
        return base;
    }
    //[stuff here as a range],[^]
    private RegEx range() {
        SkaldChoice opt = new SkaldChoice();
        boolean negate = false;
        if (peek() == '^') {
            eat('^');
            char[] data = alph.toCharArray();
            while (peek() != ']') {
                RegEx comp1 = base(false);
                char head = ((SkaldPrim) comp1).PRIMITIVE;
                data[alph.indexOf(head)] = 0;
                if (peek() == '-') {
                    eat('-');
                    RegEx comp2 = base(false);
                    char tail = ((SkaldPrim) comp2).PRIMITIVE;
                    if (alph.indexOf(head) > alph.indexOf(tail)) throw new RuntimeException("Illegal state of range start greater than range end.");
                    for (int i = alph.indexOf(head); i < alph.indexOf(tail); i++) {
                        data[i] = 0;
                    }
                }
            }
            for (int i = 0; i < data.length; i++) {
                if (data[i] != 0) {
                    opt.addChoice(new SkaldPrim(alph.charAt(i)));
                }
            }
        } else {
            while (peek() != ']') {
                opt.addChoice(base(false));
                char head = ((SkaldPrim) opt.fetchChoice(opt.choiceCount() - 1)).PRIMITIVE;
                if (peek() == '-') {
                    eat('-');
                    opt.addChoice(base(false));
                    char tail = ((SkaldPrim) opt.fetchChoice(opt.choiceCount() - 1)).PRIMITIVE;
                    if (head > tail) throw new RuntimeException("Illegal state of range start greater than range end.");
                    for (char i = (char) (head + 1); i < tail; i++) {
                        opt.addChoice(new SkaldPrim(i, negate));
                    }
                }
            }
        }
        return opt;
    }
    //[], (), (?!), \c, ., (<=), (?=), (<!), (<=), ...
    private RegEx base(boolean complexAvail) {
        RegEx regex;
        switch (peek()) {
            case '[':
                if (complexAvail) {
                    next();
                    regex = range();
                    eat(']');
                    return regex;
                }
                throw new RuntimeException("Invalid location at " + mark);
            case '(':
                if (complexAvail) {
                    next();
                    if (peek() == '?') { //Generally not implemented
                        eat('?');
                        if (peek() == '!') {
                            eat('!');
                            regex = new SkaldNegativeLookahead(regex());
                        } else if (peek() == '=') {
                            eat('=');
                            regex = new SkaldPositiveLookahead(regex());
                        } else if (peek() == '<') { //Look behind
                            eat('<');
                            if (peek() == '!') {
                                next();
                                regex = new SkaldNegativeLookbehind(regex());
                            } else if (peek() == '=') {
                                next();
                                regex = new SkaldPositiveLookbehind(regex());
                            } else {
                                throw new RuntimeException("Incorrect regex expression.");
                            }
                        } else {
                            throw new RuntimeException("Incorrect regex expression.");
                        }
                    } else {
                        regex = regex();
                    }
                    eat(')');
                    return regex;
                }
                throw new RuntimeException("Invalid location at " + mark);
            case '.':
                if (complexAvail) {
                    next();
                    return new SkaldPrim(false, true, false);
                }
                return new SkaldPrim(next());
            case '$':
                if (complexAvail) {
                    next();
                    return new SkaldPrim(false, false, true);
                }
                return new SkaldPrim(next());
            case '\\':
                next();
                switch (peek()) {
                    case 'n':
                        next();
                        return new SkaldPrim('\n');
                    case 'r':
                        next();
                        return new SkaldPrim('\r');
                    case 't':
                        next();
                        return new SkaldPrim('\t');
                    default:
                        return new SkaldPrim(next());
                }
            default:
                return new SkaldPrim(next());
        }
    }

    /* Fetch source pattern */
    public String getPattern() {
        return pattern;
    }

    /* Utilize regex interface */
    public void printStructure(int level) {
        for (int i = 0; i < level; i++) {
            System.out.print("\t");
        }
        core.printStructure(level);
    }
    public StringBuilder generateString() {
        return (new StringBuilder()).append(core.generateString());
    }
    public NFA generateNFA() {
        if (core == null) {
            return new Lausavisa();
        }
        return core.generateNFA();
    }
    public RegEx reverse() {
        core.reverse();
        return this;
    }

    public void setNFA(NFA nfa) {
        this.nfa = nfa;
    }
    public NFA getNFA() {
        return nfa;
    }
    public void setDFA(DFA dfa) {
        this.dfa = dfa;
    }
    public DFA getDFA() {
        return dfa;
    }

    public List<Integer> match(String check) {
        return match(check, 0);
    }
    public List<Integer> match(String check, int start) {
        return nfa.process(check, start);
    }

    public void compile() {
        setNFA(generateNFA().tablify());
        setDFA(getNFA().generateDFA().minimize());
    }
}
