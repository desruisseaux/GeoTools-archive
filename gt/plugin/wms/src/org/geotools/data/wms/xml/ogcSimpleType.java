package org.geotools.data.wms.xml;

import org.geotools.xml.schema.Facet;
import org.geotools.xml.schema.SimpleType;
import org.geotools.xml.schema.impl.SimpleTypeGT;
public class ogcSimpleType extends SimpleTypeGT {
    public ogcSimpleType( String name, int type, SimpleType[] parents, Facet[] facets ) {
        super(null, name, ogcSchema.NAMESPACE, type, parents, facets, 0);
    }
}
