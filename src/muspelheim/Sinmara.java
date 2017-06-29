package muspelheim;

import javafx.util.Pair;
import yggdrasil.Cosmos;
import yggdrasil.Yggdrasil;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class Sinmara extends Cosmos {
    List<Set<String>> stepVarSets = new ArrayList<>();
    private String targetAsm;

    public Sinmara(Yggdrasil context) {
        super(context);
        System.out.println("Sinmara configured.");
    }

    @Override
    protected void configure() {
        List<String> configLines;
        try {
            configLines = Files.readAllLines(Paths.get(
                    context.BASE_DIR + context.TARGET + context.MACHINE_LANG_TRANSLATION_DEC_EXTENSION
            )).stream()
                    .map(String::trim)
                    .filter(str -> !str.isEmpty() && !str.trim().startsWith("#"))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Could not access MTL declaration");
        }
        if (configLines.size() == 0) {
            throw new RuntimeException("No input detected for file.");
        }
        //Parse lines
        for (String line : configLines) {
            if (line.startsWith("/")) {
                System.out.println(Arrays.toString(line.split(":", 3)));
            } else if (line.startsWith("[") && line.endsWith("]")) {
                System.out.println("Beginning topic (unsupported) " + line + ".");
            }
        }
    }

    public void parseLines(List<String> irlLines, List<List<Pair<String, Integer>>> varSteps,
                           List<DiscreteOps> ops, List<List<String>> qualifiers) {
        varSteps.clear();
        ops.clear();
        qualifiers.clear();
        for (int i = 0; i < irlLines.size(); i++) {
            List<Pair<String, Integer>> tempVars = new ArrayList<>();
            DiscreteOps tempOp = DiscreteOps.NOP;
            List<String> tempQualifiers = new ArrayList<>();

            boolean hi = true;
            if (hi) throw new RuntimeException("ERROR: Unimplemented");

            varSteps.add(tempVars);
            ops.add(tempOp);
            qualifiers.add(tempQualifiers);
        }
    }

    public void translate(BufferedWriter writer, Machine machine, List<List<Pair<String, Integer>>> stepRegs, List<DiscreteOps> ops) {
        throw new RuntimeException("ERROR: Unimplemented");
    }

    public String getAsm() {
        return targetAsm;
    }
}
