package org.geotools.data.wms.xml;

import org.geotools.xml.schema.Attribute;
import org.geotools.xml.schema.Element;
import org.geotools.xml.schema.ElementGrouping;
import org.geotools.xml.schema.Type;
import org.geotools.xml.schema.impl.ComplexTypeGT;
public class ogcComplexType extends ComplexTypeGT {
    public ogcComplexType( String name, ElementGrouping child, Attribute[] attrs, Element[] elems,
            Type parent, boolean _abstract, boolean mixed ) {
        super(null, name, OGCSchema.NAMESPACE, child, attrs, elems, mixed, parent, _abstract,
                false, null);
    }
    public ogcComplexType( String name, ElementGrouping child, Attribute[] attrs, Element[] elems ) {
        super(null, name, OGCSchema.NAMESPACE, child, attrs, elems, false, null, false, false, null);
    }
}
