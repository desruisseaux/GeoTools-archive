package org.geotools.metadata.iso19115;

import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.Collection;

import org.geotools.util.CheckedArrayList;
import org.geotools.util.CheckedHashMap;
import org.geotools.util.CheckedHashSet;
import org.opengis.metadata.citation.DateType;
import org.opengis.metadata.citation.PresentationForm;
import org.opengis.metadata.citation.ResponsibleParty;
import org.opengis.metadata.citation.Series;
import org.opengis.util.InternationalString;

/**
 * @deprecated Replaced by {@link org.geotools.metadata.citation.Citation}.
 */
public class Citation extends MetaData implements
		org.opengis.metadata.citation.Citation {

	private List alternateTitles = new CheckedArrayList( InternationalString.class );
	private Set citedResponsibleParties = new CheckedHashSet( ResponsibleParty.class );
	
	private InternationalString title;
	private Collection dates = new HashSet();
	private InternationalString edition;
	private Date editionDate;
	private Set identifiers = new CheckedHashSet( String.class );
	private Set identifierTypes = new CheckedHashSet( String.class );
	
	private Set presentationForm = new CheckedHashSet( PresentationForm.class );
	private Series series;
	private InternationalString otherCitationDetails;
	private InternationalString collectiveTitle;
	private String ISBN;
	private String ISSN;
    public Collection getAlternateTitles() {
        return alternateTitles;
    }
    public void setAlternateTitles(List alternateTitles) {
        this.alternateTitles = alternateTitles;
    }
    public Collection getCitedResponsibleParties() {
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
    public Collection getDates() {
        return dates;
    }
    public void setDates(Collection dates) {
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
    public Collection getIdentifiers() {
        return identifiers;
    }
    public void setIdentifiers(Set identifiers) {
        this.identifiers.clear();
        this.identifiers.addAll( identifiers );
    }
    public Collection getIdentifierTypes() {
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
    public Collection getPresentationForm() {
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
