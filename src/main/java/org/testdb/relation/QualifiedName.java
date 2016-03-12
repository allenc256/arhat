package org.testdb.relation;

import com.google.common.base.Optional;

public interface QualifiedName {
    Optional<String> getQualifier();
    
    String getName();
}
