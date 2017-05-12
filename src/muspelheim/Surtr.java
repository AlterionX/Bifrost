package muspelheim;

import javafx.util.Pair;
import yggdrasil.Cosmos;
import yggdrasil.Yggdrasil;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class Surtr extends Cosmos{
    private Machine machine;
    private String outFileFormatString;

    private Sinmara reparser;

    public Surtr(Yggdrasil context) {
        super(context);
        System.out.println("Surtr configured.");
    }
    protected void configure() {
        machine = new Machine(context.BASE_DIR + context.TARGET + context.MACHINE_DEC_EXTENSION);
        if (context.DEBUG) System.out.println(machine.toDefinitionString());
        reparser = new Sinmara(context);
        outFileFormatString = context.MTL_BASE_DIR + "%s" + context.MTL_EXTENSION;
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
            Files.deleteIfExists(Paths.get(context.BASE_DIR +
                    String.format(outFileFormatString, file)));
            Files.createFile(Paths.get(context.BASE_DIR +
                    String.format(outFileFormatString, file)));
            writer = Files.newBufferedWriter(Paths.get(context.BASE_DIR +
                    String.format(outFileFormatString, file)));
        } catch (IOException e0) {
            e0.printStackTrace();
            try {
                Files.createDirectories(Paths.get(context.BASE_DIR +
                        String.format(outFileFormatString, file)).getParent());
                Files.deleteIfExists(Paths.get(context.BASE_DIR +
                        String.format(outFileFormatString, file)));
                Files.createFile(Paths.get(context.BASE_DIR +
                        String.format(outFileFormatString, file)));
                writer = Files.newBufferedWriter(Paths.get(context.BASE_DIR +
                        String.format(outFileFormatString, file)));
            } catch (IOException e1) {
                e1.printStackTrace();
                throw new RuntimeException("Cannot create mtl target.");
            }
        }
        //Input format
        List<String> irlLines;
        try {
            irlLines = Files.readAllLines(Paths.get(context.BASE_DIR + file)).stream().filter(
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
