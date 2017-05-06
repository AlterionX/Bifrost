package muspelheim;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IRLConverter {
    private static IRLConversionRule ALLOC_REG;
    private static IRLConversionRule ALLOC_STK;
    private static IRLConversionRule DEALLOC_REG;
    private static IRLConversionRule DEALLOC_MEM;

    private List<IRLConversionRule> ruleList = new ArrayList<>();
    private List<String> defiinitions = new ArrayList<>();
    private Map<String, IRLConversionRule> conversionMap = new HashMap<>();

    public IRLConverter(InputSeries rules, InputSeries definitions, Map<String, InputSeries> subcomponentDefinitionMap) {

    }

    public String convert(String line) {
        return null;
    }
}
