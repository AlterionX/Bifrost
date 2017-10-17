package tagtable;

import java.util.*;

public class TagTable {
    //region Constant labels
    private static final String IGNORE_LABEL = "%IGNORE";
    public static final String EOF_LABEL = "/EOF";
    public static final String EMP_LABEL = "/EPSILON";
    public static final String START_LABEL = "/START";
    //endregion

    public final Tag IGNORE_TAG;
    public final Tag EOF_TAG;
    public final Tag EMP_TAG;

    public final Tag START_TAG;

    private final HashMap<String, Tag> tagLabels = new HashMap<>();
    //TODO think of substitutions
    private final HashMap<Tag, Integer> tagIndices = new HashMap<>();
    private final HashMap<TagPriority, List<Tag>> tagLists = new HashMap<>();

    public TagTable() {
        for (TagPriority p : TagPriority.values()) {
            tagLists.put(p, new ArrayList<>());
        }

        this.IGNORE_TAG = addElseFindTag(TagPriority.LEX, TagTable.IGNORE_LABEL);
        this.EOF_TAG = addElseFindTag(TagPriority.LEX, TagTable.EOF_LABEL);
        this.EMP_TAG = addElseFindTag(TagPriority.LEX, TagTable.EMP_LABEL);

        this.START_TAG = addElseFindTag(TagPriority.PAR, TagTable.START_LABEL);
    }

    public Tag addElseFindTag(TagPriority priority, String value) {
        Tag temp = new Tag(priority, value, tagLists.get(priority).size());
        List<Tag> tagList = tagLists.get(priority);
        if (tagLabels.containsKey(value)) {
            temp = tagLabels.get(value);
        } else {
            tagLabels.put(value, temp);
            tagIndices.put(temp, tagList.size());
            tagList.add(temp);
        }
        return temp;
    }

    public boolean hasTag(Tag tag) {
        return tagIndices.containsKey(tag);
    }

    //Additional data fetchers
    //Locating terminal tags, which should solely consist of lexing tags. Single characters as well.
    public boolean isTerminalTag(Tag tag) {
        return tag.getPriority().equals(TagPriority.LEX);
    }
    public int terminalTagCount() {
        return tagLists.get(TagPriority.LEX).size();
    }
    public int tagCount() {
        int sum = 0;
        for (List<Tag> data : tagLists.values()) {
            sum += data.size();
        }
        return sum;
    }

    public List<Tag> fetchTags(TagPriority priority) {
        return new ArrayList<>(tagLists.get(priority));
    }
    public Set<Tag> fetchAllTags() {
        return new HashSet<>(tagIndices.keySet());
    }
}
