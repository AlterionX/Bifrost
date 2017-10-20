package bragi;

import bragi.bragi.skaldparts.SkaldPrim;

import java.util.ArrayList;
import java.util.Map;

public interface NFA extends FSAutomaton {
    NFA tablify();
    DFA generateDFA();
    Map<FSANode, Map<SkaldPrim, ArrayList<FSANode>>> getTransitionTable();

    /**
     * Prints the transition state table to the console.
     */
    default void printTable() {
        Map<FSANode, Map<SkaldPrim, ArrayList<FSANode>>> table = getTransitionTable();
        System.out.println("/************************NFA STATE TABLE************************/");
        for (FSANode k : table.keySet()) {
            System.out.println("Source state: " + k);
            if (k.getIndex() == this.getHead().getIndex()) {
                System.out.println("\tHEAD");
            } else if (k.getIndex() == this.getTerminals().iterator().next().getIndex()){
                System.out.println("\tTERMINAL");
            }
            for (SkaldPrim cond : table.get(k).keySet()) {
                System.out.println("\tCondition: " + cond.generateString() + ", Target states: " + table.get(k).get(cond));
                for (FSANode node : table.get(k).get(cond)) {
                    System.out.println("\t\t" + node + " Lookarounds: " +  node.countLookArounds());
                }
            }
        }
        System.out.println("/**********************NFA STATE TABLE END**********************/");
    }

    void merge(NFA nfa, int i);

    NFA kleeneWrap();

    void addStringToTerminal(String s);
}
