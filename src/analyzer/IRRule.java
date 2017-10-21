package analyzer;

import ast.Branch;
import ast.Leaf;
import symtable.SymTable;

import java.util.LinkedList;
import java.util.List;

/**
 * A single moment of transformation belonging to a rule chunk.
 */
class IRRule {
    private final String rule;
    private IRChunk chunk = new IRChunk();

    /**
     * Constructs the rule.
     * @param rule The rule this will represent.
     */
    public IRRule(String rule) {
        if (rule.startsWith("%F :") || rule.startsWith("%F:")) {
            boolean isProduction = true;
            this.rule = rule.substring(rule.indexOf(":") + 1).trim();
        } else {
            this.rule = rule;
        }
    }

    /**
     * Prints the rule in a pretty format.
     */
    public void printRule() {
        System.out.println("\t\t" + rule);
    }

    private static int varcounter = 0;
    private static int labelcounter = 0;
    private static int funccounter = 0;

    /**
     * Generates the string relevant to the transformation.
     * @param branch The source node.
     * @param elements The list of production rule results from children nodes.
     * @param core The list of outputs from children nodes.
     * @param fToken The token of the production rule.
     * @param additions The new String to append the transformations to.
     * @param symTable The symtable.
     */
    public void generateString(Branch branch, List<String> elements, LinkedList<StringBuilder> core,
                               StringBuilder fToken, StringBuilder additions, SymTable symTable) {
        //Otherwise, normal
        for (int i = 0; i < rule.length(); i++) {
            switch (rule.charAt(i)) {
                case '%':
                    i++;
                    switch (rule.charAt(i)) {
                        case '~':
                            i++;
                            int start = i;
                            while (i < rule.length() && rule.charAt(i) <='9' && rule.charAt(i) >= '0') {
                                i++;
                            }
                            if (start != i) {
                                int placement = Integer.parseInt(rule.substring(start, i));
                                additions.append(((Leaf) branch.getChildren().get(placement)).getSubstring());
                            } else {
                                additions.append(((Leaf) branch).getSubstring());
                            }
                            break;
                        case '0':
                        case '1':
                        case '2':
                        case '3':
                        case '4':
                        case '5':
                        case '6':
                        case '7':
                        case '8':
                        case '9':
                            start = i;
                            while (i < rule.length() && rule.charAt(i) <='9' && rule.charAt(i) >= '0') {
                                i++;
                            }
                            int placement = Integer.parseInt(rule.substring(start, i));
                            additions.append(elements.get(placement));
                            break;
                        case '%':
                            additions.append(rule.charAt(i));
                            i++;
                            break;
                        case 'G':
                            i += 2;
                            switch (rule.charAt(i)) {
                                case 'N':
                                    additions.append(varcounter);
                                    varcounter++;
                                    break;
                                case 'F':
                                    additions.append(funccounter);
                                    funccounter++;
                                    break;
                                case 'L':
                                    additions.append(labelcounter);
                                    labelcounter++;
                                    break;
                            }
                            i++;
                            break;
                        case 'F':
                            additions.append(fToken);
                            i++;
                            break;
                        case 'O':
                            i += "OUTPUT".length();
                            if (i < rule.length() && rule.charAt(i) >= '0' && rule.charAt(i) <= '9') {
                                start = i;
                                while (i < rule.length() && rule.charAt(i) <= '9' && rule.charAt(i) >= '0') {
                                    i++;
                                }
                                placement = Integer.parseInt(rule.substring(start, i));
                                for (int j = 0; j < placement; j++) {
                                    additions.append(core.removeFirst());
                                }
                            } else {
                                while (!core.isEmpty()) {
                                    additions.append(core.removeFirst());
                                }
                            }
                            break;
                        case 'R':
                            i += 3;
                            switch (rule.charAt(i)) {
                                case 'N':
                                    additions.append(varcounter);
                                    break;
                                case 'F':
                                    additions.append(funccounter);
                                    break;
                                case 'L':
                                    additions.append(labelcounter);
                                    break;
                            }
                            i++;
                            break;
                        case 'S':
                            //%SYM_PROPERTY:symbol:qualifier:property%
                            if (rule.substring(i).startsWith("SYM_PROPERTY:")) {
                                i += "SYM_PROPERTY:".length();
                                start = i;
                                while (i == start || rule.charAt(i) != '%') {
                                    if (rule.charAt(i) == ':' && rule.charAt(i + 1) == '%') {
                                        i++;
                                    }
                                    i++;
                                }
                                String[] symaccess = disperse(rule.substring(start, i), branch, elements);
                                if (symTable.hasSym(symaccess[0], symaccess[1], 0).isEmpty()) {
                                    throw new RuntimeException("Symbol " + symaccess[0] + ", with modifier " + symaccess[1] + " does not exist.");
                                }
                                additions.append(symTable.getSymProperty(symaccess[0], symaccess[1], symaccess[2]).orElseThrow(
                                        () -> new RuntimeException(
                                                "Symbol " + symaccess[0] + ", with modifier " + symaccess[1] + " does not have property " + symaccess[2] + "."
                                        )
                                ));
                                i++;
                            } else if (rule.substring(i).startsWith("SYM_FIND:")) { //%SYM_FIND:symbol%
                                i += "SYM_FIND:".length();
                                start = i;
                                while (i == start || rule.charAt(i) != '%') {
                                    if (rule.charAt(i) == ':' && rule.charAt(i + 1) == '%') {
                                        i++;
                                    }
                                    i++;
                                }
                                String[] symaccess = disperse(rule.substring(start, i), branch, elements);
                                String sym = symTable.hasSym(symaccess[0], symaccess[1], 0);
                                assert sym != null :
                                        "Symbol " + symaccess[0] + ", with modifier " + symaccess[1] + " does not exist.";
                                additions.append(sym);
                                i++;
                            }
                    }
                    break;
                default:
                    additions.append(rule.charAt(i));
                    i++;
            }
            i--;
        }
    }

    /**
     * Submethod for processing a more compact instruction.
     * @param symData The compacted instruction
     * @param branch The source node
     * @param elements The list of the production rule results of several children nodes.
     * @return The array of strings that would result from the transformation rule.
     */
    private String[] disperse(String symData, Branch branch, List<String> elements) {
        String[] symaccess = symData.split("\\s*:\\s*");
        for (int j = 0; j < symaccess.length; j++) {
            if (symaccess[j].charAt(0) == '%') {
                if (symaccess[j].charAt(1) == '~') {
                    symaccess[j] = symaccess[j].substring(2);
                    if (!symaccess[j].isEmpty()) {
                        symaccess[j] = ((Leaf) branch.getChildren().get(Integer.parseInt(symaccess[j]))).getSubstring();
                    } else {
                        symaccess[j] = ((Leaf) branch).getSubstring();
                    }
                } else {
                    symaccess[j] = symaccess[j].substring(1);
                    symaccess[j] = elements.get(Integer.parseInt(symaccess[j]));
                }
            }
        }
        return symaccess;
    }
}
