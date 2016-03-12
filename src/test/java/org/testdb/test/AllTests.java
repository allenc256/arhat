package org.testdb.test;

import java.util.Collection;

import org.junit.runners.Parameterized.Parameters;
import org.testdb.test.harness.SqlTestCase;

public class AllTests extends AbstractSqlTest {
    public AllTests(SqlTestCase testCase) {
        super(testCase);
    }
    
    @Parameters(name = "{0}")
    public static Collection<Object[]> data() throws Exception {
        return parseTestParameters("/AllTests.yml");
    }
}
