package niflheim;

import bragi.Skald;
import javafx.util.Pair;
import yggdrasil.TagPriority;
import yggdrasil.TagRecord;
import yggdrasil.Yggdrasil;

import java.util.ArrayList;
import java.util.List;

/**
 * Names after the goddess of Niflheim, Hel is a monster.
 *
 * Hel manages the rulebooks for Helvegar, one of the escapes of Niflheim.
 *
 * The rule manager of the lexer. Parses the lexer declaration file.
 */
public class Hel {
    //Public static data
    public static final String DEFAULT_ALPH = "\t\n\r !\"#$%&'()*+,-./" +
            "0123456789" +
            ":;<=>?@" +
            "ABCDEFGHIJKLMNOPQRSTUVWXYZ" +
            "[\\]^_`" +
            "abcdefghijklmnopqrstuvwxyz" +
            "{|}~";
    //Static parsing rules
    private final static String ELE_SPLIT_REGEX = ": ";
    private final static Skald STRING_PARSER = new Skald(".*" + ELE_SPLIT_REGEX, Hel.DEFAULT_ALPH);
    //Static command prefixes
    private final static String ALPHA_CMD_PRE = "%ALPHA";
    private final static String IGNOR_CMD_PRE = "%IGNORE";
    //Instance data
    private Yggdrasil parent;
    private String alphabet = null;
    private ArrayList<Pair<Integer, Skald>> parsers = new ArrayList<>();
    private Skald ignoreParser;
    /**
     * Initializes Hel with the lines of the configuration file.
     * @param parent AST tree being contributed to
     * @param lines Parser file lines
     */
    public Hel(Yggdrasil parent, List<String> lines) {
        this.parent = parent;
        for (int i = 0; i < lines.size(); i++) {
            if (lines.get(i).startsWith(ALPHA_CMD_PRE)) {
                lines.add(0, lines.remove(i));
                break;
            }
        }
        for (String line : lines) {
            parseLexLine(this, line);
        }
        //TODO combine DFAs/NFAs into one
        if (parent.DEBUG) printRegExs();
    }
    private static void parseLexLine(Hel hel, String line) {
        line = line.trim();
        String[] halves = line.split(ELE_SPLIT_REGEX, 2);
        if (line.length() == 0) {
            //Empty line
        } else if (line.charAt(0) == '#') {
            //Is a comment
        } else if (line.charAt(0) == '%') {
            //Is a command
            parseCmd(hel, halves);
        } else {
            //Is a rule
            hel.addRule(halves);
        }
    }
    private static void parseCmd(Hel hel, String[] halves) {
        switch (halves[0]) {
            case ALPHA_CMD_PRE:
                System.out.println("Parsing alphabet");
                if (hel.alphabet != null) {
                    throw new RuntimeException("Repeated alphabet declaration.");
                }
                parseAlpha(hel, halves.length < 2 ? "" : halves[1]);
                return;
            case IGNOR_CMD_PRE:
                if (hel.ignoreParser != null) {
                    hel.ignoreParser = new Skald(
                            "(" + hel.ignoreParser.getPattern() + ")|(" + halves[1] + ")",
                            Hel.DEFAULT_ALPH
                    );
                } else {
                    hel.ignoreParser = new Skald(halves[1], Hel.DEFAULT_ALPH);
                }
                return;
            default:
                throw new RuntimeException("Unrecognized command : " + halves[0] + ": " + halves[1]);
        }
    }
    private static void parseAlpha(Hel hel, String alphaDec) {
        alphaDec = alphaDec.trim();
        if (alphaDec.length() == 0) {
            hel.alphabet = DEFAULT_ALPH;
        } else {
            switch (alphaDec) {
                case "DEFAULT":
                    hel.alphabet = DEFAULT_ALPH;
                    break;
                default:
                    hel.alphabet = alphaDec;
            }
        }
    }
    private void addRule(String[] halves) {
        parent.addTagIfAbsent(halves[0], TagPriority.LEX);
        parsers.add(new Pair<>(parent.tagEncode(halves[0], TagPriority.LEX), new Skald(halves[1], alphabet)));
    }
    private void printRegExs() {
        if (ignoreParser != null) {
            System.out.println(IGNOR_CMD_PRE + ": " + ignoreParser.generateString());
        }
        for (Pair<Integer, Skald> parserSet : parsers) {
            System.out.println(parent.tagDecode(parserSet.getKey(), TagPriority.LEX) +
                    ": " + parserSet.getValue().generateString());
        }
    }

    public Skald getIgnored() {
        return ignoreParser;
    }
    public ArrayList<Pair<Integer, Skald>> getParsers() {
        return parsers;
    }
}
