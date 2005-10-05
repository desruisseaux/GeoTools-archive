package org.geotools.feature.simple;

import java.util.ArrayList;
import java.util.List;

import org.geotools.feature.schema.OrderedImpl;
import org.opengis.feature.schema.AttributeDescriptor;
import org.opengis.feature.schema.Descriptor;
import org.opengis.feature.simple.SimpleDescriptor;
import org.opengis.feature.type.ComplexType;

/**
 * Content descriptor for simple Features limits content multiplicity to 1:1,
 * attribute types multiplicity to 0:1 or 1:1, and content to a sequence of non
 * complex types.
 * 
 * @since 2.2
 * @author Gabriel Roldan, Axios Engineering
 */
public class SimpleDescriptorImpl extends OrderedImpl implements SimpleDescriptor {

	public SimpleDescriptorImpl(List<AttributeDescriptor> sequence) throws IllegalArgumentException{
		super(new ArrayList<Descriptor>(sequence));
	
		for (AttributeDescriptor node : sequence) {
			if (node.getMinOccurs() > 1 || node.getMaxOccurs() > 1) {
				throw new IllegalArgumentException("Attribute "
						+ node.getType().getName() + " has multiplicity "
						+ node.getMinOccurs() + ":" + node.getMaxOccurs()
						+ " which is not allowed for Simple Features");
			}
		
			if (node.getType() instanceof ComplexType) {
				throw new IllegalArgumentException("Attribute " 
						+ node.getType().getName()
						+ " is complex, which is not allowed for Simple Features");
			}
		}
	}
}
