/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2005-2006, GeoTools Project Managment Committee (PMC)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 */
package org.geotools.data.complex.filter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;

import org.geotools.factory.CommonFactoryFinder;
import org.geotools.feature.iso.AttributeBuilder;
import org.geotools.feature.iso.AttributeFactoryImpl;
import org.geotools.feature.iso.Types;
import org.geotools.feature.iso.type.TypeFactoryImpl;
import org.geotools.util.CheckedArrayList;
import org.opengis.feature.Attribute;
import org.opengis.feature.ComplexAttribute;
import org.opengis.feature.FeatureFactory;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.ComplexType;
import org.opengis.feature.type.Name;
import org.opengis.feature.type.TypeFactory;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.expression.Literal;
import org.opengis.filter.expression.PropertyName;
import org.opengis.util.Cloneable;
import org.xml.sax.helpers.NamespaceSupport;

/**
 * Utility class to evaluate XPath expressions against an Attribute instance,
 * which may be any Attribute, wether it is simple, complex, a feature, etc.
 * <p>
 * At the difference of the Filter subsystem, which works against Attribute
 * contents (for example to evaluate a comparison filter), the XPath subsystem,
 * for which this class is the single entry point, works against Attribute
 * instances. That is, the result of an XPath expression, if a single value, is
 * an Attribtue, not the attribute content, or a List of Attributes, for
 * instance.
 * </p>
 * 
 * @author Gabriel Roldan, Axios Engineering
 * @version $Id$
 * @source $URL$
 * @since 2.4
 */
public class XPath {
    private static final Logger LOGGER = org.geotools.util.logging.Logging.getLogger(XPath.class.getPackage().getName());

    private FilterFactory FF;

    private FeatureFactory featureFactory;

    /**
     * Used to create specific attribute descriptors for
     * {@link #set(Attribute, String, Object, String, AttributeType)} when the
     * actual attribute instance is of a derived type of the corresponding one
     * declared in the feature type.
     */
    private TypeFactory descriptorFactory;

    public XPath() {
        this.FF = CommonFactoryFinder.getFilterFactory(null);
        this.featureFactory = new AttributeFactoryImpl();
        this.descriptorFactory = new TypeFactoryImpl();
    }

    public XPath(FilterFactory ff, FeatureFactory featureFactory) {
        setFilterFactory(ff);
        setFeatureFactory(featureFactory);
    }

    public void setFilterFactory(FilterFactory ff) {
        this.FF = ff;
    }

    public void setFeatureFactory(FeatureFactory featureFactory) {
        this.featureFactory = featureFactory;
    }

    public static class StepList extends CheckedArrayList implements List, Cloneable {

        private StepList() {
            super(XPath.Step.class);
        }

        private StepList(StepList steps) {
            super(XPath.Step.class);
            addAll(steps);
        }

        public String toString() {
            StringBuffer sb = new StringBuffer();
            for (Iterator it = iterator(); it.hasNext();) {
                Step s = (Step) it.next();
                sb.append(s.toString());
                if (it.hasNext()) {
                    sb.append("/");
                }
            }
            return sb.toString();
        }

        public Object clone() {
            StepList copy = new StepList();
            Step step;
            for (Iterator it = iterator(); it.hasNext();) {
                step = (Step) it.next();
                copy.add(step.clone());
            }
            return copy;
        }

        /**
         * Compares this StepList with another for equivalence regardless of the
         * indexes of each Step.
         * 
         * @param propertyName
         * @return <code>true</code> if this step list has the same location
         *         paths than <code>propertyName</code> ignoring the indexes
         *         in each step. <code>false</code> otherwise.
         */
        public boolean equalsIgnoreIndex(StepList propertyName) {
            if (propertyName == null) {
                return false;
            }
            if (propertyName == this) {
                return true;
            }
            if (size() != propertyName.size()) {
                return false;
            }
            Iterator mine = iterator();
            Iterator him = propertyName.iterator();
            Step myStep;
            Step hisStep;
            while (mine.hasNext()) {
                myStep = (Step) mine.next();
                hisStep = (Step) him.next();
                if (!myStep.equalsIgnoreIndex(hisStep)) {
                    return false;
                }
            }
            return true;
        }
    }

