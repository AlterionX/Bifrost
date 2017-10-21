package parser;

import logger.Log;
import tagtable.Tag;
import tagtable.TagTable;
import tagtable.TagPriority;

import java.util.*;
import java.util.stream.Collectors;

//Rename to Drapa
class CFG {
    //region Global data
    private static final Set<CFGProductionType> SUPPORTED_RULE_TYPES = new HashSet<>(Collections.singletonList(
            CFGProductionType.SBNF
    ));
    //endregion

    //region Master collections
    //Easily indexable array of rules. All these are listed in the associated rules
    private final List<CFGProduction> ruleList = new ArrayList<>();
    private LinkedHashSet<CFGProduction> masterRuleSet = new LinkedHashSet<>();
    //endregion

    //region Tag/rule mappings
    //Tag to produce to integer representing index of list of rules
    private final Map<Tag, Integer> leftTagMap = new HashMap<>();
    private final List<List<CFGProduction>> rightRules = new ArrayList<>();
    //endregion

    //region First and follow sets
    //First and follow sets of the grammar
    private Map<Tag, Set<Tag>> firstSets = new HashMap<>();
    private Map<Tag, Set<Tag>> followSets = new HashMap<>();
    private final TagTable tagTable; //Reference to tagtable
    //endregion

    //Class/rule set initialization
    /**
     * Setup and parse the CFG declaration file.
     * @param tagTable The context data, AST, and symtable
     * @param config The configuration data.
     */
    public CFG(TagTable tagTable, String config) {
        this.tagTable = tagTable;
        //Split into list, then filter out empty lines and comments
        List<String> configLines = Arrays.stream(config.trim().split("\n"))
                .map(String::trim)
                .filter(t -> !t.isEmpty() && t.charAt(0) != '#')
                .collect(Collectors.toList());
        CFGProductionType type = parseFormat(configLines);
        parseCFG(configLines, type); //Notice side effect
        //Show results
        printProductions();
    }

