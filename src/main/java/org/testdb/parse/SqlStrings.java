package org.testdb.parse;

public class SqlStrings {
    private SqlStrings() {
        // empty
    }
    
    public static String escape(String str) {
        return str.replaceAll("'", "''");
    }
    
    public static String unescape(String str) {
        return str.replaceAll("''", "'");
    }
}
