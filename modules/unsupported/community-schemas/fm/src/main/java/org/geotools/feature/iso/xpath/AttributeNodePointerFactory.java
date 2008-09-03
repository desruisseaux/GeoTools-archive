package org.geotools.feature.iso.xpath;

import java.util.Locale;

import org.apache.commons.jxpath.ri.QName;
import org.apache.commons.jxpath.ri.model.NodePointer;
import org.apache.commons.jxpath.ri.model.NodePointerFactory;
import org.opengis.feature.Attribute;
import org.opengis.feature.type.AttributeType;

/**
 * A node factory which creates special node pointers featurs.
 * <p>
 * The following types are supported:
 * <ul>
 * <li>{@link Attribute}
 * <li>{@link AttributeType}
 * </ul>
 * </p>
 * 
 * @author Justin Deoliveira, The Open Planning Project
 * @author Gabriel Roldan, Axios Engineering
 * 
 */
public class AttributeNodePointerFactory implements NodePointerFactory {

    public int getOrder() {
        return 0;
    }

    public NodePointer createNodePointer(QName name, Object object, Locale locale) {

        if (object instanceof Attribute) {
            return new AttributeNodePointer(null, (Attribute) object, name);
        }

        /*
         * if (object instanceof AttributeType) { return new
         * FeatureTypePointer(null, (AttributeType) object, name); }
         */

        return null;
    }

    public NodePointer createNodePointer(NodePointer parent, QName name, Object object) {

        if (object instanceof Attribute) {
            return new AttributeNodePointer(parent, (Attribute) object, name);
        }

        /*
         * if (object instanceof AttributeType) { return new
         * FeatureTypePointer(null, (AttributeType) object, name); }
         */

        return null;
    }

}
