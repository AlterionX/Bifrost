package lexer;

import ast.AST;
import ast.Branch;
import ast.Cosmos;
import config.PathHolder;

public abstract class Lexer extends Cosmos{
    protected Lexer(PathHolder context, AST ast)  {
        super(context, ast);
    }

    public abstract void loadStream(String file);

    public abstract Branch next();
}
