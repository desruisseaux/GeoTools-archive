package org.geotools.data.complex.filter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.geotools.factory.CommonFactoryFinder;
import org.geotools.feature.iso.AttributeBuilder;
import org.geotools.feature.iso.AttributeFactoryImpl;
import org.geotools.feature.iso.Types;
import org.geotools.feature.iso.type.TypeFactoryImpl;
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

/**
 * Utility class to evaluate XPath expressions against an Attribute instance,
 * which may be any Attribute, wether it is simple, complex, a feature, etc.
 * <p>
 * At the difference of the Filter subsistem, which works against Attribute
 * contents (for example to evaluate a coparison filter), the XPath subsistem,
 * for which this class is the single entry point, works against Attribute
 * instances. That is, the result of an XPath expression, if a single value, is
 * an Attribtue, not the attribute content, or a List of Attributes, for
 * instance.
 * </p>
 * 
 * @author Gabriel Roldan
 * 
 * TODO: register namespaces in JXPathContext
 */
public class XPath {
    private static final Logger LOGGER = Logger.getLogger(XPath.class.getPackage().getName());

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

    public static class Step {
        private int index;

        private String attributeName;

        public Step(String name, int index) {
            this.attributeName = name;
            this.index = index;
        }

        public int getIndex() {
            return index;
        }

        public String getName() {
            return attributeName;
        }

        public String toString() {
            StringBuffer sb = new StringBuffer(attributeName);
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

        public static String toString(List/* <Step> */stepList) {
            StringBuffer sb = new StringBuffer();
            for (Iterator it = stepList.iterator(); it.hasNext();) {
                Step s = (Step) it.next();
                sb.append(s.getName());
                if (s.getIndex() > 1) {
                    sb.append("[").append(s.getIndex()).append("]");
                }
                if (it.hasNext()) {
                    sb.append("/");
                }
            }
            return sb.toString();
        }
    }

    public static List/* <Step> */steps(String xpathExpression) throws IllegalArgumentException {
        return XPath.steps(null, xpathExpression);
    }

