package org.testdb.statement.parse;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.testdb.database.InMemoryDatabase;
import org.testdb.parse.SQLBaseVisitor;
import org.testdb.parse.SQLLexer;
import org.testdb.parse.SQLParser;
import org.testdb.parse.SQLParser.CreateTableStatementContext;
import org.testdb.parse.SQLParser.DropTableStatementContext;
import org.testdb.parse.SQLParser.InsertStatementContext;
import org.testdb.parse.SQLParser.SelectStatementContext;
import org.testdb.parse.SQLParser.StatementContext;
import org.testdb.parse.SqlParseException;
import org.testdb.parse.SqlParseExceptionErrorListener;
import org.testdb.statement.SqlStatement;

public class SqlStatementParser {
    public SqlStatement parse(InMemoryDatabase database, String line) {
        ANTLRInputStream input = new ANTLRInputStream(line);
        SQLLexer lexer = new SQLLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        SQLParser parser = new SQLParser(tokens);
        
        parser.removeErrorListeners();
        parser.addErrorListener(new SqlParseExceptionErrorListener());
        
        return parse(database, parser.topLevelStatement().statement());
    }
    
    public SqlStatement parse(InMemoryDatabase database, StatementContext ctx) {
        SqlStatement statement = ctx.accept(new StatementVisitor(database));
        if (statement == null) {
            throw SqlParseException.create(
                    ctx.getStart(),
                    "Failed to parse statement.");
        }
        return statement;
    }
    
    private static class StatementVisitor extends SQLBaseVisitor<SqlStatement> {
        private final InMemoryDatabase database;
        
        private StatementVisitor(InMemoryDatabase database) {
            this.database = database;
        }

        @Override
        public SqlStatement visitCreateTableStatement(CreateTableStatementContext ctx) {
            SqlCreateTableStatementParser parser = new SqlCreateTableStatementParser();
            return parser.parse(database, ctx);
        }

        @Override
        public SqlStatement visitDropTableStatement(DropTableStatementContext ctx) {
            SqlDropTableStatementParser parser = new SqlDropTableStatementParser();
            return parser.parse(database, ctx);
        }
        
        @Override
        public SqlStatement visitInsertStatement(InsertStatementContext ctx) {
            SqlInsertStatementParser parser = new SqlInsertStatementParser();
            return parser.parse(database, ctx);
        }

        @Override
        public SqlStatement visitSelectStatement(SelectStatementContext ctx) {
            SqlSelectStatementParser parser = new SqlSelectStatementParser();
            return parser.parse(database, ctx);
        }
    }
}
