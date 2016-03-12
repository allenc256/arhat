package org.testdb.statement;

import org.testdb.relation.LexicographicTupleOrdering;
import org.testdb.relation.SortedMultisetRelation;

import com.google.common.collect.TreeMultiset;

public abstract class AbstractSqlStatementEvaluator implements SqlStatementVisitor<Void> {
    @Override
    public Void visitInsertStatement(SqlInsertStatement statement) {
        statement.getDatabase()
                .getTables()
                .get(statement.getTableName())
                .getTuplesSortedMultiset()
                .add(statement.getTuple());
        return null;
    }

    @Override
    public Void visitCreateTableStatement(SqlCreateTableStatement statement) {
        SortedMultisetRelation relation = SortedMultisetRelation.builder()
                .tupleSchema(statement.getTupleSchema())
                .tuplesSortedMultiset(TreeMultiset.create(LexicographicTupleOrdering.INSTANCE))
                .build();
        statement.getDatabase()
                .getTables()
                .put(statement.getTableName(), relation);
        return null;
    }

    @Override
    public Void visitDropTableStatement(SqlDropTableStatement statement) {
        statement.getDatabase()
                .getTables()
                .remove(statement.getTableName());
        return null;
    }
}
