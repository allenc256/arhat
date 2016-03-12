package org.testdb.database;

import java.util.Map;

import org.testdb.relation.SortedMultisetRelation;

import com.google.common.collect.Maps;

public class InMemoryDatabase {
    private final Map<String, SortedMultisetRelation> tables = Maps.newHashMap();
    
    public Map<String, SortedMultisetRelation> getTables() {
        return tables;
    }
    
    public void reset() {
        tables.clear();
    }
}
