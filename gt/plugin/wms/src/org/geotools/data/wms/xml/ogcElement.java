package org.geotools.data.wms.xml;

import org.geotools.xml.schema.Element;
import org.geotools.xml.schema.Type;
import org.geotools.xml.schema.impl.ElementGT;
public class ogcElement extends ElementGT {
    public ogcElement( String name, Type type, Element substitution ) {
        super(null, name, ogcSchema.NAMESPACE, type, 1, 1, false, substitution, false);
    }
    public ogcElement( String name, Type type, Element substitution, int min, int max ) {
        super(null, name, ogcSchema.NAMESPACE, type, min, max, false, substitution, false);
    }
}