    //Rule file parsing
    /**
     * Tease out the format section of the configuration file.
     * @param configList The list of lines within the configuration file. Will have the line relevant removed if grammar type exists.
     * @return The type listed, or SBNF if not declared.
     */
    private CFGProductionType parseFormat(List<String> configList) {
        //Read data
        CFGProductionType cfgType = CFGProductionType.SBNF;
        if (configList.get(0).startsWith(">GRAMMAR_TYPE")) {
            String[] split = configList.remove(0).split("\\s+");
            String typeStr = split[1].trim();
            if (split.length == 2) {
                try {
                    cfgType = CFGProductionType.valueOf(typeStr);
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                    System.out.println("Provided argument: " + typeStr);
                }
            } else {
                System.out.println("Syntax error in grammar declaration. Expected \">GRAMMAR_TYPE TYPE\", found \"" + typeStr + "\"");
            }
            if (!SUPPORTED_RULE_TYPES.contains(cfgType)) {
                throw new RuntimeException("Extended CFG grammar formats unsupported.");
            }
        } else {
            System.out.println("SBNF Grammar syntax assumed.");
        }
        return cfgType;
    }
    /**
     * Parse the given input as a list of CFG productions.
     * @param input The input.
     * @param pt The production type.
     */
    private void parseCFG(List<String> input, CFGProductionType pt) {
        //region Preprocess + parsing
        switch (pt) {
            case SBNF: {
                //region SBNF parsing
                for (String splitRule : input) {
                    int ruleBreak = splitRule.indexOf("->");
                    String left = splitRule.substring(0, ruleBreak).trim();
                    List<String> right = Arrays.stream(splitRule.substring(ruleBreak + 2).trim().split("\\s+"))
                            .map(String::trim).collect(Collectors.toList());

                    //SBNF ensures only one rule per instruction, with all tokens split by whitespace, not including the ->
                    //Assume first rule is starting rule
                    if (masterRuleSet.isEmpty() && !left.equals(TagTable.START_LABEL)) {
                        masterRuleSet.addAll(parseProduction(
                                TagTable.START_LABEL,
                                new ArrayList<>(Arrays.asList(left, TagTable.EOF_LABEL)),
                                pt
                        ));
                    }
                    masterRuleSet.addAll(parseProduction(left, right, pt));
                }
                break;
                //endregion
            } default: {
                //region Unsupported format
                System.out.println("Unsupported format.");
                throw new RuntimeException("Unsupported CFG format.");
                //endregion
            }
        }
        //endregion

        //region Grammar optimization
        //Set<Tag> removed = new HashSet<>();
        //if (OPTIMIZE_GRAMMAR) {
        //    removed.addAll(epsilonRemoval());
        //    removed.addAll(unitGenRemoval());
        //    removed.addAll(uselessRemoval());
        //    removed.addAll(isolateRemoval());
        //}
        //endregion

        //region Maintain data structures
        //Add rules to the associative containers
        for (CFGProduction rule : masterRuleSet) {
            if (!leftTagMap.containsKey(rule.getLeft())) {
                leftTagMap.put(rule.getLeft(), rightRules.size());
                rightRules.add(new ArrayList<>());
            }
            rightRules.get(leftTagMap.get(rule.getLeft())).add(rule);
            ruleList.add(rule);
        }
        //Check for unreachable conditions
        if (tagTable.tagCount() - tagTable.terminalTagCount() != leftTagMap.size()) {
            Set<Tag> missing = tagTable.fetchAllTags().stream()
                    .filter(t -> !tagTable.isTerminalTag(t)).collect(Collectors.toSet());
            missing.removeAll(leftTagMap.keySet());
            throw new RuntimeException("CFG contains nonterminals " + missing + " without rules.");
        }
        //endregion

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
            case SBNF: {
                parsedProductions.add(new CFGProduction(left, rightSeries, tagTable));
                break;
            } default: {
                System.out.println("Unsupported format.");
                throw new RuntimeException("Unsupported CFG format.");
            }
        }
        return parsedProductions;
    }

    /**
     * Prints the productions.
     */
    private void printProductions() {
        Log.dln("/********************Parsed CFG Rules************************/");
        for (Tag tag : leftTagMap.keySet()) {
            for (CFGProduction rule : rightRules.get(leftTagMap.get(tag))) {
                Log.dln(rule.toString());
            }
        }
        Log.dln("/************************************************************/");
    }

    //Rule retrieval
    /**
     * Fetch the initial production.
     * @return The initial production.
     */
    public Tag fetchZeroInstruction() {
        return masterRuleSet.iterator().next().getLeft();
    }
    /**
     * Fetch all rules produced by a given non-terminal
     * @param left The number representing a non-terminal label.
     * @return The rules associated with the non-terminal.
     */
    public List<CFGProduction> fetchRulesForLeft(Tag left) {
        if (!tagTable.isTerminalTag(left)) {
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
        Map<Tag, Set<Tag>> firstSets = new HashMap<>();
        for (Tag t: tagTable.fetchTags(TagPriority.LEX)) {
            firstSets.put(t, new HashSet<>(Collections.singleton(t)));
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
                for (Tag k : rule.getRight()) {
                    for (Tag j : firstSets.get(k)) {
                        if (!firstSets.get(rule.getLeft()).contains(j) && (!j.equals(tagTable.EMP_TAG))) {
                            firstSets.get(rule.getLeft()).add(j);
                            done = false;
                        }
                    }
                    if (!firstSets.get(k).contains(tagTable.EMP_TAG)) {
                        addEmpty = false;
                        break;
                    }
                }
                if (addEmpty) {
                    firstSets.get(rule.getLeft()).add(tagTable.EMP_TAG);
                }
            }
        }
        Log.dln("/********************First Sets Generated*********************/");
        for (Tag source : firstSets.keySet()) {
            Log.dln(source + " (" + source.getValue() + "): " +
                    firstSets.get(source));
        }
        Log.dln("/*************************************************************/");
        this.firstSets = firstSets;
    }
    /**
     * Produce, for each non-terminal, all characters that can directly follow the non-terminal.
     */
    private void produceFollowSet() {
        Map<Tag, Set<Tag>> followSets = new HashMap<>();
        for (Tag t : tagTable.fetchTags(TagPriority.PAR)) {
            followSets.put(t, new HashSet<>());
        }
        followSets.get(
                rightRules.get(leftTagMap.get(tagTable.START_TAG)).get(0).getLeft()
        ).add(tagTable.EOF_TAG);
        boolean done = false;
        while (!done) {
            done = true;
            for (CFGProduction rule : masterRuleSet) {
                //Production A -> aBc
                for (int i = 0; i < rule.getRightCount() - 1; i++) {
                    Set<Tag> followSetB = followSets.get(rule.getRightElement(i));
                    if (!tagTable.isTerminalTag(rule.getRightElement(i))) {
                        boolean found = true;
                        //For each c_j from one after i to da ENDZ
                        for (int j = i + 1; j < rule.getRightCount(); j++) {
                            boolean foundEpsilon = false;
                            Tag cjTag = rule.getRightElement(j);
                            if (tagTable.isTerminalTag(rule.getRightElement(j))) { //Terminal or empty
                                if (!Objects.equals(cjTag, tagTable.EMP_TAG)) {
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
                                for (Tag k : firstSets.get(cjTag)) {
                                    if (!followSetB.contains(k) && k != tagTable.EMP_TAG) {
                                        //Add k to follow(B)
                                        done = false;
                                        followSetB.add(k);
                                    }
                                    if (k == tagTable.EMP_TAG) {
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
                            for (Tag k : followSets.get(rule.getLeft())) {
                                if (!followSets.get(rule.getRightElement(i)).contains(k)) {
                                    done = false;
                                    followSets.get(rule.getRightElement(i)).add(k);
                                }
                            }
                        }
                    }
                }
                //A -> aB
                for (Tag k : followSets.get(rule.getLeft())) {
                    if (!tagTable.isTerminalTag(rule.getRightElement(rule.getRightCount() - 1))) {
                        if (!followSets.get(rule.getRightElement(rule.getRightCount() - 1)).contains(k)) {
                            done = false;
                            followSets.get(rule.getRightElement(rule.getRightCount() - 1)).add(k);
                        }
                    }
                }
            }
        }
        Log.dln("/********************Follow Sets Generated********************//*");
        for (Tag source : followSets.keySet()) {
            Log.dln(source + " (" + source.getValue() + "): " + followSets.get(source));
        }
        Log.dln("/*************************************************************//*");
        this.followSets = followSets;
    }
    /**
     * Fetch the first set for a given non-terminal.
     *
     * @param nonterm The non-terminal-representing integer.
     * @return A set of integers representing all the terminals the given non-terminal can be in front of.
     */
    public Set<Tag> getFirstSet(Tag nonterm) {
        return firstSets.get(nonterm);
    }
    /**
     * Fetch the follow set for a given non-terminal.
     *
     * @param nonterm The non-terminal-representing integer.
     * @return A set of integers representing all the terminals the given non-terminal can be trailed by.
     */
    public Set<Tag> getFollowSet(Tag nonterm) {
        return followSets.get(nonterm);
    }
}
