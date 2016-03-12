package org.testdb.shell;

import java.io.PrintStream;
import java.util.List;

import org.immutables.value.Value;
import org.testdb.relation.ColumnSchema;
import org.testdb.relation.Cursor;
import org.testdb.relation.Relation;
import org.testdb.relation.Tuple;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;

@Value.Immutable
@Value.Style(typeImmutable = "*")
public abstract class AbstractRelationRenderer {
    public abstract Relation getRelation();
    
    @Value.Default
    public int getLimit() {
        return 10;
    }
    
    @Value.Default
    public int getMaximumWidth() {
        return 40;
    }
    
    @Value.Default
    public PrintStream getPrintWriter() {
        return System.out;
    }
    
    @Value.Default
    public String getNullString() {
        return "(null)";
    }

    @Value.Default
    public Justification getHeaderJustification() {
        return Justification.CENTER;
    }

    @Value.Default
    public Justification getNumericJustification() {
        return Justification.RIGHT;
    }

    @Value.Default
    public Justification getDefaultJustification() {
        return Justification.LEFT;
    }
    
    public enum Justification {
        LEFT, CENTER, RIGHT
    }

    public void render() {
        List<Tuple> tuples = Lists.newArrayList();
        boolean reachedLimit = true;

        // Extract the tuples up to the limit.
        try (Cursor<Tuple> c = getRelation().getTuples()) {
            for (int i = 0; i < getLimit(); ++i) {
                if (!c.hasNext()) {
                    reachedLimit = false;
                    break;
                }
                tuples.add(c.next());
            }
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
            
        List<ColumnFormat> formats = computeColumnFormats(tuples);
        
        printHeader(formats);
        printTuples(tuples, formats);
        printFooter(tuples, reachedLimit);
    }

    private void printHeader(List<ColumnFormat> formats) {
        List<ColumnSchema> css = getRelation().getTupleSchema().getColumnSchemas();
        for (int i = 0; i < css.size(); ++i) {
            if (i == 0) {
                getPrintWriter().print(' ');
            } else {
                getPrintWriter().print(" | ");
            }
            getPrintWriter().print(formatValuePadded(
                    css.get(i).getQualifiedName().getName(),
                    getHeaderJustification(),
                    formats.get(i).width));
        }
        getPrintWriter().println();
        printSeparator(formats);
    }
    
    private void printTuples(List<Tuple> tuples, List<ColumnFormat> formats) {
        for (Tuple tuple : tuples) {
            for (int i = 0; i < tuple.size(); ++i) {
                if (i == 0) {
                    getPrintWriter().print(' ');
                } else {
                    getPrintWriter().print(" | ");
                }
                getPrintWriter().print(formatValuePadded(
                        tuple.get(i),
                        formats.get(i).justification,
                        formats.get(i).width));
            }
            getPrintWriter().println();
        }
    }

    private void printFooter(List<Tuple> tuples, boolean reachedLimit) {
        getPrintWriter().printf(
                "(%s%d row%s)\n",
                reachedLimit ? "first " : "",
                tuples.size(),
                tuples.size() != 1 ? "s" : "");
        getPrintWriter().println();
    }

    private void printSeparator(List<ColumnFormat> formats) {
        for (int i = 0; i < formats.size(); ++i) {
            if (i > 0) {
                getPrintWriter().print('+');
            }
            getPrintWriter().print(Strings.repeat("-", formats.get(i).width + 2));
        }
        getPrintWriter().println();
    }

    private List<ColumnFormat> computeColumnFormats(List<Tuple> tuples) {
        List<ColumnFormat> formats = Lists.newArrayList();
        
        for (ColumnSchema cs : getRelation().getTupleSchema().getColumnSchemas()) {
            Justification justification = getDefaultJustification();
            if (Number.class.isAssignableFrom(cs.getType().getJavaType())) {
                justification = getNumericJustification();
            }
            formats.add(new ColumnFormat(cs.getQualifiedName().getName().length(), justification));
        }
        
        for (Tuple t : tuples) {
            for (int i = 0; i < t.size(); ++i) {
                ColumnFormat format = formats.get(i);
                format.width = Integer.max(format.width, formatValue(t.get(i)).length());
            }
        }
        
        return formats;
    }
    
    private String formatValue(Object value) {
        String s = value != null ? String.valueOf(value) : getNullString();
        if (s.length() > getMaximumWidth()) {
            return s.substring(0, getMaximumWidth() - 3) + "...";
        } else {
            return s;
        }
    }
    
    private String formatValuePadded(Object value,
                                     Justification justification,
                                     int width) {
        String s = formatValue(value);
        int padding = Math.max(width - s.length(), 0);
        
        switch (justification) {
        case CENTER:
            int leftPadding = padding / 2;
            int rightPadding = padding - leftPadding;
            return Strings.repeat(" ", leftPadding) + s + Strings.repeat(" ", rightPadding);
            
        case LEFT:
            return s + Strings.repeat(" ", padding);
            
        case RIGHT:
            return Strings.repeat(" ", padding) + s;
            
        default:
            throw new IllegalStateException("Unrecognized justification: " + justification);
        }
    }
    
    private static class ColumnFormat {
        public int width;
        public Justification justification;
        
        private ColumnFormat(int width, Justification justification) {
            this.width = width;
            this.justification = justification;
        }
    }
    
    @Value.Check
    protected void check() {
        // N.B., check max width (must support one character plus "..." ellipses)
        Preconditions.checkState(getMaximumWidth() >= 4, "Maximum width must be at least 4.");
    }
}
