package org.testdb.statement;

import org.testdb.relation.Tuple;

public interface SqlInsertStatement extends SqlStatement {
    String getTableName();
    
    Tuple getTuple();

    @Override
    default <T> T accept(SqlStatementVisitor<T> visitor) {
        return visitor.visitInsertStatement(this);
    }
}
