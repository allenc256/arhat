package org.testdb.shell;

import java.io.PrintStream;
import java.util.List;

import org.immutables.value.Value;
import org.testdb.relation.ColumnSchema;
import org.testdb.relation.Cursor;
import org.testdb.relation.Relation;
import org.testdb.relation.Tuple;

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
    public PrintStream getPrintWriter() {
        return System.out;
    }
    
    @Value.Default
    public String getNullString() {
        return "(null)";
    }
    
    public void render() {
        List<Tuple> tuples = Lists.newArrayList();
        boolean reachedLimit = true;
        
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
            
        List<Integer> widths = computeColumnWidths(tuples);
        
        // Print header row.
        printValues(widths, Lists.transform(
                getRelation().getTupleSchema().getColumnSchemas(), 
                cs -> cs.getName()));
        printSeparator(widths);
        
        // Print tuples.
        for (int i = 0; i < tuples.size(); ++i) {
            printValues(widths, Lists.transform(tuples.get(i).getValues(), v -> formatValue(v)));
        }

        getPrintWriter().printf(
                "(%s%d row%s)\n",
                reachedLimit ? "first " : "",
                tuples.size(),
                tuples.size() != 1 ? "s" : "");
        getPrintWriter().println();
    }

    private void printValues(List<Integer> widths, List<String> values) {
        for (int i = 0; i < widths.size(); ++i) {
            String name = values.get(i);
            if (i > 0) {
                getPrintWriter().print('|');
            }
            getPrintWriter().print(' ');
            getPrintWriter().print(name);
            printPadding(widths.get(i) - name.length() + 1, ' ');
        }
        getPrintWriter().println();
    }

    private void printSeparator(List<Integer> widths) {
        for (int i = 0; i < widths.size(); ++i) {
            if (i > 0) {
                getPrintWriter().print('+');
            }
            printPadding(widths.get(i) + 2, '-');
        }
        getPrintWriter().println();
    }

    private List<Integer> computeColumnWidths(List<Tuple> tuples) {
        List<Integer> widths = Lists.newArrayList();
        
        for (ColumnSchema cs : getRelation().getTupleSchema().getColumnSchemas()) {
            widths.add(cs.getName().length());
        }
        
        for (Tuple t : tuples) {
            for (int i = 0; i < t.size(); ++i) {
                widths.set(i, Integer.max(widths.get(i), formatValue(t.get(i)).length()));
            }
        }
        
        return widths;
    }
    
    private void printPadding(int count, char ch) {
        for (int i = 0; i < count; ++i) {
            getPrintWriter().print(ch);
        }
    }
    
    private String formatValue(Object value) {
        return value != null ? String.valueOf(value) : getNullString();
    }
}
