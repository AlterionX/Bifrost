package asgard;

import javafx.util.Pair;
import tagtable.Tag;
import yggdrasil.Branch;
import yggdrasil.Nidhogg;
import yggdrasil.Yggdrasil;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * A chunk of rules within a rule group.
 *
 * These apply to the nodes described by the groups, with the addition of context situations.
 */
public class IRChunk {
    private boolean universal = false;
    private final List<Integer> tagIndex = new ArrayList<>();
    private final List<Tag> tagToMatch = new ArrayList<Tag>();
    private IRRule productionRule;
    private final List<IRRule> rules = new ArrayList<>();

    /**
     * Tells the chunk to be a "catch all", similar to an else statement
     */
    public void setUniversal() {
        universal = !universal;
    }
    /**
     * Sets the condition that location-th member of the children (-1 if parent) matches the tag
     * @param location The location of the relevant parent or child.
     * @param tag The tag representing the target symbol.
     */
    public void addCondition(int location, Tag tag) {
        tagIndex.add(location);
        tagToMatch.add(tag);
    }
    /**
     * Add a production intermediate representation rule, or one that simply tells
     * the parents what to refer to a node as during the parents' translation/conversion.
     * @param production The rule in question
     */
    public void addProductionIRRule(IRRule production) {
        productionRule = production;
    }
    /**
     * Add a general translation rule. Note that these will be executed in the order that they are provided.
     * @param rule Rule to be added.
     */
    public void addIRRule(IRRule rule) {
        rules.add(rule);
    }

    /**
     * Returns the list of relevant children/parents.
     * @return The list of pertinent children/parents.
     */
    public List<Integer> getPositions() {
        return tagIndex;
    }
    /**
     * Get the tags related to the positions. The ordering matches.
     * @return The list of tags
     */
    public List<Tag> getTags() {
        return tagToMatch;
    }
    /**
     * Remembers if the chunk is a "catchall" chunk
     * @return
     */
    public boolean isUniversal() {
        return universal;
    }

    /**
     * Prints rules in a clean format.
     */
    public void printRules() {
        System.out.println("\t" + universal + ":" + tagIndex + ":" + tagToMatch);
        productionRule.printRule();
        for (IRRule rule : rules) {
            rule.printRule();
        }
    }

    /**
     * Determines if a branch has the same characteristics as the provided conditions.
     * @param branch The branch to examine.
     * @return If it matched.
     */
    public boolean isMatchWith(Branch branch) {
        for (int i = 0; i < tagToMatch.size(); i++) {
            if (tagIndex.get(i) == -1) {
                if (!(branch.getParent() instanceof Branch) ||
                        !((Branch)branch.getParent()).getTag().equals(tagToMatch.get(i))) {
                    return false;
                }
            } else {
                if (tagIndex.get(i) >= branch.getChildren().size() ||
                        !((Branch) branch.getChildren().get(tagIndex.get(i))).getTag().equals(tagToMatch.get(i))) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Use transformation described by the rule chunk to modify the provided data into an end product
     * matching the rules.
     * @param branch The source branch
     * @param children The string representation of the children as provided by their production rules.
     * @param childrenOutput The transformation output from the children
     * @param symTable The symtable
     * @return A pair of String and StringBuilder, the output of the production rule and the output respectively
     */
    public Pair<String, StringBuilder> execute(Branch branch, List<String> children,
                                               LinkedList<StringBuilder> childrenOutput, Nidhogg symTable) {
        StringBuilder fToken = new StringBuilder();
        productionRule.generateString(branch, children, childrenOutput, null, fToken, symTable);
        StringBuilder out = new StringBuilder();
        for (IRRule rule : rules) {
            rule.generateString(branch, children, childrenOutput, fToken, out, symTable);
            out.append('\n');
        }
        return new Pair<>(fToken.toString(), out);
    }
}
