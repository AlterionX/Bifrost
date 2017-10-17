package yggdrasil;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class PathHolder {
    //Static, public variables
    public boolean DEBUG;
    public final String TARGET;
    public final String BASE_DIR;
    public final String IRL_BASE_DIR;
    public final String MTL_BASE_DIR;
    public final String MTL_EXTENSION;
    public final String SAMPLE_BASE_DIR;
    public final String IRL_CODE_EXTENSION;
    public final String LEXER_DEC_EXTENSION;
    public final String PARSER_DEC_EXTENSION;
    public final String MACHINE_DEC_EXTENSION;
    public final String ANALYZER_DEC_EXTENSION;
    public final String ASSEMBLER_CALL_EXTENSION;
    public final String SCOPE_CHANGER_DEC_EXTENSION;
    public final String MACHINE_LANG_TRANSLATION_DEC_EXTENSION;
    public final String INTERMEDIATE_REPRESENTATION_LANG_DEC_EXTENSION;
    //Defaults
    private static final String DEFAULT_CONFIG = "./base.cfg";
    private static final String[] DEFAULT_READ;
    static {
        List<String> data;
        try {
            data = Files.readAllLines(Paths.get(DEFAULT_CONFIG)).stream()
                    .filter(str -> !str.isEmpty())
                    .collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
            data = new ArrayList<>(Arrays.asList(
                    "f", "tyrion", "irl/", "mtl/",
                    ".mtl", "samples/", ".irl", ".lexdec",
                    ".pardec", ".mmtdec", ".anadec", ".asmrcall",
                    ".scrdec", ".mltdec", ".irldec"));
        }
        DEFAULT_READ = data.toArray(new String[0]);
    }
    public PathHolder() {
        DEBUG = DEFAULT_READ[0].equals("t");
        TARGET = DEFAULT_READ[1];
        BASE_DIR = "./" + TARGET + "/";
        IRL_BASE_DIR = DEFAULT_READ[2];
        MTL_BASE_DIR = DEFAULT_READ[3];
        MTL_EXTENSION = DEFAULT_READ[4];
        SAMPLE_BASE_DIR = DEFAULT_READ[5];
        IRL_CODE_EXTENSION = DEFAULT_READ[6];
        LEXER_DEC_EXTENSION = DEFAULT_READ[7];
        PARSER_DEC_EXTENSION = DEFAULT_READ[8];
        MACHINE_DEC_EXTENSION = DEFAULT_READ[9];
        ANALYZER_DEC_EXTENSION = DEFAULT_READ[10];
        ASSEMBLER_CALL_EXTENSION = DEFAULT_READ[11];
        SCOPE_CHANGER_DEC_EXTENSION = DEFAULT_READ[12];
        MACHINE_LANG_TRANSLATION_DEC_EXTENSION = DEFAULT_READ[13];
        INTERMEDIATE_REPRESENTATION_LANG_DEC_EXTENSION = DEFAULT_READ[14];
    }
}
