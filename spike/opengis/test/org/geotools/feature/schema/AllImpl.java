package org.geotools.feature.schema;

import java.util.HashSet;
import java.util.Set;

import javax.xml.namespace.QName;

import org.opengis.feature.schema.AllDescriptor;
import org.opengis.feature.schema.AttributeDescriptor;

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
}
