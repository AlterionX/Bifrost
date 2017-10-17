package midgard;

import logger.Log;
import tagtable.Tag;
import tagtable.TagPriority;
import tagtable.TagTable;
import vanaheimr.ReduceReduceException;
import vanaheimr.ShiftReduceException;
import yggdrasil.*;

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
public class Skadi extends Cosmos {
    private static final Set<SkadiType> SUPPORTED_PARSER_CLASSES = new HashSet<>(Arrays.asList(
            SkadiType.LR0, SkadiType.SLR
    ));
    //Stable fields
    //CFG and parser info
    private CFG cfg;
    private SkadiType type;
    //LR table/graph data
    private List<LRNode> graph;
    private List<Map<Tag, Integer>> lrTransitionTable;

    /**
     * Initialize Skadi.
     * @param context The context data, AST, and symtable.
     */
    public Skadi(PathHolder context, TagTable tagTable) {
        super(context, tagTable);
        System.out.println("Skadi configured.");
    }
    /**
     * Configure Skadi and the CFG sub-component.
     */
    protected void configure() {
        //Read Configuration
        String parserConfig;
        try {
            parserConfig = new String(Files.readAllBytes(Paths.get(
                    getContext().BASE_DIR + getContext().TARGET + getContext().PARSER_DEC_EXTENSION
            )), StandardCharsets.UTF_8).trim();
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
                System.out.println("Syntax error in grammar declaration. Expected \">PARSER_CLASS TYPE\", found \"" + input + "\"");
            } else {
                try {
                    type = SkadiType.valueOf(split[1]);
                } catch (IllegalArgumentException e) {
                    System.out.println("Unrecognized requested parser classe.");
                }
            }
            if (!SUPPORTED_PARSER_CLASSES.contains(type)) {
                throw new RuntimeException("Extended parser classes unsupported.");
            }
        }
        cfg = new CFG(getTagTable(), parserConfig);
        this.generateParser();
        //Show results
        if (getContext().DEBUG) printGraph();
        printLRTransTable();
    }

    //LR table building
    /**
     * Launch the parser generation.
     */
    private void generateParser() {
        switch (type) {
            case LR0:
                System.out.println("Constructing LR(0) table.");
                break;
            case SLR:
                System.out.println("Constructing SLR table.");
                break;
            case GLR:
            case LR1:
            case LALR:
            default:
                System.out.println("Unsupported.");
                return;
        }
        productionExpansion();
        switch (type) {
            case LR0:
                System.out.println("LR(0) transition table filled.");
                break;
            case SLR:
                System.out.println("SLR transition table filled.");
        }
    }
    /**
     * Expand the first production into a production state, then a node,
     * and launch the process of producing a graph with that node as the initial node.
     */
    private void productionExpansion() {
        if (!SUPPORTED_PARSER_CLASSES.contains(type)) {
            System.out.println("Unsupported");
            throw new RuntimeException("Unsupported parser class.");
        }
        List<CFGProduction> list = cfg.fetchRulesForLeft(cfg.fetchZeroInstruction());
        if (list.size() != 1) {
            throw new RuntimeException("The starting rule, if not automatically defined, must only have one expansion.");
        }
        LRNode handle = new LRNode(
                new LRState(0, list.get(0), getTagTable(), getTagTable().EOF_TAG),
                getTagTable()
        );
        graph = new ArrayList<>();
        graph.add(handle);
        processGraph(handle);
        populateTransitionTable();
        for (LRNode node : graph) {
            processGraph(node);
        }
    }
    /**
     * Process a single node. However, this also launches all the processing on any produced sub-nodes,
     * ultimately resulting in a full graph.
     * @param node The initial node.
     */
    private void processGraph(LRNode node) {
        closure(node);
        advance(node);
    }
    /**
     * Run a closure operation, where the node is filled out by all production state equivalents.
     * @param node The node being put under closure.
     */
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
                        if (!state.isAtEnd() && !getTagTable().isTerminalTag(state.getNext())) {
                            List<CFGProduction> rules = cfg.fetchRulesForLeft(state.getNext());
                            for (CFGProduction rule : rules) {
                                LRState reduplicatedState = (new LRState(0, rule, getTagTable()));
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
    /**
     * Done after the closure of a node. Advance the node to the next children nodes. Produces a new set of
     * nodes to put under closure, merge, then advance.
     * @param node The node to advance.
     */
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
                Map<Tag, Set<LRState>> transitions = new HashMap<>();
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
                Map<Tag, LRNode> filteredNodes = new HashMap<>();
                for (Tag x : transitions.keySet()) {
                    LRNode block = new LRNode(transitions.get(x), getTagTable());
                    closure(block);
                    if (!graph.contains(block)) {
                        graph.add(block);
                        filteredNodes.put(x, block);
                        node.addTransition(x, block);
                    } else {
                        node.addTransition(x, graph.get(graph.indexOf(block)));
                    }
                }
                for (Tag transition : filteredNodes.keySet()) {
                    advance(filteredNodes.get(transition));
                }
                break;
        }
    }
    /**
     * Turn the graph into a simple table, indexed first by a node, then the input terminal or non-terminal.
     * The entries of the table are the states to transition to or actions to take.
     */
    private void populateTransitionTable() {
        int maxQuant;
        boolean finalityTriggered = false;
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
                    Map<Tag, LRNode> outEdges = node.getOutEdges();
                    lrTransitionTable.add(new HashMap<>());
                    Map<Tag, Integer> stateRow = lrTransitionTable.get(i);
                    for (Tag k : outEdges.keySet()) {
                        if (getTagTable().isTerminalTag(k)) {
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
                            for (Tag k : cfg.getFollowSet(state.getRule().getLeft())) {
                                Integer out = stateRow.put(k, graph.size() * 3 + cfg.encodeProduction(state.getRule()));
                                if (out != null) { //Error
                                    System.out.println("SLR table generation error.");
                                    System.out.println("The provided grammar is not SLR parsable");
                                    if (out >= 3*graph.size()) {
                                        throw new ReduceReduceException("Reduce reduce exception with rules " +
                                                state.getRule() + " and " +
                                                cfg.decodeProduction(out - 3*graph.size()) +
                                                " in node:\n" + node
                                        );
                                    } else {
                                        throw new ShiftReduceException("Shift reduce exception in node:\n" + node);
                                    }
                                }
                            }
                            if (isFinal) {
                                stateRow.put(getTagTable().addElseFindTag(TagPriority.LEX, TagTable.EOF_LABEL), -1);
                                finalityTriggered = true;
                            }
                        }
                    }
                }
                break;
            case LR0:
                finalityTriggered = false;
                lrTransitionTable = new ArrayList<>(graph.size());
                maxQuant = getTagTable().tagCount();
                for (int i = 0; i < graph.size(); i++) {
                    lrTransitionTable.add(new HashMap<>(maxQuant));
                    boolean isFinal = graph.get(i).isEndNode();
                    boolean isReduce = false;
                    for (LRState state : graph.get(i).fetchStates()) {
                        if (state.isAtEnd()) {
                            if (!isReduce) {
                                for (Tag t : getTagTable().fetchTags(TagPriority.PAR)) {
                                    lrTransitionTable.get(i).put(t, graph.size() * 3 + cfg.encodeProduction(state.getRule()));
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
                    for (Tag trans : graph.get(i).getOutEdges().keySet()) {
                        if (getTagTable().isTerminalTag(trans)) {
                            if (isReduce) {
                                //Shift reduce error
                                System.out.println();
                                System.out.println("Shift-reduce error at the following node:\n" + graph.get(i));
                                System.out.println();
                                //System.exit(-5);
                                Integer oldReduce = lrTransitionTable.get(i).get(trans) - 3*graph.size();
                                lrTransitionTable.get(i).put(trans,
                                        graph.indexOf(graph.get(i).getOutEdges().get(trans)) +
                                        ((3+oldReduce) * graph.size()) + getTagTable().tagCount());
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
                        lrTransitionTable.get(i).put(getTagTable().addElseFindTag(TagPriority.PAR, TagTable.EOF_LABEL), -1);
                        finalityTriggered = true;
                    }
                }
        }
        if (!finalityTriggered) {
            System.out.println("The final accept state was... never reached? Shutting down.");
            System.exit(-1);
        }
    }

    //LR parsing stage
    /**
     * Given the current state of the stream and stack, decide the next action.
     * @param state Current state.
     * @param curr The current terminal or non-terminal.
     * @param next The next terminal or non-terminal.
     * @return The next action, a shift or reduce.
     */
    public Integer progressAndEncode(Integer state, Branch curr, Branch next) {
        Tag currTag = (curr == null ? getTagTable().EOF_TAG : curr.getTag());
        Map<Tag, Integer> transitionRow = lrTransitionTable.get(state);
        while (transitionRow.containsKey(currTag) &&
                transitionRow.get(currTag) < graph.size() &&
                transitionRow.get(currTag) >= 0) {
            transitionRow = lrTransitionTable.get(transitionRow.get(currTag));
        }
        return transitionRow.getOrDefault(currTag, -50);
    }
    /**
     * Checks if the encoded action is a reduce.
     * @param encoding Encoded action.
     * @return If the encoded action is a reduce action.
     */
    public boolean isReduce(Integer encoding) {
        return encoding >= graph.size() * 3;
    }
    /**
     * Gets the production associated with a certain reduce.
     * @param encoding The encoded action.
     * @param curr The current top of the stack.
     * @param next The current next input.
     * @return The production to execute.
     */
    public CFGProduction getReduceProduction(Integer encoding, Branch curr, Branch next) {
        Object currTag = (curr == null ? TagTable.EOF_LABEL : curr);
        if (getContext().DEBUG) {
            System.out.print("Rule reduce: encoding[" + encoding + "], current tag[" + currTag + "]");
            System.out.println(cfg.decodeProduction(encoding - 3 * graph.size()));
        }
        return cfg.decodeProduction(encoding - 3*graph.size());
    }
    /**
     * Progress the tree to the next state after a reduce.
     * @param state The current node.
     * @param nextBranch The next input.
     * @return The new node.
     */
    public Integer reduceProgress(Integer state, Branch nextBranch) {
        return lrTransitionTable.get(state).get(nextBranch.getTag());
    }
    /**
     * Determine if the action encoded is a shift.
     * @param encoding The encoded action.
     * @return If it is a shift.
     */
    public boolean isShift(Integer encoding) {
        return encoding >= graph.size() && encoding < graph.size() * 2;
    }
    /**
     * Determines if an encoded action is conveying the completed parsing status.
     * @param encoding The encoded action.
     * @param curr the current label.
     * @return If it is a completed action or not.
     */
    public boolean isComplete(Integer encoding, Branch curr) {
        return encoding == -1 && curr.getTag() == getTagTable().EOF_TAG;
    }
    /**
     * Decodes the state an encoding encodes.
     * @param encoding The encoding.
     * @return The decoded state.
     */
    public Integer decode(Integer encoding) {
        if (encoding >= graph.size() * 3 || encoding < 0) {
            throw new RuntimeException("State encodings must be between 0 and three times the number of nodes.");
        }
        return encoding % graph.size();
    }

    /**
     * Print the graph of nodes.
     */
    private void printGraph() {
        System.out.println("/************************LR Transition Graph************************/");
        System.out.println("Total node count: " + graph.size());
        for (int i = 0; i < graph.size(); i++) {
            System.out.println("Node " + i);
            System.out.println(graph.get(i));
        }
        System.out.println("/******************************************************************/");
    }
    /**
     * Print the generated transition table. Tends to be cleaner and more concise than the graph.
     * Indexes may requiring referring to fully understand.
     */
    private void printLRTransTable() {
        Log.dln("/************************LR Translation Table**********************/");
        switch (type) {
            case LALR:
            case LR1:
            case GLR:
            default:
                Log.dln("Unsupported.");
                break;
            case LR0:
            case SLR:
                Log.dln("Rules: ");
                for (int i = 0; i < cfg.getProductionCount(); i++) {
                    Log.dln("Rules " + i + ": " + cfg.decodeProduction(i));
                }
                Log.d("state");
                for (int i = 0; i < getTagTable().tagCount(); i++) {
                    Log.df("\t\t%03d", i);
                }
                Log.dln("");
                for (int i = 0; i < graph.size(); i++) {
                    Log.df("s%02d\t\t", i);
                    for (Tag t : getTagTable().fetchAllTags()) {
                        Integer value = lrTransitionTable.get(i).getOrDefault(t, -50);
                        if (value < -1) {
                            Log.d("\t\t");
                        }  else if (value < 0) {
                            Log.d("\ta\t");
                        } else if (value < graph.size()) {
                            Log.df("\t%3d\t", value);
                        } else if (value < graph.size() * 2) {
                            Log.df("\ts%3d", value - graph.size());
                        } else if (value < graph.size() * 3) {
                            //Something about a conflict here
                        } else if (value < graph.size() * 3 + getTagTable().tagCount()) { // reduce
                            Log.df("\tr%3d", value - 3 * graph.size());
                        } else {
                            //Conflicts
                            Log.df("\t%2d,%2d", (value - 3 * graph.size() - getTagTable().tagCount())%graph.size(),
                                    (value - 3 * graph.size() - getTagTable().tagCount())/graph.size());
                        }
                    }
                    Log.dln("");
                }
        }
        Log.dln("/******************************************************************/");
    }
}
