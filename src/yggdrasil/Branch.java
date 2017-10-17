package yggdrasil;

import tagtable.Tag;
import tagtable.TagPriority;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Branch implements Seedling {
    protected final Tag tag;
    private final ArrayList<Branch> children = new ArrayList<>();
    private Seedling parent;
    int level;

    public Branch(Tag tag) {
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
        for (Branch aChildren : children) {
            aChildren.lrTraverse(func, level + 1);
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
    public Tag getTag() {
        return tag;
    }
    public String toString() {
        return tag.getValue();
    }

    public void flipChildren() {
        LinkedList<Branch> children = new LinkedList<>();
        for (Branch cr : this.children) {
            children.addFirst(cr);
        }
        this.children.clear();
        this.children.addAll(children);
    }
}
