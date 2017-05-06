package midgard;

import vanaheimr.ReduceReduceException;
import vanaheimr.ShiftReduceException;
import yggdrasil.Branch;
import yggdrasil.TagPriority;
import yggdrasil.TagRecord;
import yggdrasil.Yggdrasil;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

/**
 * The list of rules for the parser and keeps the parser in check
 * Named after the Norse goddess Skadi, now a Hunter of the most obscure nonterminals.
 * Provides a rulebook for Jormungandr to create ordered chaos (aka an AST).
 */
public class Skadi {
    private static final Set<SkadiType> SUPPORTED_PARSER_CLASSES = new HashSet<>(Arrays.asList(
            SkadiType.LR0, SkadiType.SLR
    ));
    //Background information
    private CFG cfg;
    private SkadiType type;
    private Yggdrasil parent;
    //LR table data
    private List<LRNode> graph;
    private List<Map<Integer, Integer>> lrTransitionTable;

    //Initialization methods
    public Skadi(Yggdrasil parent, String inputFile) {
        this.parent = parent;
        //Read Configuration
        String parserConfig;
        try {
            parserConfig = new String(Files.readAllBytes(Paths.get(inputFile)), StandardCharsets.UTF_8).trim();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Input file incorrect.");
        }
        type = SkadiType.LR0;
        if (parserConfig.startsWith(">PARSER_CLASS")) {
            String input = parserConfig.substring(0, parserConfig.indexOf("\n"));
            parserConfig = parserConfig.substring(input.length()).trim();
            input = input.trim();
            String[] split = input.split("\\s+");
            if (split.length != 2) {
                System.out.println("Syntax error in grammar declaration. Expected \">PARSER_CLASS TYPE\", found" + input);
            } else {
                switch (split[1]) {
                    case "LR0":
                        type = SkadiType.LR0;
                        break;
                    case "SLR":
                        type = SkadiType.SLR;
                        break;
                    case "LALR":
                        type = SkadiType.LALR;
                        break;
                    case "LR1":
                        type = SkadiType.LR1;
                        break;
                    case "GLR":
                        type = SkadiType.GLR;
                        break;
                    default:
                        System.out.println("Unrecognized requested parser class.");
                }
            }
            if (!SUPPORTED_PARSER_CLASSES.contains(type)) {
                throw new RuntimeException("Extended parser classes unsupported.");
            }
        }
        cfg = new CFG(parent, parserConfig);
        this.generateParser();
        //Show results
        printGraph();
        printLRTransTable();
    }
    //LR table building
    private void generateParser() {
        switch (type) {
            case GLR:
            case LR1:
            case LALR:
            default:
                System.out.println("Unsupported.");
                return;
            case LR0:
                System.out.println("Constructing LR(0) table.");
                break;
            case SLR:
                System.out.println("Constructing SLR table.");
                break;
        }
        ruleExpansion();
        switch (type) {
            case GLR:
            case LR1:
            case LALR:
            default:
                System.out.println("Unsupported.");
                return;
            case LR0:
                System.out.println("LR(0) transition table filled.");
                break;
            case SLR:
                System.out.println("SLR transition table filled.");
                break;
        }
    }
    private void ruleExpansion() {
        if (!SUPPORTED_PARSER_CLASSES.contains(type)) {
            System.out.println("Unsupported");
            throw new RuntimeException("Unsupported parser class.");
        }
        List<CFGRule> list = cfg.fetchRulesForLeft(cfg.fetchZeroInstruction());
        if (list.size() != 1) {
            throw new RuntimeException("The starting rule, if not automatically defined, must only have one expansion.");
        }
        LRNode handle = new LRNode(new LRState(0, list.get(0), parent,
                parent.tagEncode(TagRecord.EOF_LABEL, TagPriority.SUB)), parent);
        graph = new ArrayList<>();
        graph.add(handle);
        processGraph(handle);
        populateTransitionTable();
        for (LRNode node : graph) {
            processGraph(node);
        }
    }
    private void processGraph(LRNode node) {
        closure(node);
        advance(node);
    }
    private void closure(LRNode node) {
        switch (type) {
            case GLR:
            case LR1:
            case LALR:
            default:
                System.out.println("Unsupported.");
                return;
            case LR0:
            case SLR:
                Set<LRState> states = node.fetchStates();
                Set<LRState> genStates = new HashSet<>();
                boolean done = false;
                while (!done) {
                    done = true;
                    for (LRState state : states) {
                        if (!state.isAtEnd() && !parent.isTerminal(state.getNext())) {
                            List<CFGRule> rules = cfg.fetchRulesForLeft(state.getNext());
                            for (CFGRule rule : rules) {
                                LRState reduplicatedState = (new LRState(0, rule, parent));
                                if (!node.hasRuleState(reduplicatedState) && !genStates.contains(reduplicatedState)) {
                                    genStates.add(reduplicatedState);
                                }
                            }
                        }
                    }
                    if (!genStates.isEmpty()) {
                        states = genStates;
                        genStates = new HashSet<>();
                        node.addRuleStates(states);
                        done = false;
                    }
                }
                break;
        }
    }
    private void advance(LRNode node) {
        switch (type) {
            case LALR:
            case LR1:
            case GLR:
            default:
                System.out.println("Unsupported.");
                return;
            case LR0:
            case SLR:
                Set<LRState> states = node.fetchStates();
                Map<Integer, Set<LRState>> transitions = new HashMap<>();
                for (LRState state : states) {
                    if (!state.isAtEnd()) {
                        LRState nextState = state.duplicate();
                        nextState.advance();
                        if (!transitions.containsKey(nextState.getLast())) {
                            transitions.put(nextState.getLast(), new HashSet<>());
                        }
                        transitions.get(nextState.getLast()).add(nextState);
                    }
                }
                Map<Integer, LRNode> filteredNodes = new HashMap<>();
                for (Integer x : transitions.keySet()) {
                    LRNode block = new LRNode(transitions.get(x), parent);
                    closure(block);
                    if (!graph.contains(block)) {
                        graph.add(block);
                        filteredNodes.put(x, block);
                        node.addTransition(x, block);
                    } else {
                        node.addTransition(x, graph.get(graph.indexOf(block)));
                    }
                }
                for (Integer transition : filteredNodes.keySet()) {
                    advance(filteredNodes.get(transition));
                }
                break;
        }
    }
    private void populateTransitionTable() {
        int maxQuant;
        int finalityTriggered = 0;
        switch (type) {
            case GLR:
            case LR1:
            case LALR:
            default:
                System.out.println("Unsupported.");
                return;
            case SLR:
                lrTransitionTable = new ArrayList<>(graph.size());
                for (int i = 0; i < graph.size(); i++) {
                    LRNode node = graph.get(i);
                    Map<Integer, LRNode> outEdges = node.getOutEdges();
                    lrTransitionTable.add(new HashMap<>());
                    Map<Integer, Integer> stateRow = lrTransitionTable.get(i);
                    for (Integer k : outEdges.keySet()) {
                        if (parent.isTerminal(k)) {
                            //Shift
                            stateRow.put(k, graph.size() + graph.indexOf(outEdges.get(k)));
                        } else {
                            //Goto
                            stateRow.put(k, graph.indexOf(outEdges.get(k)));
                        }
                    }
                    for (LRState state : node.fetchStates()) {
                        //Add reduces and accepts
                        boolean isFinal = graph.get(i).isEndNode();
                        if (state.isAtEnd()) {
                            for (Integer k : cfg.getFollowSet(state.getRule().getLeft())) {
                                Integer out = stateRow.put(k, graph.size() * 3 + cfg.encodeRule(state.getRule()));
                                if (out != null) { //Error
                                    System.out.println("SLR table generation error.");
                                    System.out.println("The provided grammar is not SLR parsable");
                                    if (out >= 3*graph.size()) {
                                        throw new ReduceReduceException("Reduce reduce exception with rules " +
                                                state.getRule() + " and " +
                                                cfg.decodeRule(out - 3*graph.size()) +
                                                " in node:\n" + node
                                        );
                                    } else {
                                        throw new ShiftReduceException("Shift reduce exception in node:\n" + node);
                                    }
                                }
                            }
                            if (isFinal) {
                                stateRow.put(parent.tagEncode(TagRecord.EOF_LABEL, TagPriority.SUB), -1);
                                finalityTriggered = 1;
                            }
                        }
                    }
                }
                break;
            case LR0:
                finalityTriggered = 0;
                lrTransitionTable = new ArrayList<>(graph.size());
                maxQuant = parent.tagCount();
                for (int i = 0; i < graph.size(); i++) {
                    lrTransitionTable.add(new HashMap<>(maxQuant));
                    boolean isFinal = graph.get(i).isEndNode();
                    boolean isReduce = false;
                    for (LRState state : graph.get(i).fetchStates()) {
                        if (state.isAtEnd()) {
                            if (!isReduce) {
                                for (int k = 0; k < parent.terminalCount(); k++) {
                                    lrTransitionTable.get(i).put(k, graph.size() * 3 + cfg.encodeRule(state.getRule()));
                                }
                                isReduce = true;
                            } else {
                                //Reduce reduce error
                                System.out.println();
                                System.out.println("Reduce-reduce error at the following node:\n" + graph.get(i));
                                System.out.println();
                                System.exit(-6);
                            }
                        }
                    }
                    for (Integer trans : graph.get(i).getOutEdges().keySet()) {
                        if (parent.isTerminal(trans)) {
                            if (isReduce) {
                                //Shift reduce error
                                System.out.println();
                                System.out.println("Shift-reduce error at the following node:\n" + graph.get(i));
                                System.out.println();
                                //System.exit(-5);
                                Integer oldReduce = lrTransitionTable.get(i).get(trans) - 3*graph.size();
                                lrTransitionTable.get(i).put(trans,
                                        graph.indexOf(graph.get(i).getOutEdges().get(trans)) +
                                        ((3+oldReduce) * graph.size()) + parent.tagCount());
                            } else {
                                //Shift
                                lrTransitionTable.get(i).put(trans,
                                        graph.indexOf(graph.get(i).getOutEdges().get(trans)) + graph.size());
                            }
                        } else {
                            //Non action
                            lrTransitionTable.get(i).put(trans, graph.indexOf(graph.get(i).getOutEdges().get(trans)));
                        }
                    }
                    if (isFinal) {
                        //Accept
                        lrTransitionTable.get(i).put(parent.tagEncode(TagRecord.EOF_LABEL, TagPriority.SUB), -1);
                        finalityTriggered = 1;
                    }
                }
        }
        if (finalityTriggered == 0) {
            System.out.println("The final accept state was... never reached? Shutting down.");
            System.exit(-1);
        }
    }
    private void printGraph() {
        System.out.println("/************************LR Transition Graph************************/");
        System.out.println("Total node count: " + graph.size());
        for (int i = 0; i < graph.size(); i++) {
            System.out.println("Node " + i);
            System.out.println(graph.get(i));
        }
        System.out.println("/******************************************************************/");
    }
    private void printLRTransTable() {
        System.out.println("/************************LR Translation Table**********************/");
        switch (type) {
            case LALR:
            case LR1:
            case GLR:
            default:
                System.out.println("Unsupported.");
                break;
            case LR0:
            case SLR:
                System.out.println("Rules: ");
                for (int i = 0; i < cfg.getRuleCount(); i++) {
                    System.out.println("Rules " + i + ": " + cfg.decodeRule(i));
                }
                System.out.print("state");
                for (int i = 0; i < parent.tagCount(); i++) {
                    System.out.printf("\t\t%03d", i);
                }
                System.out.println();
                for (int i = 0; i < graph.size(); i++) {
                    System.out.printf("s%02d\t\t", i);
                    for (int j = 0; j < parent.tagCount(); j++) {
                        Integer value = lrTransitionTable.get(i).getOrDefault(j, -50);
                        if (value < -1) {
                            System.out.print("\t\t");
                        }  else if (value < 0) {
                            System.out.print("\ta\t");
                        } else if (value < graph.size()) {
                            System.out.printf("\t%3d\t", value);
                        } else if (value < graph.size() * 2) {
                            System.out.printf("\ts%3d", value - graph.size());
                        } else if (value < graph.size() * 3) {
                            //Something about a conflict here
                        } else if (value < graph.size() * 3 + parent.tagCount()) { // reduce
                            System.out.printf("\tr%3d", value - 3 * graph.size());
                        } else {
                            //Conflicts
                            System.out.printf("\t%2d,%2d", (value - 3 * graph.size() - parent.tagCount())%graph.size(),
                                    (value - 3 * graph.size() - parent.tagCount())/graph.size());
                        }
                    }
                    System.out.println();
                }
        }
        System.out.println("/******************************************************************/");
    }

