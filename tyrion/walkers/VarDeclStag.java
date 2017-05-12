import asgard.ScopeChanger;
import asgard.Stag;
import yggdrasil.*;

import java.util.ArrayList;

public class VarDeclStag extends Stag {
    private int suffix = 0;

    public VarDeclStag(Yggdrasil parent) {
        super(parent, false);
    }

    @Override
    protected boolean onLaunch() {
        return false;
    }
    @Override
    protected boolean onUpEnter(Branch branch) {
        if (branch.getTag() == parent.tagEncode("VAR_DEC", TagPriority.PAR)) {
            if (parent.hasSym(((Leaf) branch.getChildren().get(0)).getSubstring(), "type") == null) {
                throw new RuntimeException("Variable contains unknown type.");
            }
            parent.addSym(((Leaf) branch.getChildren().get(1)).getSubstring(), "var");
            parent.addSymProperty(((Leaf) branch.getChildren().get(1)).getSubstring(), "var",
                    "vartype", ((Leaf) branch.getChildren().get(0)).getSubstring());
        }
        return false;
    }
    @Override
    protected boolean onDownEnter(Branch branch, Branch child) {
        if (child.getTag() == parent.tagEncode("NAME", TagPriority.SUB) && (
                branch.getTag() == parent.tagEncode("VAR_ACCSS", TagPriority.SUB) ||
                        branch.getTag() == parent.tagEncode("BASE", TagPriority.SUB)
        )) {
            //Check for decl
            if (parent.hasSym(((Leaf) child).getSubstring(), "var") == null) {
                throw new RuntimeException("Undeclared variable " + ((Leaf) child).getSubstring() + ".");
            }
            child.setLevel(parent.getSymOffset(((Leaf) child).getSubstring(), "var"));
        }
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
        return "Variable Declaration Analyzer.";
    }
}
