package org.geotools.metadata.iso19115;

import java.util.Date;
import java.util.Locale;

import org.opengis.metadata.citation.PresentationForm;
import org.opengis.metadata.citation.ResponsibleParty;
import org.opengis.metadata.citation.Series;

public class Citation extends MetaData implements
		org.opengis.metadata.citation.Citation {

	private String[] alternateTitles;
	private String title;
	private Date[] dates;
	private String edition;
	private Date editionDate;
	private String[] identifiers;
	private String[] identifierTypes;
	private ResponsibleParty[] citedResponsibleParties;
	private PresentationForm[] presentationForm;
	private Series series;
	private String otherCitationDetails;
	private String collectiveTitle;
	private String ISBN;
	private String ISSN;

	/* (non-Javadoc)
	 * @see org.opengis.metadata.citation.Citation#getTitle(java.util.Locale)
	 */
	public String getTitle(Locale arg0) {
		return title;
	}

	/* (non-Javadoc)
	 * @see org.opengis.metadata.citation.Citation#getAlternateTitles(java.util.Locale)
	 */
	public String[] getAlternateTitles(Locale arg0) {
		return alternateTitles;
	}
	public String[] getAlternateTitles() {
		return alternateTitles;
	}

	/* (non-Javadoc)
	 * @see org.opengis.metadata.citation.Citation#getDates()
	 */
	public Date[] getDates() {
		return dates;
	}

	/* (non-Javadoc)
	 * @see org.opengis.metadata.citation.Citation#getEdition(java.util.Locale)
	 */
	public String getEdition(Locale arg0) {
		return edition;
	}
	public String getEdition() {
		return edition;
	}
	/* (non-Javadoc)
	 * @see org.opengis.metadata.citation.Citation#getEditionDate()
	 */
	public Date getEditionDate() {
		return editionDate;
	}

	/* (non-Javadoc)
	 * @see org.opengis.metadata.citation.Citation#getIdentifiers()
	 */
	public String[] getIdentifiers() {
		return identifiers;
	}

	/* (non-Javadoc)
	 * @see org.opengis.metadata.citation.Citation#getIdentifierTypes()
	 */
	public String[] getIdentifierTypes() {
		return identifierTypes;
	}

	/* (non-Javadoc)
	 * @see org.opengis.metadata.citation.Citation#getCitedResponsibleParties()
	 */
	public ResponsibleParty[] getCitedResponsibleParties() {
		return citedResponsibleParties;
	}

	/* (non-Javadoc)
	 * @see org.opengis.metadata.citation.Citation#getPresentationForm()
	 */
	public PresentationForm[] getPresentationForm() {
		return presentationForm;
	}

	/* (non-Javadoc)
	 * @see org.opengis.metadata.citation.Citation#getSeries(java.util.Locale)
	 */
	public Series getSeries(Locale arg0) {
		return series;
	}
	public Series getSeries() {
		return series;
	}
	/* (non-Javadoc)
	 * @see org.opengis.metadata.citation.Citation#getOtherCitationDetails(java.util.Locale)
	 */
	public String getOtherCitationDetails(Locale arg0) {
		return otherCitationDetails;
	}

	/* (non-Javadoc)
	 * @see org.opengis.metadata.citation.Citation#getCollectiveTitle(java.util.Locale)
	 */
	public String getCollectiveTitle(Locale arg0) {
		return collectiveTitle;
	}

	/* (non-Javadoc)
	 * @see org.opengis.metadata.citation.Citation#getISBN()
	 */
	public String getISBN() {
		return ISBN;
	}

	/* (non-Javadoc)
	 * @see org.opengis.metadata.citation.Citation#getISSN()
	 */
	public String getISSN() {
		return ISSN;
	}
	

	public String getCollectiveTitle() {
		return collectiveTitle;
	}
	public void setCollectiveTitle(String collectiveTitle) {
		this.collectiveTitle = collectiveTitle;
	}
	public String getOtherCitationDetails() {
		return otherCitationDetails;
	}
	public void setOtherCitationDetails(String otherCitationDetails) {
		this.otherCitationDetails = otherCitationDetails;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public void setAlternateTitles(String[] alternateTitles) {
		this.alternateTitles = alternateTitles;
	}
	public void setCitedResponsibleParties(
			ResponsibleParty[] citedResponsibleParties) {
		this.citedResponsibleParties = citedResponsibleParties;
	}
	public void setDates(Date[] dates) {
		this.dates = dates;
	}
	public void setEdition(String edition) {
		this.edition = edition;
	}
	public void setEditionDate(Date editionDate) {
		this.editionDate = editionDate;
	}
	public void setIdentifiers(String[] identifiers) {
		this.identifiers = identifiers;
	}
	public void setISBN(String isbn) {
		ISBN = isbn;
	}
	public void setISSN(String issn) {
		ISSN = issn;
	}
	public void setPresentationForm(PresentationForm[] presentationForm) {
		this.presentationForm = presentationForm;
	}
	public void setSeries(Series series) {
		this.series = series;
	}
}
