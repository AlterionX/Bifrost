package regex;

import java.util.Set;

public interface FSANode {
    //region Transitions
    void registerConnection(FSANode terminal, RegExPrimitive single);
    Set<RegExPrimitive> possibleInputs();
    Set<FSANode> possibleTransitions(RegExPrimitive input);
    Set<FSANode> fetchNext(RegExPrimitive input);
    FSANode fetchSingleNext(RegExPrimitive input);
    //endregion

    //region LookArounds
    class LookAround {
        private FSAutomaton basis;
        private boolean negate;
        private boolean reverse;

        LookAround(FSAutomaton basis, boolean negate, boolean reverse) {
            this.basis = basis;
            this.negate = negate;
            this.reverse = reverse;
        }

        //region Accessors
        protected FSAutomaton getBasis() {
            return basis;
        }
        protected boolean isNegate() {
            return negate;
        }
        protected boolean isReverse() {
            return reverse;
        }
        //endregion
    }
    void addLookAround(FSAutomaton dfa, boolean negate, boolean reverse);
    Set<LookAround> getLookArounds();
    int countLookArounds();
    boolean checkLookArounds(String check, int i);
    void mergeLookArounds(FSANode nfaSubNode);
    //endregion

    //region Accessors
    void addRegExString(String completion);
    String getRegExString();
    void setIndex(int index);
    int getIndex();
    void setScratch(int scratch);
    int getScratch();
    //endregion
}
