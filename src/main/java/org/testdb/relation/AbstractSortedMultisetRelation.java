package org.testdb.relation;

import java.util.Set;
import java.util.stream.Stream;

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
    @Value.Auxiliary
    abstract SortedMultiset<Tuple> getTuplesSortedMultiset();

    @Override
    public Stream<Tuple> getTupleStream() {
        return getTuplesSortedMultiset().stream();
    }

    @Override
    public Stream<Tuple> getTuples(TupleRange range) {
        SortedMultiset<Tuple> set = getTuplesSortedMultiset();
        
        if (range.getLowerBound().isPresent()) {
            set = set.tailMultiset(range.getLowerBound().get(), range.getLowerBoundType());
        }
        
        if (range.getUpperBound().isPresent()) {
            set = set.headMultiset(range.getUpperBound().get(), range.getUpperBoundType());
        }
        
        return set.stream();
    }
    
    @Value.Check
    protected void check() {
        Preconditions.checkState(
                LexicographicTupleOrdering.INSTANCE.equals(getTuplesSortedMultiset().comparator()),
                "Only the lexicographic tuple ordering is currently supported.");
        
        Set<String> allNames = Sets.newHashSet();
        
        for (ColumnSchema cs : getTupleSchema().getColumnSchemas()) {
            Preconditions.checkState(
                    !cs.getName().isPresent() || allNames.add(cs.getName().get()),
                    "Column name '%s' is specified twice.",
                    cs.getName().get());
        }
    }
}
