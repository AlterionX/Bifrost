package regex;

import javafx.util.Pair;
import java.util.*;

public class Lausavisa implements NFA {
    private FSANode head;
    private FSANode terminal;

    private Map<FSANode, Map<RegExPrimitive, ArrayList<FSANode>>> table = null;
    private boolean tableIsStale = true;

    private static final String REGEX_TARG_ALPH = "\t\n\r !\"#$%&'()*+,-./" +
            "0123456789" +
            ":;<=>?@" +
            "ABCDEFGHIJKLMNOPQRSTUVWXYZ" +
            "[\\]^_`" +
            "abcdefghijklmnopqrstuvwxyz" +
            "{|}~";

    public Lausavisa() {}
    //Base element constructor
    public Lausavisa(RegExPrimitive single) {
        head = new Stef();
        terminal = new Stef();
        head.registerConnection(terminal, single);
    }
    public Lausavisa(Lausavisa shallowClone) {
        head = shallowClone.head;
        terminal = shallowClone.terminal;
    }
    //Look around constructor
    public Lausavisa(boolean negate, boolean reverse, DFA dfa) {
        head = new Stef();
        terminal = head;
        head.addLookAround(dfa, negate, reverse);
    }

    public static final int SIMPLE_CONCAT = 0;
    public static final int SIMPLE_BRANCH = 1;
    public static final int CONCAT_BRANCH = 2;

    /**
     * Merges two separate NFAs together.
     *
     * @param nfaTwo The NFA to merge into this one.
     * @param strategy The strategy used to merge NFAs.
     *                 SIMPLE_CONCAT is used to match the NFAs end to head. Effectively, the
     *                 terminal state of this NFA will be connect to the head of the second NFA.
     *                 SIMPLE_BRANCH is used to match the NFAs side by side. Effectively, a new head
     *                 is generated, and the original head of the two are mapped from that head to
     *                 the children. On the terminal end, a new terminal is created and the old terminals
     *                 mapped to the new terminal.
     *                 CONCAT_BRANCH is used to merge an additional branch when dealing with multiple branches.
     *                 The head of the current NFA will be mapped to the head of the second, while the
     *                 terminal of the second will be matched with the terminal of the first.
     * @return This NFA, having undergone these transformations
     */
    public void merge(NFA nfaTwo, int strategy) {
        tableIsStale = true;
        //System.out.println("Merging.");
        switch (strategy) {
            default:
            case SIMPLE_CONCAT:
                this.terminal.registerConnection(nfaTwo.getHead(), RegExPrimitive.getRegExPrim(true, false, false));
                this.terminal = nfaTwo.getTerminals().iterator().next();
                break;
            case SIMPLE_BRANCH:
                this.head = new Stef(this.head, RegExPrimitive.getRegExPrim(true, false, false));
                this.head.registerConnection(nfaTwo.getHead(), RegExPrimitive.getRegExPrim(true, false, false));
                FSANode temp = new Stef();
                this.terminal.registerConnection(temp, RegExPrimitive.getRegExPrim(true, false, false));
                nfaTwo.getTerminals().iterator().next().registerConnection(temp, RegExPrimitive.getRegExPrim(true, false, false));
                this.terminal = temp;
                break;
            case CONCAT_BRANCH:
                this.head.registerConnection(nfaTwo.getHead(), RegExPrimitive.getRegExPrim(true, false, false));
                nfaTwo.getTerminals().iterator().next().registerConnection(this.terminal, RegExPrimitive.getRegExPrim(true, false, false));
                break;
        }
    }
    /**
     * Wraps the nfa as a Kleene star would affect it.
     *
     * It creates a new head and maps it to the old head. It creates a new terminal, and maps the old
     * terminal to the new terminal. It then maps the old terminal to the old head.
     *
     * @return This NFA, having undergone these transformations
     */
    public NFA kleeneWrap() {
        tableIsStale = true;
        this.terminal.registerConnection(this.head, RegExPrimitive.getRegExPrim(true, false, false));
        this.head = new Stef(this.head, RegExPrimitive.getRegExPrim(true, false, false));
        FSANode nTerm = new Stef();
        this.terminal.registerConnection(nTerm, RegExPrimitive.getRegExPrim(true, false, false));
        this.terminal = nTerm;
        this.head.registerConnection(this.terminal, RegExPrimitive.getRegExPrim(true, false, false));
        return this;
    }

