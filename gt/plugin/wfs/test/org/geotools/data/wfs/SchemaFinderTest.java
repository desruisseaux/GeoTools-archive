
package org.geotools.data.wfs;

import junit.framework.TestCase;

import org.geotools.xml.SchemaFactory;
import org.geotools.xml.gml.GMLSchema;
import org.geotools.xml.ogc.FilterSchema;
import org.geotools.xml.wfs.WFSSchema;

/**
 * <p> 
 * DOCUMENT ME!
 * </p>
 * @author dzwiers
 *
 */
public class SchemaFinderTest extends TestCase {
    public void testFinder(){
        assertNotNull(SchemaFactory.getInstance(GMLSchema.NAMESPACE));
        assertNotNull(SchemaFactory.getInstance(WFSSchema.NAMESPACE));
        assertNotNull(SchemaFactory.getInstance(FilterSchema.NAMESPACE));
    }
}
