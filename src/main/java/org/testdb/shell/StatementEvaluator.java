package org.testdb.shell;

import org.testdb.database.InMemoryDatabase;
import org.testdb.parse.SQLBaseVisitor;
import org.testdb.parse.SQLParser.CreateTableStatementContext;
import org.testdb.parse.SQLParser.DropTableStatementContext;
import org.testdb.parse.SQLParser.InsertStatementContext;
import org.testdb.parse.SQLParser.SelectStatementContext;
import org.testdb.parse.SQLParser.StatementContext;

public class StatementEvaluator {
    public void evaluate(InMemoryDatabase database, StatementContext ctx) {
        ctx.accept(new StatementVisitor(database));
    }
    
    private static class StatementVisitor extends SQLBaseVisitor<Void> {
        private final InMemoryDatabase database;
        
        private StatementVisitor(InMemoryDatabase database) {
            this.database = database;
        }

        @Override
        public Void visitCreateTableStatement(CreateTableStatementContext ctx) {
            CreateTableStatementEvaluator evaluator = new CreateTableStatementEvaluator();
            evaluator.evaluate(database, ctx);
            return null;
        }

        @Override
        public Void visitDropTableStatement(DropTableStatementContext ctx) {
            DropTableStatementEvaluator evaluator = new DropTableStatementEvaluator();
            evaluator.evaluate(database, ctx);
            return null;
        }
        
        @Override
        public Void visitInsertStatement(InsertStatementContext ctx) {
            InsertStatementEvaluator evaluator = new InsertStatementEvaluator();
            evaluator.evaluate(database, ctx);
            return null;
        }

        @Override
        public Void visitSelectStatement(SelectStatementContext ctx) {
            SelectStatementEvaluator evaluator = new SelectStatementEvaluator();
            evaluator.evaluate(database, ctx);
            return null;
        }
    }
}
