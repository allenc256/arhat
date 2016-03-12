package org.testdb.relation;

import java.util.Set;

import org.immutables.value.Value;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import com.google.common.collect.SortedMultiset;

/**
 * N.B., this class technically isn't immutable, but we still use the immutables
 * annotation processor to generate it.
 */
@Value.Immutable
@Value.Style(typeImmutable = "*")
public abstract class AbstractSortedMultisetRelation implements IndexedRelation {
    public abstract SortedMultiset<Tuple> getTuplesSortedMultiset();

    @Override
    public Cursor<Tuple> getTuples() {
        return new CursorAdapter<>(getTuplesSortedMultiset().iterator());
    }

    @Override
    public Cursor<Tuple> getTuples(TupleRange range) {
        SortedMultiset<Tuple> set = getTuplesSortedMultiset();
        
        if (range.getLowerBound().isPresent()) {
            set = set.tailMultiset(range.getLowerBound().get(), range.getLowerBoundType());
        }
        
        if (range.getUpperBound().isPresent()) {
            set = set.headMultiset(range.getUpperBound().get(), range.getUpperBoundType());
        }
        
        return new CursorAdapter<>(set.iterator());
    }
    
    @Value.Check
    protected void check() {
        Preconditions.checkState(
                LexicographicTupleOrdering.INSTANCE.equals(getTuplesSortedMultiset().comparator()),
                "Only the lexicographic tuple ordering is currently supported.");
        
        for (ColumnSchema cs : getTupleSchema().getColumnSchemas()) {
            Preconditions.checkState(
                    cs.getQualifiedName().getQualifier().equals(getName()),
                    "Qualifier name must match relation name.");
        }
        
        Set<QualifiedName> allNames = Sets.newHashSet();
        for (ColumnSchema cs : getTupleSchema().getColumnSchemas()) {
            Preconditions.checkState(
                    allNames.add(cs.getQualifiedName()),
                    "Column name '%s' is specified twice.",
                    cs.getQualifiedName());
        }
    }
}
