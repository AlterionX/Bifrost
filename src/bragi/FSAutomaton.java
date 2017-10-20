package bragi;

import bragi.bragi.skaldparts.SkaldPrim;
import javafx.util.Pair;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface FSAutomaton {
    FSANode getHead();
    Set<FSANode> getTerminals();

    List<Integer> process(String stream, int i);
    int processFirstReverse(String stream, int i);
    int processFirst(String stream, int currLoc);

    static void printNFADFATransformation(Map<FSANode, Pair<Set<Stef>, Map<SkaldPrim, Stef>>> transformationTable) {
        System.out.println("/**********************NFA-DFA STATE TABLE**********************/");
        for (FSANode node : transformationTable.keySet()) {
            System.out.println("DFA node " + node + " represents NFA nodes " + transformationTable.get(node).getKey());
            for (Stef nfaSubNode : transformationTable.get(node).getKey()) {
                System.out.println("\tNFA node " + nfaSubNode + " Associated lookarounds: " + nfaSubNode.countLookArounds());
            }
            for (SkaldPrim input: transformationTable.get(node).getValue().keySet()) {
                System.out.println("\tOn input " + input.generateString() +
                        " connected to DFA node " + transformationTable.get(node).getValue().get(input));
            }
        }
        System.out.println("/********************NFA-DFA STATE TABLE END********************/");
    }

    default void printTable() {
        throw new UnsupportedOperationException("Printing an abstract table.");
    }
}
