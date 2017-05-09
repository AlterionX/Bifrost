package muspelheim.ruletypes;

public enum InputRuleType {
    RULE, CONST, REGEX, CONVERSION;

    public static InputRuleType fetchType(String type) {
        switch (type) {
            case "REGEX":
                return REGEX;
            case "RULES":
            case "RULE":
                return RULE;
            case "CONST":
            case "CONSTANT":
                return CONST;
            case "CONVERSION":
            case "CONV":
                return CONVERSION;
            default:
                throw new RuntimeException("Unrecognized rule type.");
        }
    }
}