    /**
     * 
     * @author gabriel
     * 
     */
    public static class Step implements Cloneable {
        private int index;

        private QName attributeName;

        public Step(final QName name, final int index) {
            if (name == null) {
                throw new NullPointerException("name");
            }
            if (index < 1) {
                throw new IllegalArgumentException("index shall be >= 1");
            }
            this.attributeName = name;
            this.index = index;
        }

        /**
         * Compares this Step with another for equivalence ignoring the steps
         * indexes.
         * 
         * @param hisStep
         * @return
         */
        public boolean equalsIgnoreIndex(Step other) {
            if (other == null) {
                return false;
            }
            if (other == this) {
                return true;
            }
            return other.getName().equals(getName());
        }

        public int getIndex() {
            return index;
        }

        public QName getName() {
            return attributeName;
        }

        public String toString() {
            StringBuffer sb = new StringBuffer();
            if (null != attributeName.getPrefix()) {
                sb.append(attributeName.getPrefix()).append(':');
            }
            sb.append(attributeName.getLocalPart());
            if (index > 1) {
                sb.append("[").append(index).append("]");
            }
            return sb.toString();
        }

        public boolean equals(Object o) {
            if (!(o instanceof Step)) {
                return false;
            }
            Step s = (Step) o;
            return attributeName.equals(s.attributeName) && index == s.index;
        }

        public int hashCode() {
            return 17 * attributeName.hashCode() + 37 * index;
        }

        public Object clone() {
            return new Step(this.attributeName, this.index);
        }
    }

    /**
     * Returns the list of stepts in <code>xpathExpression</code> by cleaning
     * it up removing unnecessary elements.
     * <p>
     * </p>
     * 
     * @param root
     *            non null descriptor of the root attribute, generally the
     *            Feature descriptor. Used to ignore the first step in
     *            xpathExpression if the expression's first step is named as
     *            rootName.
     * 
     * @param xpathExpression
     * @return
     * @throws IllegalArgumentException
     *             if <code>xpathExpression</code> has no steps or it isn't a
     *             valid XPath expression against <code>type</code>.
     */
    public static StepList steps(AttributeDescriptor root, String xpathExpression,
            NamespaceSupport namespaces) throws IllegalArgumentException {

        if (root == null) {
            throw new NullPointerException("root");
        }

        if (xpathExpression == null) {
            throw new NullPointerException("xpathExpression");
        }

        xpathExpression = xpathExpression.trim();

        if ("".equals(xpathExpression)) {
            throw new IllegalArgumentException("expression is empty");
        }

        StepList steps = new StepList();

        if ("/".equals(xpathExpression)) {
            xpathExpression = root.getName().getLocalPart();
        }

        if (xpathExpression.startsWith("/")) {
            xpathExpression = xpathExpression.substring(1);
        }

        final String[] partialSteps = xpathExpression.split("/");

        if (partialSteps.length == 0) {
            throw new IllegalArgumentException("no steps provided");
        }

        int startIndex = 0;

        for (int i = startIndex; i < partialSteps.length; i++) {

            String step = partialSteps[i];

            if (step.indexOf('[') != -1) {
                int start = step.indexOf('[');
                int end = step.indexOf(']');
                String stepName = step.substring(0, start);
                int stepIndex = Integer.parseInt(step.substring(start + 1, end));
                QName qName = deglose(stepName, root, namespaces);
                steps.add(new Step(qName, stepIndex));
            } else if ("..".equals(step)) {
                steps.remove(steps.size() - 1);
            } else if (".".equals(step)) {
                continue;
            } else {
                QName qName = deglose(step, root, namespaces);
                steps.add(new Step(qName, 1));
            }
        }

        // XPath simplification phase: if the xpath expression contains more
        // nodes
        // than the root node itself, and the root node is present, remove the
        // root
        // node as it is redundant
        if (root != null && steps.size() > 1) {
            Step step = (Step) steps.get(0);
            Name rootName = root.getName();
            QName stepName = step.getName();
            if (Types.equals(rootName, stepName)) {
                LOGGER.fine("removing root name from xpath " + steps + " as it is redundant");
                steps.remove(0);
            }
        }

        return steps;
    }

