import asgard.Stag;
import config.PathHolder;
import symtable.Nidhogg;
import tagtable.TagPriority;
import tagtable.TagTable;
import ast.*;

public class VarDeclStag extends Stag {

    public VarDeclStag(PathHolder pathHolder, TagTable tagTable, Nidhogg symTable) {
        super(tagTable, pathHolder, symTable, false);
    }

    @Override
    protected boolean onLaunch() {
        return false;
    }
    @Override
    protected boolean onUpEnter(Branch branch) {
        if (branch.getTag().equals(super.tagTable.addElseFindTag(TagPriority.PAR, "VAR_DEC"))) {
            assert super.symTable.hasSym(((Leaf) branch.getChildren().get(0)).getSubstring(), "type", 0) != null :
                    "Variable contains unknown type.";
            super.symTable.addSym(((Leaf) branch.getChildren().get(1)).getSubstring(), "var", 0);
            super.symTable.addSymProperty(((Leaf) branch.getChildren().get(1)).getSubstring(), "var",
                    "vartype", ((Leaf) branch.getChildren().get(0)).getSubstring());
        }
        return false;
    }
    @Override
    protected boolean onDownEnter(Branch branch, Branch child) {
        if (child.getTag() == super.tagTable.addElseFindTag(TagPriority.PAR, "NAME") && (
                branch.getTag().equals(super.tagTable.addElseFindTag(TagPriority.PAR, "VAR_ACCSS")) ||
                        branch.getTag().equals(super.tagTable.addElseFindTag(TagPriority.PAR, "BASE"))
        )) {
            //Check for decl
            assert super.symTable.hasSym(((Leaf) child).getSubstring(), "var", 0) != null :
                    "Undeclared variable " + ((Leaf) child).getSubstring() + ".";
            child.setLevel(super.symTable.getOffset(((Leaf) child).getSubstring(), "var"));
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
