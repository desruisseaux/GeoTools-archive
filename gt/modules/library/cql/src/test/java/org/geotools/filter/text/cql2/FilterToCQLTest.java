package org.geotools.filter.text.cql2;

import java.io.IOException;

import org.opengis.filter.Filter;

import junit.framework.Assert;
import junit.framework.AssertionFailedError;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestResult;
import junit.framework.TestSuite;

public class FilterToCQLTest extends TestCase {

    FilterToCQL toCQL;
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        toCQL = new FilterToCQL();
    }
    
    public void testSample() throws Exception {
        Filter filter = CQL.toFilter(FilterSample.LESS_FILTER_SAMPLE);
        
        String output = filter.accept( toCQL, null ).toString();
        assertNotNull( output );
        assertEquals( FilterSample.LESS_FILTER_SAMPLE, output );
    }
    /* NOT (ATTR1 BETWEEN 10 AND 20) */
    public void testNotBetween() throws Exception {
        cqlTest( "NOT (ATTR1 BETWEEN 10 AND 20)" );
    }
    /* ((ATTR1 < 10 AND ATTR2 < 2) OR ATTR3 > 10) */
    public void testANDOR() throws Exception {
        cqlTest( "((ATTR1 < 10 AND ATTR2 < 2) OR ATTR3 > 10)" );
    }
    /** (ATTR1 > 10 OR ATTR2 < 2) */
    public void testOR() throws Exception {
        cqlTest( "(ATTR1 > 10 OR ATTR2 < 2)" );
    }
    protected void cqlTest( String cql ) throws Exception {
        Filter filter = CQL.toFilter(cql);
        assertNotNull( cql + " parse", filter );
        
        String output = filter.accept( toCQL, null ).toString();
        assertNotNull( cql + " encode", output );
        assertEquals( cql, cql, output );        
    }
    /*
    public static Test suite(){
        TestSuite suite = new TestSuite();
        suite.addTestSuite(FilterToCQLTest.class );
        for( String cql : FilterSample.SAMPLES.keySet() ){
            suite.addTest( new CQLTest( cql ));
        }            
        return suite;
    }
    */
    static class CQLTest extends Assert implements Test {
        String cql;
        CQLTest( String cql ){
            this.cql = cql;
        }
        public int countTestCases() {
            return 1;
        }

        public void run(TestResult result) {
            result.startTest( this );
            try {
                Filter filter = CQL.toFilter( cql );    
                
                FilterToCQL toCQL = new FilterToCQL();
                String output = filter.accept( toCQL, null ).toString();
                assertNotNull( output );
                assertEquals( cql, output );
            }
            catch (AssertionError fail){
                result.addFailure(
                        this, new AssertionFailedError( fail.getMessage() ));
            }
            catch (Throwable t ){
                result.addError( this, t );
            }            
            finally {
                result.endTest(this);
            }
        }
        @Override
        public String toString() {
            return cql;
        }
        
    }
}
