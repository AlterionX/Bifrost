package yggdrasil;

/**
 * An interface to write a tree traverser. This will traverse the AST tree, from right to left,
 * the right being the first member of the production the branch originated from.
 */
public interface TreeTraverser {
    void processBranch(Seedling branch, int level, Object... additional);
}
