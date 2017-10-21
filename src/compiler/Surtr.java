package compiler;

import ast.AST;
import javafx.util.Pair;
import tagtable.TagTable;
import ast.Cosmos;
import config.PathHolder;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Surtr extends MLGenerator {
    private Machine machine;
    private String outFileFormatString;

    private Sinmara reparser;

    public Surtr(PathHolder holder, AST ast) {
        super(holder, ast);
        System.out.println("Surtr configured.");
    }
    protected void configure() {
        machine = new Machine(getContext().BASE_DIR + getContext().TARGET + getContext().MACHINE_DEC_EXTENSION);
        if (getContext().DEBUG) System.out.println(machine.toDefinitionString());
        reparser = new Sinmara(getContext(), getAST());
        outFileFormatString = getContext().MTL_BASE_DIR + "%s" + getContext().MTL_EXTENSION;
    }

    public void convert(String file) {
        System.out.println("Converting to backend");
        System.out.println("Backend name: " + reparser.getAsm());




        boolean implemented = false;
        if (!implemented) {
            System.out.println("Backend conversion system not yet implemented.");
            System.exit(0);
        }



        //Write back
        BufferedWriter writer;
        try {
            Files.deleteIfExists(Paths.get(getContext().BASE_DIR +
                    String.format(outFileFormatString, file)));
            Files.createFile(Paths.get(getContext().BASE_DIR +
                    String.format(outFileFormatString, file)));
            writer = Files.newBufferedWriter(Paths.get(getContext().BASE_DIR +
                    String.format(outFileFormatString, file)));
        } catch (IOException e0) {
            e0.printStackTrace();
            try {
                Files.createDirectories(Paths.get(getContext().BASE_DIR +
                        String.format(outFileFormatString, file)).getParent());
                Files.deleteIfExists(Paths.get(getContext().BASE_DIR +
                        String.format(outFileFormatString, file)));
                Files.createFile(Paths.get(getContext().BASE_DIR +
                        String.format(outFileFormatString, file)));
                writer = Files.newBufferedWriter(Paths.get(getContext().BASE_DIR +
                        String.format(outFileFormatString, file)));
            } catch (IOException e1) {
                e1.printStackTrace();
                throw new RuntimeException("Cannot create mtl target.");
            }
        }
        //Input format
        List<String> irlLines;
        try {
            irlLines = Files.readAllLines(Paths.get(getContext().BASE_DIR + file)).stream().filter(
                    str -> !str.startsWith("#")).collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Cannot open IRL file for reading.");
        }
        List<List<Pair<String, Integer>>> stepRegs = new ArrayList<>();
        List<DiscreteOps> ops = new ArrayList<>();
        List<List<String>> qualifiers = new ArrayList<>();
        reparser.parseLines(irlLines, stepRegs, ops, qualifiers);
        machine.registerAlloc(stepRegs);
        reparser.translate(writer, machine, stepRegs, ops);
    }
}
