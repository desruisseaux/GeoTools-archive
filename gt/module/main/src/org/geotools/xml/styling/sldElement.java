package org.geotools.xml.styling;

/**
 * This code generated using Refractions SchemaCodeGenerator For more information, view the attached
 * licensing information. CopyRight 105
 */

import org.geotools.xml.schema.Element;
import org.geotools.xml.schema.Type;
import org.geotools.xml.schema.impl.ElementGT;
public class sldElement extends ElementGT {
    public sldElement( String name, Type type, Element substitution ) {
        super(null, name, sldSchema.NAMESPACE, type, 1, 1, false, substitution, false);
    }
    public sldElement( String name, Type type, Element substitution, int min, int max ) {
        super(null, name, sldSchema.NAMESPACE, type, min, max, false, substitution, false);
    }
}
