package assembler;

import ast.AST;
import base.Cosmos;
import config.PathHolder;

public abstract class Assembler extends Cosmos {
    protected Assembler(PathHolder context, AST ast)  {
        super(context, ast);
    }

    public abstract void assemble(String[] strings);
}
