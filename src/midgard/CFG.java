package midgard;

import yggdrasil.TagPriority;
import yggdrasil.TagRecord;
import yggdrasil.Yggdrasil;

import java.util.*;

//Rename to Drapa
public class CFG {
    private static final boolean OPTIMIZE_GRAMMAR =  false;
    public static final Set<CFGProductionType> SUPPORTED_RULE_TYPES = new HashSet<>(Arrays.asList(
            CFGProductionType.SBNF
    ));
    //Easily indexable array of rules. All these are listed in the associated rules
    private List<CFGProduction> ruleList = new ArrayList<>();
    private LinkedHashSet<CFGProduction> masterRuleSet = new LinkedHashSet<>();
    //Tags and rules
    private Map<Integer, Integer> leftTagMap = new HashMap<>();
    private List<List<CFGProduction>> rightRules = new ArrayList<>();
    private boolean emptyGenerated = false;
    //First and follow sets of the grammar
    private Map<Integer, Set<Integer>> firstSets = new HashMap<>();
    private Map<Integer, Set<Integer>> followSets = new HashMap<>();
    private Yggdrasil context;
    //Class/rule set initialization
    /**
     * Setup and parse the CFG declaration file.
     * @param context The context data, AST, and symtable
     * @param config The configuration data.
     */
    public CFG(Yggdrasil context, String config) {
        this.context = context;
        //Read data
        CFGProductionType rt = CFGProductionType.SBNF;
        if (config.startsWith(">GRAMMAR_TYPE")) {
            String type = config.substring(0, config.indexOf("\n"));
            config = config.substring(type.length()).trim();
            type = type.trim();
            String[] split = type.split("\\s+");
            if (split.length != 2) {
                System.out.println("Syntax error in grammar declaration. Expected \">GRAMMAR_TYPE TYPE\", found" + type);
            } else {
                switch (split[1]) {
                    case "SBNF":
                        rt = CFGProductionType.SBNF;
                        break;
                    case "BNF":
                        rt = CFGProductionType.BNF;
                        break;
                    case "EBNF":
                        rt = CFGProductionType.EBNF;
                        break;
                    case "CNF":
                        rt = CFGProductionType.CNF;
                        break;
                    case "CFG":
                        rt = CFGProductionType.CFG;
                        break;
                    default:
                        System.out.println("Unrecognized requested grammar.");
                }
            }
            if (!SUPPORTED_RULE_TYPES.contains(rt)) {
                throw new RuntimeException("Extended CFG grammar formats unsupported.");
            }
        }
        parseCFG(config, rt);
        if (context.DEBUG) {
            //Show results
            System.out.println("/********************Parsed CFG Rules************************/");
            printProductions();
            System.out.println("/************************************************************/");
        }
    }
    //Rule file parsing
    /**
     * Parse the given input as a list of CFG productions.
     * @param input The input.
     * @param pt The production type.
     */
    private void parseCFG(String input, CFGProductionType pt) {
        //Split rules and do preprocessing
        String[] splitRules = input.split("\n");
        for (int i = 0; i < splitRules.length; i++) {
            splitRules[i] = splitRules[i].trim();
        }
        switch (pt) {
            case SBNF:
                for (String splitRule : splitRules) {
                    if (!splitRule.isEmpty()) {
                        if (!splitRule.startsWith("#")) { //Not comment
                            int ruleBreak = splitRule.indexOf("->");
                            String left = splitRule.substring(0, ruleBreak).trim();
                            String[] right = splitRule.substring(ruleBreak + 2).trim().split("\\s+");
                            for (int j = 0; j < right.length; j++) {
                                right[j] = right[j].trim();
                            }
                            //Split rules with respect to rt
                            //SBNF ensures only one rule per instruction, with all tokens split by whitespace, not including the ->
                            if (masterRuleSet.isEmpty() && !left.equals(TagRecord.START_LABEL)) {
                                masterRuleSet.addAll(parseProduction(TagRecord.START_LABEL,
                                        new ArrayList<>(Arrays.asList(left, TagRecord.EOF_LABEL)), pt));
                            }
                            masterRuleSet.addAll(parseProduction(left, Arrays.asList(right), pt));
                        }
                    }
                }
                break;
            default:
                System.out.println("Unsupported format.");
                throw new RuntimeException("Unsupported CFG format.");
        }
        Set<Integer> removed = new HashSet<>();
        if (OPTIMIZE_GRAMMAR) {
            removed.addAll(epsilonRemoval());
            removed.addAll(unitGenRemoval());
            removed.addAll(uselessRemoval());
            removed.addAll(isolateRemoval());
        }
        //Add rules to the associative containers
        for (CFGProduction rule : masterRuleSet) {
            if (!leftTagMap.containsKey(rule.getLeft())) {
                leftTagMap.put(rule.getLeft(), rightRules.size());
                rightRules.add(new ArrayList<>());
            }
            rightRules.get(leftTagMap.get(rule.getLeft())).add(rule);
            ruleList.add(rule);
        }
        if (context.tagCount() - context.terminalCount() != leftTagMap.size() + removed.size()) {
            Set<Integer> missing = new HashSet<>();
            for (int i = context.terminalCount(); i < context.tagCount(); i++) {
                missing.add(i);
            }
            missing.removeAll(leftTagMap.keySet());
            Set<String> translated = new HashSet<>();
            for (Integer k : missing) {
                translated.add(context.tagDecode(k, TagPriority.SUB));
            }
            throw new RuntimeException("CFG contains nonterminals " + translated + " without rules.");
        }
        produceFirstAndFollowSets();
    }

