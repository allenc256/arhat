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
                    Optional<String> csQualifier = cs.getQualifiedName().getQualifier();
                    String csName = cs.getQualifiedName().getName();
                    return (!qualifier.isPresent() || qualifier.equals(csQualifier)) && name.equals(csName);
                })
                .collect(Collectors.toList());
    }
    
    default int size() {
        return getColumnSchemas().size();
    }
}
