package org.opengis.feature.schema;

import java.util.List;

import org.opengis.feature.type.Type;


/**
 * Indicating an entry for a perscribed Type.
 * <p>
 * Please note the associated type may itself be complex, that has no effect
 * on the order required by the schema being described now.
 * </p>
 * @author Jody Garnett
 */
public interface Node {
	Type getType();
}