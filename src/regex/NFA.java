package regex;

import java.util.ArrayList;
import java.util.Map;

public interface NFA extends FSAutomaton {
    //region Data formatting
    NFA tablify();
    Map<FSANode, Map<RegExPrimitive, ArrayList<FSANode>>> getTransitionTable();
    //endregion

    //region NFA modification
    void merge(NFA nfa, int i);
    NFA kleeneWrap();
    DFA generateDFA();
    //endregion

    void addStringToTerminal(String s);

    //region Printing
    /**
     * Prints the transition state table to the console.
     */
    default void printTable() {
        Map<FSANode, Map<RegExPrimitive, ArrayList<FSANode>>> table = getTransitionTable();
        System.out.println("/************************NFA STATE TABLE************************/");
        for (FSANode k : table.keySet()) {
            System.out.println("Source state: " + k);
            if (k.getIndex() == this.getHead().getIndex()) {
                System.out.println("\tHEAD");
            } else if (k.getIndex() == this.getTerminals().iterator().next().getIndex()){
                System.out.println("\tTERMINAL");
            }
            for (RegExPrimitive cond : table.get(k).keySet()) {
                System.out.println("\tCondition: " + cond.generateString() + ", Target states: " + table.get(k).get(cond));
                for (FSANode node : table.get(k).get(cond)) {
                    System.out.println("\t\t" + node + " Lookarounds: " +  node.countLookArounds());
                }
            }
        }
        System.out.println("/**********************NFA STATE TABLE END**********************/");
    }
    //endregion
}
