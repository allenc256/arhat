package org.testdb.parse.statement;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.testdb.database.InMemoryDatabase;
import org.testdb.expression.Expression;
import org.testdb.expression.ImmutableVariableExpression;
import org.testdb.expression.NamedExpression;
import org.testdb.expression.aggregator.Aggregator;
import org.testdb.parse.SQLBaseVisitor;
import org.testdb.parse.SQLParser;
import org.testdb.parse.SQLParser.ExpressionContext;
import org.testdb.parse.SQLParser.SelectStatementColumnContext;
import org.testdb.parse.SQLParser.SelectStatementColumnExpressionContext;
import org.testdb.parse.SQLParser.SelectStatementColumnStarContext;
import org.testdb.parse.SQLParser.SelectStatementContext;
import org.testdb.parse.SQLParser.SelectStatementFromSubqueryContext;
import org.testdb.parse.SQLParser.SelectStatementFromTableContext;
import org.testdb.parse.expression.ExpressionHasAggregationParser;
import org.testdb.parse.expression.ExpressionParser;
import org.testdb.relation.ColumnSchema;
import org.testdb.relation.ImmutableColumnSchema;
import org.testdb.relation.ImmutableDistinctRelation;
import org.testdb.relation.ImmutableFilteredRelation;
import org.testdb.relation.ImmutableGroupByRelation;
import org.testdb.relation.ImmutableNamedRelation;
import org.testdb.relation.ImmutableNestedLoopJoinRelation;
import org.testdb.relation.ImmutableProjectedRelation;
import org.testdb.relation.ImmutableTupleSchema;
import org.testdb.relation.Relation;
import org.testdb.relation.Tuple;
import org.testdb.relation.TupleSchema;
import org.testdb.type.SqlType;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
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
        
        relation = filterIfNecessary(ctx, relation);
        relation = projectOrGroupByIfNecessary(ctx, relation);
        relation = distinctIfNecessary(ctx, relation);
        
        return relation;
    }
    
    private Relation joinRelations(Relation r1, Relation r2) {
        return ImmutableNestedLoopJoinRelation.builder()
                .leftRelation(r1)
                .rightRelation(r2)
                .build();
    }
    
    private Predicate<Tuple> toPredicate(Expression expression) {
        Preconditions.checkState(
                expression.getType() == SqlType.BOOLEAN,
                "Predicate expression must be boolean-typed.");
        return t -> (Boolean)expression.evaluate(t);
    }
    
    private Relation filterIfNecessary(SelectStatementContext ctx,
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
    
    private Relation projectOrGroupByIfNecessary(SelectStatementContext ctx,
                                                 Relation relation) {
        List<SelectStatementColumnContext> columns = ctx.selectStatementColumns().selectStatementColumn();
        boolean hasAggregations = ctx.selectStatementColumns().accept(new ExpressionHasAggregationParser());
        
        if (ctx.selectStatementGroupByClause() != null || hasAggregations) {
            return groupByRelation(ctx, relation);
        }
        
        if (columns.size() == 1 && columns.get(0).start.getType() == SQLParser.STAR_SYMBOL) {
            // N.B., a query of the form "SELECT * FROM foo" does not need a projection.
            return relation;
        }
        
        return projectRelation(ctx, relation);
    }
    
    private Relation groupByRelation(SelectStatementContext ctx,
                                     Relation relation) {
        List<Expression> groupByExpressions;
        
        if (ctx.selectStatementGroupByClause() != null) {
            groupByExpressions = ctx.selectStatementGroupByClause()
                    .expression()
                    .stream()
                    .map(e -> parseGroupByExpression(relation.getTupleSchema(), e))
                    .collect(Collectors.toList());
        } else {
            // N.B., if there's no GROUP BY clause, we're aggregating over
            // everything (e.g., "SELECT COUNT(*) FROM foo"). This is
            // easily implemented as grouping by the empty tuple "()".
            groupByExpressions = ImmutableList.of();
        }
        
        TupleSchema groupBySchema = toTupleSchema(groupByExpressions);
        
        ColumnsVisitor visitor = new ColumnsVisitor(
                groupBySchema,
                Optional.of(relation.getTupleSchema()));
        ctx.selectStatementColumns().accept(visitor);
        
        return ImmutableGroupByRelation.builder()
                .targetExpressions(visitor.getExpressions())
                .groupByExpressions(groupByExpressions)
                .sourceRelation(relation)
                .tupleSchema(visitor.getTargetTupleSchema())
                .aggregators(visitor.getAggregators())
                .build();
    }
    
    private Expression parseGroupByExpression(TupleSchema tupleSchema,
                                              ExpressionContext ctx) {
        return ctx.accept(new ExpressionParser(tupleSchema));
    }
    
    private TupleSchema toTupleSchema(List<Expression> expressions) {
        ImmutableTupleSchema.Builder b = ImmutableTupleSchema.builder();
        
        for (int i=0; i<expressions.size(); ++i) {
            Optional<String> columnName = Optional.absent();
            Expression expression = expressions.get(i);
            
            if (expression instanceof NamedExpression) {
                columnName = ((NamedExpression)expression).getName();
            }
            
            b.addColumnSchemas(ImmutableColumnSchema.builder()
                    .index(i)
                    .name(columnName)
                    .type(expression.getType())
                    .build());
        }
        
        return b.build();
    }
        
    private Relation projectRelation(SelectStatementContext ctx,
                                     Relation relation) {
        ColumnsVisitor visitor = new ColumnsVisitor(
                relation.getTupleSchema(),
                Optional.absent());
        ctx.selectStatementColumns().accept(visitor);
        
        return ImmutableProjectedRelation.builder()
                .targetExpressions(visitor.getExpressions())
                .sourceRelation(relation)
                .tupleSchema(visitor.getTargetTupleSchema())
                .build();
    }
    
    private Relation distinctIfNecessary(SelectStatementContext ctx,
                                         Relation relation) {
        if (ctx.DISTINCT() != null) {
            return ImmutableDistinctRelation.builder().sourceRelation(relation).build();
        } else {
            return relation;
        }
    }
    
    private static class ColumnsVisitor extends SQLBaseVisitor<Void> {
        private final List<Expression> expressions = Lists.newArrayList();
        private final List<Aggregator<?, ?>> aggregators = Lists.newArrayList();
        private final List<Optional<String>> columnNames = Lists.newArrayList();
        private final TupleSchema sourceTupleSchema;
        private final Optional<TupleSchema> sourcePartitionTupleSchema;

        private ColumnsVisitor(TupleSchema sourceTupleSchema,
                               Optional<TupleSchema> sourcePartitionTupleSchema) {
            this.sourceTupleSchema = sourceTupleSchema;
            this.sourcePartitionTupleSchema = sourcePartitionTupleSchema;
        }
        
        public List<Expression> getExpressions() {
            return expressions;
        }
        
        public List<Aggregator<?, ?>> getAggregators() {
            return aggregators;
        }

        @Override
        public Void visitSelectStatementColumnStar(SelectStatementColumnStarContext ctx) {
            Optional<String> qualifier = ctx.ID() != null ?
                    Optional.of(ctx.ID().getText().toLowerCase()) :
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
                
                expressions.add(ImmutableVariableExpression.builder()
                        .variableIndex(i)
                        .type(sourceTupleSchema.getColumnSchema(i).getType())
                        .build());
                columnNames.add(cs.getName());
            }
            
            return null;
        }

        @Override
        public Void visitSelectStatementColumnExpression(SelectStatementColumnExpressionContext ctx) {
            ExpressionParser visitor = new ExpressionParser(
                    sourceTupleSchema,
                    sourcePartitionTupleSchema,
                    aggregators);
            Expression expression = ctx.expression().accept(visitor);
            
            expressions.add(expression);
            
            if (ctx.ID() != null) {
                columnNames.add(Optional.of(ctx.ID().getText().toLowerCase()));
            } else if (expression instanceof NamedExpression) {
                columnNames.add(((NamedExpression)expression).getName());
            } else {
                columnNames.add(Optional.absent());
            }
            
            return null;
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
            String tableName = ctx.ID(0).getText().toLowerCase();
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
                        .aliases(ImmutableSet.of(tableName, ctx.ID(1).getText().toLowerCase()))
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
                        .aliases(ImmutableSet.of(ctx.ID().getText().toLowerCase()))
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
