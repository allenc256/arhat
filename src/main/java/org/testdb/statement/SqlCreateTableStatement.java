package org.testdb.statement;

import org.testdb.relation.TupleSchema;

public interface SqlCreateTableStatement extends SqlStatement {
    String getTableName();
    
    TupleSchema getTupleSchema();
    
    @Override
    default <T> T accept(SqlStatementVisitor<T> visitor) {
        return visitor.visitCreateTableStatement(this);
    }
}
