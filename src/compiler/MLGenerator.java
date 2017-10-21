package compiler;

import ast.AST;
import ast.Cosmos;
import config.PathHolder;
import tagtable.TagTable;

public abstract class MLGenerator extends Cosmos {
    protected MLGenerator(PathHolder context, AST ast)  {
        super(context, ast);
    }

    public abstract void convert(String targetPath);
}
