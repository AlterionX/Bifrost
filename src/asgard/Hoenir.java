package asgard;

import javafx.util.Pair;
import yggdrasil.Branch;
import yggdrasil.Seedling;
import yggdrasil.TagPriority;
import yggdrasil.Yggdrasil;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class Hoenir extends Stag{
    private String outFileFormatString = null;
    private String targetIn;
    private BufferedWriter targetIRL;
    private Map<Integer, IRGroup> ruleGroups;

    public Hoenir(Yggdrasil parent) {
        super(parent, false);
        //Read input for intermediate code generation patterns
        List<String> ruleLines;
        try {
            ruleLines = Files.readAllLines(Paths.get(parent.BASE_DIR +
                    parent.TARGET + parent.INTERMEDIATE_REPRESENTATION_LANG_DEC_EXTENSION));
            ruleLines = ruleLines.stream().filter(
                    line -> !line.isEmpty()).map(String::trim).collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Issues with the analyzer's declaration file.");
        }
        outFileFormatString = parent.BASE_DIR + parent.IRL_BASE_DIR + "%s" +
                parent.IRL_CODE_EXTENSION;
        parseIRConfig(ruleLines);
        printIRConfig();
    }

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
                i = parseRuleChunk(rules, i, ruleGroups);
            }
        }
    }
    private int parseRuleChunk(List<String> ruleStream, int position, Map<Integer, IRGroup> ruleGroups) {
        IRGroup ruleGroup;
        if (ruleStream.get(position).startsWith("/DECODE")) {
            String[] decodeStartRuleLine = ruleStream.get(position).split("\\s+");
            if (decodeStartRuleLine.length != 2) {
                throw new RuntimeException("Malformed initial line for IRGroup.");
            }
            ruleGroup = new IRGroup(parent.tagEncode(decodeStartRuleLine[1], TagPriority.SUB));
        } else {
            System.out.println("Error reading file rules.");
            throw new RuntimeException("Malformed IR representation rules.");
        }
        position++;
        while (!ruleStream.get(position).startsWith("/END")) {
            position = parseRuleSubChunk(ruleStream, position, ruleGroup);
        }
        position++;
        ruleGroups.put(ruleGroup.getTag(), ruleGroup);
        return position;
    }
    private int parseRuleSubChunk(List<String> ruleStream, int position, IRGroup group) {
        IRChunk chunk = new IRChunk();
        if (ruleStream.get(position).startsWith("/COND")) {
            String[] rule = ruleStream.get(position).split("\\s+");
            if (rule.length < 3 || rule.length % 2 == 0) {
                throw new RuntimeException("Malformed IRGroup condition");
            }
            for (int i = 1; i < rule.length - 1; i += 2) {
                chunk.addCondition(Integer.parseInt(rule[i]), parent.tagEncode(rule[i + 1], TagPriority.SUB));
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
            chunk.addIRRule(new IRRule(ruleStream.get(position)));
            position++;
        }
        return position;
    }

    private void printIRConfig() {
        for (Integer k : ruleGroups.keySet()) {
            ruleGroups.get(k).printRules();
        }
    }

    public void prime(String target) {
        targetIn = target;
        try {
            Files.deleteIfExists(Paths.get(parent.BASE_DIR +
                    String.format(outFileFormatString, targetIn)));
            Files.createFile(Paths.get(parent.BASE_DIR +
                    String.format(outFileFormatString, targetIn)));
            targetIRL = Files.newBufferedWriter(Paths.get(parent.BASE_DIR +
                    String.format(outFileFormatString, targetIn)));
        } catch (IOException e0) {
            e0.printStackTrace();
            try {
                Files.createDirectories(Paths.get(parent.BASE_DIR +
                        String.format(outFileFormatString, targetIn)).getParent());
                Files.deleteIfExists(Paths.get(parent.BASE_DIR +
                        String.format(outFileFormatString, targetIn)));
                Files.createFile(Paths.get(parent.BASE_DIR +
                        String.format(outFileFormatString, targetIn)));
                targetIRL = Files.newBufferedWriter(Paths.get(parent.BASE_DIR +
                        String.format(outFileFormatString, targetIn)));
            } catch (IOException e1) {
                e1.printStackTrace();
                throw new RuntimeException("Cannot create irl target.");
            }
        }
    }

    private Stack<ArrayList<String>> data = new Stack<>();
    private Stack<LinkedList<StringBuilder>> output = new Stack<>(); //Change to a stack of lists of these
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
        Seedling.simplePrint(branch);
        ArrayList<String> children = data.pop();
        LinkedList<StringBuilder> childOutput = output.pop();
        String production = "";
        System.out.println("Children strings: " + children);
        System.out.println("Children outputs: " + childOutput);
        StringBuilder outputString = new StringBuilder("");
        if (ruleGroups.containsKey(branch.getTag())) {
            System.out.println("Generating...");
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
                Pair<String, StringBuilder> result = workingChunk.execute(branch, children, childOutput, parent);
                production = result.getKey();
                outputString = result.getValue();
            }
            System.out.println("%F: " + production + "\nOutput:\n" + outputString);
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
            System.out.println("Re-stripped:");
            List<String> prehi = Arrays.stream(out.split("\n")).filter(str ->
                    !str.isEmpty()).collect(Collectors.toList());
            System.out.println(prehi);
            String hi = String.join("\n", prehi);
            System.out.println(hi);
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

    public String getTargetPath() {
        return String.format(outFileFormatString, targetIn);
    }
}
