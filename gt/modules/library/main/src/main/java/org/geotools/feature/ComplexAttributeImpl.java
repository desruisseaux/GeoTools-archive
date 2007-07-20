package org.geotools.feature;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.geotools.feature.type.Descriptors;
import org.geotools.resources.Utilities;
import org.opengis.feature.Association;
import org.opengis.feature.Attribute;
import org.opengis.feature.ComplexAttribute;
import org.opengis.feature.Property;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.ComplexType;
import org.opengis.feature.type.Name;
import org.opengis.feature.type.PropertyDescriptor;

public class ComplexAttributeImpl implements ComplexAttribute {

    private transient int HASHCODE = -1;

    protected AttributeDescriptor/* <ComplexType> */DESCRIPTOR;

    protected final ComplexType TYPE;

    protected final String ID;

    protected List properties;

    private List/* <Attribute> */attributes; // TODO Collection (use
                                                // properties.getClass().newInstancee()

    private List/* <Association> */associations;

    private List/* <PropertyType> */types = null;

    private List/* <Object> */values = null;

    public ComplexAttributeImpl(Collection properties, ComplexType type, String id) {

        TYPE = type;
        ID = id;

        this.properties = new ArrayList/* <Property> */();
        set(properties);
    }

    public ComplexAttributeImpl(Collection properties, AttributeDescriptor desc,
            String id) {
        this(properties, (ComplexType) desc.getType(), id);

        DESCRIPTOR = desc;
    }

    public AttributeType getType() {
        return TYPE;
    }

    public AttributeDescriptor getDescriptor() {
        return DESCRIPTOR;
    }

    public PropertyDescriptor descriptor() {
        return getDescriptor();
    }

    public String getID() {
        return ID;
    }

    public Name name() {
        return DESCRIPTOR != null ? DESCRIPTOR.getName() : null;
    }

    public boolean nillable() {
        if (getDescriptor() != null) {
            return getDescriptor().isNillable();
        }

        return true;
    }

    public Object get() {
        return Collections.unmodifiableList(properties);
    }

    public Collection associations() {
        if (associations == null) {
            synchronized (this) {
                if (associations == null) {
                    associations = new ArrayList();
                    for (Iterator itr = properties.iterator(); itr.hasNext();) {
                        Property property = (Property) itr.next();
                        if (property instanceof Association) {
                            associations.add(property);
                        }
                    }
                }
            }
        }

        return Collections.unmodifiableList(associations);
    }

    public Collection attributes() {
        if (attributes == null) {
            synchronized (this) {
                if (attributes == null) {
                    attributes = new ArrayList();
                    for (Iterator itr = properties.iterator(); itr.hasNext();) {
                        Property property = (Property) itr.next();
                        if (property instanceof Attribute) {
                            attributes.add(property);
                        }
                    }
                }
            }
        }

        return Collections.unmodifiableList(attributes);
    }

    public List/* <Property> */get(Name name) {
        // JD: this is a farily lenient check, should we be stricter about
        // matching up the namespace
        List/* <Property> */childs = new LinkedList/* <Property> */();
        
        for (Iterator itr = this.properties.iterator(); itr.hasNext();) {
            Property prop = (Property) itr.next();
            PropertyDescriptor node = prop.descriptor();
            Name propName = node.getName();
			if (name.getNamespaceURI() != null) {
                if (propName.equals(name)) {
                    childs.add(prop);
                }
            } else {
                // just do a local part compare
                String localName = propName.getLocalPart();
                if (localName.equals(name.getLocalPart())) {
                    childs.add(prop);
                }
            }

        }
        return childs;
    }

    /**
     * Represents just enough info to convey the idea of this being a "view"
     * into getAttribtues.
     */
    protected synchronized List/* <AttributeType> */types() {
        if (types == null) {
            types = createTypesView((List) get());
        }
        return types;
    }

