package org.geotools.metadata.iso19115;

import java.util.Locale;

public class Series extends MetaData implements
		org.opengis.metadata.citation.Series {

	private String page;
	private String issueIdentification;
	private String name;

	public String getName(Locale arg0) {
		return name;
	}
	public String getIssueIdentification() {
		return issueIdentification;
	}
	public void setIssueIdentification(String issueIdentification) {
		this.issueIdentification = issueIdentification;
	}
	public void setPage(String page) {
		this.page = page;
	}
	public String getPage() {
		return page;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
}
