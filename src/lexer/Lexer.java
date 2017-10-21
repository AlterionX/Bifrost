package lexer;

import ast.AST;
import ast.Branch;
import base.Cosmos;
import config.PathHolder;

public abstract class Lexer extends Cosmos{
    protected Lexer(PathHolder context, AST ast)  {
        super(context, ast);
    }

    public abstract void loadStream(String file);

    public abstract Branch next();
}
