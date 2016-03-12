package org.testdb.relation;

import com.google.common.base.Preconditions;
import com.google.common.collect.Ordering;

public class LexicographicTupleOrdering implements TupleOrdering {
    public static final LexicographicTupleOrdering INSTANCE = new LexicographicTupleOrdering();
    
    private static final Ordering<Comparable<?>> ORDERING = Ordering.natural().nullsLast();
    
    private LexicographicTupleOrdering() {
        // empty
    }
    
    @Override
    public int compare(Tuple t1, Tuple t2) {
        Preconditions.checkArgument(
                t1.size() == t2.size(),
                "Cannot compare tuples of different sizes.");
        
        int sizeCmp = Integer.compare(t1.size(), t2.size());
        if (sizeCmp != 0) {
            return sizeCmp;
        }
        
        for (int i = 0; i < t1.size(); ++i) {
            int cmp = ORDERING.compare(
                    (Comparable<?>) t1.get(i),
                    (Comparable<?>) t2.get(i));
            if (cmp != 0) {
                return cmp;
            }
        }
        
        return 0;
    }

    @Override
    public boolean isLexicoGraphic() {
        return true;
    }
}
