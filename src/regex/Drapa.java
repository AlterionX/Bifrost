package regex;

import javafx.util.Pair;

import java.util.*;

public class Drapa implements DFA {
    private Map<FSANode, Map<RegExPrimitive, FSANode>> stateTable;
    private FSANode headNode = null;
    private Set<FSANode> terminalNodes;

    public Drapa() {}

    public void processNFADFAConversionTable(
            Map<FSANode, Pair<Set<FSANode>, Map<RegExPrimitive, FSANode>>> transformationTable,
            FSANode nfaHeadNode, FSANode nfaTerminalNode) {
        stateTable = new HashMap<>();
        terminalNodes = new HashSet<>();
        //printNFADFATransformation(transformationTable);
        for (FSANode node : transformationTable.keySet()) {
            if (transformationTable.get(node).getKey().contains(nfaHeadNode)) {
                assert headNode == null : "Cannot have two separate head nodes.";
                headNode = node;
            }
            if (transformationTable.get(node).getKey().contains(nfaTerminalNode)) {
                terminalNodes.add(node);
            }
            stateTable.put(node, transformationTable.get(node).getValue());
            for (RegExPrimitive input : transformationTable.get(node).getValue().keySet()) {
                node.registerConnection(transformationTable.get(node).getValue().get(input), input);
            }
            /* Look arounds */
            for (FSANode nfaSubNode : transformationTable.get(node).getKey()) {
                node.mergeLookArounds(nfaSubNode);
            }
        }
        //printTable();
    }
    @Override
    public Map<FSANode, Map<RegExPrimitive, FSANode>> getTransitionTable() {
        return stateTable;
    }

    public static Drapa merge(ArrayList<Drapa> dfas) {
        if (dfas.size() == 0) {
            return new Drapa();
        }
        if (dfas.size() == 1) {
            return dfas.get(0);
        }
        Drapa dfa = merge(dfas.get(0), dfas.get(1));
        for (int i = 2; i < dfas.size(); i++) {
            dfa = merge(dfa, dfas.get(i));
        }
        return dfa;
    }
    private static Drapa merge(Drapa dfaOne, Drapa dfaTwo) {
        if (dfaOne == null) {
            return dfaTwo;
        }
        if (dfaTwo == null) {
            return dfaOne;
        }
        //TODO
        return null;
    }

    public Drapa minimize() {
        //TODO
        return this;
    }

    @Override
    public FSANode getHead() {
        return headNode;
    }
    @Override
    public Set<FSANode> getTerminals() {
        return terminalNodes;
    }

    public List<Integer> process(String check, int start) {
        ArrayList<Integer> successInds = new ArrayList<>();
        FSANode currentNode = headNode;
        boolean reachedEnd = false;
        int i = start;
        while (currentNode != null) {
            if (!currentNode.checkLookArounds(check, i)) return successInds;
            if (terminalNodes.contains(currentNode)) successInds.add(i);
            if (reachedEnd) {
                break;
            } else if (i >= check.length()) {
                currentNode = currentNode.fetchSingleNext(RegExPrimitive.getRegExPrim(false, false, true));
                reachedEnd = true;
            } else {
                currentNode = currentNode.fetchSingleNext(RegExPrimitive.getRegExPrim(check.charAt(i)));
                i++;
            }
        }
        return successInds;
    }
    public int processFirstReverse(String check, int start) {
        //printTable();
        FSANode currentNode = headNode;
        int i = start;
        boolean reachedEnd = false;
        while (currentNode != null) {
            if (!currentNode.checkLookArounds(check, i)) return -1;
            if (terminalNodes.contains(currentNode)) return i;
            if (reachedEnd) {
                break;
            } else if (i >= check.length()) {
                currentNode = currentNode.fetchSingleNext(RegExPrimitive.getRegExPrim(false, false, true));
                reachedEnd = true;
            } else {
                currentNode = currentNode.fetchSingleNext(RegExPrimitive.getRegExPrim(check.charAt(i)));
                i--;
            }
        }
        return -1;
    }
    public int processFirst(String check, int start) {
        FSANode currentNode = headNode;
        int i = start;
        boolean reachedEnd = false;
        while (currentNode != null) {
            if (!currentNode.checkLookArounds(check, i)) return -1;
            if (terminalNodes.contains(currentNode)) return i;
            if (reachedEnd) {
                break;
            } else if (i >= check.length()) {
                currentNode = currentNode.fetchSingleNext(RegExPrimitive.getRegExPrim(false, false, true));
                reachedEnd = true;
            } else {
                currentNode = currentNode.fetchSingleNext(RegExPrimitive.getRegExPrim(check.charAt(i)));
                i++;
            }
        }
        return -1;
    }
}
