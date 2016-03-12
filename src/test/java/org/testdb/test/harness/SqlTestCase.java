package org.testdb.test.harness;

import java.util.List;

import org.testdb.database.InMemoryDatabase;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.base.Optional;

@JsonDeserialize(as = ImmutableSqlTestCase.class)
public interface SqlTestCase {
    Optional<String> getName();
    
    String getStatement();
    
    Optional<String> getError();
    
    List<List<Object>> getTuples();
    
    void execute(InMemoryDatabase database);
}
