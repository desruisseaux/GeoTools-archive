package org.geotools.feature.schema;

import java.util.HashSet;
import java.util.Set;

import org.opengis.feature.schema.AllDescriptor;
import org.opengis.feature.schema.AttributeDescriptor;

public class AllImpl extends AbstractDescriptor implements AllDescriptor {
	Set<AttributeDescriptor> all;

	public AllImpl(Set<AttributeDescriptor> all) {
		this.all = new HashSet<AttributeDescriptor>(all);
	}

	public AllImpl(Set<AttributeDescriptor> all, int max) {
		super(max);
		this.all = new HashSet<AttributeDescriptor>(all);
	}

	public AllImpl(Set<AttributeDescriptor> all, int min, int max) {
		super(min, max);
		this.all = new HashSet<AttributeDescriptor>(all);
	}

	public Set<AttributeDescriptor> all() {
		return all;
	}
}
