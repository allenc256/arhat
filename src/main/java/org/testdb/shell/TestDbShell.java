package org.testdb.shell;

import org.testdb.database.InMemoryDatabase;
import org.testdb.parse.SqlParseException;
import org.testdb.statement.SqlStatement;
import org.testdb.statement.parse.SqlStatementParser;

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
            SqlStatementParser parser = new SqlStatementParser();
            SqlStatement statement = parser.parse(database, line);
            statement.accept(new TestDbShellSqlStatementEvaluator());
        } catch (SqlParseException e) {
            System.err.println(e.getMessage());
            System.err.flush();
        } catch (Exception e) {
            e.printStackTrace(System.err);
            System.err.println();
            System.err.flush();
        }
    }
}
