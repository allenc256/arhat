package org.testdb.type;

public enum SqlType {
    NULL("NULL", Void.class),
    INTEGER("INTEGER", Integer.class),
    STRING("STRING", String.class),
    BOOLEAN("BOOLEAN", Boolean.class);
    
    private final String name;
    private final Class<?> javaType;
    
    private SqlType(String name, Class<?> javaType) {
        this.name = name;
        this.javaType = javaType;
    }
    
    public String getName() {
        return name;
    }
    
    public Class<?> getJavaType() {
        return javaType;
    }
}
