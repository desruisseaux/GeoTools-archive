/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2004, Geotools Project Managment Committee (PMC)
 * (C) 2004, Institut de Recherche pour le D�veloppement
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *    This package contains documentation from OpenGIS specifications.
 *    OpenGIS consortium's work is fully acknowledged here.
 */
package org.geotools.metadata;

// J2SE direct dependencies
import java.util.Set;
import java.util.List;
import java.util.Collections;

// OpenGIS direct dependencies
import org.opengis.util.InternationalString;
import org.opengis.metadata.citation.ResponsibleParty;
import org.opengis.metadata.Obligation;
import org.opengis.metadata.Datatype;

// Geotools dependencies
import org.geotools.metadata.MetadataEntity;
import org.geotools.resources.Utilities;
import org.geotools.util.CheckedHashSet;
import org.geotools.util.CheckedArrayList;


/**
 * New metadata element, not found in ISO 19115, which is required to describe geographic data.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 * @author Toura�vane
 */
public class ExtendedElementInformation extends MetadataEntity implements org.opengis.metadata.ExtendedElementInformation {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = -935396252908733907L;
    
    /**
     * Name of the extended metadata element.
     */
    private String name;

    /**
     * Short form suitable for use in an implementation method such as XML or SGML.
     */
    private String shortName;

    /**
     * Three digit code assigned to the extended element.
     * Non-null only if the {@linkplain #getDataType data type}
     * is {@linkplain Datatype#CODE_LIST_ELEMENT code list element}.
     */
    private Integer domainCode;

    /**
     * Definition of the extended element.
     */
    private InternationalString definition;

    /**
     * Obligation of the extended element.
     */
    private Obligation obligation;

    /**
     * Condition under which the extended element is mandatory.
     * Non-null value only if the {@linkplain #getObligation obligation}
     * is {@linkplain Obligation#CONDITIONAL conditional}.
     */
    private InternationalString condition;

    /**
     * Code which identifies the kind of value provided in the extended element.
     */
    private Datatype dataType;

    /**
     * Maximum occurrence of the extended element.
     * Returns <code>null</code> if it doesn't apply, for example if the
     * {@linkplain #getDataType data type} is {@linkplain Datatype#ENUMERATION enumeration},
     * {@linkplain Datatype#CODE_LIST code list} or {@linkplain Datatype#CODE_LIST_ELEMENT
     * code list element}.
     */
    private Integer maximumOccurrence;

    /**
     * Valid values that can be assigned to the extended element.
     * Returns <code>null</code> if it doesn't apply, for example if the
     * {@linkplain #getDataType data type} is {@linkplain Datatype#ENUMERATION enumeration},
     * {@linkplain Datatype#CODE_LIST code list} or {@linkplain Datatype#CODE_LIST_ELEMENT
     * code list element}.
     */
    private InternationalString domainValue;

    /**
     * Name of the metadata entity(s) under which this extended metadata element may appear.
     * The name(s) may be standard metadata element(s) or other extended metadata element(s).
     */
    private Set parentEntity;

    /**
     * Specifies how the extended element relates to other existing elements and entities.
     */
    private InternationalString rule;

    /**
     * Reason for creating the extended element.
     */
    private List rationales;

    /**
     * Name of the person or organization creating the extended element.
     */
    private Set sources;
    
    /**
     * Construct an initially empty extended element information.
     */
    public ExtendedElementInformation() {
    }

    /**
     * Create an extended element information initialized to the given values.
     */
    public ExtendedElementInformation(final String name, 
                                      final InternationalString definition, 
                                      final InternationalString condition, 
                                      final Datatype datatype, 
                                      final Set parentEntity, 
                                      final InternationalString rule,
                                      final Set sources)
    {
        setName        (name);
        setDefinition  (definition);
        setCondition   (condition);
        setDataType    (dataType);
        setParentEntity(parentEntity);
        setRule        (rule);
        setSources     (sources);
    }

    /**
     * Name of the extended metadata element.
     */
    public String getName() {
        return name;
    }

    /**
     * Set the name of the extended metadata element.
     */
    public synchronized void setName(final String newValue) {
        checkWritePermission();
        name = newValue;
    }

