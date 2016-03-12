package org.testdb.expression;

import org.testdb.parse.SQLBaseVisitor;
import org.testdb.parse.SQLParser.LiteralFalseContext;
import org.testdb.parse.SQLParser.LiteralIntegerContext;
import org.testdb.parse.SQLParser.LiteralNullContext;
import org.testdb.parse.SQLParser.LiteralStringContext;
import org.testdb.parse.SQLParser.LiteralTrueContext;
import org.testdb.parse.SqlStrings;
import org.testdb.type.SqlType;

import com.google.common.base.Preconditions;

public class LiteralVisitor extends SQLBaseVisitor<Void> {
    private Object value;
    private SqlType type;
    
    public Object getValue() {
        return value;
    }

    public SqlType getType() {
        return type;
    }

    @Override
    public Void visitLiteralNull(LiteralNullContext ctx) {
        value = null;
        type = SqlType.NULL;
        return null;
    }

    @Override
    public Void visitLiteralInteger(LiteralIntegerContext ctx) {
        value = Integer.parseInt(ctx.getText());
        type = SqlType.INTEGER;
        return null;
    }

    @Override
    public Void visitLiteralString(LiteralStringContext ctx) {
        String s = ctx.getText();
        Preconditions.checkState(
                s.startsWith("'") && s.endsWith("'"),
                "Cannot parse string literal token.");
        value = SqlStrings.unescape(s.substring(1, s.length() - 1));
        type = SqlType.STRING;
        return null;
    }

    @Override
    public Void visitLiteralTrue(LiteralTrueContext ctx) {
        value = true;
        type = SqlType.BOOLEAN;
        return null;
    }

    @Override
    public Void visitLiteralFalse(LiteralFalseContext ctx) {
        value = false;
        type = SqlType.BOOLEAN;
        return null;
    }
}
