package org.testdb.shell;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.antlr.v4.runtime.tree.TerminalNode;
import org.testdb.database.InMemoryDatabase;
import org.testdb.parse.SQLParser.InsertStatementContext;
import org.testdb.parse.SQLParser.LiteralContext;
import org.testdb.parse.SqlParseException;
import org.testdb.parse.expression.LiteralParser;
import org.testdb.relation.ColumnSchema;
import org.testdb.relation.ImmutableQualifiedName;
import org.testdb.relation.ImmutableTuple;
import org.testdb.relation.SortedMultisetRelation;
import org.testdb.relation.Tuple;
import org.testdb.relation.TupleSchema;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class InsertStatementEvaluator {
    public void evaluate(InMemoryDatabase database, InsertStatementContext ctx) {
        // Parse relation.
        String tableName = ctx.ID().getText().toLowerCase();
        SortedMultisetRelation relation = database.getTables().get(tableName);
        if (relation == null) {
            throw SqlParseException.create(
                    ctx.ID().getSymbol(),
                    "relation '%s' does not exist.",
                    ctx.ID().getText());
        }

        List<ColumnSchema> columnSchemas = parseColumnSchemas(
                ctx,
                relation.getTupleSchema());
        List<Object> columnValues = parseColumnValues(
                ctx, 
                columnSchemas);
        
        relation.getTuplesSortedMultiset().add(constructTuple(
                relation,
                columnSchemas,
                columnValues));
    }

    private List<Object> parseColumnValues(InsertStatementContext ctx,
                                           List<ColumnSchema> columnSchemas) {
        List<Object> columnValues = Lists.newArrayList();
        List<LiteralContext> ls = ctx.insertStatementValues().literal();
        
        for (int i = 0; i < ls.size(); ++i) {
            if (i >= columnSchemas.size()) {
                throw SqlParseException.create(
                        ls.get(i).getStart(),
                        "more target expressions than columns specified.");
            }
            
            Object value = parseLiteralValue(ls.get(i));
            ColumnSchema cs = columnSchemas.get(i);
            
            if (value != null && !value.getClass().equals(cs.getType().getJavaType())) {
                throw SqlParseException.create(
                        ls.get(i).getStart(),
                        "expected literal of type %s.",
                        cs.getType());
            }
            
            columnValues.add(value);
        }
        
        return columnValues;
    }

    private List<ColumnSchema> parseColumnSchemas(InsertStatementContext ctx,
                                                  TupleSchema tupleSchema) {
        Set<ColumnSchema> columnNames = Sets.newLinkedHashSet();
        
        for (TerminalNode id : ctx.insertStatementColumns().ID()) {
            String columnName = id.getText().toLowerCase();
            
            Collection<ColumnSchema> css = tupleSchema.getColumnSchemas(
                    ImmutableQualifiedName.of(columnName));
            if (css.isEmpty()) {
                throw SqlParseException.create(
                        id.getSymbol(),
                        "column '%s' does not exist.",
                        columnName);
            }
            
            if (!columnNames.add(Iterables.getOnlyElement(css))) {
                throw SqlParseException.create(
                        id.getSymbol(),
                        "column '%s' specified more than once.",
                        columnName);
            }
            
            if (columnNames.size() > ctx.insertStatementValues().literal().size()) {
                throw SqlParseException.create(
                        id.getSymbol(),
                        "more target columns than expressions specified.");
            }
        }
        
        return ImmutableList.copyOf(columnNames);
    }

    private Object parseLiteralValue(LiteralContext value) {
        LiteralParser v = new LiteralParser();
        value.accept(v);
        Preconditions.checkState(v.getType() != null);
        return v.getValue();
    }

    private Tuple constructTuple(SortedMultisetRelation relation,
                                 List<ColumnSchema> columnSchemas,
                                 List<Object> columnValues) {
        List<Object> values = Lists.newArrayListWithCapacity(
                relation.getTupleSchema().size());
        
        for (int i = 0; i < relation.getTupleSchema().size(); ++i) {
            values.add(null);
        }
        
        for (int i = 0; i < columnSchemas.size(); ++i) {
            values.set(columnSchemas.get(i).getIndex(), columnValues.get(i));
        }
        
        return ImmutableTuple.builder().values(values).build();
    }
}
