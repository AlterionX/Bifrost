package niflheim;

import bragi.Skald;
import javafx.util.Pair;
import yggdrasil.Cosmos;
import yggdrasil.TagPriority;
import yggdrasil.Yggdrasil;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Names after the goddess of Niflheim, Hel is a monster.
 *
 * Hel manages the rulebooks for Helvegar, one of the escapes of Niflheim.
 *
 * The rule manager of the lexer. Parses the lexer declaration file.
 */
public class Hel extends Cosmos{
    //Public static data
    public static final String DEFAULT_ALPH = "\t\n\r !\"#$%&'()*+,-./" +
            "0123456789" +
            ":;<=>?@" +
            "ABCDEFGHIJKLMNOPQRSTUVWXYZ" +
            "[\\]^_`" +
            "abcdefghijklmnopqrstuvwxyz" +
            "{|}~";
    //Static parsing rules
    private final static String ELE_SPLIT_REGEX = "\\s*:\\s*";
    //Static command prefixes
    private final static String ALPHA_CMD_PRE = "%ALPHA";
    private final static String IGNOR_CMD_PRE = "%IGNORE";
    //Stable fields
    private String alphabet;
    private ArrayList<Pair<Integer, Skald>> parsers;
    private Skald ignoreParser;

    /**
     * Initializes Hel.
     * @param context The context data, AST, and symtable.
     */
    public Hel(Yggdrasil context) {
        super(context);
        System.out.println("Hel configured.");
    }

    /**
     * Initializes the variables, as well as pulling from the provided configuration details of the context.
     */
    protected void configure() {
        //Init variables, as this is a call in the super class, this class's variable haven't been initialized yet
        parsers = new ArrayList<>();
        //Begin configuration
        List<String> lines;
        try {
            lines = Files.readAllLines(Paths.get(
                    context.BASE_DIR + context.TARGET + context.LEXER_DEC_EXTENSION
            )).stream().map(String::trim)
                    .filter(str -> !str.isEmpty() && str.charAt(0) != '#').collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
            lines = new ArrayList<>();
        }
        for (int i = 0; i < lines.size(); i++) {
            if (lines.get(i).startsWith(ALPHA_CMD_PRE)) {
                lines.add(0, lines.remove(i));
                break;
            }
        }
        //Parse the line
        for (String line : lines) {
            String[] halves = line.split(ELE_SPLIT_REGEX, 2);
            if (line.charAt(0) == '%') {
                //Is a command
                switch (halves[0]) {
                    case ALPHA_CMD_PRE:
                        System.out.println("Parsing alphabet");
                        if (alphabet != null) {
                            throw new RuntimeException("Repeated alphabet declaration.");
                        }
                        String alphaDec = (halves.length < 2 ? "" : halves[1]).trim();
                        if (alphaDec.length() == 0) {
                            alphabet = DEFAULT_ALPH;
                        } else {
                            switch (alphaDec) {
                                case "DEFAULT":
                                    alphabet = DEFAULT_ALPH;
                                    break;
                                default:
                                    alphabet = alphaDec;
                            }
                        }
                        break;
                    case IGNOR_CMD_PRE:
                        if (ignoreParser != null) {
                            ignoreParser = new Skald(
                                    "(" + ignoreParser.getPattern() + ")|(" + halves[1] + ")",
                                    Hel.DEFAULT_ALPH
                            );
                        } else {
                            ignoreParser = new Skald(halves[1], Hel.DEFAULT_ALPH);
                        }
                        break;
                    default:
                        throw new RuntimeException("Unrecognized command : " + halves[0] + ": " + halves[1]);
                }
            } else {
                //Is a rule
                context.addTagIfAbsent(halves[0], TagPriority.LEX);
                parsers.add(new Pair<>(context.tagEncode(halves[0], TagPriority.LEX), new Skald(halves[1], alphabet)));
            }
        }
        //TODO combine DFAs/NFAs into one
        if (context.DEBUG) printRegExs();
    }
    /**
     * Print all regex decompositions of the parsers that were initialized.
     */
    private void printRegExs() {
        if (ignoreParser != null) {
            System.out.println(IGNOR_CMD_PRE + ": " + ignoreParser.generateString());
        }
        for (Pair<Integer, Skald> parserSet : parsers) {
            System.out.println(context.tagDecode(parserSet.getKey(), TagPriority.LEX) +
                    ": " + parserSet.getValue().generateString());
        }
    }

    /**
     * Get the special ignore parser.
     * @return A parser that matches ignored substrings.
     */
    public Skald getIgnoreParser() {
        return ignoreParser;
    }
    /**
     * Get a list of parsers and their corresponding tags.
     * @return A list of pairs of parsers and the tag that they match.
     */
    public ArrayList<Pair<Integer, Skald>> getParsers() {
        return parsers;
    }
}