    private static QName deglose(final String prefixedName, final AttributeDescriptor root,
            final NamespaceSupport namespaces) {
        if (prefixedName == null) {
            throw new NullPointerException("prefixedName");
        }

        QName name = null;

        final String prefix;
        final String namespaceUri;
        final String localName;
        final Name rootName = root.getName();
        final String defaultNamespace = rootName.getNamespaceURI() == null ? XMLConstants.NULL_NS_URI
                : rootName.getNamespaceURI();

        int prefixIdx = prefixedName.indexOf(':');

        if (prefixIdx == -1) {
            localName = prefixedName;
            namespaceUri = defaultNamespace;
            if (XMLConstants.NULL_NS_URI.equals(defaultNamespace)) {
                prefix = XMLConstants.DEFAULT_NS_PREFIX;
            } else {
                if (!localName.equals(rootName.getLocalPart())) {
                    LOGGER.warning("Using root's namespace " + defaultNamespace
                            + " for step named '" + localName + "', as no prefix was stated");
                }
                prefix = namespaces.getPrefix(defaultNamespace);

                if (prefix == null) {
                    throw new IllegalStateException("Default namespace is not mapped to a prefix: "
                            + defaultNamespace);
                }
            }
        } else {
            prefix = prefixedName.substring(0, prefixIdx);
            localName = prefixedName.substring(prefixIdx + 1);
            namespaceUri = namespaces.getURI(prefix);
        }

        name = new QName(namespaceUri, localName, prefix);

        return name;
    }

    /**
     * Sets the value of the attribute of <code>att</code> addressed by
     * <code>xpath</code> and of type <code>targetNodeType</code> to be
     * <code>value</code> with id <code>id</code>.
     * 
     * @param att
     *            the root attribute for which to set the child attribute value
     * @param xpath
     *            the xpath expression that addresses the <code>att</code>
     *            child whose value is to be set
     * @param value
     *            the value of the attribute addressed by <code>xpath</code>
     * @param id
     *            the identifier of the attribute addressed by
     *            <code>xpath</code>, might be <code>null</code>
     * @param targetNodeType
     *            the expected type of the attribute addressed by
     *            <code>xpath</code>, or <code>null</code> if unknown
     * @return
     */
    public Attribute set(final Attribute att, final StepList xpath, Object value, String id,
            AttributeType targetNodeType) {
        if (XPath.LOGGER.isLoggable(Level.CONFIG)) {
            XPath.LOGGER.entering("XPath", "set", new Object[] { att, xpath, value, id,
                    targetNodeType });
        }

        final StepList steps = new StepList(xpath);

        // if (steps.size() < 2) {
        // throw new IllegalArgumentException("parent not yet built for " +
        // xpath);
        // }

        ComplexAttribute parent = (ComplexAttribute) att;
        Name rootName = null;
        if (parent.getDescriptor() != null) {
            rootName = parent.getDescriptor().getName();
            Step rootStep = (Step) steps.get(0);
            QName stepName = rootStep.getName();
            if (stepName.getLocalPart().equals(rootName.getLocalPart())) {
                if (XMLConstants.NULL_NS_URI.equals(stepName.getNamespaceURI())
                        || stepName.getNamespaceURI().equals(rootName.getNamespaceURI())) {
                    // first step is the self reference to att, so skip it
                    steps.remove(0);
                }
            }
        }

        Iterator stepsIterator = steps.iterator();

        for (; stepsIterator.hasNext();) {
            final XPath.Step currStep = (Step) stepsIterator.next();
            final ComplexType parentType = (ComplexType) parent.getType();
            final QName stepName = currStep.getName();
            final Name attributeName = Types.toName(stepName);

            AttributeDescriptor currStepDescriptor = null;

            if (targetNodeType == null) {
                if (null == attributeName.getNamespaceURI()) {
                    currStepDescriptor = (AttributeDescriptor) Types.descriptor(parentType,
                            attributeName.getLocalPart());
                } else {
                    currStepDescriptor = (AttributeDescriptor) Types.descriptor(parentType,
                            attributeName);
                }

                if (currStepDescriptor == null) {
                    // need to take the non easy way, may be the instance has a
                    // value for this step with a different name, of a derived
                    // type of the one declared in the parent type
                    String prefixedStepName = currStep.toString();
                    PropertyName name = FF.property(prefixedStepName);
                    Attribute child = (Attribute) name.evaluate(parent);
                    if (child != null) {
                        currStepDescriptor = child.getDescriptor();
                    }
                }
            } else {
                AttributeDescriptor actualDescriptor;
                if (null == attributeName.getNamespaceURI()) {
                    actualDescriptor = (AttributeDescriptor) Types.descriptor(parentType,
                            attributeName.getLocalPart(), targetNodeType);
                } else {
                    actualDescriptor = (AttributeDescriptor) Types.descriptor(parentType,
                            attributeName, targetNodeType);
                }

                if (actualDescriptor != null) {
                    int minOccurs = actualDescriptor.getMinOccurs();
                    int maxOccurs = actualDescriptor.getMaxOccurs();
                    boolean nillable = actualDescriptor.isNillable();
                    currStepDescriptor = descriptorFactory.createAttributeDescriptor(
                            targetNodeType, attributeName, minOccurs, maxOccurs, nillable);
                }
            }

            if (currStepDescriptor == null) {
                throw new IllegalArgumentException(currStep
                        + " is not a valid location path for type " + parentType.getName());
            }

            final boolean isLastStep = !stepsIterator.hasNext();

            if (isLastStep) {
                // reached the leaf
                if (currStepDescriptor == null) {
                    throw new IllegalArgumentException(currStep
                            + " is not a valid location path for type " + parentType.getName());
                }
                int index = currStep.getIndex();
                Attribute attribute = setValue(currStepDescriptor, id, value, index, parent,
                        targetNodeType);
                return attribute;
            } else {
                // parent = appendComplexProperty(parent, currStep,
                // currStepDescriptor);
                int index = currStep.getIndex();
                Attribute _parent = setValue(currStepDescriptor, null, null, index, parent, null);
                parent = (ComplexAttribute) _parent;
            }
        }
        throw new IllegalStateException();
    }

