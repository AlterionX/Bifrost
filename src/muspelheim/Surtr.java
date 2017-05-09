package muspelheim;

import yggdrasil.Yggdrasil;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

public class Surtr {
    private Yggdrasil parent;
    private String backendType;
    private Machine machine;
    private String outFileFormatString;



    public Surtr(Yggdrasil parent) {
        this.parent = parent;
        List<String> config;
        try {
            config = Files.readAllLines(Paths.get(parent.BASE_DIR + parent.TARGET +
                    parent.MACHINE_LANG_TRANSLATION_DEC_EXTENSION)).stream().filter(
                    str -> !str.isEmpty() && !str.trim().startsWith("#")
            ).map(
                    String::trim
            ).collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Could not access MTL declaration");
        }
        outFileFormatString = parent.MTL_BASE_DIR + "%s" + parent.MTL_EXTENSION;
        if (config.size() == 0) {
            throw new RuntimeException("No input detected for file.");
        }
        if (!config.get(0).startsWith("::")) {
            backendType = config.remove(0);
        }
        parseMLT(String.join("\n", config));
    }
    private void parseMLT(String config) {
        System.out.println("Creating machine.");
        InputSeries series = new InputSeries(config);
        machine = new Machine(series);
    }

    public void convert(String file) {
        List<String> irlLines;
        try {
            irlLines = Files.readAllLines(Paths.get(parent.BASE_DIR + file)).stream().filter(
                    str -> !str.startsWith("#")).collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Cannot open IRL file for reading.");
        }
        BufferedWriter writer;
        try {
            Files.deleteIfExists(Paths.get(parent.BASE_DIR +
                    String.format(outFileFormatString, file)));
            Files.createFile(Paths.get(parent.BASE_DIR +
                    String.format(outFileFormatString, file)));
            writer = Files.newBufferedWriter(Paths.get(parent.BASE_DIR +
                    String.format(outFileFormatString, file)));
        } catch (IOException e0) {
            e0.printStackTrace();
            try {
                Files.createDirectories(Paths.get(parent.BASE_DIR +
                        String.format(outFileFormatString, file)).getParent());
                Files.deleteIfExists(Paths.get(parent.BASE_DIR +
                        String.format(outFileFormatString, file)));
                Files.createFile(Paths.get(parent.BASE_DIR +
                        String.format(outFileFormatString, file)));
                writer = Files.newBufferedWriter(Paths.get(parent.BASE_DIR +
                        String.format(outFileFormatString, file)));
            } catch (IOException e1) {
                e1.printStackTrace();
                throw new RuntimeException("Cannot create mtl target.");
            }
        }
        System.out.println("Converting to backend: " + backendType);
        try {
            for (String line : irlLines) {
                writer.write(machine.convertLine(line));
                writer.write("\n");
            }
            writer.flush();
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException("Failed to write to target file.");
        }
    }
}
