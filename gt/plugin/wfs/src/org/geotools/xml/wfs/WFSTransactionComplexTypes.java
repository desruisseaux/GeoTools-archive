
package org.geotools.xml.wfs;

import org.geotools.xml.wfs.WFSBasicComplexTypes.FeatureCollectionType;
import org.geotools.xml.wfs.WFSSchema.WFSComplexType;

/**
 * <p> 
 * DOCUMENT ME!
 * </p>
 * @author dzwiers
 *
 */
public class WFSTransactionComplexTypes {

    /**
     * <p>
     * This class represents an TransactionType within the WFS Schema.  This
     * includes both the data and parsing functionality associated with a
     * TransactionType.
     * </p>
     *
     * @see WFSComplexType
     */
    static class TransactionType extends WFSComplexType {
        // singleton instance
        private static final WFSComplexType instance = new TransactionType();
        public static WFSComplexType getInstance(){return instance;}

    }


    static class GetFeatureWithLockType  extends WFSComplexType{
        // singleton instance
        private static final WFSComplexType instance = new FeatureCollectionType();
        public static WFSComplexType getInstance(){return instance;}
        
    }

    static class LockFeatureType  extends WFSComplexType{
        // singleton instance
        private static final WFSComplexType instance = new FeatureCollectionType();
        public static WFSComplexType getInstance(){return instance;}
        
    }

    static class LockType  extends WFSComplexType{
        // singleton instance
        private static final WFSComplexType instance = new LockType();
        public static WFSComplexType getInstance(){return instance;}
        
    }

    static class InsertElementType  extends WFSComplexType{
        // singleton instance
        private static final WFSComplexType instance = new InsertElementType();
        public static WFSComplexType getInstance(){return instance;}
        
    }

    static class UpdateElementType  extends WFSComplexType{
        // singleton instance
        private static final WFSComplexType instance = new UpdateElementType();
        public static WFSComplexType getInstance(){return instance;}
        
    }

    static class DeleteElementType  extends WFSComplexType{
        // singleton instance
        private static final WFSComplexType instance = new DeleteElementType();
        public static WFSComplexType getInstance(){return instance;}
        
    }

    static class NativeType  extends WFSComplexType{
        // singleton instance
        private static final WFSComplexType instance = new NativeType();
        public static WFSComplexType getInstance(){return instance;}
        
    }

    static class PropertyType extends WFSComplexType{
        // singleton instance
        private static final WFSComplexType instance = new PropertyType();
        public static WFSComplexType getInstance(){return instance;}
        
    }

    static class WFS_LockFeatureResponseType  extends WFSComplexType{
        // singleton instance
        private static final WFSComplexType instance = new WFS_LockFeatureResponseType();
        public static WFSComplexType getInstance(){return instance;}
        
    }

    static class FeaturesLockedType  extends WFSComplexType{
        // singleton instance
        private static final WFSComplexType instance = new FeaturesLockedType();
        public static WFSComplexType getInstance(){return instance;}
        
    }

    static class FeaturesNotLockedType  extends WFSComplexType{
        // singleton instance
        private static final WFSComplexType instance = new FeaturesNotLockedType();
        public static WFSComplexType getInstance(){return instance;}
        
    }

    static class WFS_TransactionResponseType  extends WFSComplexType{
        // singleton instance
        private static final WFSComplexType instance = new WFS_TransactionResponseType();
        public static WFSComplexType getInstance(){return instance;}
        
    }

    static class TransactionResultType  extends WFSComplexType{
        // singleton instance
        private static final WFSComplexType instance = new TransactionResultType();
        public static WFSComplexType getInstance(){return instance;}
        
    }

    static class InsertResultType  extends WFSComplexType{
        // singleton instance
        private static final WFSComplexType instance = new InsertResultType();
        public static WFSComplexType getInstance(){return instance;}
        
    }

    static class StatusType  extends WFSComplexType{
        // singleton instance
        private static final WFSComplexType instance = new StatusType();
        public static WFSComplexType getInstance(){return instance;}
        
    }
}
