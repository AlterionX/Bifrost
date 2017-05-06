package niflheim;

import bragi.Skald;
import javafx.util.Pair;
import yggdrasil.Leaf;
import yggdrasil.TagPriority;
import yggdrasil.TagRecord;
import yggdrasil.Yggdrasil;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Named after the path to Niflheim, Helvegar controls the flow from Niflheim, the lexer.
 *
 * The lexer is functioning on a set of rules provided by Hel.
 */
public class Helvegar {
    //Complex, hierarchy related fields
    private Yggdrasil parent;
    private Hel regexSet;
    //Simple fields
    private String stream;
    private int mark;
    private ArrayList<Leaf> leaves = new ArrayList<>();
    private boolean preparsed;

    public Helvegar(Yggdrasil parent) {
        this.parent = parent;
        List<String> lines;
        try {
            lines = Files.readAllLines(Paths.get(parent.BASE_DIR + parent.TARGET + parent.LEXER_DEC_EXTENSION));
        } catch (IOException e) {
            e.printStackTrace();
            lines = new ArrayList<>();
        }
        regexSet = new Hel(parent, lines);
    }

    public void prime(String inputFile) {
        preparsed = false;
        try {
            clearAndSet(new String(
                    Files.readAllBytes(
                            Paths.get(parent.BASE_DIR + parent.SAMPLE_BASE_DIR + inputFile)
                    ), StandardCharsets.UTF_8
            ));
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Error reading file.");
        }
    }
    private void clearAndSet(String data) {
        leaves.clear();
        preparsed = false;
        stream = data;
        mark = 0;
    }
    public Leaf next() {
        if (preparsed) {
            mark++;
            while (mark < leaves.size() &&
                    leaves.get(mark).getTag() == parent.tagEncode(TagRecord.IGNORE_LABEL, TagPriority.LEX)) {
                mark++;
            }
            return mark < leaves.size() ?
                    leaves.get(mark) :
                    new Leaf(parent.tagEncode(TagRecord.EOF_LABEL, TagPriority.LEX), "", null, parent);
        }
        //Use the ignore parser here
        if (regexSet.getIgnored() != null) {
            List<Integer> mark2 = regexSet.getIgnored().match(stream, mark);
            if (mark2 != null && !mark2.isEmpty()) {
                mark = mark2.get(mark2.size() - 1);
            }
        }
        //Find next lexeme
        for (Pair<Integer, Skald> parserPair : regexSet.getParsers()) {
            List<Integer> kList = parserPair.getValue().match(stream, mark);
            if (!kList.isEmpty()) {
                int k = kList.get(kList.size() - 1);
                if (k > mark) {
                    Leaf lexeme = new Leaf(parserPair.getKey(), stream.substring(mark, k),null, parent);
                    mark = k;
                    leaves.add(lexeme);
                    return lexeme;
                }
            }
        }
        return new Leaf(parent.tagEncode(TagRecord.EOF_LABEL, TagPriority.LEX), "", null, parent);
    }
}