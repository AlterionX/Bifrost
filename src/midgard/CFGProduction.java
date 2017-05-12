package midgard;

import yggdrasil.*;

import java.util.*;

public class CFGProduction implements Iterable<Integer> {
    private Yggdrasil parent;
    private Integer left;
    private List<Integer> rule;
    private List<List<String>> actions;

    public CFGProduction(String left, Iterable<String> rightSeries, Yggdrasil parent) {
        this.parent = parent;
        //Error checking
        if (left.isEmpty()) throw new RuntimeException("False cmd.");
        if (parent.isTerminal(left)) throw new RuntimeException("Left-hand side cannot be a terminal.");
        //Create rule
        this.left = parent.addTagIfAbsent(left, TagPriority.PAR);
        rule = new ArrayList<>();
        actions = new ArrayList<>();
        for (String term : rightSeries) {
            if (term.charAt(0) != '%') {
                rule.add(parent.addTagIfAbsent(term, TagPriority.PAR));
                actions.add(new ArrayList<>());
            } else {
                actions.get(actions.size() - 1).add(term);
            }
        }
    }
    public CFGProduction(int left, List<Integer> rule, Yggdrasil parent) {
        this.parent = parent;
        this.left = left;
        this.rule = new ArrayList<>();
        this.rule.addAll(rule);
    }

    //Fetch things from left and right
    public int getLeft() {
        return left;
    }
    public List<Integer> getRight() {
        return rule;
    }
    public int getRightCount() {
        return rule.size();
    }
    public Integer getRightElement(int k) {
        return rule.get(k);
    }
    //Following a rule reduce
    public Branch ruleReduce(Stack<Integer> stateStack, Stack<Branch> productionStack) {
        if (parent.DEBUG) System.out.println("Reducing " + productionStack + " with state stack " + stateStack + " with rule " + this);
        Branch nextBranch = new Branch(left, parent);
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
        return nextBranch;
    }

    private void branchMerge(Branch nextBranch, LinkedList<Branch> branches) {
        if (parent.DEBUG) {
            System.out.println("Pre-concat branches");
            for (Branch branch : branches) {
                Seedling.simplePrint(branch);
            }
        }
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
                            Branch subBranch = new Branch(parent.tagEncode(actionDecoded[2], TagPriority.SUB), parent);
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
        if (parent.DEBUG) {
            (new Scanner(System.in)).nextLine();
            System.out.println("Post-concat branches");
            Seedling.simplePrint(nextBranch);
            (new Scanner(System.in)).nextLine();
        }
    }

    //Iterator for internal list of rules
    public Iterator<Integer> iterator() {
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
        return rule.stream().reduce((first, second) -> first + second).orElse(0) + left;
    }
    public String toString() {
        StringBuilder sb = new StringBuilder().append(left).append(" (").append(parent.tagDecode(left, TagPriority.SUB)).append(") -> ").append("[");
        for (int i = 0; i < rule.size(); i++) {
            sb.append(rule.get(i)).append(" (").append(parent.tagDecode(rule.get(i), TagPriority.SUB)).append(")").append(actions.get(i));
            if (i != rule.size() - 1) {
                sb.append(", ");
            }
        }
        return sb.append("]").toString();
    }
}