    /**
     * Returns the list of stepts in <code>xpathExpression</code> by cleaning
     * it up removing unnecessary elements.
     * <p>
     * </p>
     * 
     * @param rootName
     *            used to ignore the first step in xpathExpression if the
     *            expression's first step is named as rootName.
     * 
     * @param xpathExpression
     * @return
     * @throws IllegalArgumentException
     *             if <code>xpathExpression</code> has no steps or it isn't a
     *             valid XPath expression against <code>type</code>.
     */
    public static List/* <Step> */steps(Name rootName, String xpathExpression)
            throws IllegalArgumentException {

        if (xpathExpression == null) {
            throw new NullPointerException("xpathExpression");
        }

        xpathExpression = xpathExpression.trim();

        if ("".equals(xpathExpression)) {
            throw new IllegalArgumentException("expression is empty");
        }

        List/* <Step> */steps = new LinkedList/* <Step> */();
        steps.add(new Step(".", 1));

        if ("/".equals(xpathExpression)) {
            return steps;
        }
        if (xpathExpression.startsWith("/")) {
            xpathExpression = xpathExpression.substring(1);
        }

        final String[] partialSteps = xpathExpression.split("/");

        if (partialSteps.length == 0) {
            throw new IllegalArgumentException("no steps provided");
        }

        int startIndex = 0;
        if (rootName != null && rootName.getLocalPart().equals(partialSteps[0])) {
            XPath.LOGGER.finer("ignoring type name, since its redundant");
            startIndex = 1;
        }

        for (int i = startIndex; i < partialSteps.length; i++) {
            String step = partialSteps[i];
            if (step.indexOf('[') != -1) {
                XPath.LOGGER.finer("removing index from step " + step);
                int start = step.indexOf('[');
                int end = step.indexOf(']');
                String stepName = step.substring(0, start);
                int stepIndex = Integer.parseInt(step.substring(start + 1, end));
                steps.add(new Step(stepName, stepIndex));
            } else if ("..".equals(step)) {
                steps.remove(steps.size() - 1);
            } else if (".".equals(step)) {
                continue;
            } else {
                // steps.add(new Step(step, steps.size() + 1));
                steps.add(new Step(step, 1));
            }
        }

        // verify final location path is valid
        /*
         * StringBuffer sb = new StringBuffer(); for (Iterator it =
         * steps.iterator(); it.hasNext();) {
         * sb.append(((Step)it.next()).getName()); if (it.hasNext()) {
         * sb.append("/"); } } String finalPath = sb.toString();
         * AttributeDescriptor addressedType = (AttributeDescriptor) get(node,
         * finalPath); if (addressedType == null) { throw new
         * IllegalArgumentException("final location path '" + finalPath + "'
         * does not addresses an attribute of " + node); }
         * 
         */
        return steps;
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
    public Attribute set(final Attribute att, final String xpath, Object value, String id,
            AttributeType targetNodeType) {
        if (XPath.LOGGER.isLoggable(Level.CONFIG)) {
            XPath.LOGGER.entering("XPath", "set", new Object[] { att, xpath, value, id,
                    targetNodeType });
        }

        ComplexAttribute parent = (ComplexAttribute) att;
        Name rootName = null;
        if (att.getDescriptor() != null) {
            rootName = att.getDescriptor().getName();
        }
        List steps = XPath.steps(rootName, xpath);

        if (steps.size() < 2) {
            throw new IllegalArgumentException("parent not yet built for " + xpath);
        }

        // first step is the self reference to att, so skip it
        Iterator stepsIterator = steps.subList(1, steps.size()).iterator();
        for (; stepsIterator.hasNext();) {
            final XPath.Step currStep = (Step) stepsIterator.next();
            final ComplexType parentType = (ComplexType) parent.getType();
            final String currStepLocalName = currStep.getName();
            AttributeDescriptor currStepDescriptor = null;
            if (targetNodeType == null) {
                currStepDescriptor = (AttributeDescriptor) Types.descriptor(parentType,
                        currStepLocalName);
                if(currStepDescriptor == null){
                    //need to take the non easy way, may be the instance has a value
                    //for this step with a different name, of a derived type of the
                    //one declared in the parent type
                    PropertyName name = FF.property(currStepLocalName);
                    Attribute child = (Attribute) name.evaluate(parent);
                    if(child != null){
                        currStepDescriptor = child.getDescriptor();
                    }
                }
            } else {
                // @todo: no target node namespace check here, we need XPath to
                // be namespace aware
                AttributeDescriptor actualDescriptor = (AttributeDescriptor) Types.descriptor(
                        parentType, currStepLocalName, targetNodeType);
                if (actualDescriptor != null) {
                    String namespace = actualDescriptor.getName().getNamespaceURI();
                    Name name = new org.geotools.feature.Name(namespace, currStepLocalName);
                    int minOccurs = actualDescriptor.getMinOccurs();
                    int maxOccurs = actualDescriptor.getMaxOccurs();
                    boolean nillable = actualDescriptor.isNillable();
                    currStepDescriptor = descriptorFactory.createAttributeDescriptor(
                            targetNodeType, name, minOccurs, maxOccurs, nillable);
                }
            }

            if (currStepDescriptor == null) {
                throw new IllegalArgumentException(currStepLocalName
                        + " is not a valid location path for type " + parentType.getName());
            }

            final boolean isLastStep = !stepsIterator.hasNext();

            if (isLastStep) {
                // reached the leaf
                if (currStepDescriptor == null) {
                    throw new IllegalArgumentException(currStepLocalName
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

    /*
     * private ComplexAttribute appendComplexProperty(final ComplexAttribute
     * parent, final XPath.Step currStep, final AttributeDescriptor
     * currStepDescriptor) {
     * 
     * AttributeBuilder builder = new AttributeBuilder(featureFactory);
     * PropertyName attExp = FF.property(currStep.toString()); Object
     * addressedObj = attExp.evaluate(parent);
     * 
     * ComplexAttribute newAttribute;
     * 
     * if (addressedObj == null) { builder.init(parent); // we need the actual
     * Name or the builder will use the // parent's namespace,which might be
     * wrong if (currStepDescriptor != null) { Name stepName =
     * currStepDescriptor.getName(); builder.add(null, stepName); } else {
     * String currStepLocalName = currStep.getName(); builder.add(null,
     * currStepLocalName); }
     * 
     * ComplexAttribute attribute = (ComplexAttribute) builder.build(); Object
     * newParentValue = attribute.get(); parent.set(newParentValue);
     * 
     * addressedObj = attExp.evaluate(attribute); newAttribute =
     * (ComplexAttribute) addressedObj; if (addressedObj == null) {
     * System.out.println("break here"); } } else { if (addressedObj instanceof
     * Collection) { Collection collection = ((Collection) addressedObj);
     * newAttribute = (ComplexAttribute) collection.iterator().next(); } else {
     * newAttribute = (ComplexAttribute) addressedObj; } } return newAttribute; }
     */

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

    public boolean isComplexType(final String attrXPath, final AttributeDescriptor featureType) {
        PropertyName attExp = FF.property(attrXPath);
        Object type = attExp.evaluate(featureType);
        if (type == null) {
            throw new IllegalArgumentException("path not found: " + attrXPath);
        }

        AttributeDescriptor node = (AttributeDescriptor) type;
        return node.getType() instanceof ComplexType;
    }

}
