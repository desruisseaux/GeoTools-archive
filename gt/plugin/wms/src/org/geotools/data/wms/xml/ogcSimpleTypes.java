package org.geotools.data.wms.xml;

import org.geotools.xml.schema.Facet;
import org.geotools.xml.schema.SimpleType;
import org.geotools.xml.schema.impl.FacetGT;

public class ogcSimpleTypes {

    protected static class CapabilitiesSectionType extends ogcSimpleType {
        private static SimpleType instance = new CapabilitiesSectionType();
        public static SimpleType getInstance() {
            return instance;
        }
        private static SimpleType[] parents = new SimpleType[]{
                org.geotools.xml.xsi.XSISimpleTypes.String.getInstance()/* simpleType name is string */
        };
        private static Facet[] facets = new Facet[]{
            new FacetGT(Facet.ENUMERATION, "/"),
            new FacetGT(Facet.ENUMERATION, "/OGC_CAPABILITIES/ServiceMetadata"),
            new FacetGT(Facet.ENUMERATION, "/OGC_CAPABILITIES/OperationSignatures"),
            new FacetGT(Facet.ENUMERATION, "/OGC_CAPABILITIES/ContentMetadata"),
            new FacetGT(Facet.ENUMERATION, "/OGC_CAPABILITIES/Common")
        };

        private CapabilitiesSectionType() {
            super("ogc:CapabilitiesSectionType", 4, parents, facets);
        }
    }

    protected static class FormatType extends ogcSimpleType {
        private static SimpleType instance = new FormatType();
        public static SimpleType getInstance() {
            return instance;
        }
        private static SimpleType[] parents = new SimpleType[]{org.geotools.xml.xsi.XSISimpleTypes.String
                .getInstance()/* simpleType name is string */
        };
        private static Facet[] facets = new Facet[]{
            new FacetGT(Facet.ENUMERATION, "image/gif"),
            new FacetGT(Facet.ENUMERATION, "image/jpg"),
            new FacetGT(Facet.ENUMERATION, "image/png")
        };

        private FormatType() {
            super("FormatType", 4, parents, facets);
        }
    }
    protected static class OWSType extends ogcSimpleType {
        private static SimpleType instance = new OWSType();
        public static SimpleType getInstance() {
            return instance;
        }
        private static SimpleType[] parents = new SimpleType[]{org.geotools.xml.xsi.XSISimpleTypes.String
                .getInstance()/* simpleType name is string */
        };
        private static Facet[] facets = new Facet[]{
            new FacetGT(Facet.ENUMERATION, "WMS")
        };

        private OWSType() {
            super("OWSType", 4, parents, facets);
        }
    }
    protected static class ExceptionsType extends ogcSimpleType {
        private static SimpleType instance = new ExceptionsType();
        public static SimpleType getInstance() {
            return instance;
        }
        private static SimpleType[] parents = new SimpleType[]{org.geotools.xml.xsi.XSISimpleTypes.String
                .getInstance()/* simpleType name is string */
        };
        private static Facet[] facets = new Facet[]{
            new FacetGT(Facet.ENUMERATION, "application/vnd.ogc.se+inimage"),
            new FacetGT(Facet.ENUMERATION, "application/vnd.ogc.se+xml")
        };

        private ExceptionsType() {
            super("ExceptionsType", 4, parents, facets);
        }
    }
}
