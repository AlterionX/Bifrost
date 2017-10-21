package assembler;

import ast.AST;
import ast.Cosmos;
import config.PathHolder;
import tagtable.TagTable;

public abstract class Assembler extends Cosmos {
    protected Assembler(PathHolder context, AST ast)  {
        super(context, ast);
    }

    public abstract void assemble(String[] strings);
}
