package org.geotools.data.wfs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Enumeration for the supported WFS versions
 * 
 * @author Gabriel Roldan
 * @version $Id$
 * @since 2.5.x
 * @URL $URL$
 */
public enum Version {
    v1_0_0("1.0.0"), v1_1_0("1.1.0");

    private String version;

    private Version(final String version) {
        this.version = version;
    }

    public String toString() {
        return this.version;
    }

    public static Version highest() {
        List<Version> versions = new ArrayList<Version>(Arrays.asList(values()));
        Collections.sort(versions);
        return versions.get(versions.size() - 1);
    }
}
