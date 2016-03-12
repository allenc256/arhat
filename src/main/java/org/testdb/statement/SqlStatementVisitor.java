package org.testdb.statement;

public interface SqlStatementVisitor<T> {
    T visitSelectStatement(SqlSelectStatement statement);
    
    T visitInsertStatement(SqlInsertStatement statement);
    
    T visitCreateTableStatement(SqlCreateTableStatement statement);
    T visitDropTableStatement(SqlDropTableStatement statement);
}
