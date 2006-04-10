/**
 * 
 */
package org.geotools.data;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Diff{
	public final Map modified2;
	public int nextFID=0;
	public final Map added;
	public Diff( ){
		modified2=Collections.synchronizedMap(new HashMap());
		added=Collections.synchronizedMap(new HashMap());
	}

	public boolean isEmpty() {
		return modified2.isEmpty() && added.isEmpty();
	}

	public void clear() {
		nextFID=0;
		added.clear();
		modified2.clear();
	}

	public Diff(Diff other){ 
		modified2=Collections.synchronizedMap(new HashMap(other.modified2));
		added=Collections.synchronizedMap(new HashMap(other.added));
		nextFID=other.nextFID;
	}

}