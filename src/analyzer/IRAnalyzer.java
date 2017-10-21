package analyzer;

import ast.AST;
import ast.Branch;
import ast.Cosmos;
import config.PathHolder;

public abstract class IRAnalyzer extends Cosmos{
    protected IRAnalyzer(PathHolder context, AST ast)  {
        super(context, ast);
    }

    public abstract void analyze(Branch root, String file);

    public abstract String getTargetPath();
}
