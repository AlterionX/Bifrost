package asgard;

import yggdrasil.Branch;
import yggdrasil.Seedling;
import yggdrasil.Yggdrasil;

public abstract class Stag {
    protected Yggdrasil parent;
    private ScopeChanger scopeChanger;
    public Stag(Yggdrasil parent, boolean initial) {
        this.parent = parent;
        scopeChanger = new ScopeChanger(parent, initial);
    }

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
                }
                scopeChanger.onDownEnter(branch, (Branch) child);
            }
            if (onUpExit(branch)) {
                scopeChanger.onUpExit(branch);
                return;
            }
        }
        scopeChanger.onUpExit(branch);
    }

    protected abstract boolean onLaunch();
    protected abstract boolean onUpEnter(Branch branch);
    protected abstract boolean onDownEnter(Branch branch, Branch child);
    protected abstract boolean onUpExit(Branch branch);
    protected abstract boolean onDownExit(Branch branch, Branch child);
    protected abstract boolean onComplete();

    public abstract String getWalkerName();
}