    /**
     * Short form suitable for use in an implementation method such as XML or SGML.
     * NOTE: other methods may be used.
     * Returns <code>null</code> if the {@linkplain #getDataType data type}
     * is {@linkplain Datatype#CODE_LIST_ELEMENT code list element}.
     */
    public String getShortName()  {
        return shortName;
    }

    /**
     * Set a short form suitable for use in an implementation method such as XML or SGML.
     */
    public synchronized void setShortName(final String newValue)  {
        checkWritePermission();
        shortName = newValue;
    }

    /**
     * Three digit code assigned to the extended element.
     * Returns a non-null value only if the {@linkplain #getDataType data type}
     * is {@linkplain Datatype#CODE_LIST_ELEMENT code list element}.
     */
    public Integer getDomainCode() {
        return domainCode;
    }

    /**
     * Set a three digit code assigned to the extended element.
     */
    public synchronized void setDomainCode(final Integer newValue) {
        checkWritePermission();
        domainCode = newValue;
    }

    /**
     * Definition of the extended element.
     */
    public InternationalString getDefinition()  {
        return definition;
    }
    
    /**
     * Set the definition of the extended element.
     */
    public synchronized void setDefinition(final InternationalString newValue)  {
        checkWritePermission();
        definition = newValue;
    }

    /**
     * Obligation of the extended element.
     */
    public Obligation getObligation()  {
        return obligation;
    }
    
    /**
     * Set the obligation of the extended element.
     */
    public synchronized void setObligation(final Obligation newValue)  {
        checkWritePermission();
        obligation = newValue;
    }

    /**
     * Condition under which the extended element is mandatory.
     * Returns a non-null value only if the {@linkplain #getObligation obligation}
     * is {@linkplain Obligation#CONDITIONAL conditional}.
     */
    public InternationalString getCondition() {
        return condition;
    }

    /**
     * Set the condition under which the extended element is mandatory.
     */
    public synchronized void setCondition(final InternationalString newValue) {
        checkWritePermission();
        condition = newValue;
    }

    /**
     * Code which identifies the kind of value provided in the extended element.
     */
    public Datatype getDataType() {
        return dataType;
    }

    /**
     * Set the code which identifies the kind of value provided in the extended element.
     */
    public synchronized void setDataType(final Datatype newValue) {
        checkWritePermission();
        dataType = newValue;
    }

    /**
     * Maximum occurrence of the extended element.
     * Returns <code>null</code> if it doesn't apply, for example if the
     * {@linkplain #getDataType data type} is {@linkplain Datatype#ENUMERATION enumeration},
     * {@linkplain Datatype#CODE_LIST code list} or {@linkplain Datatype#CODE_LIST_ELEMENT
     * code list element}.
     */
    public Integer getMaximumOccurrence() {
        return maximumOccurrence;
    }

    /**
     * Set the maximum occurrence of the extended element.
     */
    public synchronized void setMaximumOccurrence(final Integer newValue) {
        checkWritePermission();
        maximumOccurrence = newValue;
    }

    /**
     * Valid values that can be assigned to the extended element.
     * Returns <code>null</code> if it doesn't apply, for example if the
     * {@linkplain #getDataType data type} is {@linkplain Datatype#ENUMERATION enumeration},
     * {@linkplain Datatype#CODE_LIST code list} or {@linkplain Datatype#CODE_LIST_ELEMENT
     * code list element}.
     */
    public InternationalString getDomainValue() {
        return domainValue;
    }

    /**
     * Set the valid values that can be assigned to the extended element.
     */
    public synchronized void setDomainValue(final InternationalString newValue) {
        checkWritePermission();
        domainValue = newValue;
    }

    /**
     * Name of the metadata entity(s) under which this extended metadata element may appear.
     * The name(s) may be standard metadata element(s) or other extended metadata element(s).
     */
    public Set getParentEntity() {
        final Set parentEntity = this.parentEntity; // Avoid synchronization
        return (parentEntity!=null) ? parentEntity : Collections.EMPTY_SET;
    }

    /**
     * Set the name of the metadata entity(s) under which this extended metadata element may appear.
     */
    public synchronized void setParentEntity(final Set newValues) {
        checkWritePermission();
        if (parentEntity == null) {
            parentEntity = new CheckedHashSet(String.class);
        } else {
            parentEntity.clear();
        }
        parentEntity.addAll(newValues);
    }

