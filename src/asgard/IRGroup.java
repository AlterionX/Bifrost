package asgard;

import java.util.ArrayList;
import java.util.List;

/**
 * A "group" of intermediate representational transformation rules.
 *
 * These pertain to an entire class of nodes, which have their own unique tags.
 */
public class IRGroup {
    private Integer initialTag;
    private List<IRChunk> ruleChunks = new ArrayList<>();
    private IRChunk catchall = null;

    /**
     * Initialize the rule group.
     * @param initialTag The tag this group will look at
     */
    public IRGroup(Integer initialTag) {
        this.initialTag = initialTag;
    }

    /**
     * Add the rule chunk to the list of chunks.
     * @param chunk The chunk to add.
     */
    public void addIRRuleChunk(IRChunk chunk) {
        if (chunk.isUniversal()) {
            if (catchall != null) {
                throw new RuntimeException("Duplicate catchall IRRuleChunks.");
            }
            catchall = chunk;
            return;
        }
        ruleChunks.add(chunk);
    }

    /**
     * Gets the tag relevant to this group.
     * @return The tag
     */
    public Integer getTag() {
        return initialTag;
    }

    /**
     * Prints the rules belonging to this group in a pretty format.
     */
    public void printRules() {
        System.out.println("Tag: " + initialTag);
        for (IRChunk chunk : ruleChunks) {
            chunk.printRules();
        }
    }

    /**
     * Fetch the chunks that belong to this group.
     * @return A list of the chunks.
     */
    public List<IRChunk> getChunks() {
        return ruleChunks;
    }

    /**
     * Get the catchall chunk of the group, the else statement.
     * @return
     */
    public IRChunk getCatchAll() {
        return catchall;
    }
}
