/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) Copyright IBM Corporation, 2005. All rights reserved.
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
 *
 */
package org.geotools.data.db2;

import org.geotools.data.DefaultTransaction;
import org.geotools.data.FeatureSource;
import org.geotools.data.FeatureWriter;
import org.geotools.data.Transaction;
import org.geotools.feature.Feature;
import java.io.IOException;


/**
 * Exercise DB2FeatureWriter.
 *
 * @author David Adler - IBM Corporation
 */
public class DB2FeatureWriterTest extends DB2TestCase {
    private DB2DataStore dataStore = null;

    /**
     * Get a DB2DataStore that we will use for all the tests.
     *
     * @throws Exception
     */
    public void setUp() throws Exception {
        super.setUp();
        dataStore = getDataStore();
    }

    public void testRemove() throws IOException {
        try {
            DB2FeatureStore fs = (DB2FeatureStore) dataStore.getFeatureSource(
                    "Roads");
            Transaction trans = null;
            trans = new DefaultTransaction("trans1");

            //			fs.setTransaction(trans);
            FeatureWriter fw = this.dataStore.getFeatureWriter("Roads", trans);

            while (fw.hasNext()) {
                Feature f = fw.next();
                fw.remove();
            }

            trans.commit();
            trans.close();
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public void xtestAppend() throws IOException {
        try {
            FeatureWriter fw = this.dataStore.getFeatureWriterAppend("Roads",
                    Transaction.AUTO_COMMIT);
            boolean hasNext = fw.hasNext();
            fw.write();

            Feature f = fw.next();
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}
