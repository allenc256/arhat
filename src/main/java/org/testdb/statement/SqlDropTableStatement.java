package org.testdb.statement;

public interface SqlDropTableStatement extends SqlStatement {
    String getTableName();
    
    @Override
    default <T> T accept(SqlStatementVisitor<T> visitor) {
        return visitor.visitDropTableStatement(this);
    }
}