    private Attribute setValue(final AttributeDescriptor descriptor, final String id, Object value,
            final int index, final ComplexAttribute parent, final AttributeType targetNodeType) {

        final Name attributeName = descriptor.getName();

        // adapt value to context
        Literal literal = FF.literal(value);
        Class binding = descriptor.getType().getBinding();
        value = literal.evaluate(value, binding);

        Attribute leafAttribute = null;

        Object currStepValue = parent.get(attributeName);

        if (currStepValue instanceof Collection) {
            List values = new ArrayList((Collection) currStepValue);
            if (values.size() >= index) {
                leafAttribute = (Attribute) values.get(index - 1);
            }
        } else if (currStepValue instanceof Attribute) {
            leafAttribute = (Attribute) currStepValue;
        } else if (currStepValue != null) {
            throw new IllegalStateException("Unkown addressed object. Xpath:" + attributeName
                    + ", addressed: " + currStepValue.getClass().getName() + " ["
                    + currStepValue.toString() + "]");
        }

        if (leafAttribute == null) {
            AttributeBuilder builder = new AttributeBuilder(featureFactory);
            builder.init(parent);
            if (targetNodeType != null) {
                leafAttribute = builder.add(id, value, attributeName, targetNodeType);
            } else {
                leafAttribute = builder.add(id, value, attributeName);
            }
            Attribute parentCopy = builder.build();
            Object newParentValue = parentCopy.get();
            parent.set(newParentValue);
        }

        if (value != null) {
            leafAttribute.set(value);
        }
        return leafAttribute;
    }

    public boolean isComplexType(final StepList attrXPath, final AttributeDescriptor featureType) {
        PropertyName attExp = FF.property(attrXPath.toString());
        Object type = attExp.evaluate(featureType);
        if (type == null) {
            type = attExp.evaluate(featureType);
            throw new IllegalArgumentException("path not found: " + attrXPath);
        }

        AttributeDescriptor node = (AttributeDescriptor) type;
        return node.getType() instanceof ComplexType;
    }

}
