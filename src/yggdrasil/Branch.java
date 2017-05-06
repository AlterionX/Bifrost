package yggdrasil;

import java.util.ArrayList;
import java.util.List;

public class Branch implements Seedling {
    private Integer tag;
    private ArrayList<Branch> children = new ArrayList<>();
    private Seedling parent;
    protected Yggdrasil master;
    protected int level;

    public Branch(Integer tag, Yggdrasil master) {
        this.master = master;
        this.tag = tag;
    }

    public void append(Branch subNode) {
        subNode.registerParent(this);
        children.add(subNode);
    }
    public void registerParent(Seedling seedling) {
        this.parent = seedling;
    }
    public Seedling getParent() {
        return parent;
    }
    public void lrTraverse(TreeTraverser func, int level) {
        func.processBranch(this, level);
        for (int i = 0; i < children.size(); i++) {
            children.get(i).lrTraverse(func, level + 1);
        }
    }
    public void setLevel(int level) {
        this.level = level;
    }
    public int getLevel() {
        return level;
    }

    @Override
    public List<? extends Seedling> getChildren() {
        return children;
    }
    public int getTag() {
        return tag;
    }
    public String toString() {
        return master.tagDecode(tag, TagPriority.SUB);
    }
}
