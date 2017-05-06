package asgard;

import java.util.ArrayList;
import java.util.List;

public class IRGroup {
    private Integer initialTag;
    private List<IRChunk> ruleChunks = new ArrayList<>();
    private IRChunk catchall = null;

    public IRGroup(Integer initialTag) {
        this.initialTag = initialTag;
    }

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

    public Integer getTag() {
        return initialTag;
    }

    public void printRules() {
        System.out.println("Tag: " + initialTag);
        for (IRChunk chunk : ruleChunks) {
            chunk.printRules();
        }
    }

    public List<IRChunk> getChunks() {
        return ruleChunks;
    }

    public IRChunk getCatchAll() {
        return catchall;
    }
}
