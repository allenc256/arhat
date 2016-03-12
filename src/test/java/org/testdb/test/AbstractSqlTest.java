package org.testdb.test;

import java.io.InputStream;
import java.util.Collection;
import java.util.List;

import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.testdb.database.InMemoryDatabase;
import org.testdb.test.harness.ImmutableSqlTestCase;
import org.testdb.test.harness.SqlTestCase;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.google.common.collect.Lists;

@RunWith(Parameterized.class)
public abstract class AbstractSqlTest {
    private static InMemoryDatabase DATABASE;
    
    @ClassRule
    public static final TestRule SETUP_RULE = (base, description) -> {
        DATABASE = new InMemoryDatabase();
        return base;
    };
    
    private final SqlTestCase testCase;
    
    public AbstractSqlTest(SqlTestCase testCase) {
        this.testCase = testCase;
    }
    
    public static Collection<Object[]> parseTestParameters(String resource) throws Exception {
        try (InputStream is = AbstractSqlTest.class.getResourceAsStream(resource)) {
            ObjectMapper mapper = new ObjectMapper(new YAMLFactory())
                    .registerModule(new GuavaModule());
            List<SqlTestCase> tcs = mapper.readValue(
                    is,
                    new TypeReference<List<SqlTestCase>>() {});
            List<Object[]> params = Lists.newArrayList();
            
            for (int i = 0; i < tcs.size(); ++i) {
                SqlTestCase tc = ImmutableSqlTestCase.builder()
                        .from(tcs.get(i))
                        .name(tcs.get(i).getName().or("test" + (i+1)))
                        .build();
                params.add(new Object[] { tc });
            }
            
            return params;
        }
    }
    
    @Test
    public void testRequest() throws Exception {
        testCase.execute(DATABASE);
    }
}
