package niflheim;

import tagtable.TagTable;
import ast.Leaf;
import config.PathHolder;

public class NiflheimTester {
    public static void main(String[] args) {
        PathHolder ph = new PathHolder();
        ph.DEBUG = true;
        Helvegar helvegar = fetchHelvegar(ph, new TagTable());
        Leaf hi;
        while (!(hi = helvegar.next()).getTag().getValue().equals(TagTable.EOF_LABEL)) {
            System.out.println(hi.getSubstring());
        }
    }

    public static Helvegar fetchHelvegar(PathHolder ph, TagTable tt) {
        Helvegar helvegar = new Helvegar(ph, tt);
        helvegar.loadStream("hello.tyr");
        return helvegar;
    }
}
