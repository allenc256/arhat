package org.testdb.parse;

import com.google.common.base.Optional;

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
    
    public static boolean isLowerCase(String str) {
        for (int i=0; i<str.length(); ++i) {
            char ch = str.charAt(i);
            if (ch != Character.toLowerCase(ch)) {
                return false;
            }
        }
        return true;
    }
    
    public static boolean isLowerCase(Optional<String> str) {
        return !str.isPresent() || isLowerCase(str.get());
    }
}
