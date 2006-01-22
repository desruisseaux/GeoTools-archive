package org.geotools.xml.styling;

/**
 * This code generated using Refractions SchemaCodeGenerator For more information, view the attached
 * licensing information. CopyRight 105
 */

import org.geotools.xml.schema.Facet;
import org.geotools.xml.schema.SimpleType;
import org.geotools.xml.schema.impl.FacetGT;
import org.geotools.xml.schema.impl.SimpleTypeGT;

public class sldSimpleTypes {

    protected static class _Service extends SimpleTypeGT {
        private static SimpleType instance = new _Service();
        public static SimpleType getInstance() {
            return instance;
        }
        private static SimpleType[] parents = new SimpleType[]{
                org.geotools.xml.xsi.XSISimpleTypes.String.getInstance()/* simpleType name is string */,
                org.geotools.xml.xsi.XSISimpleTypes.String.getInstance()/* simpleType name is string */
        };
        private static Facet[] facets = new Facet[]{
            new FacetGT(1, "WFS"), 
            new FacetGT(1, "WCS")
            };

        private _Service() {
            super(null, "Service", sldSchema.NAMESPACE, SimpleType.RESTRICTION, parents, facets, 0);
        }
    }
}
