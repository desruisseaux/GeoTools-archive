
package org.geotools.xml.wfs;

import org.geotools.xml.wfs.WFSSchema.WFSComplexType;

/**
 * <p> 
 * DOCUMENT ME!
 * </p>
 * @author Norman Barker www.comsine.com
 * @author dzwiers
 *
 */
public class WFSBasicComplexTypes {

    /**
     * <p>
     * This class represents an GetFeatureTypeType within the WFS Schema.  This
     * includes both the data and parsing functionality associated with a
     * GetFeatureTypeType.
     * </p>
     *
     * @see WFSComplexType
     */
    static class GetFeatureType extends WFSComplexType {
        // singleton instance
        private static final WFSComplexType instance = new GetFeatureType();
        public static WFSComplexType getInstance(){return instance;}
        
    }

    /**
     * <p>
     * This class represents an DescribeFeatureType within the WFS Schema.
     * This includes both the data and parsing functionality associated with a
     * DescribeFeatureType.
     * </p>
     *
     * @see WFSComplexType
     */
    static class DescribeFeatureTypeType extends WFSComplexType {
        // singleton instance
        private static final WFSComplexType instance = new DescribeFeatureTypeType();
        public static WFSComplexType getInstance(){return instance;}
    }

    /**
     * <p>
     * This class represents an GetCapabilitiesType within the WFS Schema.
     * This includes both the data and parsing functionality associated with a
     * GetCapabilitiesType.
     * </p>
     *
     * @see WFSComplexType
     */
    static class GetCapabilitiesType extends WFSComplexType {
        // singleton instance
        private static final WFSComplexType instance = new GetCapabilitiesType();
        public static WFSComplexType getInstance(){return instance;}

    }

    static class QueryType extends WFSComplexType{
        // singleton instance
        private static final WFSComplexType instance = new QueryType();
        public static WFSComplexType getInstance(){return instance;}
        
    }

    static class FeatureCollectionType  extends WFSComplexType{
        // singleton instance
        private static final WFSComplexType instance = new FeatureCollectionType();
        public static WFSComplexType getInstance(){return instance;}
        
    }
}
