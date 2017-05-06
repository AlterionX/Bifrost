package asgard;

import javafx.util.Pair;
import yggdrasil.Branch;
import yggdrasil.Yggdrasil;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class IRChunk {
    private boolean universal = false;
    private List<Integer> tagIndex = new ArrayList<>();
    private List<Integer> tagToMatch = new ArrayList<>();
    private IRRule productionRule;
    private List<IRRule> rules = new ArrayList<>();

    public void setUniversal() {
        universal = !universal;
    }
    public void addCondition(int location, Integer tag) {
        tagIndex.add(location);
        tagToMatch.add(tag);
    }
    public void addProductionIRRule(IRRule production) {
        productionRule = production;
    }
    public void addIRRule(IRRule rule) {
        rules.add(rule);
    }

    public List<Integer> getPositions() {
        return tagIndex;
    }
    public List<Integer> getTags() {
        return tagToMatch;
    }
    public boolean isUniversal() {
        return universal;
    }

    public void printRules() {
        System.out.println("\t" + universal + ":" + tagIndex + ":" + tagToMatch);
        productionRule.printRule();
        for (IRRule rule : rules) {
            rule.printRule();
        }
    }

    public boolean isMatchWith(Branch branch) {
        for (int i = 0; i < tagToMatch.size(); i++) {
            if (tagIndex.get(i) >= branch.getChildren().size() ||
                    ((Branch) branch.getChildren().get(tagIndex.get(i))).getTag() != tagToMatch.get(i)) {
                return false;
            }
        }
        return true;
    }

    public Pair<String, StringBuilder> execute(Branch branch, List<String> children, LinkedList<StringBuilder> childrenOutput, Yggdrasil context) {
        StringBuilder fToken = new StringBuilder();
        productionRule.generateString(branch, children, childrenOutput, null, fToken, context);
        StringBuilder out = new StringBuilder();
        for (IRRule rule : rules) {
            rule.generateString(branch, children, childrenOutput, fToken, out, context);
            out.append('\n');
        }
        return new Pair<>(fToken.toString(), out);
    }
}
