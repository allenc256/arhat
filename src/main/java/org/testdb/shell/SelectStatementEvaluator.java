package org.testdb.shell;

import java.util.List;

import org.testdb.database.InMemoryDatabase;
import org.testdb.expression.Expression;
import org.testdb.expression.ExpressionVisitor;
import org.testdb.parse.SQLParser;
import org.testdb.parse.SQLParser.SelectStatementColumnContext;
import org.testdb.parse.SQLParser.SelectStatementContext;
import org.testdb.relation.ColumnSchema;
import org.testdb.relation.ImmutableColumnSchema;
import org.testdb.relation.ImmutableFilteredRelation;
import org.testdb.relation.ImmutableProjectedRelation;
import org.testdb.relation.ImmutableTupleSchema;
import org.testdb.relation.Relation;
import org.testdb.relation.Tuple;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

public class SelectStatementEvaluator {
    public void evaluate(InMemoryDatabase database, SelectStatementContext ctx) {
        String tableName = ctx.selectStatementFromClause().ID().getText();
        Relation relation = database.getTables().get(tableName);
        
        Preconditions.checkState(
                relation != null,
                "Relation '%s' does not exist.",
                tableName);
        
        relation = applyFilterIfNecessary(ctx, relation);
        relation = applyProjectionIfNecessary(ctx, relation);
        
        RelationRenderer.builder()
                .relation(relation)
                .build()
                .render();
    }
    
    private Predicate<Tuple> toPredicate(Expression<? extends Object> expression) {
        return t -> {
            Object result = expression.evaluate(t);
            Preconditions.checkState(
                    result instanceof Boolean,
                    "Predicate expression must be boolean-typed.");
            return (Boolean)result;
        };
    }
    
    private Relation applyFilterIfNecessary(SelectStatementContext ctx,
                                            Relation relation) {
        if (ctx.selectStatementWhereClause() == null) {
            return relation;
        }
        
        Expression<? extends Object> expression = ctx.selectStatementWhereClause()
                .expression()
                .accept(new ExpressionVisitor(relation.getTupleSchema()));
        return ImmutableFilteredRelation.builder()
                .sourceRelation(relation)
                .filterPredicate(toPredicate(expression))
                .build();
    }

    private Relation applyProjectionIfNecessary(SelectStatementContext ctx,
                                                Relation relation) {
        List<SelectStatementColumnContext> columns = ctx.selectStatementColumns().selectStatementColumn();
        
        if (columns.size() == 1
                && columns.get(0).column.getType() == SQLParser.STAR) {
            // N.B., a query of the form "SELECT * FROM foo"; no projection needed.
            return relation;
        }
        
        ImmutableTupleSchema.Builder targetSchema = ImmutableTupleSchema.builder();
        List<Integer> targetSchemaMapping = Lists.newArrayList();

        for (SelectStatementColumnContext c : columns) {
            switch (c.column.getType()) {

            // Each time we encounter a "*", copy all source columns to the
            // input. Note that we allow multiple "*"'s to be specified (as does
            // postgres).
            case SQLParser.STAR:
                for (ColumnSchema cs : relation.getTupleSchema().getColumnSchemas()) {
                    targetSchema.addColumnSchemas(ImmutableColumnSchema.builder()
                            .from(cs)
                            .index(targetSchemaMapping.size())
                            .build());
                    targetSchemaMapping.add(cs.getIndex());
                }
                break;
                
            case SQLParser.ID:
                String columnName = c.column.getText();
                Iterable<ColumnSchema> css = relation.getTupleSchema().getColumnSchemas(columnName);
                int numColumns = Iterables.size(css);
                
                if (numColumns == 0) {
                    throw new IllegalStateException(String.format("Column '%s' does not exist.", columnName));
                }
                if (numColumns > 1) {
                    throw new IllegalStateException(String.format("Column name '%s' is ambiguous.", columnName));
                }
                
                ColumnSchema cs = Iterables.getOnlyElement(css);
                targetSchema.addColumnSchemas(ImmutableColumnSchema.builder()
                        .from(cs)
                        .index(targetSchemaMapping.size())
                        .build());
                targetSchemaMapping.add(cs.getIndex());
                break;
                
            default:
                throw new IllegalStateException("Unexpected token type.");
            }
        }
        
        return ImmutableProjectedRelation.builder()
                .sourceRelation(relation)
                .tupleSchema(targetSchema.build())
                .schemaMapping(targetSchemaMapping)
                .build();
    }
}
