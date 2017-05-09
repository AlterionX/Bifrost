package muspelheim.ruletypes;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class InputRule {
    public static InputRule parseRule(InputRuleType type, String split) {
        switch (type) {
            case RULE:
                List<String> tokens =  Arrays.stream(split.split(":|\\s+")).map(String::trim).collect(Collectors.toList());
                return new InputRuleRule(tokens);
            case CONST:

                break;
            case REGEX:
                break;
            case CONVERSION:

        }
        //TODO
        return null;
    }
}