    /**
     * Parse the provided rule set. The first call to this per-instance will attempt to use the first rule
     * as basis of starting rule.
     * @param left The left side of the rule.
     * @param rightSeries The labels on the right side of the rule.
     * @param type The type of production. See the README and the enum for more details.
     * @return The list of productions generated from the provided data.
     */
    private List<CFGProduction> parseProduction(String left, List<String> rightSeries, CFGProductionType type) {
        List<CFGProduction> parsedProductions = new ArrayList<>();
        switch (type) {
            case SBNF:
                parsedProductions.add(new CFGProduction(left, rightSeries, context));
                break;
            default:
                System.out.println("Unsupported format.");
                throw new RuntimeException("Unsupported CFG format.");
        }
        return parsedProductions;
    }
    /**
     * Prints the productions.
     */
    private void printProductions() {
        for (Integer tag : leftTagMap.keySet()) {
            for (CFGProduction rule : rightRules.get(leftTagMap.get(tag))) {
                System.out.println(rule);
            }
        }
    }
    //Rule retrieval
    /**
     * Fetch the initial production.
     * @return The initial production.
     */
    public Integer fetchZeroInstruction() {
        return masterRuleSet.iterator().next().getLeft();
    }
    /**
     * Fetch all rules produced by a given non-terminal
     * @param left The number representing a non-terminal label.
     * @return The rules associated with the non-terminal.
     */
    public List<CFGProduction> fetchRulesForLeft(Integer left) {
        if (!context.isTerminal(left)) {
            return rightRules.get(leftTagMap.get(left));
        } else {
            return new ArrayList<>();
        }
    }
    /**
     * Get the number of rules of the CFG.
     * @return The number of rules.
     */
    public int getProductionCount() {
        return masterRuleSet.size();
    }
    /**
     * Get the number associated with a rule.
     * @param rule The rule to look up in the list of rules.
     * @return The integer representing the rule.
     */
    public Integer encodeProduction(CFGProduction rule) {
        return ruleList.indexOf(rule);
    }
    /**
     * Get the rule represented by the given number.
     * @param index The rule's representation.
     * @return The rule.
     */
    public CFGProduction decodeProduction(Integer index) {
        return ruleList.get(index);
    }
    //CFG simplification
    /**
     * Remove epsilon production, where non-terminals become the terminal empty string.
     *
     * @return A set of integers representing non-terminal labels that are no longer being used by the new rules.
     */
    private Set<Integer> epsilonRemoval() {
        System.out.println("Removing epsilon rules.");

        System.out.println("Finding nullables.");
        Set<Integer> nullables = new HashSet<>();
        boolean done = false;
        while (!done) {
            done = true;
            List<Integer> qualifiedRemoval = new ArrayList<>();
            for (int i = 0; i < context.tagCount(); i++) {
                qualifiedRemoval.add(0);
            }
            for (CFGProduction rule : masterRuleSet) {
                if (rule.getRight().contains(context.tagEncode(TagRecord.EMP_LABEL, TagPriority.PAR))) {
                    if (rule.getRightCount() == 1 && !nullables.contains(rule.getLeft())) {
                        //
                        nullables.add(rule.getLeft());
                        qualifiedRemoval.set(rule.getLeft(), qualifiedRemoval.get(rule.getLeft()) + 1);
                    } else {
                        for (int i = rule.getRightCount() - 1; i >= 0; i--) {
                            if ((int) rule.getRightElement(i) == context.tagEncode(TagRecord.EMP_LABEL, TagPriority.PAR)) {
                                rule.getRight().remove(i);
                            }
                        }
                    }
                } else {
                    if (nullables.containsAll(rule.getRight())
                            && !nullables.contains(rule.getLeft())) {
                        nullables.add(rule.getLeft());
                        qualifiedRemoval.set(rule.getLeft(), qualifiedRemoval.get(rule.getLeft()) + 1);
                    }
                }
            }
        }
        System.out.println("Nullables: " + nullables);
        //Generate all combos
        done = false;
        Set<CFGProduction> nextRuleSet = new HashSet<>();
        nextRuleSet.addAll(masterRuleSet);
        while (!done) {
            Set<CFGProduction> generatedRuleSet = new HashSet<>();
            {
                for (CFGProduction rule : nextRuleSet) {
                    for (int i = 0; i < rule.getRightCount(); i++) {
                        if (rule.getRightCount() != 1 && nullables.contains(rule.getRightElement(i))) {
                            List<Integer> genRight = new ArrayList<>();
                            genRight.addAll(rule.getRight());
                            genRight.remove(i);
                            generatedRuleSet.add(new CFGProduction(rule.getLeft(), genRight, context));
                        }
                    }
                }
                nextRuleSet = generatedRuleSet;
                masterRuleSet.addAll(generatedRuleSet);
                done = generatedRuleSet.isEmpty();
            }
        }
        System.out.println("/****************New reduced epsilon rule set*****************/");
        for (CFGProduction rule : masterRuleSet) {
            System.out.println(rule);
        }
        System.out.println("/*************************************************************/");
        Set <Integer> unitEpsilonRuleSet = new HashSet<>();
        Set <Integer> weakEpsilonRemove = new HashSet<>();
        for (CFGProduction rule : masterRuleSet) {
            if (rule.getRightCount() == 1 &&
                    (int) rule.getRightElement(0) == context.tagEncode(TagRecord.EMP_LABEL, TagPriority.PAR)) {
                unitEpsilonRuleSet.add(rule.getLeft());
            } else {
                weakEpsilonRemove.add(rule.getLeft());
            }
        }
        System.out.println("Unit epsilon rule nonterminals: " + unitEpsilonRuleSet);
        System.out.println("Meaningful nonterminals: " + weakEpsilonRemove);
        nextRuleSet = new LinkedHashSet<>();
        for (CFGProduction rule : masterRuleSet) {
            if (weakEpsilonRemove.contains(rule.getLeft())) {
                if (rule.getRightCount() > 1 ||
                        (int) rule.getRightElement(0) != context.tagEncode(TagRecord.EMP_LABEL, TagPriority.PAR)) {
                    nextRuleSet.add(rule);
                }
            } else if (!unitEpsilonRuleSet.contains(rule.getLeft())) {
                boolean found = false;
                for (Integer right : rule.getRight()) {
                    if (unitEpsilonRuleSet.contains(right)) found = true;
                }
                if (found) continue;
                nextRuleSet.add(rule);
            }
        }
        masterRuleSet = (LinkedHashSet<CFGProduction>) nextRuleSet;
        System.out.println("/********************New non-epsilon rule set*****************/");
        for (CFGProduction rule : masterRuleSet) {
            System.out.println(rule);
        }
        System.out.println("/*************************************************************/");
        unitEpsilonRuleSet.removeAll(weakEpsilonRemove);
        System.out.println("Epsilon rules removed.");
        return unitEpsilonRuleSet;
    }
    /**
     * Remove unit generations. Only useful for analyzing grammars, not so much for parsing.
     *
     * Unit generations are rules in the for of A -> B, B -> a, or similar.
     *
     * @return A set of integers representing non-terminal labels that are no longer being used by the new rules.
     */
    private Set<Integer> unitGenRemoval() {
        System.out.println("Removing unit productions.");
        Set<CFGProduction> unitProductions = new HashSet<>();
        Set<CFGProduction> otherProductions = new HashSet<>();
        for (CFGProduction rule : masterRuleSet) {
            if (rule.getRightCount() == 1 &&
                    !context.isTerminal(rule.getLeft()) &&
                    !context.isTerminal(rule.getRightElement(0))) {
                unitProductions.add(rule);
            } else {
                otherProductions.add(rule);
            }
        }
        System.out.println("/***************Located unit production rules*****************/");
        for (CFGProduction rule : unitProductions) {
            System.out.println(rule);
        }
        System.out.println("/*************************************************************/");
        System.out.println("/***************Located other production rules****************/");
        for (CFGProduction rule : otherProductions) {
            System.out.println(rule);
        }
        System.out.println("/*************************************************************/");
        Set<CFGProduction> newRules = new HashSet<>();
        for (CFGProduction rule : unitProductions) {
            for (CFGProduction subRule : otherProductions) {
                if (rule.getRightElement(0) == subRule.getLeft()) {
                    newRules.add(new CFGProduction(rule.getLeft(), subRule.getRight(), context));
                }
            }
        }
        System.out.println("/***************Rules to maintain equivalence****************/");
        for (CFGProduction rule : newRules) {
            System.out.println(rule);
        }
        System.out.println("/*************************************************************/");
        masterRuleSet.removeAll(unitProductions);
        masterRuleSet.addAll(newRules);
        System.out.println("/**************New unit-production-less rule set**************/");
        for (CFGProduction rule : masterRuleSet) {
            System.out.println(rule);
        }
        System.out.println("/*************************************************************/");
        Set<Integer> droppedNonTerms = new HashSet<>();
        for (CFGProduction rule : unitProductions) {
            boolean found = false;
            for (CFGProduction subRule : masterRuleSet) {
                if (subRule.getLeft() == rule.getLeft()) {
                    found = true;
                    break;
                }
            }
            if (found) continue;
            droppedNonTerms.add(rule.getLeft());
        }
        System.out.println("/********************Dropped non-terminals********************/");
        for (Integer term : droppedNonTerms) {
            System.out.println(term);
        }
        System.out.println("/*************************************************************/");
        System.out.println("Unit productions removed");
        return droppedNonTerms;
    }
    /**
     * Removes rules that do not terminate, specifically non-terminals that are not reached,
     * or rules that are recursive, but do not have a terminal state such as C -> aCb.
     *
     * @return A set of integers representing non-terminal labels that are no longer being used by the new rules.
     */
    private Set<Integer> uselessRemoval() {
        System.out.println("Removing useless rules.");
        Set<Integer> terminalGenerator = new HashSet<>();
        Set<CFGProduction> terminalRules = new HashSet<>();
        for (CFGProduction rule : masterRuleSet) {
            boolean found = false;
            for (Integer k : rule.getRight()) {
                if (!context.isTerminal(k)) {
                    found = true;
                    break;
                }
            }
            if (found) continue;
            terminalGenerator.add(rule.getLeft());
            terminalRules.add(rule);
        }
        System.out.println("/******************Initial terminal rule set******************/");
        for (CFGProduction rule : terminalRules) {
            System.out.println(rule);
        }
        System.out.println("/*************************************************************/");
        boolean done = false;
        while (!done) {
            done = true;
            for (CFGProduction rule : masterRuleSet) {
                if (!terminalRules.contains(rule)) {
                    boolean found = false;
                    for (Integer k : rule.getRight()) {
                        if (!context.isTerminal(k) && !terminalGenerator.contains(k)) {
                            found = true;
                        }
                    }
                    if (found) continue;
                    done = false;
                    terminalGenerator.add(rule.getLeft());
                    terminalRules.add(rule);
                }
            }
        }
        Set<Integer> removed = new HashSet<>();
        Set<CFGProduction> removedRules = new HashSet<>();
        for (CFGProduction rule : masterRuleSet) {
            if (!terminalGenerator.contains(rule.getLeft())) {
                removed.add(rule.getLeft());
            }
            if (!terminalRules.contains(rule)) {
                removedRules.add(rule);
            }
        }
        masterRuleSet.removeAll(removedRules);
        System.out.println("/********************New reduced rule set*********************/");
        for (CFGProduction rule : masterRuleSet) {
            System.out.println(rule);
        }
        System.out.println("/*************************************************************/");
        System.out.println("/************************Removed rules************************/");
        for (CFGProduction rule : removedRules) {
            System.out.println(rule);
        }
        System.out.println("/*************************************************************/");
        System.out.println("Useless rules removed.");
        return removed;
    }
    /**
     * Remove isolated and unreachable non-terminals more specifically than uselessRemoval. This may be slightly
     * helpful for CFG processing.
     *
     * @return A set of integers representing non-terminal labels that are no longer being used by the new rules.
     */
    private Set<Integer> isolateRemoval() {
        Set<Integer> accessible =
                new HashSet<>(Collections.singleton(context.tagEncode(TagRecord.START_LABEL, TagPriority.SUB)));
        Set<Integer> nonTerms = new HashSet<>();
        for (CFGProduction rule : masterRuleSet) {
            nonTerms.add(rule.getLeft());
            System.out.println(rule);
        }
        boolean done = false;
        while (!done) {
            Set<Integer> nextAccessible = new HashSet<>();
            for (CFGProduction rule : masterRuleSet) {
                if (accessible.contains(rule.getLeft())) {
                    for (Integer k : rule.getRight()) {
                        if (!accessible.contains(k)) {
                            nextAccessible.add(k);
                        }
                    }
                }
            }
            accessible.addAll(nextAccessible);
            done = nextAccessible.isEmpty();
        }
        System.out.println("/******************Initial accessible set*********************/");
        for (Integer k : accessible) {
            System.out.println(context.tagDecode(k, TagPriority.SUB));
        }
        System.out.println("/*************************************************************/");
        LinkedHashSet<CFGProduction> nextMaster = new LinkedHashSet<>();
        for (CFGProduction rule : masterRuleSet) {
            if (accessible.contains(rule.getLeft())) {
                nextMaster.add(rule);
            }
        }
        masterRuleSet = nextMaster;
        nonTerms.removeAll(accessible);
        System.out.println("/************Set with unreachable states removed**************/");
        for (CFGProduction k : masterRuleSet) {
            System.out.println(k);
        }
        System.out.println("/*************************************************************/");
        return nonTerms;
    }
    //First and follow sets
    /**
     * Produce the first and follow sets of the CFG
     */
    private void produceFirstAndFollowSets() {
        produceFirstSet();
        produceFollowSet();
    }
    /**
     * Produce, for each label, all possible characters in the text that can appear as the first character of the
     * lexeme represented by said label.
     */
    private void produceFirstSet() {
        Map<Integer, Set<Integer>> firstSets = new HashMap<>();
        for (Integer k = 0; k < context.terminalCount(); k++) {
            firstSets.put(k, new HashSet<>(Collections.singleton(k)));
        }
        for (CFGProduction rule : masterRuleSet) {
            if (!firstSets.containsKey(rule.getLeft())) {
                firstSets.put(rule.getLeft(), new HashSet<>());
            }
        }
        boolean done = false;
        while (!done) {
            done = true;
            for (CFGProduction rule : masterRuleSet) {
                boolean addEmpty = true;
                for (Integer k : rule.getRight()) {
                    for (Integer j : firstSets.get(k)) {
                        if (!firstSets.get(rule.getLeft()).contains(j) &&
                                j != context.tagEncode(TagRecord.EMP_LABEL, TagPriority.SUB)) {
                            firstSets.get(rule.getLeft()).add(j);
                            done = false;
                        }
                    }
                    if (!firstSets.get(k).contains(context.tagEncode(TagRecord.EMP_LABEL, TagPriority.SUB))) {
                        addEmpty = false;
                        break;
                    }
                }
                if (addEmpty) {
                    firstSets.get(rule.getLeft()).add(context.tagEncode(TagRecord.EMP_LABEL, TagPriority.SUB));
                }
            }
        }
        if (context.DEBUG) {
            System.out.println("/********************First Sets Generated*********************/");
            for (Integer source : firstSets.keySet()) {
                System.out.println(source + " (" + context.tagDecode(source, TagPriority.SUB) + "): " +
                        firstSets.get(source));
            }
            System.out.println("/*************************************************************/");
        }
        this.firstSets = firstSets;
    }
    /**
     * Produce, for each non-terminal, all characters that can directly follow the non-terminal.
     */
    private void produceFollowSet() {
        Map<Integer, Set<Integer>> followSets = new HashMap<>();
        for (int i = context.terminalCount(); i < context.tagCount(); i++) {
            followSets.put(i, new HashSet<>());
        }
        followSets.get(
                rightRules.get(leftTagMap.get(context.tagEncode(TagRecord.START_LABEL, TagPriority.SUB))).get(0).getLeft()
        ).add(context.tagEncode(TagRecord.EOF_LABEL, TagPriority.SUB));
        boolean done = false;
        while (!done) {
            done = true;
            for (CFGProduction rule : masterRuleSet) {
                //Production A -> aBc
                for (int i = 0; i < rule.getRightCount() - 1; i++) {
                    Set<Integer> followSetB = followSets.get(rule.getRightElement(i));
                    if (!context.isTerminal(rule.getRightElement(i))) {
                        boolean found = true;
                        //For each c_j from one after i to da ENDZ
                        for (int j = i + 1; j < rule.getRightCount(); j++) {
                            boolean foundEpsilon = false;
                            Integer cjTag = rule.getRightElement(j);
                            if (context.isTerminal(rule.getRightElement(j))) { //Terminal or empty
                                if (!Objects.equals(cjTag, context.tagEncode(TagRecord.EMP_LABEL, TagPriority.SUB))) {
                                    if (!followSetB.contains(rule.getRightElement(j))) {
                                        //Add c_j to follow(B)
                                        followSetB.add(cjTag);
                                        done = false;
                                    }
                                } else {
                                    if (j == rule.getRightCount() - 1) {
                                        foundEpsilon = true;
                                    }
                                }
                            } else { //Nonterminal
                                //For each member of FIRST(c_j)
                                for (Integer k : firstSets.get(cjTag)) {
                                    if (!followSetB.contains(k) &&
                                            (int) k != context.tagEncode(TagRecord.EMP_LABEL, TagPriority.SUB)) {
                                        //Add k to follow(B)
                                        done = false;
                                        followSetB.add(k);
                                    }
                                    if ((int) k == context.tagEncode(TagRecord.EMP_LABEL, TagPriority.SUB)) {
                                        foundEpsilon = true;
                                    }
                                }
                            }
                            //Can only continue if epsilon is located
                            if (!foundEpsilon) {
                                found = false;
                                break;
                            }
                        }
                        //FIRST(c) has epsilon, add follows
                        if (found) {
                            for (Integer k : followSets.get(rule.getLeft())) {
                                if (!followSets.get(rule.getRightElement(i)).contains(k)) {
                                    done = false;
                                    followSets.get(rule.getRightElement(i)).add(k);
                                }
                            }
                        }
                    }
                }
                //A -> aB
                for (Integer k : followSets.get(rule.getLeft())) {
                    if (!context.isTerminal(rule.getRightElement(rule.getRightCount() - 1))) {
                        if (!followSets.get(rule.getRightElement(rule.getRightCount() - 1)).contains(k)) {
                            done = false;
                            followSets.get(rule.getRightElement(rule.getRightCount() - 1)).add(k);
                        }
                    }
                }
            }
        }
        if (context.DEBUG) {
            System.out.println("/********************Follow Sets Generated********************/");
            for (Integer source : followSets.keySet()) {
                System.out.println(source + " (" + context.tagDecode(source, TagPriority.SUB) + "): " + followSets.get(source));
            }
            System.out.println("/*************************************************************/");
        }
        this.followSets = followSets;
    }
    /**
     * Fetch the first set for a given non-terminal.
     *
     * @param nonterm The non-terminal-representing integer.
     * @return A set of integers representing all the terminals the given non-terminal can be in front of.
     */
    public Set<Integer> getFirstSet(Integer nonterm) {
        return firstSets.get(nonterm);
    }
    /**
     * Fetch the follow set for a given non-terminal.
     *
     * @param nonterm The non-terminal-representing integer.
     * @return A set of integers representing all the terminals the given non-terminal can be trailed by.
     */
    public Set<Integer> getFollowSet(Integer nonterm) {
        return followSets.get(nonterm);
    }
}
