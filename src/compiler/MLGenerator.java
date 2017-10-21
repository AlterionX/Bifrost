package compiler;

import ast.AST;
import base.Cosmos;
import config.PathHolder;

public abstract class MLGenerator extends Cosmos {
    protected MLGenerator(PathHolder context, AST ast)  {
        super(context, ast);
    }

    public abstract void convert(String targetPath);
}
