package org.testdb.shell;

import java.util.List;
import java.util.stream.Collectors;

import org.testdb.database.InMemoryDatabase;
import org.testdb.parse.SQLParser;
import org.testdb.parse.SqlStrings;
import org.testdb.parse.SQLParser.InsertStatementContext;
import org.testdb.parse.SQLParser.InsertStatementValueContext;
import org.testdb.relation.ColumnSchema;
import org.testdb.relation.ImmutableTuple;
import org.testdb.relation.SortedMultisetRelation;
import org.testdb.relation.Tuple;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

public class InsertStatementEvaluator {
    public void evaluate(InMemoryDatabase database, InsertStatementContext ctx) {
        String tableName = ctx.ID().getText();
        SortedMultisetRelation relation = database.getTables().get(tableName);
        
        if (relation == null) {
            throw new IllegalStateException(
                    String.format("Table '%s' does not exist.", tableName));
        }
        
        List<String> columnNames = ctx.insertStatementColumns()
                .ID()
                .stream()
                .map(id -> id.getText())
                .collect(Collectors.toList());
        
        List<Object> columnValues = ctx.insertStatementValue()
                .stream()
                .map(value -> parseValue(value))
                .collect(Collectors.toList());
        
        Preconditions.checkState(
                ImmutableSet.copyOf(columnNames).size() == columnNames.size(),
                "Column names must be unique.");
        Preconditions.checkState(
                columnNames.size() == columnValues.size(),
                "Count of columns must match count of values.");
        
        relation.getTuplesSortedMultiset().add(
                constructTuple(relation, columnNames, columnValues));
    }

    private Tuple constructTuple(SortedMultisetRelation relation,
                                 List<String> columnNames,
                                 List<Object> columnValues) {
        List<Object> values = Lists.newArrayListWithCapacity(
                relation.getTupleSchema().size());
        
        for (int i = 0; i < relation.getTupleSchema().size(); ++i) {
            values.add(null);
        }
        
        for (int i = 0; i < columnNames.size(); ++i) {
            String name = columnNames.get(i);
            Object value = columnValues.get(i);
            ColumnSchema cs = Iterables.getOnlyElement(
                    relation.getTupleSchema().getColumnSchemas(name));
            values.set(cs.getIndex(), value);
        }
        
        return ImmutableTuple.builder()
                .schema(relation.getTupleSchema())
                .values(values)
                .build();
    }

    private Object parseValue(InsertStatementValueContext value){
        switch (value.start.getType()) {
        case SQLParser.NULL:
            return null;
        case SQLParser.STRING_LITERAL:
            String s = value.getText();
            Preconditions.checkState(
                    s.startsWith("'") && s.endsWith("'"),
                    "Cannot parse string literal token.");
            return SqlStrings.unescape(s.substring(1, s.length() - 1));
        case SQLParser.INTEGER_LITERAL:
            return Integer.parseInt(value.getText());
        default:
            throw new IllegalStateException("Unexpected token type.");
        }
    }
}
