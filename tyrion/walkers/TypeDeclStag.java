import yggdrasil.Branch;
import asgard.Stag;
import yggdrasil.Seedling;
import yggdrasil.TagPriority;
import yggdrasil.Yggdrasil;

public class TypeDeclStag extends Stag{
    private static final int[] TYPE_SIZE = {8, 8, 8, 8};
    private static String[] TYPE_LIST = {"int", "int_arr", "str", "str_arr"};
    public TypeDeclStag(Yggdrasil parent) {
        super(parent, true);
    }

    @Override
    protected boolean onLaunch() {
        //Default types
        for (int i = 0; i < TYPE_LIST.length; i++) {
            parent.addSym(TYPE_LIST[i], "type");
            parent.addSymProperty(TYPE_LIST[i], "type",
                    "level", "primitive");
            parent.addSymProperty(TYPE_LIST[i], "type",
                    "sizeof", String.valueOf(TYPE_SIZE[i]));
        }
        return false;
    }
    @Override
    protected boolean onUpEnter(Branch branch) {
        if (branch.getTag() == parent.tagEncode("ASSIGNMENT", TagPriority.SUB)) {
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
        return null;
    }
}
