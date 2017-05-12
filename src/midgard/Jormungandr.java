package midgard;

import yggdrasil.Branch;
import yggdrasil.Cosmos;
import yggdrasil.Seedling;
import yggdrasil.Yggdrasil;

import java.util.Stack;

/**
 * Consumes the lexemes of Niflheim to create the structures of Midgard.
 *
 * Named after the serpent surrounding Midgard, Jormungandr now looks towards Yggdrasil, attempting to
 * create more complex ordered chaos.
 *
 * The parser.
 */
public class Jormungandr extends Cosmos {
    //Stable fields
    private Skadi skadi;

    /**
     * Initializes Jormungandr.
     * @param context The context data, AST, and symtable
     */
    public Jormungandr(Yggdrasil context) {
        super(context);
        System.out.println("Jormungandr configured.");
    }
    /**
     * Initialize the parser tables.
     */
    protected void configure() {
        skadi = new Skadi(context);
    }

    /**
     * Based on the rules in Skadi, parse the lexeme stream. Then use the rule to shift and reduce.
     * @return Whether or not the file is free of syntax errors.
     */
    public boolean parse() {
        System.out.println("Converting source code to AST");
        if (context.DEBUG) System.out.println("/*************************Generating AST***************************/");
        Stack<Branch> productionStack = new Stack<>();
        Stack<Integer> stateStack = new Stack<>();
        Branch curr = context.nextToken();
        Branch next = context.nextToken();
        stateStack.push(0);
        productionStack.push(null);
        boolean failed = false;
        while (true) {
            Integer actionEncoding = skadi.progressAndEncode(stateStack.peek(), curr, next);
            if (context.DEBUG) System.out.println("Processing stack: " + productionStack + ", state stack: " + stateStack + ", lookahead 1: " + curr);
            if (context.DEBUG) System.out.println("Action to take during this step: " + actionEncoding);
            if (skadi.isComplete(actionEncoding, curr)) {
                if (productionStack.size() == 3 && stateStack.size() == 3) {
                    if (context.DEBUG) System.out.println("Parse complete");
                    productionStack.pop();
                    stateStack.pop();
                    break;
                }
                System.out.println("");
                System.out.println("Syntax error @ stage " + stateStack.peek());
                failed = true;
                break;
            }
            if (skadi.isReduce(actionEncoding)) { //REDUCE
                CFGProduction rule = skadi.getReduceProduction(actionEncoding, curr, next);
                rule.ruleReduce(stateStack, productionStack);
                stateStack.push(skadi.reduceProgress(stateStack.peek(), productionStack.peek()));
            } else if (skadi.isShift(actionEncoding)) { //SHIFT
                productionStack.push(curr);
                curr = next;
                next = context.nextToken();
                stateStack.push(skadi.decode(actionEncoding));
            } else { //ERROR
                System.out.println("Syntax error on reading " + curr + ", and " + next + ", with stack " + productionStack + ".");
                failed = true;
                break;
            }
        }
        if (!failed) {
            context.addCore(productionStack.peek());
        } else {
            Seedling.simplePrint(productionStack.peek());
        }
        if (context.DEBUG) System.out.println("/******************************************************************/");
        return !failed;
    }
}
