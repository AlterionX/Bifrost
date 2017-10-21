package compiler;

import ast.AST;
import ast.Cosmos;
import config.PathHolder;

public abstract class MLRuleBook extends Cosmos {
    protected MLRuleBook(PathHolder context, AST ast) {
        super(context, ast);
    }
}
