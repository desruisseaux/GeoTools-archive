package org.geotools.metadata.iso19115;

import org.opengis.util.InternationalString;

/**
 * @deprecated Replaced by {@link org.geotools.metadata.citation.Series}.
 */
public class Series extends MetaData implements
		org.opengis.metadata.citation.Series {

	private String page;
	private String issueIdentification;
	private InternationalString name;
    public String getIssueIdentification() {
        return issueIdentification;
    }
    public void setIssueIdentification(String issueIdentification) {
        this.issueIdentification = issueIdentification;
    }
    public InternationalString getName() {
        return name;
    }
    public void setName(InternationalString name) {
        this.name = name;
    }
    public String getPage() {
        return page;
    }
    public void setPage(String page) {
        this.page = page;
    }
}
