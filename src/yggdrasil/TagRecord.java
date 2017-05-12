package yggdrasil;

import java.util.*;

public class TagRecord {
    //Constant labels
    public static final String IGNORE_LABEL = "%IGNORE";
    public static final String EOF_LABEL = "/EOF";
    public static final String EMP_LABEL = "/EPSILON";
    public static final String START_LABEL = "/START";

    private List<String> lexTags = new ArrayList<>();
    private List<String> parTags = new ArrayList<>();
    private List<String> subTags = new ArrayList<>();
    //Variable containers for holding relational data between substitute and proper tags
    private Map<Integer, List<Integer>> parSubConvMap = new HashMap<>();
    private Map<Integer, Integer> subParConvMap = new HashMap<>();

    public TagRecord() {
        addLexTagIfAbsent(IGNORE_LABEL);
        addLexTagIfAbsent(EOF_LABEL);
        addLexTagIfAbsent(EMP_LABEL);
        addParTagIfAbsent(START_LABEL);
    }
    //Lexical tags, regular expression sub-components of the language, should only be terminals
    //Modifying tags
    public int addLexTagIfAbsent(String tag) {
        if (!lexTags.contains(tag)) {
            lexTags.add(tag);
            return lexTags.size() - 1;
        }
        return lexTags.indexOf(tag);
    }
    public boolean hasLexLabel(String tag) {
        return lexTags.contains(tag);
    }
    public boolean hasLexTag(Integer tag) {
        return tag < lexTags.size();
    }
    //Encoding and decoding of tags
    public int lexEncode(String label) {
        return lexTags.indexOf(label);
    }
    public String lexDecode(int tag) {
        return lexTags.get(tag);
    }
    //Parsing tags, context-free grammar tags
    //Modifying and querying state of parsing tags (both terminal and non-terminal)
    public int addParTagIfAbsent(String tag) {
        if (hasLexLabel(tag)) return lexEncode(tag);
        if (!parTags.contains(tag)) {
            parTags.add(tag);
            return parTags.size() - 1 + lexTags.size();
        }
        return parTags.indexOf(tag) + lexTags.size();
    }
    public boolean hasParLabel(String tag) {
        return hasLexLabel(tag) || parTags.contains(tag);
    }
    public boolean hasParTag(Integer tag) {
        return hasLexTag(tag) || tag - lexTags.size() < parTags.size();
    }
    //Encoding and decoding of parsing tags
    public int parEncode(String label) {
        if (!hasParLabel(label)) return -1;
        return hasLexLabel(label) ? lexEncode(label) : parTags.indexOf(label) + lexTags.size();
    }
    public String parDecode(int tag) {
        if (tag >= lexTags.size() + parTags.size()) throw new RuntimeException("Unknown parsing tag.");
        return tag < lexTags.size() ? lexDecode(tag) : parTags.get(tag - lexTags.size());
    }
    //Substitute tags, for substituting things away from the standard CFG. currently mostly unused.
    //Generating tags
    public String generateSubTag(String oldTag) {
        if (subTags.contains(oldTag)) {
            return generateSubTag(getSubOrig(subEncode(oldTag)));
        } else if (hasParLabel(oldTag)) {
            List<Integer> subs = parSubConvMap.putIfAbsent(parEncode(oldTag), new ArrayList<>());
            if (subs == null) {
                subs = parSubConvMap.get(parEncode(oldTag));
            }

            String secTag = oldTag + "%" + subs.size();
            subTags.add(secTag);
            subParConvMap.putIfAbsent(subEncode(secTag), parEncode(oldTag));
            subs.add(subEncode(secTag));
            return secTag;
        } else {
            throw new RuntimeException("No such tag as " + oldTag + " exists");
        }
    }
    public String getSubOrig(int tag) {
        return parDecode(subParConvMap.get(tag));
    }
    public boolean hasSubLabel(String tag) {
        return hasLexLabel(tag) || hasParLabel(tag) || subTags.contains(tag);
    }
    public boolean hasSubTag(Integer tag) {
        return hasLexTag(tag) || hasParTag(tag) || tag - lexTags.size() - parTags.size() < subTags.size();
    }
    //Encoding and decoding of substituted and parsing tags
    public int subEncode(String label) {
        if (!hasSubLabel(label)) return -1;
        return hasLexLabel(label) ? lexEncode(label) : (hasParLabel(label) ? parEncode(label) : subTags.indexOf(label) + lexTags.size() + parTags.size());
    }
    public String subDecode(int tag) {
        if (tag == -1) throw new RuntimeException("Tags cannot have negative values.");
        return tag < lexTags.size() ? lexDecode(tag) : (tag < parTags.size() + lexTags.size() ? parDecode(tag) : (subTags.get(tag - parTags.size() - lexTags.size())));
    }
    //Additional data fetchers
    //Locating terminal tags, which should solely consist of lexing tags. Single characters as well
    public boolean isTerminal(String label) {
        return hasLexLabel(label);
    }
    public boolean isTerminal(int tag) {
        return tag < lexTags.size();
    }
    public int terminalCount() {
        return lexTags.size();
    }
    public int tagCount() {
        return lexTags.size() + parTags.size() + subTags.size();
    }
}
