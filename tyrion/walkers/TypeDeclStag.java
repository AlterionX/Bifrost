import asgard.Stag;
import tagtable.TagTable;
import yggdrasil.Branch;
import yggdrasil.Nidhogg;
import yggdrasil.PathHolder;
import tagtable.TagPriority;

public class TypeDeclStag extends Stag{
    private static final int[] NATIVE_TYPE_SIZE = {8/*, 8, 8, 8*/};
    private static String[] NATIVE_TYPE_LIST = {"int"/*, "int_arr", "str", "str_arr"*/};
    public TypeDeclStag(PathHolder pathHolder, TagTable tagTable, Nidhogg symTable) {
        super(tagTable, pathHolder, symTable, true);
    }

    @Override
    protected boolean onLaunch() {
        //Default types
        for (int i = 0; i < NATIVE_TYPE_LIST.length; i++) {
            super.symTable.addSym(NATIVE_TYPE_LIST[i], "type", 0);
            super.symTable.addSymProperty(NATIVE_TYPE_LIST[i], "type",
                    "level", "primitive");
            super.symTable.addSymProperty(NATIVE_TYPE_LIST[i], "type",
                    "sizeof", String.valueOf(NATIVE_TYPE_SIZE[i]));
        }
        return false;
    }
    @Override
    protected boolean onUpEnter(Branch branch) {
        if (branch.getTag().equals(super.tagTable.addElseFindTag(TagPriority.SUB, "ASSIGNMENT"))) {
            branch.flipChildren();
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
        return false;
    }
    @Override
    public String getWalkerName() {
        return "Type declaration";
    }
}
