package asgard;

import yggdrasil.Branch;
import yggdrasil.Seedling;
import yggdrasil.Yggdrasil;

public abstract class Stag {
    protected Yggdrasil parent;
    private ScopeChanger scopeChanger;
    /**
     * Construct the Stag.
     * @param parent context data, AST, and symtable
     * @param initial If is the first stag
     */
    public Stag(Yggdrasil parent, boolean initial) {
        this.parent = parent;
        scopeChanger = new ScopeChanger(parent, initial);
    }

    /**
     * Begin the walk, calling the triggers as well.
     * @param walker The walker to launch.
     * @param root The starting position.
     * @param topLevel If is at top, so don't backtrack.
     */
    public static void startWalk(Stag walker, Branch root, boolean topLevel) {
        walker.scopeChanger.onLaunch(topLevel);
        if (!walker.onLaunch()) {
            walker.walk(root);
            if (walker.onComplete()) {
                walker.scopeChanger.onComplete(topLevel);
                return;
            }
        }
        walker.scopeChanger.onComplete(topLevel);
    }
    /**
     * Walk the tree, triggering all of the hooks.
     * @param branch The branch to walk through.
     */
    protected void walk(Branch branch) {
        scopeChanger.onUpEnter(branch);
        if (!onUpEnter(branch)) {
            for (Seedling child : branch.getChildren()) {
                if (!onDownExit(branch, (Branch) child)) {
                    scopeChanger.onDownExit(branch, (Branch) child);
                    walk((Branch) child);
                    if (onDownEnter(branch, (Branch) child)) {
                        scopeChanger.onDownEnter(branch, (Branch) child);
                        scopeChanger.onUpExit(branch);
                        return;
                    }
                    scopeChanger.onDownEnter(branch, (Branch) child);
                }
            }
            if (onUpExit(branch)) {
                scopeChanger.onUpExit(branch);
                return;
            }
        }
        scopeChanger.onUpExit(branch);
    }

    /**
     * Launches at startup.
     * @return Whether to halt the program
     */
    protected abstract boolean onLaunch();
    /**
     * Launches at after initial call to squirrel.
     * @return Whether to halt the program
     */
    protected abstract boolean onUpEnter(Branch branch);
    /**
     * Launches after the state change on entering from a child node.
     * @return Whether to halt the program
     */
    protected abstract boolean onDownEnter(Branch branch, Branch child);
    /**
     * Launches after the state change on exiting to a child node.
     * @return Whether to halt the program
     */
    protected abstract boolean onUpExit(Branch branch);
    /**
     * Launches after the state change on entering from a child node.
     * @return Whether to halt the program
     */
    protected abstract boolean onDownExit(Branch branch, Branch child);
    /**
     * Launches on complete.
     * @return No idea why this returns.
     */
    protected abstract boolean onComplete();

    /**
     * Fetch a colloquial name for the walker.
     * @return The name
     */
    public abstract String getWalkerName();
}
