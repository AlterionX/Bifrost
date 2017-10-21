package compiler;

import ast.AST;
import base.Cosmos;
import config.PathHolder;

public abstract class MLRuleBook extends Cosmos {
    protected MLRuleBook(PathHolder context, AST ast) {
        super(context, ast);
    }
}
