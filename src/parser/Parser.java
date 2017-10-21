package parser;

import ast.AST;
import ast.Cosmos;
import config.PathHolder;
import tagtable.TagTable;

public abstract class Parser extends Cosmos {
    protected Parser(PathHolder context, AST ast)  {
        super(context, ast);
    }

    public abstract boolean parse();
}
