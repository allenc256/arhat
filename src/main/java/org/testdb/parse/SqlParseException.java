package org.testdb.parse;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.antlr.v4.runtime.Token;

import com.google.common.base.Optional;

/**
 * Based off code from "The Definitive ANTLR 4 Reference".
 */
public class SqlParseException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    
    private final Optional<String> shortMessage;
    private final Optional<Integer> line;
    private final Optional<Integer> charPositionInLine;

    private SqlParseException(String message,
                              String shortMessage,
                              int line,
                              int charPositionInLine) {
        super(message);
        
        this.shortMessage = Optional.of(shortMessage);
        this.line = Optional.of(line);
        this.charPositionInLine = Optional.of(charPositionInLine);
    }
    
    private SqlParseException(String message) {
        super(message);
        
        this.shortMessage = Optional.absent();
        this.line = Optional.absent();
        this.charPositionInLine = Optional.absent();
    }

    public Optional<String> getShortMessage() {
        return shortMessage;
    }

    public Optional<Integer> getLine() {
        return line;
    }

    public Optional<Integer> getCharPositionInLine() {
        return charPositionInLine;
    }
    
    public static SqlParseException create(String msg) {
        return new SqlParseException(String.format("ERROR: %s\n", msg));
    }
    
    public static SqlParseException create(Token offendingSymbol,
                                           String format,
                                           Object... args) {
        return create(offendingSymbol, String.format(format, args));
    }
    
    public static SqlParseException create(Token offendingSymbol, String msg) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        
        pw.printf("ERROR: %s\n", msg);
        formatErrorLine(
                pw,
                extractErrorLine(offendingSymbol),
                offendingSymbol.getLine(),
                offendingSymbol.getCharPositionInLine(),
                extractErrorLength(offendingSymbol));
        
        return new SqlParseException(
                sw.toString(),
                msg,
                offendingSymbol.getLine(),
                offendingSymbol.getCharPositionInLine());
    }
    
    private static String extractErrorLine(Token offendingToken) {
        // HACK HACK HACK - should fix this to handle streamed inputs.
        String input = offendingToken.getTokenSource().getInputStream().toString();
        String[] lines = input.split("\n");
        return lines[offendingToken.getLine() - 1];
    }
    
    private static int extractErrorLength(Token offendingToken) {
        int start = offendingToken.getStartIndex();
        int stop = offendingToken.getStopIndex();
        return start >= 0 && stop >= start ? stop - start + 1 : 0;
    }

    private static void formatErrorLine(PrintWriter pw,
                                        String errorLine,
                                        int lineNumber,
                                        int charPositionInLine,
                                        int length) {
        String prefix = String.format(
                "LINE (%d:%d): ",
                lineNumber,
                charPositionInLine);

        pw.print(prefix);
        pw.println(errorLine);
        for (int i = 0; i < charPositionInLine + prefix.length(); i++) {
            pw.print(" ");
        }
        for (int i = 0; i < length; i++) {
            pw.print("^");
        }
        pw.flush();
    }
    
    
}
