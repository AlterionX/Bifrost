import asgard.ScopeChanger;
import asgard.Stag;
import yggdrasil.*;

import java.util.ArrayList;

public class VarDeclStag extends Stag {
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
            System.out.println("We have here, a declaration of war.");
            if (!parent.hasSym(((Leaf) branch.getChildren().get(0)).getSubstring(), "type")) {
                throw new RuntimeException("This type, " + ((Leaf) branch.getChildren().get(0)).getSubstring() +
                        " is not recognized.");
            }
            parent.addSym(((Leaf) branch.getChildren().get(1)).getSubstring(), "var");
            parent.addSymProperty(((Leaf) branch.getChildren().get(1)).getSubstring(), "var",
                    "vartype", ((Leaf) branch.getChildren().get(0)).getSubstring());
            System.out.println(((Leaf) branch.getChildren().get(1)).getSubstring() + ": " +
                    parent.getSym(((Leaf) branch.getChildren().get(1)).getSubstring(), "var"));
        }
        return false;
    }
    @Override
    protected boolean onDownEnter(Branch branch, Branch child) {
        if (child.getTag() == parent.tagEncode("NAME", TagPriority.SUB) &&
                branch.getTag() == parent.tagEncode("VAR_ACCSS", TagPriority.SUB)) {
            //Check for decl
            if (!parent.hasSym(((Leaf) child).getSubstring(), "var")) {
                throw new RuntimeException("Undeclared variable " + ((Leaf) child).getSubstring() + ".");
            }
            child.setLevel(parent.getSymOffset(((Leaf) child).getSubstring(), "var"));
            Seedling.simplePrint(child);
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
