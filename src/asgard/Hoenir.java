package asgard;

import javafx.util.Pair;
import tagtable.Tag;
import tagtable.TagTable;
import ast.Branch;
import tagtable.TagPriority;
import symtable.Nidhogg;
import config.PathHolder;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class Hoenir extends Stag {
    private String outFileFormatString = null;
    private String targetIn;
    private BufferedWriter targetIRL;
    private Map<Tag, IRGroup> ruleGroups;

    /**
     * Construct the walker and launch the parsing process for children.
     * @param symTable The symtable.
     */
    public Hoenir(PathHolder holder, TagTable tagTable, Nidhogg symTable) {
        super(tagTable, holder, symTable, false);
        //Read input for intermediate code generation patterns
        List<String> ruleLines;
        try {
            ruleLines = Files.readAllLines(Paths.get(holder.BASE_DIR +
                    holder.TARGET + holder.INTERMEDIATE_REPRESENTATION_LANG_DEC_EXTENSION));
            ruleLines = ruleLines.stream().filter(
                    line -> !line.isEmpty()).map(String::trim).collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Issues with the analyzer's declaration file.");
        }
        outFileFormatString = holder.BASE_DIR + holder.IRL_BASE_DIR + "%s" +
                holder.IRL_CODE_EXTENSION;
        parseIRConfig(ruleLines);
        //if (context.DEBUG) printIRConfig();
    }

    /**
     * Parse the intermediate rule representation config declaration.
     * @param rules The list of strings representing rules.
     */
    private void parseIRConfig(List<String> rules) {
        for (int i = 0; i < rules.size(); i++) {
            rules.add(i, rules.remove(i).trim());
        }
        ruleGroups = new HashMap<>();
        int i = 0;
        while (i < rules.size()) {
            if (rules.get(i).startsWith(">")) {
                //Output
                outFileFormatString = rules.get(i).substring(1);
                i++;
            } else {
                i = parseRuleGroup(rules, i, ruleGroups);
            }
        }
    }
    /**
     * Parse the next chunk of rules in the stream of rules.
     * @param ruleStream The stream of rules.
     * @param position The current position in the stream.
     * @param ruleGroups The place to put these new groups.
     * @return The current position of the stream.
     */
    private int parseRuleGroup(List<String> ruleStream, int position, Map<Tag, IRGroup> ruleGroups) {
        IRGroup ruleGroup;
        if (ruleStream.get(position).startsWith("/DECODE")) {
            String[] decodeStartRuleLine = ruleStream.get(position).split("\\s+");
            if (decodeStartRuleLine.length != 2) {
                throw new RuntimeException("Malformed initial line for IRGroup.");
            }
            ruleGroup = new IRGroup(super.tagTable.addElseFindTag(TagPriority.PAR, decodeStartRuleLine[1]));
        } else {
            System.out.println("Error reading file rules.");
            throw new RuntimeException("Malformed IR representation rules.");
        }
        position++;
        while (!ruleStream.get(position).startsWith("/END")) {
            position = parseRuleChunk(ruleStream, position, ruleGroup);
        }
        position++;
        ruleGroups.put(ruleGroup.getTag(), ruleGroup);
        return position;
    }
    /**
     * Parse a group of a rule chunk.
     * @param ruleStream The stream of rules.
     * @param position The current position in the stream we are at.
     * @param group The group to add this sub-chunk to.
     * @return The new position in the stream.
     */
    private int parseRuleChunk(List<String> ruleStream, int position, IRGroup group) {
        IRChunk chunk = new IRChunk();
        if (ruleStream.get(position).startsWith("/COND")) {
            String[] rule = ruleStream.get(position).split("\\s+");
            if (rule.length < 3 || rule.length % 2 == 0) {
                throw new RuntimeException("Malformed IRGroup condition");
            }
            for (int i = 1; i < rule.length - 1; i += 2) {
                chunk.addCondition(Integer.parseInt(rule[i]), super.tagTable.addElseFindTag(TagPriority.PAR, rule[i + 1]));
            }
        } else if (ruleStream.get(position).startsWith("/CATCHALL")) {
            chunk.setUniversal();
        } else {
            throw new RuntimeException("Illegal start of IRRuleBlock.");
        }
        position++;
        while (!ruleStream.get(position).startsWith("/END")) {
            position = parseIRRule(ruleStream, position, chunk);
        }
        position++;
        group.addIRRuleChunk(chunk);
        return position;
    }
    /**
     * Parse a single rule inside of a group.
     * @param ruleStream The stream of rules.
     * @param position The position in the stream.
     * @param chunk The chunk to populate.
     * @return The new position in the stream.
     */
    private int parseIRRule(List<String> ruleStream, int position, IRChunk chunk) {
        if (ruleStream.get(position).startsWith("%F :") || ruleStream.get(position).startsWith("%F:")) {
            chunk.addProductionIRRule(new IRRule(ruleStream.get(position)));
            position++;
        } else {
            System.out.println("ERROR" + (ruleStream.get(position).startsWith("%F :") || ruleStream.get(position).startsWith("%F:")));
            System.out.println(ruleStream.get(position));
            throw new RuntimeException("Initial IRRule must consist of a single production rule.");
        }
        while (!ruleStream.get(position).startsWith("/END")) {
            StringBuilder rules;
            rules = new StringBuilder(ruleStream.get(position));
            chunk.addIRRule(new IRRule(rules.toString()));
            position++;
        }
        return position;
    }
    /**
     * Print the parsed rules.
     */
    private void printIRConfig() {
        for (Tag k : ruleGroups.keySet()) {
            ruleGroups.get(k).printRules();
        }
    }

    /**
     * Initialize a walk with a target output.
     * @param target The target input name.
     */
    public void prime(String target) {
        targetIn = target;
        try {
            Files.deleteIfExists(Paths.get(super.holder.BASE_DIR +
                    String.format(outFileFormatString, targetIn)));
            Files.createFile(Paths.get(super.holder.BASE_DIR +
                    String.format(outFileFormatString, targetIn)));
            targetIRL = Files.newBufferedWriter(Paths.get(super.holder.BASE_DIR +
                    String.format(outFileFormatString, targetIn)));
        } catch (IOException e0) {
            e0.printStackTrace();
            try {
                Files.createDirectories(Paths.get(super.holder.BASE_DIR +
                        String.format(outFileFormatString, targetIn)).getParent());
                Files.deleteIfExists(Paths.get(super.holder.BASE_DIR +
                        String.format(outFileFormatString, targetIn)));
                Files.createFile(Paths.get(super.holder.BASE_DIR +
                            String.format(outFileFormatString, targetIn)
                ));
                targetIRL = Files.newBufferedWriter(Paths.get(super.holder.BASE_DIR +
                        String.format(outFileFormatString, targetIn)));
            } catch (IOException e1) {
                e1.printStackTrace();
                throw new RuntimeException("Cannot create irl target.");
            }
        }
    }

    private final Stack<ArrayList<String>> data = new Stack<>();
    private final Stack<LinkedList<StringBuilder>> output = new Stack<>();
    @Override
    protected boolean onLaunch() {
        try {
            targetIRL.write("#Intermediate Representation Generation\n");
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Cannot write to file.");
        }
        data.push(new ArrayList<>());
        output.push(new LinkedList<>());
        return false;
    }
    @Override
    protected boolean onUpEnter(Branch branch) {
        data.push(new ArrayList<>());
        output.push(new LinkedList<>());
        return false;
    }
    @Override
    protected boolean onDownEnter(Branch branch, Branch child) {
        return false;
    }
    @Override
    protected boolean onUpExit(Branch branch) {
        ArrayList<String> children = data.pop();
        LinkedList<StringBuilder> childOutput = output.pop();
        String production = "";
        StringBuilder outputString = new StringBuilder("");
        if (ruleGroups.containsKey(branch.getTag())) {
            //Do rule things, pushing the final one to the next arraylist up
            IRGroup group = ruleGroups.get(branch.getTag());
            IRChunk workingChunk = group.getCatchAll();
            for (IRChunk chunk : group.getChunks()) {
                if (chunk.isMatchWith(branch)) {
                    workingChunk = chunk;
                    break;
                }
            }
            if (workingChunk != null && workingChunk.isMatchWith(branch)) {
                //Execute chunk
                Pair<String, StringBuilder> result = workingChunk.execute(branch, children, childOutput, super.symTable);
                production = result.getKey();
                outputString = result.getValue();
            }
        }
        //Push production to stack's first arrayList
        data.peek().add(production);
        output.peek().addLast(outputString);
        return false;
    }
    @Override
    protected boolean onDownExit(Branch branch, Branch child) {
        return false;
    }
    @Override
    protected boolean onComplete() {
        try {
            String out = output.toString();
            out = out.substring(2, out.length() - 2);
            List<String> prehi = Arrays.stream(out.split("\n")).filter(str ->
                    !str.isEmpty()).collect(Collectors.toList());
            String hi = String.join("\n", prehi);
            System.out.println("/**********************Reinterpreted code***********************/");
            System.out.println(hi);
            System.out.println("/***************************************************************/");
            targetIRL.write(hi);
            targetIRL.flush();
            targetIRL.close();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Cannot write to output irl file.");
        }
        data.clear();
        output.clear();
        return false;
    }
    @Override
    public String getWalkerName() {
        return "Hoenir Intermediate Translator.";
    }

    /**
     * Get the output path.
     * @return The output path.
     */
    public String getTargetPath() {
        return String.format(outFileFormatString, targetIn);
    }
}
