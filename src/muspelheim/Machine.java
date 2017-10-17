package muspelheim;

import javafx.util.Pair;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

class Machine {
    private String machineName;
    private int regCount;
    private final Map<Integer, List<String>> properties = new HashMap<>();
    private final List<String> allProperty = new ArrayList<>();
    private String[] names;

    private Map<String, Map<Integer, Integer>> varToIntToReg;

    private Stack<String> stackBase; //Word aligned
    private Stack<Integer> stackData; //Word aligned

    public Machine(String configFile) {
        List<String> config;
        try {
            config = Files.readAllLines(Paths.get(configFile)).stream()
                    .filter(str -> (!str.isEmpty()) && (!str.startsWith("#")))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Could not access machine configuration declaration file.");
        }
        configure(config);
        System.out.println("Machine configured.");
    }
    private void configure(List<String> configLines) {
        machineName = configLines.get(0);
        regCount = Integer.parseInt(configLines.get(1).split("\\s*:\\s*")[1]);
        names = new String[regCount];
        for (int i = 2; i < configLines.size(); i++) {
            String configLine = configLines.get(i);
            String[] configPair = configLine.split("\\s*:\\s*");
            if (configPair.length != 2) {
                System.out.println("Error with malformed declaration file.");
            }
            String[] configRule = configPair[0].split("\\s+");
            switch (configRule[0]) {
                case "REGSIZE":
                    int regSize = Integer.parseInt(configPair[1]);
                    break;
                case "PROPERTY":
                    switch (configRule[1]) {
                        case "ALL":
                            allProperty.add(configPair[1]);
                            break;
                        default:
                            Integer register = Integer.parseInt(configRule[1]);
                            if (!properties.containsKey(register)) {
                                properties.put(register, new ArrayList<>());
                            }
                            properties.get(register).add(configPair[1]);
                    }
                    break;
                case "NAME":
                    Integer register = Integer.parseInt(configRule[1]);
                    names[register] = configPair[1];
                    break;
                default:
                    throw new RuntimeException("Unknown rule type in machine declaration file.");
            }
        }
    }
    /**
     * Produce the machine data in a easy to understand format.
     * @return The string representing the machine data.
     */
    public String toDefinitionString() {
        StringBuilder sb = new StringBuilder();
        sb.append(machineName).append("\n").append("Register count: ").append(regCount).append("\n");
        sb.append("\t").append(allProperty).append("\n");
        for (int i = 0; i < regCount; i++) {
            sb.append("Register ").append(i).append(":\n\tName: ").append(names[i]).append("\n\tProperties: ").append(properties.get(i)).append("\n");
        }
        return sb.toString();
    }

    public void registerAlloc(List<List<Pair<String, Integer>>> stepVars) {
        Map<String, List<Integer[]>> ranges = new HashMap<>();
        Map<String, Integer[]> lastFound = new HashMap<>();
        for (List<Pair<String, Integer>> stepVar : stepVars) {
            //Detect ranges
            if (stepVar.size() == 1) { //Usage
                Pair<String, Integer> operand = stepVar.get(0);
                //Make sure to extend last known range, other errors should be caught by syntax checker...
                if (!lastFound.containsKey(operand.getKey())) {
                    throw new RuntimeException("Used variable has not been stored.");
                }
                throw new RuntimeException("Unimplemented.");
            } else if (stepVar.size() == 2) { //Two argument storage/other
                //
                throw new RuntimeException("Unimplemented.");
            } else if (stepVar.size() == 3) { //Assignment
                //For operands, this is usage, so should bring a temporary closure, unless used again
                Pair<String, Integer> operand1 = stepVar.get(0);
                Pair<String, Integer> operand2 = stepVar.get(1);
                //For destinations, this is an assignment that overrides any old values
                Pair<String, Integer> destination = stepVar.get(2);
                throw new RuntimeException("Unimplemented.");
            } else {
                throw new RuntimeException("Unexpected variable format of not one or three variables.");
            }
        }
        //Add remaining ranges not previously looked at
        for (String key : lastFound.keySet()) {
            if (!ranges.containsKey(key)) {
                ranges.put(key, new ArrayList<>());
            }
            if (lastFound.get(key)[1] == -1) { //Unfinished
                lastFound.get(key)[1] = stepVars.size();
            }
            ranges.get(key).add(lastFound.get(key));
        }
        throw new RuntimeException("ERROR: TODO");
    }
    public List<Integer> savedRegs() {
        throw new RuntimeException("Unimplemented");
    }
    public List<Integer> restoredRegs() {
        throw new RuntimeException("Unimplemented");
    }
}
