import asgard.Stag;
import tagtable.TagTable;
import ast.Branch;
import symtable.Nidhogg;
import config.PathHolder;

public class ConstantifyStag extends Stag {
    public ConstantifyStag(PathHolder pathHolder, TagTable tagTable, Nidhogg symTable) {
        super(tagTable, pathHolder, symTable, false);
    }

    @Override
    protected boolean onLaunch() {
        return false;
    }

    @Override
    protected boolean onUpEnter(Branch branch) {
        return false;
    }

    @Override
    protected boolean onDownEnter(Branch branch, Branch child) {
        return false;
    }

    @Override
    protected boolean onUpExit(Branch branch) {
        return false;
    }

    @Override
    protected boolean onDownExit(Branch branch, Branch child) {
        return false;
    }

    @Override
    protected boolean onComplete() {
        return false;
    }

    @Override
    public String getWalkerName() {
        return "Constantify Optimizer -- TODO";
    }
}
