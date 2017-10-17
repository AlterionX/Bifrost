package midgard;

import niflheim.Helvegar;
import niflheim.NiflheimTester;
import tagtable.TagTable;
import yggdrasil.*;

import java.util.Map;

public class MidgardTester {
    public static void main(String[] args) {
        TagTable tt = new TagTable();
        PathHolder ph = new PathHolder();
        ph.DEBUG = true;

        Helvegar helvegar = NiflheimTester.fetchHelvegar(ph, tt);

        WorldTree tremp = new WorldTree() {
            @Override
            public void launch() {

            }

            @Override
            public void addCore(Branch newCore) {

            }

            @Override
            public void printCores() {

            }

            @Override
            public int getCoreCount() {
                return 0;
            }

            @Override
            public Core getCore(int index) {
                return null;
            }

            @Override
            public TagTable getTagTable() {
                return null;
            }

            @Override
            public Nidhogg getSymTable() {
                return null;
            }
        };

        Jormungandr jormungandr = new Jormungandr(ph, tt, helvegar, tremp);

        jormungandr.parse();
    }
}