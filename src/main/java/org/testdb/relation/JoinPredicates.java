package org.testdb.relation;

public class JoinPredicates {
    private JoinPredicates() {
        // empty
    }
    
    private static final JoinPredicate ALWAYS_TRUE = new JoinPredicate() {
        @Override
        public boolean matches(Tuple t1, Tuple t2) {
            return true;
        }
    };
    
    public static JoinPredicate alwaysTrue() {
        return ALWAYS_TRUE;
    }
    
    private static final JoinPredicate ALWAYS_FALSE = new JoinPredicate() {
        @Override
        public boolean matches(Tuple t1, Tuple t2) {
            return false;
        }
    };
    
    public static JoinPredicate alwaysFalse() {
        return ALWAYS_FALSE;
    }
}
