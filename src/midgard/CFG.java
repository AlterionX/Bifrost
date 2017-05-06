package midgard;

import yggdrasil.TagPriority;
import yggdrasil.TagRecord;
import yggdrasil.Yggdrasil;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

//Rename to Drapa
public class CFG {
    private static final boolean OPTIMIZE_GRAMMAR =  false;
    public static final Set<CFGRuleType> SUPPORTED_RULE_TYPES = new HashSet<>(Arrays.asList(
            CFGRuleType.SBNF
    ));
    //Easily indexable array of rules. All these are listed in the associated rules
    private List<CFGRule> ruleList = new ArrayList<>();
    private LinkedHashSet<CFGRule> masterRuleSet = new LinkedHashSet<>();
    //Tags and rules
    private Map<Integer, Integer> leftTagMap = new HashMap<>();
    private List<List<CFGRule>> rightRules = new ArrayList<>();
    private boolean emptyGenerated = false;
    //First and follow sets of the grammar
    private Map<Integer, Set<Integer>> firstSets = new HashMap<>();
    private Map<Integer, Set<Integer>> followSets = new HashMap<>();
    private Yggdrasil parent;
    //Class/rule set initialization
    public CFG(Yggdrasil parent, String cfgConfig) {
        this.parent = parent;
        //Read data
        CFGRuleType rt = CFGRuleType.SBNF;
        if (cfgConfig.startsWith(">GRAMMAR_TYPE")) {
            String type = cfgConfig.substring(0, cfgConfig.indexOf("\n"));
            cfgConfig = cfgConfig.substring(type.length()).trim();
            type = type.trim();
            String[] split = type.split("\\s+");
            if (split.length != 2) {
                System.out.println("Syntax error in grammar declaration. Expected \">GRAMMAR_TYPE TYPE\", found" + type);
            } else {
                switch (split[1]) {
                    case "SBNF":
                        rt = CFGRuleType.SBNF;
                        break;
                    case "BNF":
                        rt = CFGRuleType.BNF;
                        break;
                    case "EBNF":
                        rt = CFGRuleType.EBNF;
                        break;
                    case "CNF":
                        rt = CFGRuleType.CNF;
                        break;
                    case "CFG":
                        rt = CFGRuleType.CFG;
                        break;
                    default:
                        System.out.println("Unrecognized requested grammar.");
                }
            }
            if (!SUPPORTED_RULE_TYPES.contains(rt)) {
                throw new RuntimeException("Extended CFG grammar formats unsupported.");
            }
        }
        parseCFG(cfgConfig, rt);
        //Show results
        System.out.println("/********************Parsed CFG Rules************************/");
        printRules();
        System.out.println("/************************************************************/");
    }
    //Rule file parsing
    private void parseCFG(String input, CFGRuleType rt) {
        //Split rules and do preprocessing
        String[] splitRules = input.split("\n");
        for (int i = 0; i < splitRules.length; i++) {
            splitRules[i] = splitRules[i].trim();
        }
        switch (rt) {
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
                                masterRuleSet.addAll(parseRule(TagRecord.START_LABEL,
                                        new ArrayList<>(Arrays.asList(left, TagRecord.EOF_LABEL)), rt));
                            }
                            masterRuleSet.addAll(parseRule(left, Arrays.asList(right), rt));
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
        for (CFGRule rule : masterRuleSet) {
            if (!leftTagMap.containsKey(rule.getLeft())) {
                leftTagMap.put(rule.getLeft(), rightRules.size());
                rightRules.add(new ArrayList<>());
            }
            rightRules.get(leftTagMap.get(rule.getLeft())).add(rule);
            ruleList.add(rule);
        }
        if (parent.tagCount() - parent.terminalCount() != leftTagMap.size() + removed.size()) {
            Set<Integer> missing = new HashSet<>();
            for (int i = parent.terminalCount(); i < parent.tagCount(); i++) {
                missing.add(i);
            }
            missing.removeAll(leftTagMap.keySet());
            Set<String> translated = new HashSet<>();
            for (Integer k : missing) {
                translated.add(parent.tagDecode(k, TagPriority.SUB));
            }
            throw new RuntimeException("CFG contains nonterminals " + translated + " without rules.");
        }
        produceFirstAndFollowSets();
    }
    //Parsing rules
    private List<CFGRule> parseRule(String left, List<String> rightSeries, CFGRuleType type) {
        List<CFGRule> parsedRules = new ArrayList<>();
        switch (type) {
            case SBNF:
                parsedRules.add(new CFGRule(left, rightSeries, parent));
                break;
            default:
                System.out.println("Unsupported format.");
                throw new RuntimeException("Unsupported CFG format.");
        }
        return parsedRules;
    }
    private void printRules() {
        for (Integer tag : leftTagMap.keySet()) {
            for (CFGRule rule : rightRules.get(leftTagMap.get(tag))) {
                System.out.println(rule);
            }
        }
    }
    //Rule retrieval
    public Integer fetchZeroInstruction() {
        return masterRuleSet.iterator().next().getLeft();
    }
    public List<CFGRule> fetchRulesForLeft(Integer left) {
        if (!parent.isTerminal(left)) {
            return rightRules.get(leftTagMap.get(left));
        } else {
            return new ArrayList<>();
        }
    }
    public int getRuleCount() {
        return masterRuleSet.size();
    }
    public Integer encodeRule(CFGRule rule) {
        return ruleList.indexOf(rule);
    }
    public CFGRule decodeRule(Integer index) {
        return ruleList.get(index);
    }
    //CFG simplification, not performed due to inability of parser
    private Set<Integer> epsilonRemoval() {
        System.out.println("Removing epsilon rules.");

        System.out.println("Finding nullables.");
        Set<Integer> nullables = new HashSet<>();
        boolean done = false;
        while (!done) {
            done = true;
            List<Integer> qualifiedRemoval = new ArrayList<>();
            for (int i = 0; i < parent.tagCount(); i++) {
                qualifiedRemoval.add(0);
            }
            for (CFGRule rule : masterRuleSet) {
                if (rule.getRight().contains(parent.tagEncode(TagRecord.EMP_LABEL, TagPriority.PAR))) {
                    if (rule.getRightCount() == 1 && !nullables.contains(rule.getLeft())) {
                        //
                        nullables.add(rule.getLeft());
                        qualifiedRemoval.set(rule.getLeft(), qualifiedRemoval.get(rule.getLeft()) + 1);
                    } else {
                        for (int i = rule.getRightCount() - 1; i >= 0; i--) {
                            if ((int) rule.getRightElement(i) == parent.tagEncode(TagRecord.EMP_LABEL, TagPriority.PAR)) {
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
        Set<CFGRule> nextRuleSet = new HashSet<>();
        nextRuleSet.addAll(masterRuleSet);
        while (!done) {
            Set<CFGRule> generatedRuleSet = new HashSet<>();
            {
                for (CFGRule rule : nextRuleSet) {
                    for (int i = 0; i < rule.getRightCount(); i++) {
                        if (rule.getRightCount() != 1 && nullables.contains(rule.getRightElement(i))) {
                            List<Integer> genRight = new ArrayList<>();
                            genRight.addAll(rule.getRight());
                            genRight.remove(i);
                            generatedRuleSet.add(new CFGRule(rule.getLeft(), genRight, parent));
                        }
                    }
                }
                nextRuleSet = generatedRuleSet;
                masterRuleSet.addAll(generatedRuleSet);
                done = generatedRuleSet.isEmpty();
            }
        }
        System.out.println("/****************New reduced epsilon rule set*****************/");
        for (CFGRule rule : masterRuleSet) {
            System.out.println(rule);
        }
        System.out.println("/*************************************************************/");
        Set <Integer> unitEpsilonRuleSet = new HashSet<>();
        Set <Integer> weakEpsilonRemove = new HashSet<>();
        for (CFGRule rule : masterRuleSet) {
            if (rule.getRightCount() == 1 &&
                    (int) rule.getRightElement(0) == parent.tagEncode(TagRecord.EMP_LABEL, TagPriority.PAR)) {
                unitEpsilonRuleSet.add(rule.getLeft());
            } else {
                weakEpsilonRemove.add(rule.getLeft());
            }
        }
        System.out.println("Unit epsilon rule nonterminals: " + unitEpsilonRuleSet);
        System.out.println("Meaningful nonterminals: " + weakEpsilonRemove);
        nextRuleSet = new LinkedHashSet<>();
        for (CFGRule rule : masterRuleSet) {
            if (weakEpsilonRemove.contains(rule.getLeft())) {
                if (rule.getRightCount() > 1 ||
                        (int) rule.getRightElement(0) != parent.tagEncode(TagRecord.EMP_LABEL, TagPriority.PAR)) {
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
        masterRuleSet = (LinkedHashSet<CFGRule>) nextRuleSet;
        System.out.println("/********************New non-epsilon rule set*****************/");
        for (CFGRule rule : masterRuleSet) {
            System.out.println(rule);
        }
        System.out.println("/*************************************************************/");
        unitEpsilonRuleSet.removeAll(weakEpsilonRemove);
        System.out.println("Epsilon rules removed.");
        return unitEpsilonRuleSet;
    }
    private Set<Integer> unitGenRemoval() {
        System.out.println("Removing unit productions.");
        Set<CFGRule> unitProductions = new HashSet<>();
        Set<CFGRule> otherProductions = new HashSet<>();
        for (CFGRule rule : masterRuleSet) {
            if (rule.getRightCount() == 1 &&
                    !parent.isTerminal(rule.getLeft()) &&
                    !parent.isTerminal(rule.getRightElement(0))) {
                unitProductions.add(rule);
            } else {
                otherProductions.add(rule);
            }
        }
        System.out.println("/***************Located unit production rules*****************/");
        for (CFGRule rule : unitProductions) {
            System.out.println(rule);
        }
        System.out.println("/*************************************************************/");
        System.out.println("/***************Located other production rules****************/");
        for (CFGRule rule : otherProductions) {
            System.out.println(rule);
        }
        System.out.println("/*************************************************************/");
        Set<CFGRule> newRules = new HashSet<>();
        for (CFGRule rule : unitProductions) {
            for (CFGRule subRule : otherProductions) {
                if (rule.getRightElement(0) == subRule.getLeft()) {
                    newRules.add(new CFGRule(rule.getLeft(), subRule.getRight(), parent));
                }
            }
        }
        System.out.println("/***************Rules to maintain equivalence****************/");
        for (CFGRule rule : newRules) {
            System.out.println(rule);
        }
        System.out.println("/*************************************************************/");
        masterRuleSet.removeAll(unitProductions);
        masterRuleSet.addAll(newRules);
        System.out.println("/**************New unit-production-less rule set**************/");
        for (CFGRule rule : masterRuleSet) {
            System.out.println(rule);
        }
        System.out.println("/*************************************************************/");
        Set<Integer> droppedNonTerms = new HashSet<>();
        for (CFGRule rule : unitProductions) {
            boolean found = false;
            for (CFGRule subRule : masterRuleSet) {
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
    private Set<Integer> uselessRemoval() {
        System.out.println("Removing useless rules.");
        Set<Integer> terminalGenerator = new HashSet<>();
        Set<CFGRule> terminalRules = new HashSet<>();
        for (CFGRule rule : masterRuleSet) {
            boolean found = false;
            for (Integer k : rule.getRight()) {
                if (!parent.isTerminal(k)) {
                    found = true;
                    break;
                }
            }
            if (found) continue;
            terminalGenerator.add(rule.getLeft());
            terminalRules.add(rule);
        }
        System.out.println("/******************Initial terminal rule set******************/");
        for (CFGRule rule : terminalRules) {
            System.out.println(rule);
        }
        System.out.println("/*************************************************************/");
        boolean done = false;
        while (!done) {
            done = true;
            for (CFGRule rule : masterRuleSet) {
                if (!terminalRules.contains(rule)) {
                    boolean found = false;
                    for (Integer k : rule.getRight()) {
                        if (!parent.isTerminal(k) && !terminalGenerator.contains(k)) {
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
        Set<CFGRule> removedRules = new HashSet<>();
        for (CFGRule rule : masterRuleSet) {
            if (!terminalGenerator.contains(rule.getLeft())) {
                removed.add(rule.getLeft());
            }
            if (!terminalRules.contains(rule)) {
                removedRules.add(rule);
            }
        }
        masterRuleSet.removeAll(removedRules);
        System.out.println("/********************New reduced rule set*********************/");
        for (CFGRule rule : masterRuleSet) {
            System.out.println(rule);
        }
        System.out.println("/*************************************************************/");
        System.out.println("/************************Removed rules************************/");
        for (CFGRule rule : removedRules) {
            System.out.println(rule);
        }
        System.out.println("/*************************************************************/");
        System.out.println("Useless rules removed.");
        return removed;
    }
    private Set<Integer> isolateRemoval() {
        Set<Integer> accessible =
                new HashSet<>(Collections.singleton(parent.tagEncode(TagRecord.START_LABEL, TagPriority.SUB)));
        Set<Integer> nonTerms = new HashSet<>();
        for (CFGRule rule : masterRuleSet) {
            nonTerms.add(rule.getLeft());
            System.out.println(rule);
        }
        boolean done = false;
        while (!done) {
            Set<Integer> nextAccessible = new HashSet<>();
            for (CFGRule rule : masterRuleSet) {
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
            System.out.println(parent.tagDecode(k, TagPriority.SUB));
        }
        System.out.println("/*************************************************************/");
        LinkedHashSet<CFGRule> nextMaster = new LinkedHashSet<>();
        for (CFGRule rule : masterRuleSet) {
            if (accessible.contains(rule.getLeft())) {
                nextMaster.add(rule);
            }
        }
        masterRuleSet = nextMaster;
        nonTerms.removeAll(accessible);
        System.out.println("/************Set with unreachable states removed**************/");
        for (CFGRule k : masterRuleSet) {
            System.out.println(k);
        }
        System.out.println("/*************************************************************/");
        return nonTerms;
    }
    //First and follow sets
    private void produceFirstAndFollowSets() {
        produceFirstSet();
        produceFollowSet();
    }
    private void produceFirstSet() {
        Map<Integer, Set<Integer>> firstSets = new HashMap<>();
        for (Integer k = 0; k < parent.terminalCount(); k++) {
            firstSets.put(k, new HashSet<>(Collections.singleton(k)));
        }
        for (CFGRule rule : masterRuleSet) {
            if (!firstSets.containsKey(rule.getLeft())) {
                firstSets.put(rule.getLeft(), new HashSet<>());
            }
        }
        boolean done = false;
        while (!done) {
            done = true;
            for (CFGRule rule : masterRuleSet) {
                boolean addEmpty = true;
                for (Integer k : rule.getRight()) {
                    for (Integer j : firstSets.get(k)) {
                        if (!firstSets.get(rule.getLeft()).contains(j) &&
                                j != parent.tagEncode(TagRecord.EMP_LABEL, TagPriority.SUB)) {
                            firstSets.get(rule.getLeft()).add(j);
                            done = false;
                        }
                    }
                    if (!firstSets.get(k).contains(parent.tagEncode(TagRecord.EMP_LABEL, TagPriority.SUB))) {
                        addEmpty = false;
                        break;
                    }
                }
                if (addEmpty) {
                    firstSets.get(rule.getLeft()).add(parent.tagEncode(TagRecord.EMP_LABEL, TagPriority.SUB));
                }
            }
        }

        System.out.println("/********************First Sets Generated*********************/");
        for (Integer source : firstSets.keySet()) {
            System.out.println(source + " (" + parent.tagDecode(source, TagPriority.SUB) + "): " +
                    firstSets.get(source));
        }
        System.out.println("/*************************************************************/");
        this.firstSets = firstSets;
    }
    private void produceFollowSet() {
        Map<Integer, Set<Integer>> followSets = new HashMap<>();
        for (int i = parent.terminalCount(); i < parent.tagCount(); i++) {
            followSets.put(i, new HashSet<>());
        }
        followSets.get(
                rightRules.get(leftTagMap.get(parent.tagEncode(TagRecord.START_LABEL, TagPriority.SUB))).get(0).getLeft()
        ).add(parent.tagEncode(TagRecord.EOF_LABEL, TagPriority.SUB));
        boolean done = false;
        while (!done) {
            done = true;
            for (CFGRule rule : masterRuleSet) {
                //Production A -> aBc
                for (int i = 0; i < rule.getRightCount() - 1; i++) {
                    Set<Integer> followSetB = followSets.get(rule.getRightElement(i));
                    if (!parent.isTerminal(rule.getRightElement(i))) {
                        boolean found = true;
                        //For each c_j from one after i to da ENDZ
                        for (int j = i + 1; j < rule.getRightCount(); j++) {
                            boolean foundEpsilon = false;
                            Integer cjTag = rule.getRightElement(j);
                            if (parent.isTerminal(rule.getRightElement(j))) { //Terminal or empty
                                if (cjTag != parent.tagEncode(TagRecord.EMP_LABEL, TagPriority.SUB)) {
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
                                            (int) k != parent.tagEncode(TagRecord.EMP_LABEL, TagPriority.SUB)) {
                                        //Add k to follow(B)
                                        done = false;
                                        followSetB.add(k);
                                    }
                                    if ((int) k == parent.tagEncode(TagRecord.EMP_LABEL, TagPriority.SUB)) {
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
                    if (!parent.isTerminal(rule.getRightElement(rule.getRightCount() - 1))) {
                        if (!followSets.get(rule.getRightElement(rule.getRightCount() - 1)).contains(k)) {
                            done = false;
                            followSets.get(rule.getRightElement(rule.getRightCount() - 1)).add(k);
                        }
                    }
                }
            }
        }
        System.out.println("/********************Follow Sets Generated********************/");
        for (Integer source : followSets.keySet()) {
            System.out.println(source + " (" + parent.tagDecode(source, TagPriority.SUB) + "): " + followSets.get(source));
        }
        System.out.println("/*************************************************************/");
        this.followSets = followSets;
    }
    public Set<Integer> getFirstSet(Integer nonterm) {
        return firstSets.get(nonterm);
    }
    public Set<Integer> getFollowSet(Integer nonterm) {
        return followSets.get(nonterm);
    }
}
