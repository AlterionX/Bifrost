import asgard.Stag;
import config.PathHolder;
import symtable.Nidhogg;
import tagtable.TagPriority;
import tagtable.TagTable;
import ast.*;

public class DArgListStag extends Stag {
    String funcName;
    int argNum = 0;
    int paramSize;

    public DArgListStag(PathHolder pathHolder, TagTable tagTable, Nidhogg symTable, String lastFuncName) {
        super(tagTable, pathHolder, symTable, false);
        this.funcName = lastFuncName;
    }
    @Override
    protected boolean onLaunch() {
        return false;
    }
    @Override
    protected boolean onUpEnter(Branch branch) {
        if (branch.getTag() == super.tagTable.addElseFindTag(TagPriority.SUB, "VAR_DEC")) {
            super.symTable.addSymProperty(funcName, "function", "arg" + argNum + "type",
                    ((Leaf) branch.getChildren().get(0)).getSubstring());
            paramSize += Integer.parseInt(super.symTable.getSymProperty(((Leaf) branch.getChildren().get(0)).getSubstring(), "type", "sizeof").orElse("asdf"));
            super.symTable.addSymProperty(funcName, "function", "arg" + argNum + "name",
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
        super.symTable.addSymProperty(funcName, "function", "argCount", String.valueOf(argNum));
        super.symTable.addSymProperty(funcName, "function", "paramSize", String.valueOf(paramSize));
        return false;
    }
    @Override
    public String getWalkerName() {
        return "Declaration argument list walker.";
    }
}
