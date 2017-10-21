package lexer;

import ast.AST;
import ast.Yggdrasil;
import tagtable.TagTable;
import ast.Leaf;
import config.PathHolder;

public class NiflheimTester {
    public static void main(String[] args) {
        PathHolder ph = new PathHolder();
        ph.DEBUG = true;
        Helvegar helvegar = fetchHelvegar(ph, new Yggdrasil());
        Leaf hi;
        while (!(hi = helvegar.next()).getTag().getValue().equals(TagTable.EOF_LABEL)) {
            System.out.println(hi.getSubstring());
        }
    }

    public static Helvegar fetchHelvegar(PathHolder ph, AST ast) {
        Helvegar helvegar = new Helvegar(ph, ast);
        helvegar.loadStream("hello.tyr");
        return helvegar;
    }
}
