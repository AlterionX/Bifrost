package lexer;

import ast.AST;
import base.Cosmos;
import config.PathHolder;

public abstract class LexerRuleBook extends Cosmos {
    protected LexerRuleBook(PathHolder context, AST ast) {
        super(context, ast);
    }
}
