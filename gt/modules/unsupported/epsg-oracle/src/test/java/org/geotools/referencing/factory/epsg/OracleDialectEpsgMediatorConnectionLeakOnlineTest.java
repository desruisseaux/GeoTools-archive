package org.geotools.referencing.factory.epsg;

import net.sourceforge.groboutils.junit.v1.MultiThreadedTestRunner;
import net.sourceforge.groboutils.junit.v1.TestRunnable;

import org.apache.commons.dbcp.BasicDataSource;
import org.geotools.factory.Hints;
import org.geotools.referencing.factory.epsg.OracleDialectEpsgMediatorOnlineStressTest.ClientThread;
import org.geotools.referencing.factory.epsg.oracle.OracleOnlineTestCase;

/**
 * Multi-threaded test to check that no connections are leaked by the EPSG
 * mediator/factory code.
 * 
 * @author Cory Horner (Refractions Research)
 */
public class OracleDialectEpsgMediatorConnectionLeakOnlineTest extends OracleOnlineTestCase {

    final static int RUNNER_COUNT = 3;
    final static int ITERATIONS = 3;
    final static int MAX_TIME = 2 * 60 * 1000;
    final static int MAX_WORKERS = 2;
    final static boolean VERBOSE = false;
    
    OracleDialectEpsgMediator mediator;
    BasicDataSource wrappedDataSource;
    String[] codes;
    Hints hints;

    protected void connect() throws Exception {
        super.connect();
        hints = new Hints(Hints.CACHE_POLICY, "none");     
        hints.put(Hints.AUTHORITY_MAX_ACTIVE, new Integer(MAX_WORKERS));
        if (datasource == null) {
            fail("no datasource available");
        }
        wrappedDataSource = new BasicDataSource(){
        	{
        		this.dataSource = datasource;
        	}        	
        };
        mediator = new OracleDialectEpsgMediator(80, hints, wrappedDataSource);
        codes = OracleDialectEpsgMediatorOnlineStressTest.getCodes();
    }

    public void testLeak() throws Throwable {
        TestRunnable runners[] = new TestRunnable[RUNNER_COUNT];
        for (int i = 0; i < RUNNER_COUNT; i++) {
            ClientThread thread = new OracleDialectEpsgMediatorOnlineStressTest.ClientThread(i, mediator); 
            thread.iterations = ITERATIONS;
            runners[i] = thread;
        }
        MultiThreadedTestRunner mttr = new MultiThreadedTestRunner(runners, null);
        mttr.runTestRunnables(MAX_TIME);
        
        //count exceptions and metrics
        int exceptions = 0;
        for (int i = 0; i < RUNNER_COUNT; i++) {
            ClientThread thread = (ClientThread) runners[i];
            exceptions += thread.exceptions;
        }
        //destroy the mediator, check for open connections or exceptions
        mediator.dispose();
        assertEquals(0, wrappedDataSource.getNumActive());
        assertEquals(0, exceptions);
    }
    
}
