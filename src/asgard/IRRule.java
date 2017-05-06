package asgard;

import yggdrasil.Branch;
import yggdrasil.Leaf;
import yggdrasil.Yggdrasil;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class IRRule {
    private String rule;
    private boolean isProduction;

    public IRRule(String rule) {
        if (rule.startsWith("%F :") || rule.startsWith("%F: ")) {
            isProduction = true;
            this.rule = rule.substring(rule.indexOf(":") + 1).trim();
        } else {
            this.rule = rule;
        }
    }

    public void printRule() {
        System.out.println("\t\t" + rule);
    }

    private static int varcounter = 0;
    private static int labelcounter = 0;
    private static int funccounter = 0;

    public void generateString(Branch branch, List<String> elements, LinkedList<StringBuilder> core,
                               StringBuilder fToken, StringBuilder additions, Yggdrasil context) {
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
                                String[] symaccess = rule.substring(start, i).split(":");
                                for (int j = 0; j < 3; j++) {
                                    if (symaccess[j].charAt(0) == '%') {
                                        if (symaccess[j].charAt(1) == '~') {
                                            symaccess[j] = symaccess[j].substring(2);
                                            if (symaccess[j].isEmpty()) {
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
                                if (!context.hasSym(symaccess[0], symaccess[1])) {
                                    throw new RuntimeException("Symbol " + symaccess[0] + ", with modifier " + symaccess[1] + " does not exist.");
                                }
                                System.out.println(context.getSym(symaccess[0], symaccess[1]));
                                if (context.getSymProperty(symaccess[0], symaccess[1], symaccess[2]).isEmpty()) {
                                    throw new RuntimeException("Symbol " + symaccess[0] + ", with modifier " + symaccess[1] + " does not have property " + symaccess[2] + ".");
                                }
                                additions.append(context.getSymProperty(symaccess[0], symaccess[1], symaccess[2]));
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
}
