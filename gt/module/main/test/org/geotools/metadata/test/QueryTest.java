
package org.geotools.metadata.test;

import java.io.IOException;

import junit.framework.TestCase;

import org.geotools.expr.Expr;
import org.geotools.expr.Exprs;
import org.geotools.metadata.Query;


/**
 * TODO type description
 * 
 * @author jeichar
 *
 */
public class QueryTest extends TestCase {

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
        Expr expr=Exprs.meta("fileData/name");
        Query q= new Query(expr);
        assertTrue(q.accepts(mdata));
        
        expr=Exprs.meta("data");
        q= new Query(expr);
        assertTrue(q.accepts(mdata));

        
    }

}
