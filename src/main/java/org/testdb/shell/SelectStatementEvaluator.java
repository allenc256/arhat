package org.testdb.shell;

import java.util.List;

import org.testdb.database.InMemoryDatabase;
import org.testdb.expression.AbstractIdentifierExpression;
import org.testdb.expression.Expression;
import org.testdb.expression.ExpressionVisitor;
import org.testdb.expression.ImmutableIdentifierExpression;
import org.testdb.parse.SQLBaseVisitor;
import org.testdb.parse.SQLParser;
import org.testdb.parse.SQLParser.SelectStatementColumnContext;
import org.testdb.parse.SQLParser.SelectStatementColumnExpressionContext;
import org.testdb.parse.SQLParser.SelectStatementColumnStarContext;
import org.testdb.parse.SQLParser.SelectStatementContext;
import org.testdb.relation.ColumnSchema;
import org.testdb.relation.ImmutableColumnSchema;
import org.testdb.relation.ImmutableFilteredRelation;
import org.testdb.relation.ImmutableProjectedRelation;
import org.testdb.relation.ImmutableTupleSchema;
import org.testdb.relation.Relation;
import org.testdb.relation.Tuple;
import org.testdb.relation.TupleSchema;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
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
    
    private Predicate<Tuple> toPredicate(Expression expression) {
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
        
        Expression expression = ctx.selectStatementWhereClause()
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
        
        // N.B., a query of the form "SELECT * FROM foo" does not need a projection.
        if (columns.size() == 1 && columns.get(0).start.getType() == SQLParser.STAR_SYMBOL) {
            return relation;
        }
        
        SelectStatementColumnsVisitor visitor = new SelectStatementColumnsVisitor(
                relation.getTupleSchema());
        ctx.selectStatementColumns().accept(visitor);
        
        return ImmutableProjectedRelation.builder()
                .expressions(visitor.getExpressions())
                .sourceRelation(relation)
                .tupleSchema(visitor.getTargetTupleSchema())
                .build();
    }
    
    private static class SelectStatementColumnsVisitor extends SQLBaseVisitor<Void> {
        private final List<Expression> expressions = Lists.newArrayList();
        private final List<String> columnNames = Lists.newArrayList();
        private final TupleSchema sourceTupleSchema;

        private SelectStatementColumnsVisitor(TupleSchema sourceTupleSchema) {
            this.sourceTupleSchema = sourceTupleSchema;
        }

        @Override
        public Void visitSelectStatementColumnStar(SelectStatementColumnStarContext ctx) {
            for (ColumnSchema cs : sourceTupleSchema.getColumnSchemas()) {
                expressions.add(ImmutableIdentifierExpression.builder()
                        .tupleSchema(sourceTupleSchema)
                        .columnName(cs.getName())
                        .build());
                columnNames.add(cs.getName());
            }
            
            return null;
        }

        @Override
        public Void visitSelectStatementColumnExpression(SelectStatementColumnExpressionContext ctx) {
            ExpressionVisitor visitor = new ExpressionVisitor(sourceTupleSchema);
            Expression expression = ctx.expression().accept(visitor);
            
            expressions.add(expression);
            if (expression instanceof AbstractIdentifierExpression) {
                columnNames.add(((AbstractIdentifierExpression)expression).getColumnName());
            } else {
                columnNames.add("?column?");
            }
            
            return null;
        }
        
        public List<Expression> getExpressions() {
            return expressions;
        }
        
        public TupleSchema getTargetTupleSchema() {
            Preconditions.checkState(
                    expressions.size() == columnNames.size(),
                    "Expression count and column name count mismatched.");
            
            List<ColumnSchema> columns = Lists.newArrayList();
            
            for (int i = 0; i < expressions.size(); ++i) {
                columns.add(ImmutableColumnSchema.builder()
                        .index(i)
                        .name(columnNames.get(i))
                        .type(expressions.get(i).getType())
                        .build());
            }
            
            return ImmutableTupleSchema.builder().columnSchemas(columns).build();
        }
    }
}
