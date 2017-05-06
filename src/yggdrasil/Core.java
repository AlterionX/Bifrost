package yggdrasil;

import java.util.ArrayList;
import java.util.List;

public class Core implements Seedling {
    private Branch internal;

    public Core(Branch newCore) {
        internal = newCore;
    }

    public void registerParent(Seedling seedling) {
        throw new RuntimeException("Cores have no parent but the tree of parent.");
    }
    public void lrTraverse(TreeTraverser func, int level) {
        System.out.println(this);
        internal.lrTraverse(func, level);
    }
    public void setLevel(int level) {}
    public int getLevel() {
        return 0;
    }

    public List<Seedling> getChildren() {
        return new ArrayList<>();
    }

    public String toString() {
        return "Starting point: CORE";
    }

    public Branch getInternal() {
        return internal;
    }
}
