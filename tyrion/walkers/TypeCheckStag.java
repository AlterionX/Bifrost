import asgard.Stag;
import yggdrasil.Branch;
import yggdrasil.Yggdrasil;

public class TypeCheckStag extends Stag {
    public TypeCheckStag(Yggdrasil parent) {
        super(parent, false);
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
        return "Type Checker -- TODO";
    }
}
