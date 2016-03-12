package org.testdb.shell;

import java.util.List;
import java.util.stream.Collectors;

import org.testdb.database.InMemoryDatabase;
import org.testdb.parse.SQLParser.InsertStatementContext;
import org.testdb.parse.SQLParser.LiteralContext;
import org.testdb.parse.expression.LiteralParser;
import org.testdb.relation.ColumnSchema;
import org.testdb.relation.ImmutableQualifiedName;
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
        
        List<Object> columnValues = ctx.insertStatementValues()
                .literal()
                .stream()
                .map(this::parseLiteralValue)
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

    private Object parseLiteralValue(LiteralContext value) {
        LiteralParser v = new LiteralParser();
        value.accept(v);
        Preconditions.checkState(
                v.getType() != null,
                "Failed to parse literal value.");
        return v.getValue();
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
            ColumnSchema cs = Iterables.getOnlyElement(relation
                    .getTupleSchema()
                    .getColumnSchemas(ImmutableQualifiedName.of(name)));
            values.set(cs.getIndex(), value);
        }
        
        return ImmutableTuple.builder().values(values).build();
    }
}