    /**
     * Create a more lookup efficient state transition table representation.
     * @return The current NFA, packaged with the transition table representation.
     */
    public NFA tablify() {
        if (!tableIsStale) return this;
        ArrayList<FSANode> nodes = new ArrayList<>();
        table = new HashMap<>();
        //System.out.println("BFSing NFA.");
        this.bfs(nodes, table);
        //System.out.println("BFSed NFA.");
        tableIsStale = false;
        return this;
    }
    /**
     * Traverses the NFA starting at the head node, populating both the list of nodes, as well as the
     * transition state table.
     *
     * @param nodes The list of nodes to fill.
     * @param table The transition state table to fill.
     */
    private void bfs(ArrayList<FSANode> nodes, Map<FSANode, Map<RegExPrimitive, ArrayList<FSANode>>> table) {
        Stack<FSANode> temp = new Stack<>();
        temp.push(this.head);
        Stack<FSANode> next = new Stack<>();
        int currIndex = 0;

        while (!temp.empty()) {
            //System.out.println("Current stack: " + temp);
            FSANode node = temp.empty() ? null : temp.peek();
            while (node != null) {
                if (node.getScratch() == 0) {
                    node.setScratch(1);
                    node.setIndex(currIndex);
                    currIndex++;
                    nodes.add(node);
                    table.putIfAbsent(node, new HashMap<>());

                    for (RegExPrimitive cond : node.possibleInputs()) {
                        table.get(node).putIfAbsent(cond, new ArrayList<>());
                        for (FSANode node2 : node.possibleTransitions(cond)) {
                            nodes.add(node2);
                            table.get(node).putIfAbsent(cond, new ArrayList<>());

                            next.push(node2);
                            table.get(node).get(cond).add(node2);
                        }
                    }
                }
                node = temp.empty() ? null : temp.pop();
            }
            Stack<FSANode> stackFlop = temp;
            temp = next;
            next = stackFlop;
        }
        //System.out.println("All nodes: " + nodes);
    }
    /**
     * Gets a shallow copy of the transition state table.
     *
     * @return The transition state table.
     */
    public Map<FSANode, Map<RegExPrimitive, ArrayList<FSANode>>> getTransitionTable() {
        if (tableIsStale) tablify();
        return table;
    }

    public void addStringToTerminal(String regexString) {
        terminal.addRegExString(regexString);
    }

    /**
     * Produce a DFA from this NFA.
     *
     * @return The produced DFA
     */
    public DFA generateDFA() {
        /* Prepare */
        if (tableIsStale) tablify();
        Map<FSANode, Pair<Set<FSANode>, Map<RegExPrimitive, FSANode>>> dfaTable = new HashMap<>();
        /* Iteration one, head node */
        Set<FSANode> initial = closure(
                new HashSet<>(Collections.singletonList(this.head)),
                RegExPrimitive.getRegExPrim(true, false, tableIsStale));
        initial.add(head);
        createNFADFATransitionNode(initial, dfaTable);
        /*Continue iteration*/
        boolean complete = false;
        while (!complete) {
            complete = true;
            for (FSANode dfaNode : dfaTable.keySet()) {
                if (dfaNode.getScratch() == 0) {
                    processUnaryNode(dfaNode, dfaTable.get(dfaNode).getKey(), dfaTable);
                    complete = false;
                    break;
                }
            }
        }
        /* Final creation of the DFA */
        DFA dfa = new Drapa();
        dfa.processNFADFAConversionTable(dfaTable, this.head, terminal);
        return dfa;
    }
    /**
     * Analyze all outputs from a single DFA node, and generate the corresponding DFA TARGET nodes.
     *
     * @param dfaNode The source DFA node.
     * @param nfaSub The NFA representation of the source node.
     * @param dfaTable The master NFA to DFA table.
     */
    private void processUnaryNode(FSANode dfaNode, Set<FSANode> nfaSub,
                                  Map<FSANode, Pair<Set<FSANode>, Map<RegExPrimitive, FSANode>>> dfaTable) {
        //Find all valid inputs to the DFA node
        Set<RegExPrimitive> possibleVals = new HashSet<>();
        for (FSANode node : nfaSub) {
            possibleVals.addAll(node.possibleInputs());
        }
        //Remove useless empty transitions (all empty transitions at this point should be internal
        possibleVals.remove(RegExPrimitive.getRegExPrim(true, false, tableIsStale));
        //Analyze each path
        for (RegExPrimitive prim : possibleVals) {
            //Simulate a move of type prim, followed by continuation to end
            Set<FSANode> nfaProgression = closure(nfaSub, prim);
            nfaProgression.addAll(closure(nfaProgression, RegExPrimitive.getRegExPrim(true, false, tableIsStale)));
            registerNFADFATransitionEntry(dfaTable, dfaNode, nfaProgression, prim);
        }
        //Mark the DFA node as processed, so do not lrConstruct again.
        dfaNode.setScratch(1);
    }
    /**
     * Marks an entry in the table from the provided DFA node to the DFA node represented by the set of NFA
     * nodes provided.
     *
     * @param dfaTable The master NFA to DFA transformation table.
     * @param dfaNode The source DFA node.
     * @param nfaProgression The TARGET NFA node set.
     * @param prim The input.
     * @return The DFA node representation of the NFA node set.
     */
    private void registerNFADFATransitionEntry(Map<FSANode, Pair<Set<FSANode>, Map<RegExPrimitive, FSANode>>> dfaTable,
                                               FSANode dfaNode, Set<FSANode> nfaProgression,
                                               RegExPrimitive prim) {
        //System.out.println("DFA Node " + dfaNode + ", with input " + prim.generateString() + ", connected to NFA node set " + nfaProgression);
        //Verify quality of nfaProgression set
        for (FSANode dfaModel : dfaTable.keySet()) {
            if (nfaProgression.size() != dfaTable.get(dfaModel).getKey().size()) continue;
            boolean found = true;
            for (FSANode nfaElement : dfaTable.get(dfaModel).getKey()) {
                if (!nfaProgression.contains(nfaElement)) {
                    found = false;
                    break;
                }
            }
            if (found) {
                //Old entry, mark transition and complete
                markNFADFATransitionEntry(dfaNode, dfaModel, prim, dfaTable);
                return;
            }
        }
        //Entirely new DFA node
        markNFADFATransitionEntry(dfaNode, createNFADFATransitionNode(nfaProgression, dfaTable), prim, dfaTable);
    }
    /**
     * Creates a node for the final DFA and places this in the table.
     * @param nfaRepresentation The NFA nodes that consist this DFA node.
     * @param dfaTable The master NFA to DFA transformation table.
     * @return The node placed into the table.
     */
    private FSANode createNFADFATransitionNode(Set<FSANode> nfaRepresentation,
                                               Map<FSANode, Pair<Set<FSANode>, Map<RegExPrimitive, FSANode>>> dfaTable) {
        //System.out.println("Insertion of DFA node representing " + nfaRepresentation);
        FSANode nextNode = new Stef();
        nextNode.setIndex(dfaTable.size());
        dfaTable.put(nextNode, new Pair<>(nfaRepresentation, new HashMap<>()));
        return nextNode;
    }
    /**
     * Marks a transition in the table.
     *
     * @param dfaNode The source node.
     * @param dfaModel The destination node.
     * @param prim The input value.
     * @param dfaTable The master NFA to DFA transformation table
     * @return The destination node.
     */
    private FSANode markNFADFATransitionEntry(FSANode dfaNode, FSANode dfaModel, RegExPrimitive prim,
                                              Map<FSANode, Pair<Set<FSANode>, Map<RegExPrimitive, FSANode>>> dfaTable) {
        FSANode node = dfaTable.get(dfaNode).getValue().put(prim, dfaModel);
        if (node != null) throw new RuntimeException("One source state in a DFA cannot" +
                " have multiple destinations under a single input.");
        return dfaModel;
    }

