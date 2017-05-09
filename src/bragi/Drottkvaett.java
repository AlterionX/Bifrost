package bragi;

import bragi.bragi.skaldparts.SkaldPrim;
import javafx.util.Pair;

import java.util.*;

public class Drottkvaett {
    ArrayList<Character> input;
    Map<Stef, Map<SkaldPrim, Stef>> stateTable;
    Stef headNode = null;
    Set<Stef> terminalNodes;

    public Drottkvaett() {}

    public void processNFADFAConversionTable(
            Map<Stef, Pair<Set<Stef>, Map<SkaldPrim, Stef>>> transformationTable,
            Stef nfaHeadNode, Stef nfaTerminalNode) {
        stateTable = new HashMap<>();
        terminalNodes = new HashSet<>();
        //printNFADFATransformation(transformationTable);
        for (Stef node : transformationTable.keySet()) {
            if (transformationTable.get(node).getKey().contains(nfaHeadNode)) {
                if (headNode == null) {
                    headNode = node;
                } else {
                    throw new RuntimeException("Cannot have two separate head nodes.");
                }
            }
            if (transformationTable.get(node).getKey().contains(nfaTerminalNode)) {
                terminalNodes.add(node);
            }
            stateTable.put(node, transformationTable.get(node).getValue());
            for (SkaldPrim input : transformationTable.get(node).getValue().keySet()) {
                node.registerConnection(transformationTable.get(node).getValue().get(input), input);
            }
            /* Look arounds */
            for (Stef nfaSubNode : transformationTable.get(node).getKey()) {
                node.mergeLookArounds(nfaSubNode);
            }
        }
        //printTable();
    }
    private void printNFADFATransformation(Map<Stef, Pair<Set<Stef>, Map<SkaldPrim, Stef>>> transformationTable) {
        System.out.println("/**********************NFA-DFA STATE TABLE**********************/");
        for (Stef node : transformationTable.keySet()) {
            System.out.println("DFA node " + node + " represents NFA nodes " + transformationTable.get(node).getKey());
            for (Stef nfaSubNode : transformationTable.get(node).getKey()) {
                System.out.println("\tNFA node " + nfaSubNode + " Associated lookarounds: " + nfaSubNode.visur.size());
            }
            for (SkaldPrim input: transformationTable.get(node).getValue().keySet()) {
                System.out.println("\tOn input " + input.generateString() +
                        " connected to DFA node " + transformationTable.get(node).getValue().get(input));
            }
        }
        System.out.println("/********************NFA-DFA STATE TABLE END********************/");
    }
    public void printTable() {
        System.out.println("/************************DFA STATE TABLE************************/");
        for (Stef node : stateTable.keySet()) {
            System.out.print("DFA node " + node);
            if (node == headNode) {
                System.out.print(": HEAD");
            }
            if (terminalNodes.contains(node)) {
                System.out.print(": TERMINAL");
            }
            System.out.println();
            for (SkaldPrim input: stateTable.get(node).keySet()) {
                System.out.println("\tOn input " + input.generateString() +
                        " connected to DFA node " + stateTable.get(node).get(input));
            }
            System.out.println("\tAssociated Lookarounds: " + node.visur.size());
        }
        System.out.println("/**********************DFA STATE TABLE END**********************/");
    }

    public static Drottkvaett merge(ArrayList<Drottkvaett> dfas) {
        if (dfas.size() == 0) {
            return new Drottkvaett();
        }
        if (dfas.size() == 1) {
            return dfas.get(0);
        }
        Drottkvaett dfa = merge(dfas.get(0), dfas.get(1));
        for (int i = 2; i < dfas.size(); i++) {
            dfa = merge(dfa, dfas.get(i));
        }
        return dfa;
    }
    public static Drottkvaett merge(Drottkvaett dfaOne, Drottkvaett dfaTwo) {
        if (dfaOne == null) {
            return dfaTwo;
        }
        if (dfaTwo == null) {
            return dfaOne;
        }
        //TODO
        return null;
    }

    public Drottkvaett minimize() {
        //TODO
        return this;
    }

    public List<Integer> process(String check, int start) {
        ArrayList<Integer> successInds = new ArrayList<>();
        Stef currentNode = headNode;
        boolean reachedEnd = false;
        int i = start;
        while (currentNode != null) {
            if (!currentNode.lookaroundCheck(check, i)) return successInds;
            if (terminalNodes.contains(currentNode)) successInds.add(i);
            if (reachedEnd) {
                break;
            } else if (i >= check.length()) {
                currentNode = currentNode.fetchSingleNext(new SkaldPrim(false, false, true));
                reachedEnd = true;
            } else {
                currentNode = currentNode.fetchSingleNext(new SkaldPrim(check.charAt(i)));
                i++;
            }
        }
        return successInds;
    }
    public int processFirstReverse(String check, int start) {
        //printTable();
        Stef currentNode = headNode;
        int i = start;
        boolean reachedEnd = false;
        while (currentNode != null) {
            if (!currentNode.lookaroundCheck(check, i)) return -1;
            if (terminalNodes.contains(currentNode)) return i;
            if (reachedEnd) {
                break;
            } else if (i >= check.length()) {
                currentNode = currentNode.fetchSingleNext(new SkaldPrim(false, false, true));
                reachedEnd = true;
            } else {
                currentNode = currentNode.fetchSingleNext(new SkaldPrim(check.charAt(i)));
                i--;
            }
        }
        return -1;
    }
    public int processFirst(String check, int start) {
        Stef currentNode = headNode;
        int i = start;
        boolean reachedEnd = false;
        while (currentNode != null) {
            if (!currentNode.lookaroundCheck(check, i)) return -1;
            if (terminalNodes.contains(currentNode)) return i;
            if (reachedEnd) {
                break;
            } else if (i >= check.length()) {
                currentNode = currentNode.fetchSingleNext(new SkaldPrim(false, false, true));
                reachedEnd = true;
            } else {
                currentNode = currentNode.fetchSingleNext(new SkaldPrim(check.charAt(i)));
                i++;
            }
        }
        return -1;
    }
}
