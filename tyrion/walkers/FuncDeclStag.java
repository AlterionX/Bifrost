import asgard.ScopeChanger;
import asgard.Stag;
import yggdrasil.*;

import java.util.ArrayList;

public class FuncDeclStag extends Stag {
    private boolean inFunc = false;
    private String lastFuncName;

    public FuncDeclStag(Yggdrasil parent) {
        super(parent, true);
    }

    @Override
    protected boolean onLaunch() {
        return false;
    }
    @Override
    protected boolean onUpEnter(Branch branch) {
        if (branch.getTag() == parent.tagEncode("FUNC_DEC", TagPriority.PAR)) {
            System.out.println("We have here, a declaration of peace.");
            if (inFunc) {
                throw new RuntimeException("Error. Function has been declared inside a function. Aborting.");
            }
            inFunc = true;
        }
        return false;
    }
    @Override
    protected boolean onDownEnter(Branch branch, Branch child) {
        if (branch.getTag() == parent.tagEncode("FUNC_DEC", TagPriority.SUB)) {
            if (child.getTag() == parent.tagEncode("NAME", TagPriority.SUB)) {
                System.out.println("\tFunction name: " + ((Leaf) child).getSubstring());
                parent.addSym(((Leaf) child).getSubstring(), "function");
                lastFuncName = ((Leaf) child).getSubstring();
            } else if (child.getTag() == parent.tagEncode("D_ARGLIST", TagPriority.SUB)) {
                System.out.println("\tArgument list: ");
                DArgListStag argWalker = new DArgListStag(parent, lastFuncName);
                Stag.startWalk(argWalker, child, false);
            } else if (child.getTag() == parent.tagEncode("STMT", TagPriority.SUB)) {
                System.out.println("\tStatement begins.");
            }
        }
        return false;
    }
    @Override
    protected boolean onUpExit(Branch branch) {
        if (branch.getTag() == parent.tagEncode("FUNC_DEC", TagPriority.PAR)) {
            System.out.println("The function has ended.");
            inFunc = false;
            System.out.println();
        }
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
        return "Function Declaration Analyzer.";
    }
}
