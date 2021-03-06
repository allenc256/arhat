package org.testdb.parse.expression;

import org.testdb.parse.SQLBaseVisitor;
import org.testdb.parse.SQLParser.LiteralFalseContext;
import org.testdb.parse.SQLParser.LiteralIntegerContext;
import org.testdb.parse.SQLParser.LiteralNullContext;
import org.testdb.parse.SQLParser.LiteralStringContext;
import org.testdb.parse.SQLParser.LiteralTrueContext;
import org.testdb.parse.SqlParseException;
import org.testdb.parse.SqlStrings;
import org.testdb.type.SqlType;

public class LiteralParser extends SQLBaseVisitor<Void> {
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
        if (!(s.startsWith("'") && s.endsWith("'"))) {
            throw SqlParseException.create(
                    ctx.getStart(),
                    "cannot parse string literal.");
        }
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
