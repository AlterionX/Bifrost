package midgard;

import yggdrasil.Branch;
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
public class Jormungandr {
    //AST, CFG data
    private Yggdrasil parent;
    private Skadi skadi;
    //Parser logic
    public Jormungandr(Yggdrasil parent) {
        this.parent = parent;
        skadi = new Skadi(parent, parent.BASE_DIR + parent.TARGET + parent.PARSER_DEC_EXTENSION);
    }
    public boolean parse() {
        System.out.println("/*************************Generating AST***************************/");
        Stack<Branch> productionStack = new Stack<>();
        Stack<Integer> stateStack = new Stack<>();
        Branch curr = parent.nextToken();
        Branch next = parent.nextToken();
        stateStack.push(0);
        productionStack.push(null);
        boolean failed = false;
        while (true) {
            Integer actionEncoding = skadi.progressAndEncode(stateStack.peek(), curr, next);
            if (parent.DEBUG) System.out.println("Processing stack: " + productionStack + ", state stack: " + stateStack + ", lookahead 1: " + curr);
            if (parent.DEBUG) System.out.println("Action to take during this step: " + actionEncoding);
            if (skadi.isComplete(actionEncoding, curr)) {
                if (productionStack.size() == 3 && stateStack.size() == 3) {
                    if (parent.DEBUG) System.out.println("Parse complete");
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
                CFGRule rule = skadi.reduceRule(actionEncoding, curr, next);
                rule.ruleReduce(stateStack, productionStack);
                stateStack.push(skadi.reduceProgress(stateStack.peek(), productionStack.peek()));
            } else if (skadi.isShift(actionEncoding)) { //SHIFT
                productionStack.push(curr);
                curr = next;
                next = parent.nextToken();
                stateStack.push(skadi.decode(actionEncoding));
            } else { //ERROR
                System.out.println("Syntax error on reading " + curr + ", and " + next + ", with stack " + productionStack + ".");
                failed = true;
                break;
            }
        }
        if (!failed) {
            parent.addCore(productionStack.peek());
            Seedling.simplePrint(productionStack.peek());
        }
        System.out.println("/******************************************************************/");
        return !failed;
    }
}
