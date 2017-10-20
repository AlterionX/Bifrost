import asgard.Stag;
import tagtable.TagPriority;
import tagtable.TagTable;
import ast.Branch;
import ast.Leaf;
import symtable.Nidhogg;
import config.PathHolder;

public class FuncDeclStag extends Stag {
    private boolean inFunc = false;
    private String lastFuncName;

    private int suffix;

    public FuncDeclStag(PathHolder pathHolder, TagTable tagTable, Nidhogg symTable) {
        super(tagTable, pathHolder, symTable, true);
    }

    @Override
    protected boolean onLaunch() {
        return false;
    }
    @Override
    protected boolean onUpEnter(Branch branch) {
        if (branch.getTag().equals(super.tagTable.addElseFindTag(TagPriority.PAR, "FUNC_DEC"))) {
            //System.out.println("We have here, a declaration of peace.");
            if (inFunc) {
                throw new RuntimeException("Error. Function has been declared inside a function. Aborting.");
            }
            inFunc = true;
        }
        return false;
    }
    @Override
    protected boolean onDownEnter(Branch branch, Branch child) {
        if (branch.getTag().equals(super.tagTable.addElseFindTag(TagPriority.SUB, "FUNC_DEC"))) {
            if (child.getTag().equals(super.tagTable.addElseFindTag(TagPriority.SUB, "NAME"))) {
                //System.out.println("\tFunction name: " + ((Leaf) child).getSubstring());
                super.symTable.addSym(((Leaf) child).getSubstring(), "function", -1);
                lastFuncName = ((Leaf) child).getSubstring();
            } else if (child.getTag().equals(super.tagTable.addElseFindTag(TagPriority.SUB, "D_ARGLIST"))) {
                //System.out.println("\tArgument list: ");
                DArgListStag argWalker = new DArgListStag(super.holder, super.tagTable, super.symTable, lastFuncName);
                Stag.startWalk(argWalker, child, false);
            } else if (child.getTag().equals(super.tagTable.addElseFindTag(TagPriority.SUB, "STMT"))) {
                //System.out.println("\tStatement begins.");
            }
        }
        return false;
    }
    @Override
    protected boolean onUpExit(Branch branch) {
        if (branch.getTag().equals(super.tagTable.addElseFindTag(TagPriority.PAR, "FUNC_DEC"))) {
            //System.out.println("The function has ended.");
            inFunc = false;
            //System.out.println();
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
