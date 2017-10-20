package ast;

import tagtable.Tag;

import java.util.ArrayList;
import java.util.List;

public class Leaf extends Branch implements Seedling {
    private String substring;

    public Leaf(Tag tag, String substring) {
        super(tag);
        this.substring = substring;
    }

    public String getSubstring() {
        return substring;
    }
    public void setSubstring(String substring) {
        this.substring = substring;
    }

    public void append() {
        throw new RuntimeException("Terminal cannot be appended to.");
    }
    public void lrTraverse(BranchProcessFunction func, int level) {
        func.processBranch(this, level);
    }

    public List<Seedling> getChildren() {
        return new ArrayList<>();
    }
    public String toString() {
        return "\"" + substring + "\" :::: " + super.tag.getValue() + "::::LEVEL" + level;
    }
}
