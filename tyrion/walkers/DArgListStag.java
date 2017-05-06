import asgard.Stag;
import yggdrasil.*;

public class DArgListStag extends Stag {
    String funcName;
    int argNum = 0;

    public DArgListStag(Yggdrasil parent, String lastFuncName) {
        super(parent, false);
        this.funcName = lastFuncName;
    }
    @Override
    protected boolean onLaunch() {
        return false;
    }
    @Override
    protected boolean onUpEnter(Branch branch) {
        if (branch.getTag() == parent.tagEncode("VAR_DEC", TagPriority.SUB)) {
            parent.addSymProperty(funcName, "function", "arg" + argNum + "type",
                    ((Leaf) branch.getChildren().get(0)).getSubstring());
            parent.addSymProperty(funcName, "function", "arg" + argNum + "name",
                    ((Leaf) branch.getChildren().get(1)).getSubstring());
            argNum++;
        }
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
        parent.addSymProperty(funcName, "function", "argCount", String.valueOf(argNum));
        System.out.println("\t\t" + parent.getSym(funcName, "function"));
        return false;
    }
    @Override
    public String getWalkerName() {
        return "Declaration argument list walker.";
    }
}
