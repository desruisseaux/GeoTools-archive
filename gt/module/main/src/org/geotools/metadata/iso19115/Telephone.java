package org.geotools.metadata.iso19115;

public class Telephone extends MetaData implements
		org.opengis.metadata.citation.Telephone {

	private String voice;
	private String facsimile;

	public String getVoice() {
		return voice;
	}
	public String getFacsimile() {
		return facsimile;
	}
	public void setFacsimile(String facsimile) {
		this.facsimile = facsimile;
	}
	public void setVoice(String voice) {
		this.voice = voice;
	}
}
