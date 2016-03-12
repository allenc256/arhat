package org.testdb.test.harness;

import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.immutables.value.Value;
import org.junit.Assert;
import org.testdb.database.InMemoryDatabase;
import org.testdb.relation.Tuple;
import org.testdb.statement.AbstractSqlStatementEvaluator;
import org.testdb.statement.SqlSelectStatement;
import org.testdb.statement.SqlStatement;
import org.testdb.statement.parse.SqlStatementParser;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.collect.Lists;

@Value.Immutable
@JsonDeserialize(as = ImmutableSqlTestCase.class)
public abstract class AbstractSqlTestCase extends Assert implements SqlTestCase {
    @Override
    public void execute(InMemoryDatabase database) {
        if (getStatement().trim().equalsIgnoreCase("RESET")) {
            database.reset();
            return;
        }
        
        List<List<Object>> actualTuples = Lists.newArrayList();
        
        try {
            SqlStatementParser parser = new SqlStatementParser();
            SqlStatement statement = parser.parse(database, getStatement());
            statement.accept(new AbstractSqlStatementEvaluator() {
                @Override
                public Void visitSelectStatement(SqlSelectStatement statement) {
                    actualTuples.addAll(statement.getRelation()
                            .getTupleStream()
                            .map(Tuple::getValues)
                            .collect(Collectors.toList()));
                    return null;
                }
            });
        } catch (Exception e) {
            if (!getError().isPresent()) {
                throw e;
            }
            checkException(e);
        }
        
        assertEquals(getTuples(), actualTuples);
    }

    private void checkException(Exception e) {
        assertNotNull("expected error.", e);
        
        Pattern pattern = Pattern.compile(getError().get());
        if (!pattern.matcher(e.getMessage()).find()) {
            fail(String.format(
                    "exception message '%s' does not match expected regex '%s'.",
                    e.getMessage(),
                    getError().get()));
        }
    }

    @Override
    public String toString() {
        return String.format("%s (%s)", getName().or("test"), getStatement());
    }
}
