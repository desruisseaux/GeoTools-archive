package org.geotools.metadata.iso19115;

import java.util.Date;
import java.util.Set;
import java.util.Map;
import java.util.HashMap;
import java.util.List;

import org.geotools.util.ListOf;
import org.geotools.util.MapOf;
import org.geotools.util.SetOf;
import org.opengis.util.InternationalString;
import org.opengis.metadata.citation.DateType;
import org.opengis.metadata.citation.PresentationForm;
import org.opengis.metadata.citation.ResponsibleParty;
import org.opengis.metadata.citation.Series;

public class Citation extends MetaData implements
		org.opengis.metadata.citation.Citation {

	private List alternateTitles = new ListOf( InternationalString.class );
	private Set citedResponsibleParties = new SetOf( ResponsibleParty.class );
	
	private InternationalString title;
	private Map dates = new MapOf( DateType.class, Date.class );
	private InternationalString edition;
	private Date editionDate;
	private Set identifiers = new SetOf( String.class );
	private Set identifierTypes = new SetOf( String.class );
	
	private Set presentationForm = new SetOf( PresentationForm.class );
	private Series series;
	private InternationalString otherCitationDetails;
	private InternationalString collectiveTitle;
	private String ISBN;
	private String ISSN;
    public List getAlternateTitles() {
        return alternateTitles;
    }
    public void setAlternateTitles(List alternateTitles) {
        this.alternateTitles = alternateTitles;
    }
    public Set getCitedResponsibleParties() {
        return citedResponsibleParties;
    }
    public void setCitedResponsibleParties(Set citedResponsibleParties) {
        this.citedResponsibleParties = citedResponsibleParties;
    }
    public InternationalString getCollectiveTitle() {
        return collectiveTitle;
    }
    public void setCollectiveTitle(InternationalString collectiveTitle) {
        this.collectiveTitle = collectiveTitle;
    }
    public Map getDates() {
        return dates;
    }
    public void setDates(Map dates) {
        this.dates = dates;
    }
    public InternationalString getEdition() {
        return edition;
    }
    public void setEdition(InternationalString edition) {
        this.edition = edition;
    }
    public Date getEditionDate() {
        return editionDate;
    }
    public void setEditionDate(Date editionDate) {
        this.editionDate = editionDate;
    }
    public Set getIdentifiers() {
        return identifiers;
    }
    public void setIdentifiers(Set identifiers) {
        this.identifiers.clear();
        this.identifiers.addAll( identifiers );
    }
    public Set getIdentifierTypes() {
        return identifierTypes;
    }
    public void setIdentifierTypes(Set identifierTypes) {
        this.identifierTypes.clear();
        this.identifierTypes.addAll(identifierTypes);
    }
    public String getISBN() {
        return ISBN;
    }
    public void setISBN(String isbn) {
        ISBN = isbn;
    }
    public String getISSN() {
        return ISSN;
    }
    public void setISSN(String issn) {
        ISSN = issn;
    }
    public InternationalString getOtherCitationDetails() {
        return otherCitationDetails;
    }
    public void setOtherCitationDetails(InternationalString otherCitationDetails) {
        this.otherCitationDetails = otherCitationDetails;
    }
    public Set getPresentationForm() {
        return presentationForm;
    }
    public void setPresentationForm(Set presentationForm) {
        this.presentationForm.clear();
        this.presentationForm.addAll( presentationForm );
    }
    public Series getSeries() {
        return series;
    }
    public void setSeries(Series series) {
        this.series = series;
    }
    public InternationalString getTitle() {
        return title;
    }
    public void setTitle(InternationalString title) {
        this.title = title;
    }
}
