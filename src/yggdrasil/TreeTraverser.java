package yggdrasil;

/**
 * An interface to write a tree traverser. This will traverse the AST tree, from left to right,
 * the left being the first member of the production the branch originated from.
 */
interface TreeTraverser {
    void processBranch(Seedling branch, int level, Object... additional);
}
