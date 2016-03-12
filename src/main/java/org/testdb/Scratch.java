package org.testdb;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.testdb.SQLParser.StatementContext;

public class Scratch {
    public static void main(String[] args) throws Exception {
        ANTLRInputStream input = new ANTLRInputStream("select * from foo");
        SQLLexer lexer = new SQLLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        SQLParser parser = new SQLParser(tokens);
        StatementContext tree = parser.statement();
        System.out.println(tree.toStringTree(parser));
    }
}
