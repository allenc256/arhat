package org.testdb.parse.statement;

import java.util.List;

import org.testdb.database.InMemoryDatabase;
import org.testdb.expression.AbstractIdentifierExpression;
import org.testdb.expression.Expression;
import org.testdb.expression.ImmutableExtractIndexExpression;
import org.testdb.parse.SQLBaseVisitor;
import org.testdb.parse.SQLParser;
import org.testdb.parse.SQLParser.SelectStatementColumnContext;
import org.testdb.parse.SQLParser.SelectStatementColumnExpressionContext;
import org.testdb.parse.SQLParser.SelectStatementColumnStarContext;
import org.testdb.parse.SQLParser.SelectStatementContext;
import org.testdb.parse.SQLParser.SelectStatementFromSubqueryContext;
import org.testdb.parse.SQLParser.SelectStatementFromTableContext;
import org.testdb.parse.expression.ExpressionParser;
import org.testdb.relation.ColumnSchema;
import org.testdb.relation.ImmutableColumnSchema;
import org.testdb.relation.ImmutableFilteredRelation;
import org.testdb.relation.ImmutableNamedRelation;
import org.testdb.relation.ImmutableNestedLoopJoinRelation;
import org.testdb.relation.ImmutableProjectedRelation;
import org.testdb.relation.ImmutableTupleSchema;
import org.testdb.relation.JoinPredicates;
import org.testdb.relation.Relation;
import org.testdb.relation.Tuple;
import org.testdb.relation.TupleSchema;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;

public class SelectStatementParser {
    public Relation parse(InMemoryDatabase database, SelectStatementContext ctx) {
        Relation relation = ctx.selectStatementFromClause()
                .selectStatementFromTableOrSubquery()
                .stream()
                .map(s -> s.accept(new FromClauseVisitor(database)))
                .reduce((r1, r2) -> joinRelations(r1, r2))
                .get();
        relation = applyFilterIfNecessary(ctx, relation);
        relation = applyProjectionIfNecessary(ctx, relation);
        return relation;
    }
    
    private Relation joinRelations(Relation r1, Relation r2) {
        return ImmutableNestedLoopJoinRelation.builder()
                .fromRelation(r1)
                .toRelation(r2)
                .joinPredicate(JoinPredicates.alwaysTrue())
                .build();
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
                .accept(new ExpressionParser(relation.getTupleSchema()));
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
        
        ColumnsVisitor visitor = new ColumnsVisitor(
                relation.getTupleSchema());
        ctx.selectStatementColumns().accept(visitor);
        
        return ImmutableProjectedRelation.builder()
                .expressions(visitor.getExpressions())
                .sourceRelation(relation)
                .tupleSchema(visitor.getTargetTupleSchema())
                .build();
    }
    
    private static class ColumnsVisitor extends SQLBaseVisitor<Void> {
        private final List<Expression> expressions = Lists.newArrayList();
        private final List<Optional<String>> columnNames = Lists.newArrayList();
        private final TupleSchema sourceTupleSchema;

        private ColumnsVisitor(TupleSchema sourceTupleSchema) {
            this.sourceTupleSchema = sourceTupleSchema;
        }

        @Override
        public Void visitSelectStatementColumnStar(SelectStatementColumnStarContext ctx) {
            Optional<String> qualifier = ctx.ID() != null ?
                    Optional.of(ctx.ID().getText()) :
                    Optional.absent();
                    
            if (qualifier.isPresent()) {
                boolean validQualifier = sourceTupleSchema.getColumnSchemas()
                        .stream()
                        .filter(cs -> cs.getQualifierAliases().contains(qualifier.get()))
                        .findAny()
                        .isPresent();
                Preconditions.checkState(
                        validQualifier,
                        "Qualifier '%s' does not match any input relations.",
                        qualifier.get());
            }
            
            for (int i = 0; i < sourceTupleSchema.size(); ++i) {
                ColumnSchema cs = sourceTupleSchema.getColumnSchema(i);
                
                if (qualifier.isPresent() && !cs.getQualifierAliases().contains(qualifier.get())) {
                    continue;
                }
                
                expressions.add(ImmutableExtractIndexExpression.builder()
                        .tupleIndex(i)
                        .type(sourceTupleSchema.getColumnSchema(i).getType())
                        .build());
                columnNames.add(cs.getName());
            }
            
            return null;
        }

        @Override
        public Void visitSelectStatementColumnExpression(SelectStatementColumnExpressionContext ctx) {
            ExpressionParser visitor = new ExpressionParser(sourceTupleSchema);
            Expression expression = ctx.expression().accept(visitor);
            
            expressions.add(expression);
            
            if (ctx.ID() != null) {
                columnNames.add(Optional.of(ctx.ID().getText()));
            } else if (expression instanceof AbstractIdentifierExpression) {
                columnNames.add(Optional.of(((AbstractIdentifierExpression)expression).getColumnName().getName()));
            } else {
                columnNames.add(Optional.absent());
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
    
    private static class FromClauseVisitor extends SQLBaseVisitor<Relation> {
        private InMemoryDatabase database;
        
        private FromClauseVisitor(InMemoryDatabase database) {
            this.database = database;
        }
        
        @Override
        public Relation visitSelectStatementFromTable(SelectStatementFromTableContext ctx) {
            String tableName = ctx.ID(0).getText();
            Relation relation = database.getTables().get(tableName);
            
            Preconditions.checkState(
                    relation != null,
                    "Relation '%s' does not exist.",
                    tableName);
            Preconditions.checkState(
                    ctx.ID().size() <= 2,
                    "Expected at most two ID tokens.");
            
            if (ctx.ID().size() > 1) { 
                // N.B., it should be possible to refer to the table using
                // *either* the full table name or the alias in this case.
                relation = ImmutableNamedRelation.builder()
                        .sourceRelation(relation)
                        .aliases(ImmutableSet.of(tableName, ctx.ID(1).getText()))
                        .build();
            }
            
            return relation;
        }

        @Override
        public Relation visitSelectStatementFromSubquery(SelectStatementFromSubqueryContext ctx) {
            SelectStatementParser parser = new SelectStatementParser();
            Relation relation = parser.parse(database, ctx.selectStatement());
            
            if (ctx.ID() != null) {
                relation = ImmutableNamedRelation.builder()
                        .sourceRelation(relation)
                        .aliases(ImmutableSet.of(ctx.ID().getText()))
                        .build();
            } else {
                // N.B., it's important that we wrap the sub-query in a
                // ImmutableNamedRelation wrapper here even if no explicit
                // subquery name was specfied in the query. In this case, the
                // wrapper ensures that qualifiers in the subquery don't "leak"
                // out of the subquery.
                relation = ImmutableNamedRelation.builder()
                        .sourceRelation(relation)
                        .build();
            }
            
            return relation;
        }
    }
}
