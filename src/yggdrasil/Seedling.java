package yggdrasil;

import logger.Log;

import java.util.List;

public interface Seedling {
    char[] LVL_DELIM = {'|', ':'};
    /**
     * Iterates through all sub-children and prints out their representations.
     * @param start The first sub-child.
     */
    static void simplePrint(Seedling start) {
        start.lrTraverse((branch, level, additional) -> {
            for (int i = 0; i < level; i++) {
                Log.l(LVL_DELIM[i % LVL_DELIM.length] + "\t");
            }
            Log.l(((Integer) level).toString());
            Log.lln(branch.toString());
        }, 0);
    }

    /**
     * Sets a Branch as the parend of another Seedling.
     * @param seedling the parent
     */
    void registerParent(Seedling seedling);
    /**
     * Returns a list of the children in proper CFG-based order.
     * @return The list of sub nodes of the node
     */
    List<? extends Seedling> getChildren();

    /**
     * Traverses the tree, from the leftmost child (the first child in proper CFG-base order) to the rightmost.
     * @param func A function to be called with the TreeTraverser interface.
     * @param level
     */
    void lrTraverse(TreeTraverser func, int level);

    void setLevel(int level);
    int getLevel();
}
