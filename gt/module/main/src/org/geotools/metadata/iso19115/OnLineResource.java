package org.geotools.metadata.iso19115;

import java.net.URI;

import org.opengis.metadata.citation.OnLineFunction;
import org.opengis.util.InternationalString;

/**
 * @deprecated Replaced by {@link org.geotools.metadata.citation.OnLineResource}.
 */
public class OnLineResource extends MetaData implements
		org.opengis.metadata.citation.OnLineResource {
	private OnLineFunction function;
	private InternationalString description;
	private String applicationProfile;
	private String protocol;
	private URI linkage;
    public String getApplicationProfile() {
        return applicationProfile;
    }
    public void setApplicationProfile(String applicationProfile) {
        this.applicationProfile = applicationProfile;
    }
    public InternationalString getDescription() {
        return description;
    }
    public void setDescription(InternationalString description) {
        this.description = description;
    }
    public OnLineFunction getFunction() {
        return function;
    }
    public void setFunction(OnLineFunction function) {
        this.function = function;
    }
    public URI getLinkage() {
        return linkage;
    }
    public void setLinkage(URI linkage) {
        this.linkage = linkage;
    }
    public String getProtocol() {
        return protocol;
    }
    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }
}
