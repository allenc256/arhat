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
                    if (!cs.getQualifiedName().isPresent()) {
                        return false;
                    }
                    Optional<String> csQualifier = cs.getQualifiedName().get().getQualifier();
                    String csName = cs.getQualifiedName().get().getName();
                    return (!qualifier.isPresent() || qualifier.equals(csQualifier)) && name.equals(csName);
                })
                .collect(Collectors.toList());
    }
    
    default int size() {
        return getColumnSchemas().size();
    }
}
