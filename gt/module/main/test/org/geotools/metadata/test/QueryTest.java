/*
 *    uDig - User Friendly Desktop Internet GIS client
 *    http://udig.refractions.net
 *    (C) 2004, Refractions Research Inc.
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
package org.geotools.metadata.test;

import java.io.IOException;

import org.geotools.data.coverage.grid.TestCaseSupport;
import org.geotools.expr.Expr;
import org.geotools.expr.Exprs;
import org.geotools.metadata.Query;


/**
 * TODO type description
 * 
 * @author jeichar
 *
 */
public class QueryTest extends TestCaseSupport {

    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
    }

    /*
     * Class under test for boolean accepts(Metadata)
     */
    public void testAcceptsMetadata() throws IOException{
        StupidNestedMetadata mdata=new StupidNestedMetadataImpl();
        Expr expr=Exprs.meta("FileData/Name");
        Query q= new Query(expr);
        assertTrue(q.accepts(mdata));
        
        expr=Exprs.meta("Data");
        q= new Query(expr);
        assertTrue(q.accepts(mdata));

        
    }

}
