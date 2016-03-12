package org.testdb.relation;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import com.google.common.base.Optional;

public interface TupleSchema {
    List<ColumnSchema> getColumnSchemas();
    
    default ColumnSchema getColumnSchema(int column) {
        return getColumnSchemas().get(column);
    }
    
    default Collection<ColumnSchema> getColumnSchemas(QualifiedName qualifiedName) {
        Optional<String> qualifier = qualifiedName.getQualifier();
        String name = qualifiedName.getName();
        
        return getColumnSchemas()
                .stream()
                .filter(cs -> {
                    if (!cs.getName().isPresent()) {
                        return false;
                    }
                    if (qualifier.isPresent() && !cs.getQualifierAliases().contains(qualifier.get())) {
                        return false;
                    }
                    return name.equals(cs.getName().get());
                })
                .collect(Collectors.toList());
    }
    
    default int size() {
        return getColumnSchemas().size();
    }
    
    default boolean matchesType(Tuple tuple) {
        if (getColumnSchemas().size() != tuple.size()) {
            return false;
        }
        
        for (int i = 0; i < tuple.size(); ++i) {
            Object value = tuple.get(i);
            if (value != null && !getColumnSchema(i).getType().getJavaType().equals(value.getClass())) {
                return false;
            }
        }
        
        return true;
    }
}
