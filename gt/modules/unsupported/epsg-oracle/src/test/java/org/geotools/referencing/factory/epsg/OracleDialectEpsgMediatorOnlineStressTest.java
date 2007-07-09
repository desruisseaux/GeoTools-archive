/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002-2007, GeoTools Project Managment Committee (PMC)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.geotools.referencing.factory.epsg;

import java.util.Random;

import net.sourceforge.groboutils.junit.v1.MultiThreadedTestRunner;
import net.sourceforge.groboutils.junit.v1.TestRunnable;

import org.geotools.factory.Hints;
import org.geotools.geometry.DirectPosition2D;
import org.geotools.referencing.CRS;
import org.geotools.referencing.factory.epsg.oracle.OracleOnlineTestCase;
import org.opengis.geometry.DirectPosition;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;

public class OracleDialectEpsgMediatorOnlineStressTest extends
        OracleOnlineTestCase {

    //tests currently fail with multiple threads
    final static int RUNNER_COUNT = 1;
    final static int ITERATIONS = 1;
    final static int MAX_TIME = 10 * 60 * 1000;
    
    OracleDialectEpsgMediator mediator;
    
    protected void connect() throws Exception {
        super.connect();
        Hints hints = new Hints(Hints.BUFFER_POLICY, "none");     
        if (datasource == null) {
            fail("no datasource available");
        }
        mediator = new OracleDialectEpsgMediator(80, hints, datasource);
    }
    
    public void testRunners() throws Throwable {
        
        TestRunnable runners[] = new TestRunnable[RUNNER_COUNT];
        for (int i = 0; i < RUNNER_COUNT; i++) {
            ClientThread thread = new ClientThread(i, mediator); 
            thread.iterations = ITERATIONS;
            runners[i] = thread;
        }
        MultiThreadedTestRunner mttr = new MultiThreadedTestRunner(runners, null);
        mttr.runTestRunnables(MAX_TIME);
        
//        for (int i = 0; i < runnerCount; i++) {
//            ClientThread thread = (ClientThread) runners[i];
//        }
    }
    
    public class ClientThread extends TestRunnable {

        String values;
        int id = -1; //thread identifier
        
        /** number of iterations to perform */
        public int iterations = 10;

        Random rand = new Random();
        String[] codes = {"3005", "4145", "2729", "2166", "2043", "31528", "2936", "32639", "5775"};

        OracleDialectEpsgMediator mediator; //victim
        
        public ClientThread(int id, OracleDialectEpsgMediator mediator) {
            this.id = id;
            this.mediator = mediator;
        }

        private String getRandomCode() {
            return codes[rand.nextInt(codes.length - 1)];
        }
        
        private CoordinateReferenceSystem acquireCRS(String code) throws FactoryException {
            return mediator.createCoordinateReferenceSystem(code);
        }
        
        public void runTest() throws Throwable {
            for (int i = 0; i < iterations; i++) {
                //select first CRS
                String code1 = "4326"; //getRandomCode();
                CoordinateReferenceSystem crs1 = acquireCRS(code1);

                //select second CRS
                String code2 = null;
                while (code2 == null || code1.equalsIgnoreCase(code2)) {
                    code2 = getRandomCode();
                }
                CoordinateReferenceSystem crs2 = acquireCRS(code2);
                
                //reproject
                MathTransform transform = CRS.findMathTransform(crs1, crs2, true);
                DirectPosition pos = new DirectPosition2D(48.417, 123.35);
                try {
                    transform.transform(pos, null);
                } catch (Exception e) {
                    //chomp
                }
                
                //TODO: record time elapsed
            }
        }

    }

}