   /**
     * Specifies how the extended element relates to other existing elements and entities.
     */
    public InternationalString getRule() {
        return rule;
    }

    /**
     * Set how the extended element relates to other existing elements and entities.
     */
    public synchronized void setRule(final InternationalString newValue) {
        checkWritePermission();
        rule = newValue;
    }

    /**
     * Reason for creating the extended element.
     */
    public List getRationales() {
        final List rationales = this.rationales; // Avoid synchronization
        return (rationales!=null) ? rationales : Collections.EMPTY_LIST;
    }

    /**
     * Set the reason for creating the extended element.
     */
    public synchronized void setRationales(final List newValues) {
        checkWritePermission();
        if (rationales == null) {
            rationales = new CheckedArrayList(InternationalString.class);
        } else {
            rationales.clear();
        }
        rationales.addAll(newValues);
    }

    /**
     * Name of the person or organization creating the extended element.
     */
    public Set getSources() {
        final Set sources = this.sources; // Avoid synchronization
        return (sources!=null) ? sources : Collections.EMPTY_SET;
    }
        
    /**
     * Set the name of the person or organization creating the extended element.
     */
    public synchronized void setSources(final Set newValues) {
        checkWritePermission();
        if (sources == null) {
            sources = new CheckedHashSet(ResponsibleParty.class);
        } else {
            sources.clear();
        }
        sources.addAll(newValues);
    }

    /**
     * Declare this metadata and all its attributes as unmodifiable.
     */
    protected void freeze() {
        super.freeze();
        name              = (String)              unmodifiable(name);
        shortName         = (String)              unmodifiable(shortName);
        domainCode        = (Integer)             unmodifiable(domainCode);
        definition        = (InternationalString) unmodifiable(definition);
        obligation        = (Obligation)          unmodifiable(obligation);
        condition         = (InternationalString) unmodifiable(condition);
        dataType          = (Datatype)            unmodifiable(dataType);
        maximumOccurrence = (Integer)             unmodifiable(maximumOccurrence);
        domainValue       = (InternationalString) unmodifiable(domainValue);
        parentEntity      = (Set)                 unmodifiable(parentEntity);
        rule              = (InternationalString) unmodifiable(rule);
        rationales        = (List)                unmodifiable(rationales);
        sources           = (Set)                 unmodifiable(sources);
    }

    /**
     * Compare this ExtendedElementInformation with the specified object for equality.
     */
    public synchronized boolean equals(final Object object) {
        if (object == this) {
            return true;
        }
        if (object!=null && object.getClass().equals(getClass())) {
            final ExtendedElementInformation that = (ExtendedElementInformation) object;
            return Utilities.equals(this.name,              that.name             ) &&
                   Utilities.equals(this.shortName,         that.shortName        ) &&
                   Utilities.equals(this.domainCode,        that.domainCode       ) &&
                   Utilities.equals(this.definition,        that.definition       ) &&
                   Utilities.equals(this.obligation,        that.obligation       ) &&
                   Utilities.equals(this.condition,         that.condition        ) &&
                   Utilities.equals(this.dataType,          that.dataType         ) &&
                   Utilities.equals(this.maximumOccurrence, that.maximumOccurrence) &&
                   Utilities.equals(this.domainValue,       that.domainValue      ) &&
                   Utilities.equals(this.parentEntity,      that.parentEntity     ) &&
                   Utilities.equals(this.rule,              that.rule             ) &&
                   Utilities.equals(this.rationales,        that.rationales       ) &&
                   Utilities.equals(this.sources,           that.sources          );
        }
        return false;
    }

    /**
     * Returns a hash code value for this object. For performance reason, this method do
     * not uses all attributes for computing the hash code. Instead, it uses the attributes
     * that are the most likely to be unique.
     */
    public synchronized int hashCode() {
        int code = (int)serialVersionUID;
        if (name      != null) code ^= name     .hashCode();
        if (shortName != null) code ^= shortName.hashCode();
        return code;
    }

    /**
     * Returns a string representation of this object.
     */
    public String toString() {
        return String.valueOf(name);
    }        
}
