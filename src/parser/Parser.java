package parser;

import ast.AST;
import base.Cosmos;
import config.PathHolder;

public abstract class Parser extends Cosmos {
    protected Parser(PathHolder context, AST ast)  {
        super(context, ast);
    }

    public abstract boolean parse();
}
