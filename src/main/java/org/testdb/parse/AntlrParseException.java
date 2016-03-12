package org.testdb.parse;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.Token;

/**
 * Based off code from "The Definitive ANTLR 4 Reference".
 */
public class AntlrParseException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    
    private final String shortMessage;
    private final int line;
    private final int charPositionInLine;

    private AntlrParseException(String message,
                                String shortMessage,
                                int line,
                                int charPositionInLine) {
        super(message);
        
        this.shortMessage = shortMessage;
        this.line = line;
        this.charPositionInLine = charPositionInLine;
    }

    public String getShortMessage() {
        return shortMessage;
    }

    public int getLine() {
        return line;
    }

    public int getCharPositionInLine() {
        return charPositionInLine;
    }
    
    public static AntlrParseException create(Recognizer<?, ?> recognizer,
                              Object offendingSymbol,
                              int line,
                              int charPositionInLine,
                              String msg) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        
        pw.println("line " + line + ":" + charPositionInLine + " " + msg);
        underlineError(
                pw,
                recognizer,
                (Token) offendingSymbol,
                line,
                charPositionInLine);
        
        return new AntlrParseException(
                sw.toString(),
                msg,
                line,
                charPositionInLine);
    }

    private static void underlineError(PrintWriter pw,
                                       Recognizer<?, ?> recognizer,
                                       Token offendingToken,
                                       int line,
                                       int charPositionInLine) {
        CommonTokenStream tokens = (CommonTokenStream) recognizer.getInputStream();
        String input = tokens.getTokenSource().getInputStream().toString();
        String[] lines = input.split("\n");
        String errorLine = lines[line - 1];
        
        pw.println(errorLine);
        
        for (int i = 0; i < charPositionInLine; i++) {
            pw.print(" ");
        }
        int start = offendingToken.getStartIndex();
        int stop = offendingToken.getStopIndex();
        if (start >= 0 && stop >= 0) {
            for (int i = start; i <= stop; i++) {
                pw.print("^");
            }
        }
        
        pw.println();
        pw.flush();
    }
}