    //LR parsing stage
    public Integer progressAndEncode(Integer state, Branch curr, Branch next) {
        Integer currTag = (curr == null ? parent.tagEncode(TagRecord.EOF_LABEL, TagPriority.SUB) : curr.getTag());
        Map<Integer, Integer> transitionRow = lrTransitionTable.get(state);
        while (transitionRow.containsKey(currTag) &&
                transitionRow.get(currTag) < graph.size() &&
                transitionRow.get(currTag) >= 0) {
            transitionRow = lrTransitionTable.get(transitionRow.get(currTag));
        }
        return transitionRow.getOrDefault(currTag, -50);
    }
    public boolean isReduce(Integer encoding) {
        return encoding >= graph.size() * 3;
    }
    public CFGRule reduceRule(Integer encoding, Branch curr, Branch next) {
        Object currTag = (curr == null ? "%EOF" : curr);
        if (parent.DEBUG) {
            System.out.print("Rule reduce: encoding[" + encoding + "], current tag[" + currTag + "]");
            System.out.println(cfg.decodeRule(encoding - 3 * graph.size()));
        }
        return cfg.decodeRule(encoding - 3*graph.size());
    }
    public Integer reduceProgress(Integer state, Branch nextBranch) {
        return lrTransitionTable.get(state).get(nextBranch.getTag());
    }
    public boolean isShift(Integer encoding) {
        return encoding >= graph.size() && encoding < graph.size() * 2;
    }
    public boolean isComplete(Integer encoding, Branch curr) {
        return encoding == -1 && curr.getTag() == parent.tagEncode(TagRecord.EOF_LABEL, TagPriority.SUB);
    }
    public Integer decode(Integer encoding) {
        if (encoding >= graph.size() * 3 || encoding < 0) {
            throw new RuntimeException("State encodings must be between 0 and three times the number of nodes.");
        }
        return encoding % graph.size();
    }
}
