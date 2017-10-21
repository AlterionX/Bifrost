package parser;

import ast.AST;
import ast.Cosmos;
import config.PathHolder;

public abstract class ParserRuleBook extends Cosmos {
    protected ParserRuleBook(PathHolder context, AST ast) {
        super(context, ast);
    }
}