    /**
     * Find all nodes from the provided heads, that the given input would lead to.
     *
     * @param headNodes The set of starting nodes
     * @param prim The input value.
     * @return The set of nodes reached through the input
     */
    private Set<FSANode> closure(Set<FSANode> headNodes, RegExPrimitive prim) {
        for (FSANode node : table.keySet()) {
            node.setScratch(0);
        }
        //System.out.println("Finding closure of " + headNodes + " with input " + prim.generateString());
        Set<FSANode> nodeSet = new HashSet<>();
        for (FSANode node : headNodes) {
            node.setScratch(1);
            if (node.possibleInputs().contains(prim)) {
                nodeSet.addAll(node.possibleTransitions(prim));
            }
        }
        //System.out.println("\tInitial processing located " + nodeSet);
        boolean done = false;
        while (!done) {
            done = true;
            for (FSANode node : nodeSet) {
                if (node.getScratch() == 0) {
                    done = false;
                    node.setScratch(1);
                    if (node.possibleInputs().contains(prim)) {
                        nodeSet.addAll(node.possibleTransitions(prim));
                    }
                    break;
                }
            }
        }
        //System.out.println("\tNode set starting from " + headNodes + " utilising connection " + prim.generateString() + " results in:  " + nodeSet + ".");
        return nodeSet;
    }

    @Override
    public FSANode getHead() {
        return head;
    }

    @Override
    public Set<FSANode> getTerminals() {
        HashSet<FSANode> temp = new HashSet<>();
        temp.add(terminal);
        return temp;
    }

    public ArrayList<Integer> process(String check, int start) {
        Set<FSANode> temp = new HashSet<>();
        Set<FSANode> next = new HashSet<>();
        temp.add(head);
        ArrayList<Integer> success = new ArrayList<>();
        for (int i = start; i < check.length(); ++i) {
            boolean done = false;
            while (!done) {
                done = true;
                temp.addAll(closure(temp, RegExPrimitive.getRegExPrim(true, false, false)));
            }
            for (FSANode node : temp) {
                if (node == terminal) success.add(i);
                next.addAll(node.fetchNext(RegExPrimitive.getRegExPrim(check.charAt(i))));
            }
            //System.out.println("Next iteration: " + next);
            temp.clear();
            Set<FSANode> swap = next;
            next = temp;
            temp = swap;
        }
        for (FSANode node : temp) {
            if (node == terminal) success.add(check.length());
        }
        return success;
    }

    @Override
    public int processFirstReverse(String stream, int i) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int processFirst(String stream, int currLoc) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}