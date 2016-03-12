package org.testdb.shell;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.testdb.database.InMemoryDatabase;
import org.testdb.parse.AntlrParseException;
import org.testdb.parse.AntlrParseExceptionErrorListener;
import org.testdb.parse.SQLLexer;
import org.testdb.parse.SQLParser;
import org.testdb.parse.SQLParser.StatementContext;

import jline.console.ConsoleReader;

public class TestDbShell {
    public static void main(String[] args) throws Exception {
        ConsoleReader reader = new ConsoleReader();
        InMemoryDatabase database = new InMemoryDatabase();
        
        evaluateStatement(database, "create table foobar (foo integer, bar string, baz boolean);");
        evaluateStatement(database, "insert into foobar (foo, bar, baz) values (1, 'hello', true);");
        evaluateStatement(database, "insert into foobar (foo, bar, baz) values (2, 'world', false);");
        evaluateStatement(database, "create table quux (foo integer, value string);");
        evaluateStatement(database, "insert into quux (foo, value) values (1, 'lorem');");
        evaluateStatement(database, "insert into quux (foo, value) values (2, 'ipsum');");
        evaluateStatement(database, "insert into quux (foo, value) values (3, 'dolor');");
        
        reader.setPrompt("testdb# ");
        
        String line;
        while ((line = reader.readLine()) != null) {
            if (line.trim().equals("")) {
                continue;
            }
            evaluateStatement(database, line);
        }
    }

    private static void evaluateStatement(InMemoryDatabase database,
                                          String line) {
        try {
            SQLParser parser = parseSql(line);
            StatementContext statement = parser.topLevelStatement().statement();
            StatementEvaluator evaluator = new StatementEvaluator();
            evaluator.evaluate(database, statement);
        } catch (AntlrParseException e) {
            System.err.println(e.getMessage());
            System.err.flush();
        } catch (Exception e) {
            e.printStackTrace(System.err);
            System.err.println();
            System.err.flush();
        }
    }

    private static SQLParser parseSql(String line) {
        ANTLRInputStream input = new ANTLRInputStream(line);
        SQLLexer lexer = new SQLLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        SQLParser parser = new SQLParser(tokens);
        
        parser.removeErrorListeners();
        parser.addErrorListener(new AntlrParseExceptionErrorListener());
        
        return parser;
    }
}
