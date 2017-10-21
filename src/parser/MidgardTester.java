package parser;

import config.PathHolder;
import lexer.Helvegar;
import lexer.NiflheimTester;
import symtable.Nidhogg;
import tagtable.TagTable;
import ast.*;

public class MidgardTester {
    public static void main(String[] args) {
        PathHolder holder = new PathHolder();
        AST ast = new Yggdrasil();
        holder.DEBUG = true;

        Helvegar helvegar = NiflheimTester.fetchHelvegar(holder, ast);

        Jormungandr jormungandr = new Jormungandr(holder, ast, helvegar);

        jormungandr.parse();
    }
}
