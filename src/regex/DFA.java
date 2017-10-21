package regex;

import javafx.util.Pair;

import java.util.Map;
import java.util.Set;

public interface DFA extends FSAutomaton {
    void processNFADFAConversionTable(
            Map<FSANode, Pair<Set<FSANode>, Map<RegExPrimitive, FSANode>>> transformationTable,
            FSANode nfaHeadNode, FSANode nfaTerminalNode);
    Map<FSANode, Map<RegExPrimitive, FSANode>> getTransitionTable();

    @Override
    default void printTable() {
        System.out.println("/************************DFA STATE TABLE************************/");
        for (FSANode node : getTransitionTable().keySet()) {
            System.out.print("DFA node " + node);
            if (node == getHead()) {
                System.out.print(": HEAD");
            }
            if (getTerminals().contains(node)) {
                System.out.print(": TERMINAL");
            }
            System.out.println();
            for (RegExPrimitive input: getTransitionTable().get(node).keySet()) {
                System.out.println("\tOn input " + input.generateString() +
                        " connected to DFA node " + getTransitionTable().get(node).get(input));
            }
            System.out.println("\tAssociated Lookarounds: " + node.countLookArounds());
        }
        System.out.println("/**********************DFA STATE TABLE END**********************/");
    }

    DFA minimize();
}
