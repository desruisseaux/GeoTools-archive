package org.geotools.feature.iso.xpath;

import java.util.Collection;

import org.apache.commons.jxpath.ri.Compiler;
import org.apache.commons.jxpath.ri.QName;
import org.apache.commons.jxpath.ri.compiler.NodeNameTest;
import org.apache.commons.jxpath.ri.compiler.NodeTest;
import org.apache.commons.jxpath.ri.compiler.NodeTypeTest;
import org.apache.commons.jxpath.ri.model.NodeIterator;
import org.apache.commons.jxpath.ri.model.NodePointer;
import org.opengis.feature.Attribute;
import org.opengis.feature.ComplexAttribute;

/**
 * Special node pointer for {@link org.geotools.feature.Feature}.
 * 
 * @author Justin Deoliveira, The Open Planning Project
 * @author Gabriel Roldan, Axios Engineering
 * 
 */
public class AttributeNodePointer extends NodePointer {

    /**
     * 
     */
    private static final long serialVersionUID = -5637103253645991273L;

    /**
     * The name of hte node.
     */
    QName name;

    /**
     * The underlying feature
     */
    Attribute feature;

    protected AttributeNodePointer(NodePointer parent, Attribute feature, QName name) {
        super(parent);
        this.name = name;
        this.feature = feature;
    }

    public boolean isLeaf() {
        return !(feature instanceof ComplexAttribute);
    }

    public boolean isCollection() {
        return !isLeaf();
    }

    public int getLength() {
        return isLeaf() ? 0 : ((Collection) feature.get()).size();
    }

    public QName getName() {
        return name;
    }

    public Object getBaseValue() {
        return null;
    }

    public Object getImmediateNode() {
        return feature;
    }

    public void setValue(Object value) {
        feature = (Attribute) value;
    }

    public int compareChildNodePointers(NodePointer pointer1, NodePointer pointer2) {

        return 0;
    }

    public NodeIterator childIterator(NodeTest test, boolean reverse, NodePointer startWith) {
        if (test instanceof NodeNameTest) {
            NodeNameTest nodeNameTest = (NodeNameTest) test;

            if (!nodeNameTest.isWildcard()) {
                return new AttributeNodeIterator(this, nodeNameTest);
            } else {
                return new AttributeNodeIterator(this);
            }
        }

        if (test instanceof NodeTypeTest) {
            NodeTypeTest nodeTypeTest = (NodeTypeTest) test;
            if (nodeTypeTest.getNodeType() == Compiler.NODE_TYPE_NODE) {
                return new AttributeNodeIterator(this);
            }
        }

        return super.childIterator(test, reverse, startWith);
    }

    public NodeIterator attributeIterator(QName qname) {
        /*TODO
        if (qname.getName().equals("id") || qname.getName().equals("fid")) {
            return new SingleFeaturePropertyIterator(this, -1);
        }
        */

        return super.attributeIterator(qname);
    }

}
