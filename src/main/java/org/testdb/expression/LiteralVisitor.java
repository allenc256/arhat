package org.testdb.expression;

import org.testdb.parse.SQLBaseVisitor;
import org.testdb.parse.SqlStrings;
import org.testdb.parse.SQLParser.LiteralFalseContext;
import org.testdb.parse.SQLParser.LiteralIntegerContext;
import org.testdb.parse.SQLParser.LiteralNullContext;
import org.testdb.parse.SQLParser.LiteralStringContext;
import org.testdb.parse.SQLParser.LiteralTrueContext;

import com.google.common.base.Preconditions;

public class LiteralVisitor extends SQLBaseVisitor<Object> {
    private LiteralVisitor() {
        // empty
    }
    
    private static LiteralVisitor INSTANCE = new LiteralVisitor();
    
    public static LiteralVisitor instance() {
        return INSTANCE;
    }
    
    @Override
    public Object visitLiteralNull(LiteralNullContext ctx) {
        return null;
    }

    @Override
    public Integer visitLiteralInteger(LiteralIntegerContext ctx) {
        return Integer.parseInt(ctx.getText());
    }

    @Override
    public String visitLiteralString(LiteralStringContext ctx) {
        String s = ctx.getText();
        Preconditions.checkState(
                s.startsWith("'") && s.endsWith("'"),
                "Cannot parse string literal token.");
        return SqlStrings.unescape(s.substring(1, s.length() - 1));
    }

    @Override
    public Boolean visitLiteralTrue(LiteralTrueContext ctx) {
        return true;
    }

    @Override
    public Boolean visitLiteralFalse(LiteralFalseContext ctx) {
        return false;
    }
}