    /** Factory method so subclasses can optimize */
    protected List/* <AttributeType> */createTypesView(
            final List/* <Attribute> */source) {
        if (source == null)
            return Collections.EMPTY_LIST;

        return new AbstractList/* <AttributeType> */() {
            // @Override
            public Object /* AttributeType */get(int index) {
                return ((Attribute) source.get(index)).getType();
            }

            // @Override
            public int size() {
                return source.size();
            }

            // @Override
            public Object /* AttributeType */remove(int index) {
                Attribute removed = (Attribute) source.remove(index);
                if (removed != null) {
                    return removed.getType();
                }
                return null;
            }

            /**
             * Unsupported.
             * <p>
             * We may be able to do this for nilable types, or types that have a
             * default value.
             * </p>
             * 
             * @param index
             * @param type
             */
            // @Override
            public void add(int index, Object o) {
                throw new UnsupportedOperationException(
                        "Cannot add directly to types");
            }
        };
    }

    public synchronized List/* <Object> */values() {
        if (values == null) {
            values = createValuesView((List) get());
        }
        return values;
    }

    /** Factory method so subclasses can optimize */
    protected List/* <Object> */createValuesView(
            final List/* <Attribute> */source) {
        return new AbstractList/* <Object> */() {
            // @Override
            public Object get(int index) {
                return ((Attribute) source.get(index)).get();
            }

            // @Override
            public Object set(int index, Object value) {
                Object replaced = ((Attribute) source.get(index)).get();
                ((Attribute) source.get(index)).set(value);
                return replaced;
            }

            // @Override
            public int size() {
                return source.size();
            }

            // @Override
            public Object /* AttributeType */remove(int index) {
                Attribute removed = (Attribute) source.remove(index);
                if (removed != null) {
                    return removed.get();
                }
                return null;
            }

            /**
             * Unsupported, we can support this for flat schema.
             * <p>
             * We may be able to do this after walking the schema and figuring
             * out that there is only one binding for the provided object.
             * </p>
             * 
             * @param index
             * @param testType
             */
            // @Override
            public void add(int index, Object value) {
                throw new UnsupportedOperationException(
                        "Cannot add directly to values");
            }
        };
    }

    public void set(Object newValue) {

        if (newValue == null) {
            properties.clear();
        } else {
            properties = new ArrayList((Collection) newValue);
        }

        // reset "views"
        attributes = null;
        associations = null;
        types = null;
        values = null;
    }

    protected Object get(AttributeType type) {
        if (type == null) {
            throw new NullPointerException("type");
        }

        // JD: Is this crazy or is it just me? This method returns an object
        // in one case, and collection in the other?
        ComplexType ctype = TYPE;
        if (Descriptors.multiple(ctype, type)) {
            List/* <Object> */got = new ArrayList/* <Object> */();
            for (Iterator itr = properties.iterator(); itr.hasNext();) {
                Attribute attribute = (Attribute) itr.next();
                if (attribute.getType().equals(type)) {
                    got.add(attribute.get());
                }
            }
            return got;
        } else {
            for (Iterator itr = properties.iterator(); itr.hasNext();) {
                Attribute attribute = (Attribute) itr.next();
                if (attribute.getType().equals(type)) {
                    return attribute.get();
                }
            }
            return null;
        }
    }

    public boolean equals(Object o) {
        if (!(o instanceof ComplexAttributeImpl)) {
            return false;
        }
        ComplexAttributeImpl c = (ComplexAttributeImpl) o;

        if (!Utilities.equals(ID, c.ID))
            return false;

        if (!Utilities.equals(TYPE, c.TYPE))
            return false;

        if (!Utilities.equals(DESCRIPTOR, c.DESCRIPTOR)) {
            return false;
        }

        return this.properties.equals(c.properties);
    }

    public int hashCode() {
        if (HASHCODE == -1) {
            HASHCODE = 23 + (TYPE == null ? 1 : TYPE.hashCode())
                    * (DESCRIPTOR == null ? 1 : DESCRIPTOR.hashCode())
                    * properties.hashCode() * (ID == null ? 1 : ID.hashCode());
        }
        return HASHCODE;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer(getClass().getName());
        List/* <Attribute> */atts = this.properties;
        sb.append("[id=").append(this.ID).append(", name=").append(
                DESCRIPTOR != null ? DESCRIPTOR.getName().toString() : "null")
                .append(", type=").append(getType().getName()).append('\n');
        for (Iterator itr = atts.iterator(); itr.hasNext();) {
            Attribute att = (Attribute) itr.next();
            sb.append(att);
            sb.append('\n');
        }
        sb.append("]");
        return sb.toString();
    }

    public Object operation(Name arg0, List arg1) {
        throw new UnsupportedOperationException("operation not supported yet");
    }

}