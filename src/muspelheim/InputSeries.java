package muspelheim;

import bragi.Skald;
import muspelheim.ruletypes.InputRule;
import muspelheim.ruletypes.InputRuleType;
import niflheim.Hel;

import java.util.ArrayList;
import java.util.List;

public class InputSeries {
    private List<InputRule> rules = new ArrayList<>();
    private String seriesName;

    private static String singleLineRuleRegEx = "::/[A-Z]*-[A-Z_]+::[^\\n]+\\n?";
    private static String multiLineRuleRegEx = "::/[A-Z]*-[A-Z_]+::.(.:|.)*::/END-[A-Z_]+::\\n?";
    private static String rulePrefixRegEx = "/[A-Z]+-";

    public InputSeries(String input) {
        List<InputSeries> list = new ArrayList<>();
        Skald singleLineRegex = new Skald(singleLineRuleRegEx, Hel.DEFAULT_ALPH);
        singleLineRegex.compile();
        Skald multiLineRegex = new Skald(multiLineRuleRegEx, Hel.DEFAULT_ALPH);
        multiLineRegex.compile();
        Skald rulePrefixRegex = new Skald(rulePrefixRegEx, Hel.DEFAULT_ALPH);
        rulePrefixRegex.compile();

        List<InputRule> rules = new ArrayList<>();

        boolean done = false;
        int start = 0;
        while (!done) {
            done = true;
            List<Integer> single = multiLineRegex.match(input.substring(start).trim());
            if (single.size() == 0) {
                single = singleLineRegex.match(input.substring(start).trim());
                if (single.size() != 0) {
                    System.out.println("Multiline");
                    done = false;
                } else {
                    System.out.println("no match");
                }
            } else {
                System.out.println("Single");
                done = false;
            }
            if (!done) {
                String temp = input.substring(start, start + single.get(single.size() - 1));
                start += temp.length();
                //Process
                String[] lines = temp.trim().split("\\n");
                InputRuleType type = InputRuleType.fetchType(lines[0].substring(3, rulePrefixRegex.match(lines[0], 2).get(0) - 1));
                System.out.println("Rule of type: " + type.name());
                if (lines.length == 1) {
                    //Single line
                    String[] data = lines[0].substring(2).split("::");
                    if (data.length > 2) {
                        for (int i = 2; i < data.length; i++) {
                            data[0] += data[i];
                        }
                    }
                    //TODO
                } else {
                    InputRuleType type2 = InputRuleType.fetchType(lines[lines.length - 1].substring(3, rulePrefixRegex.match(lines[lines.length - 1], 2).get(0) - 1));
                    if (type != type2) {
                        //Error
                        throw new RuntimeException("Unexpected termination");
                    }
                    //TODO
                }
            }
        }
        System.exit(-1);
    }

    public List<InputRule> getRules() {
        return rules;
    }
    public String getSeriesName() {
        return seriesName;
    }

    public String toString() {
        return "Name:" + seriesName + "=" + rules;
    }
}
