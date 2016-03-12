package org.testdb.relation;

public interface JoinPredicate {
    boolean matches(Tuple t1, Tuple t2);
}
