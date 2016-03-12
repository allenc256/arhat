package org.testdb.statement;

import org.testdb.database.InMemoryDatabase;

public interface SqlStatement {
    InMemoryDatabase getDatabase();
    
    <T> T accept(SqlStatementVisitor<T> visitor);
}
