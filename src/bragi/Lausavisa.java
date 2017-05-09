package bragi;

import bragi.bragi.skaldparts.SkaldPrim;
import javafx.util.Pair;
import java.util.*;

public class Lausavisa {
    private Stef head;
    private Stef terminal;

    private Map<Stef, Map<SkaldPrim, ArrayList<Stef>>> table = null;
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
    public Lausavisa(SkaldPrim single) {
        head = new Stef();
        terminal = new Stef();
        head.registerConnection(terminal, single);
    }
    public Lausavisa(Lausavisa shallowClone) {
        head = shallowClone.head;
        terminal = shallowClone.terminal;
    }
    //Look around constructor
    public Lausavisa(boolean negate, boolean reverse, Drottkvaett dfa) {
        head = new Stef();
        terminal = head;
        head.visur.add(dfa);
        head.negateLookAround.add(negate);
        head.reverseLookAround.add(reverse);
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
    public Lausavisa merge(Lausavisa nfaTwo, int strategy) {
        tableIsStale = true;
        //System.out.println("Merging.");
        switch (strategy) {
            default:
            case SIMPLE_CONCAT:
                this.terminal.registerConnection(nfaTwo.head, new SkaldPrim(true, false, false));
                this.terminal = nfaTwo.terminal;
                break;
            case SIMPLE_BRANCH:
                this.head = new Stef(this.head, new SkaldPrim(true, false, false));
                this.head.registerConnection(nfaTwo.head, new SkaldPrim(true, false, false));
                Stef temp = new Stef();
                this.terminal.registerConnection(temp, new SkaldPrim(true, false, false));
                nfaTwo.terminal.registerConnection(temp, new SkaldPrim(true, false, false));
                this.terminal = temp;
                break;
            case CONCAT_BRANCH:
                this.head.registerConnection(nfaTwo.head, new SkaldPrim(true, false, false));
                nfaTwo.terminal.registerConnection(this.terminal, new SkaldPrim(true, false, false));
                break;
        }
        return this;
    }
    /**
     * Wraps the nfa as a Kleene star would affect it.
     *
     * It creates a new head and maps it to the old head. It creates a new terminal, and maps the old
     * terminal to the new terminal. It then maps the old terminal to the old head.
     *
     * @return This NFA, having undergone these transformations
     */
    public Lausavisa kleeneWrap() {
        tableIsStale = true;
        this.terminal.registerConnection(this.head, new SkaldPrim(true, false, false));
        this.head = new Stef(this.head, new SkaldPrim(true, false, false));
        Stef nTerm = new Stef();
        this.terminal.registerConnection(nTerm, new SkaldPrim(true, false, false));
        this.terminal = nTerm;
        this.head.registerConnection(this.terminal, new SkaldPrim(true, false, false));
        return this;
    }

    /**
     * Create a more lookup efficient state transition table representation.
     * @return The current NFA, packaged with the transition table representation.
     */
    public Lausavisa tablify() {
        if (!tableIsStale) return this;
        ArrayList<Stef> nodes = new ArrayList<>();
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
    private void bfs(ArrayList<Stef> nodes, Map<Stef, Map<SkaldPrim, ArrayList<Stef>>> table) {
        Stack<Stef> temp = new Stack<>();
        temp.push(this.head);
        Stack<Stef> next = new Stack<>();
        int currIndex = 0;

        while (!temp.empty()) {
            //System.out.println("Current stack: " + temp);
            Stef node = temp.empty() ? null : temp.peek();
            while (node != null) {
                if (node.mark == 0) {
                    node.mark = 1;
                    node.index = currIndex;
                    currIndex++;
                    nodes.add(node);
                    table.putIfAbsent(node, new HashMap<>());

                    for (SkaldPrim cond : node.shiftMatrix.keySet()) {
                        table.get(node).putIfAbsent(cond, new ArrayList<>());
                        for (Stef node2 : node.shiftMatrix.get(cond)) {
                            nodes.add(node2);
                            table.get(node).putIfAbsent(cond, new ArrayList<>());

                            next.push(node2);
                            table.get(node).get(cond).add(node2);
                        }
                    }
                }
                node = temp.empty() ? null : temp.pop();
            }
            Stack<Stef> stackFlop = temp;
            temp = next;
            next = stackFlop;
        }
        //System.out.println("All nodes: " + nodes);
    }
    /**
     * Prints the transition state table to the console.
     */
    public void printTable() {
        if (tableIsStale) tablify();
        System.out.println("/************************NFA STATE TABLE************************/");
        for (Stef k : table.keySet()) {
            System.out.println("Source state: " + k);
            if (k.index == this.head.index) {
                System.out.println("\tHEAD");
            } else if (k.index == this.terminal.index){
                System.out.println("\tTERMINAL");
            }
            for (SkaldPrim cond : table.get(k).keySet()) {
                System.out.println("\tCondition: " + cond.generateString() + ", Target states: " + table.get(k).get(cond));
                for (Stef node : table.get(k).get(cond)) {
                    System.out.println("\t\t" + node + " Lookarounds: " +  node.visur.size());
                }
            }
        }
        System.out.println("/**********************NFA STATE TABLE END**********************/");
    }
    /**
     * Gets a shallow copy of the transition state table.
     *
     * @return The transition state table.
     */
    public Map<Stef, Map<SkaldPrim, ArrayList<Stef>>> fetchTable() {
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
    public Drottkvaett generateDFA() {
        /* Prepare */
        if (tableIsStale) tablify();
        Map<Stef, Pair<Set<Stef>, Map<SkaldPrim, Stef>>> dfaTable = new HashMap<>();
        /* Iteration one, head node */
        Set<Stef> initial = closure(
                new HashSet<>(Collections.singletonList(this.head)),
                new SkaldPrim(true, false, tableIsStale));
        initial.add(head);
        createNFADFATransitionNode(initial, dfaTable);
        /*Continue iteration*/
        boolean complete = false;
        while (!complete) {
            complete = true;
            for (Stef dfaNode : dfaTable.keySet()) {
                if (dfaNode.mark == 0) {
                    processUnaryNode(dfaNode, dfaTable.get(dfaNode).getKey(), dfaTable);
                    complete = false;
                    break;
                }
            }
        }
        /* Final creation of the DFA */
        Drottkvaett dfa = new Drottkvaett();
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
    private void processUnaryNode(Stef dfaNode, Set<Stef> nfaSub,
                                  Map<Stef, Pair<Set<Stef>, Map<SkaldPrim, Stef>>> dfaTable) {
        //Find all valid inputs to the DFA node
        Set<SkaldPrim> possibleVals = new HashSet<>();
        for (Stef node : nfaSub) {
            possibleVals.addAll(node.shiftMatrix.keySet());
        }
        //Remove useless empty transitions (all empty transitions at this point should be internal
        possibleVals.remove(new SkaldPrim(true, false, tableIsStale));
        //Analyze each path
        for (SkaldPrim prim : possibleVals) {
            //Simulate a move of type prim, followed by continuation to end
            Set<Stef> nfaProgression = closure(nfaSub, prim);
            nfaProgression.addAll(closure(nfaProgression, new SkaldPrim(true, false, tableIsStale)));
            registerNFADFATransitionEntry(dfaTable, dfaNode, nfaProgression, prim);
        }
        //Mark the DFA node as processed, so do not lrConstruct again.
        dfaNode.mark = 1;
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
    private Stef registerNFADFATransitionEntry(Map<Stef, Pair<Set<Stef>, Map<SkaldPrim, Stef>>> dfaTable,
                                               Stef dfaNode, Set<Stef> nfaProgression,
                                               SkaldPrim prim) {
        //System.out.println("DFA Node " + dfaNode + ", with input " + prim.generateString() + ", connected to NFA node set " + nfaProgression);
        //Verify quality of nfaProgression set
        for (Stef dfaModel : dfaTable.keySet()) {
            if (nfaProgression.size() != dfaTable.get(dfaModel).getKey().size()) continue;
            boolean found = true;
            for (Stef nfaElement : dfaTable.get(dfaModel).getKey()) {
                if (!nfaProgression.contains(nfaElement)) {
                    found = false;
                    break;
                }
            }
            if (found) {
                //Old entry, mark transition and complete
                return markNFADFATransitionEntry(dfaNode, dfaModel, prim, dfaTable);
            }
        }
        //Entirely new DFA node
        return markNFADFATransitionEntry(dfaNode, createNFADFATransitionNode(nfaProgression, dfaTable), prim, dfaTable);
    }
    /**
     * Creates a node for the final DFA and places this in the table.
     * @param nfaRepresentation The NFA nodes that consist this DFA node.
     * @param dfaTable The master NFA to DFA transformation table.
     * @return The node placed into the table.
     */
    private Stef createNFADFATransitionNode(Set<Stef> nfaRepresentation,
                                            Map<Stef, Pair<Set<Stef>, Map<SkaldPrim, Stef>>> dfaTable) {
        //System.out.println("Insertion of DFA node representing " + nfaRepresentation);
        Stef nextNode = new Stef();
        nextNode.index = dfaTable.size();
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
    private Stef markNFADFATransitionEntry(Stef dfaNode, Stef dfaModel, SkaldPrim prim,
                                           Map<Stef, Pair<Set<Stef>, Map<SkaldPrim, Stef>>> dfaTable) {
        Stef node = dfaTable.get(dfaNode).getValue().put(prim, dfaModel);
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
    public Set<Stef> closure(Set<Stef> headNodes, SkaldPrim prim) {
        for (Stef node : table.keySet()) {
            node.mark = 0;
        }
        //System.out.println("Finding closure of " + headNodes + " with input " + prim.generateString());
        Set<Stef> nodeSet = new HashSet<>();
        for (Stef node : headNodes) {
            node.mark = 1;
            if (node.shiftMatrix.containsKey(prim)) {
                nodeSet.addAll(node.shiftMatrix.get(prim));
            }
        }
        //System.out.println("\tInitial processing located " + nodeSet);
        boolean done = false;
        while (!done) {
            done = true;
            for (Stef node : nodeSet) {
                if (node.mark == 0) {
                    done = false;
                    node.mark = 1;
                    if (node.shiftMatrix.containsKey(prim)) {
                        nodeSet.addAll(node.shiftMatrix.get(prim));
                    }
                    break;
                }
            }
        }
        //System.out.println("\tNode set starting from " + headNodes + " utilising connection " + prim.generateString() + " results in:  " + nodeSet + ".");
        return nodeSet;
    }

    public ArrayList<Integer> process(String check, int start) {
        Set<Stef> temp = new HashSet<>();
        Set<Stef> next = new HashSet<>();
        temp.add(head);
        ArrayList<Integer> success = new ArrayList<>();
        for (int i = start; i < check.length(); ++i) {
            boolean done = false;
            while (!done) {
                done = true;
                temp.addAll(closure(temp, new SkaldPrim(true, false, false)));
            }
            for (Stef node : temp) {
                if (node == terminal) success.add(i);
                next.addAll(node.fetchNext(new SkaldPrim(check.charAt(i))));
            }
            //System.out.println("Next iteration: " + next);
            temp.clear();
            Set<Stef> swap = next;
            next = temp;
            temp = swap;
        }
        for (Stef node : temp) {
            if (node == terminal) success.add(check.length());
        }
        return success;
    }
}