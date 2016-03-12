package org.testdb.shell;

import org.testdb.database.InMemoryDatabase;
import org.testdb.parse.SQLParser.SelectStatementContext;
import org.testdb.relation.Cursor;
import org.testdb.relation.Relation;
import org.testdb.relation.Tuple;

import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;

public class SelectStatementEvaluator {
    public void evaluate(InMemoryDatabase database, SelectStatementContext ctx) {
        String tableName = ctx.selectStatementFromClause().ID().getText();
        Relation relation = database.getTables().get(tableName);
        
        Preconditions.checkState(
                relation != null,
                "Relation '%s' does not exist.",
                tableName);
        
        try (Cursor<Tuple> c = relation.getTuples()) {
            c.forEachRemaining(t -> System.out.println(t));
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }
}
