package org.geotools.xml.styling;

/**
 * This code generated using Refractions SchemaCodeGenerator For more information, view the attached
 * licensing information. CopyRight 105
 */

import org.geotools.xml.schema.Attribute;
import org.geotools.xml.schema.Element;
import org.geotools.xml.schema.ElementGrouping;
import org.geotools.xml.schema.Type;
import org.geotools.xml.schema.impl.ComplexTypeGT;
public class sldComplexType extends ComplexTypeGT {
    public sldComplexType( String name, ElementGrouping child, Attribute[] attrs, Element[] elems,
            Type parent, boolean _abstract, boolean mixed ) {
        super(null, name, sldSchema.NAMESPACE, child, attrs, elems, mixed, parent, _abstract,
                false, null);
    }
    public sldComplexType( String name, ElementGrouping child, Attribute[] attrs, Element[] elems ) {
        super(null, name, sldSchema.NAMESPACE, child, attrs, elems, false, null, false, false, null);
    }
}
