package lexer;

import regex.Skald;
import config.PathHolder;
import javafx.util.Pair;
import tagtable.Tag;
import tagtable.TagTable;
import ast.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

/**
 * Named after the path to Niflheim, Helvegar controls the flow from Niflheim, the lexer.
 *
 * The lexer is functioning on a set of rules provided by Hel.
 */
public class Helvegar extends Lexer {
    //Stable fields
    private Hel regexSet;
    //Dynamic fields
    private String stream;
    private int mark;

    /**
     * The constructor of Helvegar.
     * @param context The context data, AST, and symtable.
     */
    public Helvegar(PathHolder context, AST ast) {
        super(context, ast);
        System.out.println("Helvegar configured.");
    }
    /**
     * Initializes the list of rules necessary for Helvegar to lex files.
     */
    @Override
    protected void configure() {
        regexSet = new Hel(getContext(), getAST());
    }

    /**
     * Loads a file into Helvegar, treating it as if it were a stream of characters.
     * @param inputFile The input file to read from.
     */
    public void loadStream(String inputFile) {
        try {
            resetStream(new String(Files.readAllBytes(Paths.get(
                    getContext().BASE_DIR + getContext().SAMPLE_BASE_DIR + inputFile
                    )), StandardCharsets.UTF_8
            ));
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Error reading file.");
        }
    }
    /**
     * Resets the stream's data to that of another string.
     * @param data The new data.
     */
    private void resetStream(String data) {
        stream = data;
        mark = 0;
    }
    /**
     * Fetch the next lexeme in the string, as matched by the parsers in Hel
     * @return the next lexeme, or EOF if none are found.
     */
    public Leaf next() {
        //Use the ignore parser here
        if (regexSet.getIgnoreRegex() != null) {
            List<Integer> mark2 = regexSet.getIgnoreRegex().match(stream, mark);
            if (mark2 != null && !mark2.isEmpty()) {
                mark = mark2.get(mark2.size() - 1);
            }
        }
        //Find next lexeme
        for (Pair<Tag, Skald> parserPair : regexSet.getRegexes()) {
            List<Integer> kList = parserPair.getValue().match(stream, mark);
            if (!kList.isEmpty()) {
                int k = kList.get(kList.size() - 1);
                if (k > mark) {
                    Leaf lexeme = new Leaf(parserPair.getKey(), stream.substring(mark, k));
                    mark = k;
                    return lexeme;
                }
            }
        }
        return new Leaf(getTagTable().EOF_TAG, "");
    }
}