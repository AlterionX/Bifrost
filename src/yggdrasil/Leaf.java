package yggdrasil;

import java.util.ArrayList;
import java.util.List;

public class Leaf extends Branch implements Seedling {
    private String substring;
    private Object value;

    public Leaf(int tag, String substring, Branch parent, Yggdrasil master) {
        super(tag, master);
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
    public void lrTraverse(TreeTraverser func, int level) {
        func.processBranch(this, level);
    }

    public List<Seedling> getChildren() {
        return new ArrayList<>();
    }
    public String toString() {
        return "\"" + substring + "\" :::: " + super.master.tagDecode(getTag(), TagPriority.LEX) + "::::LEVEL" + level;
    }
}
