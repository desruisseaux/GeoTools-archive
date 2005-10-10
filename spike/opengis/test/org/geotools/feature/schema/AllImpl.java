package org.geotools.feature.schema;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.namespace.QName;

import org.geotools.feature.Descriptors;
import org.opengis.feature.Attribute;
import org.opengis.feature.ComplexAttribute;
import org.opengis.feature.schema.AllDescriptor;
import org.opengis.feature.schema.AttributeDescriptor;
import org.opengis.feature.type.AttributeType;

/**
 * Implementation of AllDescriptor.
 * <p>
 * Imposes the following restrictions on order:
 * <ul>
 * <li>Types with this content descriptor may only have AttributeDescriptors
 * (aka, simple types).
 * <li>Each child AttributeDescriptor multiplicity is limited to 0:0, 0:1, 1:1
 * <li>No attributes with same name and different types are allowed.
 * </ul>
 * </p>
 * 
 * @author Jody Garnett
 * @author Gabriel Roldan
 */
public class AllImpl extends AbstractDescriptor implements AllDescriptor {
	Set<AttributeDescriptor> all;

	public AllImpl(Set<AttributeDescriptor> all) {
		this(all, 1, 1);
	}

	public AllImpl(Set<AttributeDescriptor> all, int max) {
		this(all, 1, max);
	}

	public AllImpl(Set<AttributeDescriptor> all, int min, int max) {
		super(min, max);
		Set<QName> attNames = new HashSet<QName>();
		for (AttributeDescriptor node : all) {
			if (node.getMinOccurs() > 1) {
				throw new IllegalArgumentException("MinOccurs for "
						+ node.getType().getName() + " is set to "
						+ node.getMinOccurs() + ". Upper limit is 1");
			}
			if (node.getMaxOccurs() > 1) {
				throw new IllegalArgumentException("MaxOccurs for "
						+ node.getType().getName() + " is set to "
						+ node.getMaxOccurs() + ". Upper limit is 1");
			}
			if (attNames.contains(node.getType().getName())) {
				throw new IllegalArgumentException(
						"Duplicated attribute names not allowed. "
								+ node.getType().getName()
								+ " found more than one time");
			}
			attNames.add(node.getType().getName());
		}
		this.all = new HashSet<AttributeDescriptor>(all);
	}

	public Set<AttributeDescriptor> all() {
		return all;
	}

	public int hashCode() {
		return super.hashCode() ^ (37 * all.hashCode());
	}

	public boolean equals(Object o) {
		if (!(o instanceof AllImpl))
			return false;
		if (!super.equals(o))
			return false;

		AllImpl d = (AllImpl) o;
		boolean sameContent = this.all.equals(d.all);
		return sameContent;
	}

	public void validate(List<Attribute> content) throws NullPointerException,
			IllegalArgumentException {
		if (content == null) {
			throw new NullPointerException("content");
		}

		List<AttributeType> usedTypes = new ArrayList<AttributeType>();
		List<AttributeType> allowedTypes = Descriptors.types(this);
		int index = 0;
		for (Attribute att : content) {
			// att shall not be null
			checkAttIsNotNull(index, att);
			// and has to be of one of the allowed types
			checkAttIsOfAllowedType(allowedTypes, index, att);
			AttributeType type = att.getType();

			// cannot be more than one instance of its type
			// (shortcut to multiplicity rangecheck)
			if (usedTypes.contains(type)) {
				throw new IllegalArgumentException("Attribute of type "
						+ type.getName() + " encountered more than once.");
			}
			usedTypes.add(type);

			index++;
		}
		// and the multiplicity specified in each AttributeDescriptor respected
		for (AttributeDescriptor node : this.all) {
			int min = node.getMinOccurs();
			int max = node.getMaxOccurs();
			AttributeType expectedType = node.getType();
			if (max == 0 && usedTypes.contains(expectedType)) {
				throw new IllegalArgumentException(
						expectedType.getName()
								+ " was fund, thus it is not allowed since maxOccurs is set to 0");
			}
			if (min == 1 && !usedTypes.contains(expectedType)) {
				throw new IllegalArgumentException(
						expectedType.getName()
								+ " was not fund, thus it have to since minOccurs is set to 1");
			}
		}

	}
}
