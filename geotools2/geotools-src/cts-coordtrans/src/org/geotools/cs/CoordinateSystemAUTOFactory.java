package org.geotools.cs;

import org.geotools.proj4j.projections.Utm;
import org.geotools.pt.CoordinatePoint;
import org.geotools.units.Unit;

/**
 * Generate Automatic Projections (dynamic projections) based on code and location.
 * <p>
 * Automatic Projections are defined in Annex E of OGC-01-068r3.
 * </p>
 * <ul>
 * <li>AUTO projection codes are in the range 42000-42499</li>
 * <li>lon0 and lat0 are centeral point of the projection</li>
 * </ul>
 * <p>
 * The lon0/lat0 are provided by the SRS parameter of the map request
 * (see Section 6.5.5.2 of OGC 01-068r3)
 * </p>
 * <p>
 * This is a first-attempt CoordinateSystemAuthority to me and is not up
 * to the usual high standards of the rest of this package. Please aid in
 * improving this class with bug reports etc...
 * </p> 
 * @author Jody Garnett
 */
public class CoordinateSystemAUTOFactory
	extends CoordinateSystemAuthorityFactory {

    public CoordinateSystemAUTOFactory(final CoordinateSystemFactory factory) {
    	super( factory );
    }
	/**
	 * Authority known as "Automatic".
	 * <p>
	 * I assume this is the "display name" presented to end users?
	 * </p>
	 * @see org.geotools.cs.CoordinateSystemAuthorityFactory#getAuthority()
	 */
	public String getAuthority() {
		return "AUTO";
	}	
	/** Parse the code string to retrive the code number.
	 * <p>
	 * Assume format: AUTO:code|lon0|lat0
	 * </p>
	 * @return code for code
	 */	
	protected int toCode( String code ){
		int fieldStart = code.indexOf(':');
		int fieldEnd = code.indexOf('|');
		String field = code.substring(fieldStart+1,fieldEnd);
		return Integer.parseInt( field );
	}
	/** Parse the lon0 string to retrive the code number.
	 * <p>
	 * Assume format: AUTO:code|lon0|lat0
	 * </p>
	 * @return code for code
	 */		
	protected double toLon0( String code ){
		int fieldStart = code.indexOf('|');
		int fieldEnd = code.indexOf('|',fieldStart );
		String field = code.substring(fieldStart+1,fieldEnd);		
		return Double.parseDouble( field );	
	}
	/** Parse the lon0 string to retrive the code number.
	 * <p>
	 * Assume format: AUTO:code|lon0|lat0
	 * </p>
	 * @return code for code
	 */		
	protected double toLat0( String code ){
		int skip = code.indexOf('|');
		int fieldStart = code.indexOf('|',skip );
		String field = code.substring(fieldStart+1);		
		return Double.parseDouble( field );
	}
	
	/* (non-Javadoc)
	 * @see org.geotools.cs.CoordinateSystemAuthorityFactory#createUnit(java.lang.String)
	 */
	public Unit createUnit(String code) throws FactoryException {
		switch( toCode( code ) ){
			case 42001: return null; // Decimal_Degree
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.geotools.cs.CoordinateSystemAuthorityFactory#createEllipsoid(java.lang.String)
	 */
	public Ellipsoid createEllipsoid(String code) throws FactoryException {
		switch( toCode( code ) ){
			case 42001: 
				return Ellipsoid.WGS84;
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.geotools.cs.CoordinateSystemAuthorityFactory#createPrimeMeridian(java.lang.String)
	 */
	public PrimeMeridian createPrimeMeridian(String code)
		throws FactoryException {
		switch( toCode( code ) ){
			case 42001: 
				return PrimeMeridian.GREENWICH;
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.geotools.cs.CoordinateSystemAuthorityFactory#createDatum(java.lang.String)
	 */
	public Datum createDatum(String code) throws FactoryException {
		switch( toCode( code ) ){
			case 42001: 
				 return new Datum("WGS_1984", DatumType.GEOCENTRIC);
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.geotools.cs.CoordinateSystemAuthorityFactory#createCoordinateSystem(java.lang.String)
	 */
	public CoordinateSystem createCoordinateSystem(String code)
		throws FactoryException {
		switch( toCode(code)){
			case 42001: return createAuto42001( code );
		}
		return null;		
	}
	/**
	 * WGS 84 / Auto UTM creation for AUTO:42001
	 * <p>
	 * From the OGC 01-068r3:
	 * <pre><code>
	 * PROJCS["WGS 84 / Auto UTM",
	 *   [GEOGCS["WGS 84",
	 *       DATUM["WGS_1984",
	 *           SPHEROID["WGS_1984", 6378137, 298.257223564]
	 *       ],
	 *       PRIMEM["Greenwich",0],
	 *       UNIT["Decimal_Degree", 0.0174532925199433]
	 *   ],
	 *   PROJECTON["Transverse_Mercator"],
	 *   PARAMETER["Central_Meridian", $centralMeridian ],
	 *   PARAMETER["Latitude_of_Origion", 0 ],
	 *   PARAMETER["False_Easting", 500000 ],
	 *   PARAMETER["False_Northing", $falseNorthing ],
	 *   PARAMETER["Scale_Factor", 0.9996 ],
	 *   UNIT["Meter",1],
	 * ]
	 * </code></pre>
	 * </p>
	 * <p>
	 * Where:
	 * <ul>
	 * <li>$centralMeridian = -183 + $zone * 6
	 * <li>$zone = min( floor( $lon0 + 180.0)/6)+1, 60 )
	 * <li>$falseNorthing = $lat0 >= 0 ? 0 : 10000000;
	 * </ul>
	 * </p>
	 * @param code
	 * @return
	 * @throws FactoryException
	 */
	public CoordinateSystem createAuto42001( String code ) throws FactoryException{
		double lon0 = toLon0( code );
		double lat0 = toLat0( code );
		double falseNorthing = lat0 >= 0.0 ? 0.0 : 10000000.0;
		double zone = Math.min( Math.floor( (lon0 + 180.0)/6.0)+1, 60 );
		double centralMeridian = -183.0 + zone*6.0;
		
		String name = "WGS 84 / Auto UTM";
		HorizontalDatum datum = factory.createHorizontalDatum("WGS_84", DatumType.GEOCENTRIC, Ellipsoid.WGS84, null );
		PrimeMeridian meridian = factory.createPrimeMeridian("Central_Meridian", Unit.DEGREE, centralMeridian );				
		return factory.createGeographicCoordinateSystem(name,
														Unit.DEGREE,
														datum,
														meridian,
														AxisInfo.LONGITUDE,
														AxisInfo.LATITUDE);
	}
}
