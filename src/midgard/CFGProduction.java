package midgard;

import tagtable.Tag;
import tagtable.TagPriority;
import tagtable.TagTable;
import ast.*;

import java.util.*;

public class CFGProduction implements Iterable<Tag> {
    private final TagTable tagTable;
    private Tag left;
    private List<Tag> rule;
    private List<List<String>> actions;

    public CFGProduction(String left, Iterable<String> rightSeries, TagTable tagTable) {
        this.tagTable = tagTable;
        //Error checking
        if (left.isEmpty()) throw new RuntimeException("False cmd.");
        if (tagTable.isTerminalTag(tagTable.addElseFindTag(TagPriority.PAR, left))) throw new RuntimeException("Left-hand side, " + left + " cannot be a terminal.");
        //Create rule
        this.left = tagTable.addElseFindTag(TagPriority.PAR, left);
        rule = new ArrayList<>();
        actions = new ArrayList<>();
        for (String term : rightSeries) {
            if (term.charAt(0) != '%') {
                rule.add(tagTable.addElseFindTag(TagPriority.PAR, term));
                actions.add(new ArrayList<>());
            } else {
                actions.get(actions.size() - 1).add(term);
            }
        }
    }
    public CFGProduction(Tag left, List<Tag> rule, TagTable tagTable) {
        this.tagTable = tagTable;
        this.left = left;
        this.rule = new ArrayList<>();
        this.rule.addAll(rule);
    }

    //Fetch things from left and right
    public Tag getLeft() {
        return left;
    }
    public List<Tag> getRight() {
        return rule;
    }
    public int getRightCount() {
        return rule.size();
    }
    public Tag getRightElement(int k) {
        return rule.get(k);
    }
    //Following a rule reduce
    public void ruleReduce(Stack<Integer> stateStack, Stack<Branch> productionStack) {
        //if (tagTable.DEBUG) System.out.println("Reducing " + productionStack + " with state stack " + stateStack + " with rule " + this);
        Branch nextBranch = new Branch(left);
        LinkedList<Branch> branches = new LinkedList<>();
        for (int i = 0; i < rule.size(); i++) {
            if (productionStack.peek() == null) {
                System.out.println("Something popped too much or didn't push enough.");
                System.exit(-500);
            }
            branches.addFirst(productionStack.pop());
            stateStack.pop();
        }
        branchMerge(nextBranch, branches);
        productionStack.push(nextBranch);
    }

    private void branchMerge(Branch nextBranch, LinkedList<Branch> branches) {
        /*if (tagTable.DEBUG) {
            System.out.println("Pre-concat branches");
            for (Branch branch : branches) {
                Seedling.simplePrint(branch);
            }
        }*/
        LinkedList<Branch> tempList = new LinkedList<>();
        for (int i = 0; i < branches.size(); i++) {
            List<String> tokenActions = actions.get(i);
            if (tokenActions.size() != 0) {
                if (tokenActions.size() > 1) {
                    System.out.println("Multi-instruction actions not yet supported");
                    throw new RuntimeException("Unsupported feature: Multi-instruction node mod.");
                }
                for (String action : tokenActions) {
                    //Do action
                    String[] actionDecoded = action.split(":");
                    switch (actionDecoded[0]) {
                        case "%LVL":
                        case "%LEVEL":
                            int levels = Integer.parseInt(actionDecoded[1]);
                            if (levels == -1) {
                                for (Seedling child : branches.get(i).getChildren()) {
                                    tempList.addLast((Branch) child);
                                }
                            } else {
                                System.out.println("Unsupported level modification");
                                throw new RuntimeException("Unsupported multi-level creation.");
                            }
                            break;
                        case "%RMV":
                        case "%REMOVE":
                            //NO-OP
                            break;
                        case "%RECAST":
                        case "%POP":
                            //Make a new branch, pop up several levels, and append to the nextBranch
                            tempList.addLast(branches.get(i));
                            Branch subBranch = new Branch(tagTable.addElseFindTag(TagPriority.PAR, actionDecoded[2]));
                            for (int j = 0; j < Integer.parseInt(actionDecoded[1]); j++) {
                                subBranch.append(tempList.removeLast());
                            }
                            tempList.addLast(subBranch);
                            break;
                    }
                }
            } else {
                tempList.addLast(branches.get(i));
            }
        }
        while (!tempList.isEmpty()) {
            nextBranch.append(tempList.removeFirst());
        }
        /*if (tagTable.DEBUG) {
            (new Scanner(System.in)).nextLine();
            System.out.println("Post-concat branches");
            Seedling.simplePrint(nextBranch);
            (new Scanner(System.in)).nextLine();
        }*/
    }

    //Iterator for internal list of rules
    public Iterator<Tag> iterator() {
        return rule.iterator();
    }
    //Object overrides, used for standard containers and Collections API
    public boolean equals(Object o) {
        if (o instanceof CFGProduction &&
                Objects.equals(this.left, ((CFGProduction) o).left) &&
                this.rule.size() == ((CFGProduction) o).rule.size()) {
            for (int i = 0; i < this.rule.size(); i++) {
                if (!this.rule.get(i).equals(((CFGProduction) o).rule.get(i))) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }
    public int hashCode() {
        return rule.stream()
                .map(Tag::hashCode)
                .reduce((first, second) -> first + second)
                .orElse(0) + left.hashCode();
    }
    public String toString() {
        StringBuilder sb = new StringBuilder().append(left).append(" -> [");
        for (int i = 0; i < rule.size(); i++) {
            sb.append(rule.get(i)).append(" ").append(actions.get(i));
            if (i != rule.size() - 1) {
                sb.append(", ");
            }
        }
        return sb.append("]").toString();
    }
}
