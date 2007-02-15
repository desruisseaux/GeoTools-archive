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
import org.opengis.feature.Attribute;
import org.opengis.feature.ComplexAttribute;
import org.opengis.feature.FeatureFactory;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.ComplexType;
import org.opengis.feature.type.Name;
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
    private static final Logger LOGGER = Logger.getLogger(XPath.class
            .getPackage().getName());

    private FilterFactory FF;

    private FeatureFactory featureFactory;

    public XPath() {
        this.FF = CommonFactoryFinder.getFilterFactory(null);
        this.featureFactory = new AttributeFactoryImpl();
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

    public static List/* <Step> */steps(String xpathExpression)
            throws IllegalArgumentException {
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
                int stepIndex = Integer
                        .parseInt(step.substring(start + 1, end));
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
    public Attribute set2(final Attribute att, final String xpath,
            Object value, String id, AttributeType targetNodeType) {
        if (XPath.LOGGER.isLoggable(Level.CONFIG)) {
            XPath.LOGGER.entering("XPath", "set", new Object[] { att, xpath, value,
                    id, targetNodeType });
        }

        PropertyName attExp = FF.property(xpath);

        Object addressedObj = attExp.evaluate(att);

        Attribute targetAttribute = null;

        if (addressedObj == null) {
            AttributeDescriptor descriptor = att.getDescriptor();
            Name name = descriptor == null ? null : descriptor.getName();

            List/* <Step> */stepList = XPath.steps(name, xpath);

            if (stepList.size() < 2) {
                throw new IllegalArgumentException("parent not yet built for "
                        + xpath);
            }
            Step lastStep = (Step) stepList.remove(stepList.size() - 1);
            String parentPath = Step.toString(stepList);

            attExp = FF.property(parentPath);

            Object parents = attExp.evaluate(att);

            Attribute parentAtt = null;
            if (parents instanceof Collection) {
                XPath.LOGGER
                        .fine("warn, we're assuming parent is the first in the list?!");
                // REVISIT: might parents be empty?
                parentAtt = (Attribute) ((Collection) parents).iterator()
                        .next();
            } else if (parents instanceof Attribute) {
                parentAtt = (Attribute) parents;
            }

            // parent does not exists, create it
            if (parentAtt == null) {
                parentAtt = set(att, parentPath, null, null, null);
            }

            assert parentAtt != null;

            if (!(parentAtt.getType() instanceof ComplexType)) {
                throw new IllegalArgumentException(
                        "parent is not complex, cant add children " + xpath);
            }

            ComplexAttribute parent = (ComplexAttribute) parentAtt;
            ComplexType parentType = (ComplexType) parent.getType();

            AttributeDescriptor targetDescriptor = null;

            // REVISIT: safe cast?
            targetDescriptor = (AttributeDescriptor) Types.descriptor(
                    parentType, lastStep.getName());

            if (targetNodeType == null) {

                if (targetDescriptor == null) {
                    throw new IllegalArgumentException("attribute "
                            + lastStep.getName() + " not found in "
                            + parent.getDescriptor().getName());
                }
                targetNodeType = targetDescriptor.getType();
            }

            AttributeBuilder builder = new AttributeBuilder(featureFactory);
            if (targetNodeType instanceof ComplexType) {
                builder.setType(targetNodeType);
                targetAttribute = builder.build(id);
                targetAttribute.set(value);
                List content = new ArrayList((Collection) parent.get());
                content.add(targetAttribute);
                parent.set(content);
            } else {
                builder.init(parent);
                Name childName = targetDescriptor.getName();
                builder.add(value, childName);
                Attribute attribute = builder.build();
                Object newParentValue = attribute.get();
                parent.set(newParentValue);
            }

        } else if (addressedObj instanceof Attribute) {

            targetAttribute = (Attribute) addressedObj;
            if (value != null) {
                targetAttribute.set(value);
            }

        } else if (addressedObj instanceof List) {
            List siblings = (List) addressedObj;
            if (siblings.size() > 0) {
                targetAttribute = (Attribute) siblings.get(0);
                if (value != null) {
                    targetAttribute.set(value);
                }
            }
        } else {
            throw new IllegalStateException("unkown xpath result: "
                    + addressedObj);
        }
        return targetAttribute;
    }

    public Attribute set(final Attribute att, final String xpath, Object value,
            String id, AttributeType targetNodeType) {
        if (XPath.LOGGER.isLoggable(Level.CONFIG)) {
            XPath.LOGGER.entering("XPath", "set", new Object[] { att, xpath, value,
                    id, targetNodeType });
        }

        ComplexAttribute parent = (ComplexAttribute) att;
        List steps = XPath.steps(xpath);

        if (steps.size() < 2) {
            throw new IllegalArgumentException("parent not yet built for "
                    + xpath);
        }

        // first step is the self reference to att, so skip it
        Iterator it = steps.subList(1, steps.size()).iterator();
        AttributeBuilder builder = new AttributeBuilder(featureFactory);
        for (; it.hasNext();) {
            ComplexType parentType = (ComplexType) parent.getType();
            XPath.Step currStep = (Step) it.next();
            String currStepName = currStep.getName();
            PropertyName attExp = FF.property(currStep.toString());
            Object addressedObj = attExp.evaluate(att);

            if (it.hasNext()) {
                if (addressedObj == null) {
                    builder.init(parent);
                    builder.add(null, currStepName);
                    ComplexAttribute attribute = (ComplexAttribute) builder
                            .build();
                    addressedObj = attExp.evaluate(attribute);
                    Object newParentValue = attribute.get();
                    parent.set(newParentValue);
                    parent = (ComplexAttribute) addressedObj;
                    assert parent != null;
                } else {
                    if (addressedObj instanceof Collection) {
                        Collection collection = ((Collection) addressedObj);
                        parent = (ComplexAttribute) collection.iterator()
                                .next();
                    } else {
                        parent = (ComplexAttribute) addressedObj;
                    }
                }
            } else {
                // reached the leaf
                AttributeDescriptor leafDescriptor = (AttributeDescriptor) Types
                        .descriptor(parentType, currStepName);
                if (leafDescriptor == null) {
                    throw new IllegalArgumentException(currStepName
                            + " is not a valid location path for type "
                            + parentType.getName());
                }
                Literal literal = FF.literal(value);
                Class binding = leafDescriptor.getType().getBinding();
                value = literal.evaluate(value, binding);

                builder.init(parent);
                Attribute leafAttribute = null;

                if (addressedObj instanceof Collection) {
                    Collection values = (Collection) addressedObj;
                    if (values.size() > 0) {
                        leafAttribute = (Attribute) values.iterator().next();
                    }
                } else if (addressedObj instanceof Attribute) {
                    leafAttribute = (Attribute) addressedObj;
                } else if (addressedObj != null) {
                    throw new IllegalStateException(
                            "Unkown addressed object. Xpath:" + xpath
                                    + ", addressed: "
                                    + addressedObj.getClass().getName() + " ["
                                    + addressedObj.toString() + "]");
                }
                if (leafAttribute == null) {
                    Name name = leafDescriptor.getName();
                    leafAttribute = builder.add(id, value, name);
                } else {
                    leafAttribute.set(value);
                }
                Attribute parentCopy = builder.build();
                Object newParentValue = parentCopy.get();
                parent.set(newParentValue);
                return leafAttribute;
            }
        }
        throw new IllegalStateException();
    }

    public boolean isComplexType(final String attrXPath,
            final AttributeDescriptor featureType) {
        PropertyName attExp = FF.property(attrXPath);
        Object type = attExp.evaluate(featureType);
        if (type == null) {
            throw new IllegalArgumentException("path not found: " + attrXPath);
        }

        AttributeDescriptor node = (AttributeDescriptor) type;
        return node.getType() instanceof ComplexType;
    }

}
