/*
 * Created on 18-Jun-2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.geotools.validation;

import java.util.ArrayList;
import java.util.List;

import org.geotools.feature.Feature;


/**
 * @source $URL$
 */
public final class DefaultFeatureResults implements ValidationResults {
	Validation trial;
	public List error = new ArrayList();
	public List warning = new ArrayList();
	public void setValidation(Validation validation) {
		trial = validation;									
	}
	public void error(Feature feature, String message) {
		String where = feature != null ? feature.getID() : "all"; 
		error.add( where + ":"+ message );
		System.err.println( where + ":"+ message );
	}
	public void warning(Feature feature, String message) {
		String where = feature != null ? feature.getID() : "all";
		warning.add( where + ":"+ message );
		System.out.println( where + ":"+ message );
	}
}
