/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2008, Open Source Geospatial Foundation (OSGeo)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.geotools.coverage.io.range;

import java.awt.image.SampleModel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.measure.Measure;
import javax.measure.quantity.Dimensionless;
import javax.measure.quantity.Length;
import javax.measure.quantity.Quantity;
import javax.measure.unit.SI;
import javax.measure.unit.Unit;

import org.geotools.feature.NameImpl;
import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultEngineeringCRS;
import org.geotools.referencing.cs.DefaultCoordinateSystemAxis;
import org.geotools.referencing.cs.DefaultLinearCS;
import org.geotools.referencing.datum.DefaultEngineeringDatum;
import org.geotools.util.MeasurementRange;
import org.geotools.util.SimpleInternationalString;
import org.opengis.coverage.SampleDimension;
import org.opengis.feature.type.Name;
import org.opengis.referencing.crs.SingleCRS;
import org.opengis.referencing.cs.AxisDirection;
import org.opengis.referencing.cs.CoordinateSystemAxis;
import org.opengis.referencing.datum.EngineeringDatum;
import org.opengis.util.InternationalString;

/**
 * Definition of one axis in a field for which we have some
 * measurements/observations/forecasts. The {@link Axis} data structure
 * describes the nature of each control variable for a certain {@link RangeDescriptor}
 * 
 * 
 * @author Simone Giannecchini, GeoSolutions
 */
public class Axis<Q extends Quantity>{

	/**
	 * Implementation of {@link Axis} for multibands images.
	 * 
	 * <p>
	 * This implementation of Axis can be seen as a stub implementation since in
	 * this case we do not really have an {@link Axis} for this kind of data, or
	 * rather we have an axis that just represents an ordinal or a certain set of .
	 * 
	 * @author Simone Giannecchini, GeoSolutions
	 * @todo add convenience constructor based on {@link SampleDimension} and or
	 *       {@link SampleModel}
	 */
	public static class DimensionlessAxis extends Axis<Dimensionless> {
		

		/**
	     * 
	     */
	    public DimensionlessAxis(final Name name,final InternationalString description) {
	    	super(name,description,Unit.ONE );
	    }
	    
		/**
	     * 
	     */
	    public DimensionlessAxis(final String name,final String description) {
	    	super(new NameImpl(name),new SimpleInternationalString(description),Unit.ONE );
	    }
	    
		/**
	     * 
	     */
	    public DimensionlessAxis(final String name) {
	    	super(new NameImpl(name),new SimpleInternationalString(name),Unit.ONE );
	    }

	}
	public static class WavelengthAxis extends Axis<Length>{
		
		/**
		 * Singleton instance of a {@link WavelengthAxis} that measures in nanometers.
		 */
		public final static WavelengthAxis WAVELENGTH_AXIS_NM= new WavelengthAxis("WAVELENGTH_AXIS_NM",RangeUtilities.WAVELENGTH_UOM);
		
	    /**
		 * A bin for the wavelength axis
		 */
		public static class WavelengthBin extends AxisBin<MeasurementRange<Double>,Length>{
					
			/**
			 * 
			 */
			private static final long serialVersionUID = -3977921692927799401L;
			
			public WavelengthBin( Name name, double value, InternationalString description, WavelengthAxis axis ){
				super(
						name,
						description,
						axis,
						MeasurementRange.create(value, value, RangeUtilities.WAVELENGTH_UOM));
				
			}
		
			public WavelengthBin( String name, double value, String description, WavelengthAxis axis ){
				super(
						new NameImpl(name),
						new SimpleInternationalString(description),
						axis,
						MeasurementRange.create(value, value, RangeUtilities.WAVELENGTH_UOM));
				
			}		
			
			
			public WavelengthBin( Name name, double from, double to, InternationalString description, WavelengthAxis axis ) {
				super(
						name,
						description,
						axis,
						MeasurementRange.create(from, to, RangeUtilities.WAVELENGTH_UOM));
			}
			public WavelengthBin( String name, double from, double to, String description, WavelengthAxis axis ) {
				super(
						new NameImpl(name),
						new SimpleInternationalString(description),
						axis,
						MeasurementRange.create(from, to, RangeUtilities.WAVELENGTH_UOM));
			}		
		}

		/**
		 * Keys for this {@link Axis}.
		 */
		private ArrayList<Measure<MeasurementRange<Double>, Length>> keys;
		
		private NameImpl name;

		/** LANDSAT7 definition of BLUE */
		public static final WavelengthBin LANDSAT7_BLUE_AXIS_BIN= new WavelengthBin( "BLUE", 450, 520, "useful for soil/vegetation discrimination, forest type mapping, and identifying man-made features",WavelengthAxis.WAVELENGTH_AXIS_NM);

		/** Blue light between 450-495 nm */
		public static final WavelengthBin COLOR_BLUE_AXIS_BIN= new WavelengthBin( "Blue", 450, 495, "Visible light between 450-495 nm" ,WavelengthAxis.WAVELENGTH_AXIS_NM);

		/** Green light between 495-570 nm */	
		public static final WavelengthBin COLOR_GREEN_AXIS_BIN= new WavelengthBin( "Green", 495, 570, "Visible light between 495-570 nm" ,WavelengthAxis.WAVELENGTH_AXIS_NM);

		/** Orange light between 590-620 nm */	
		public static final WavelengthBin COLOR_ORANGE_AXIS_BIN= new WavelengthBin( "Green", 590,620, "Visible light between 590-620 nm",WavelengthAxis.WAVELENGTH_AXIS_NM );

		/** Red between 620-750 nm */
		public static final WavelengthBin COLOR_RED_AXIS_BIN= new WavelengthBin( "Yellow", 620, 750, "Visible light between 620-750 nm" ,WavelengthAxis.WAVELENGTH_AXIS_NM);

		/** Violet light between 380-450 nm */
		public static final WavelengthBin COLOR_VIOLET_AXIS_BIN= new WavelengthBin( "Violet", 380, 450, "Visible light between 380-450 nm" ,WavelengthAxis.WAVELENGTH_AXIS_NM);

		/** Yellow between 570-590 nm */
		public static final WavelengthBin COLOR_YELLOW_AXIS_BIN= new WavelengthBin( "Yellow", 570,590, "Visible light between 570-590 nm" ,WavelengthAxis.WAVELENGTH_AXIS_NM);

		/** LANDSAT7 definition of GREEN */
		public static  final WavelengthBin GREEN_AXIS_BIN= new WavelengthBin( "GREEN", 520, 610, "penetrates clear water fairly well, and gives excellent contrast between clear and turbid (muddy) water.",WavelengthAxis.WAVELENGTH_AXIS_NM);

		/** LANDSAT7 definition of NIR */	
		public static  final WavelengthBin NIR_AXIS_BIN =new WavelengthBin("NIR", 750, 900, " good for mapping shorelines and biomass content",WavelengthAxis.WAVELENGTH_AXIS_NM);

		/** LANDSAT7 definition of RED */	
		public static  final WavelengthBin RED_AXIS_BIN= new WavelengthBin("RED",  630, 690, "useful for identifying vegetation types, soils, and urban (city and town) features",WavelengthAxis.WAVELENGTH_AXIS_NM);

		/** LANDSAT7 definition of SWIR */
		public static  final WavelengthBin SWIR_AXIS_BIN =new WavelengthBin("SWIR", 1550, 17560, "useful to measure the moisture content of soil and vegetation",WavelengthAxis.WAVELENGTH_AXIS_NM);

		/** LANDSAT7 definition of SWIR2 */
		public static  final WavelengthBin SWIR2_AXIS_BIN= new WavelengthBin("SWIR2", 2080, 23500, "useful to measure the moisture content of soil and vegetation",WavelengthAxis.WAVELENGTH_AXIS_NM);

		/** LANDSAT7 definition of TIR */
		public static  final WavelengthBin TIR_AXIS_BIN= new WavelengthBin("TIR", 10400, 12500, "useful to observe temperature",WavelengthAxis.WAVELENGTH_AXIS_NM);
		
		private static final DefaultEngineeringCRS CRS;
		static {
			final CoordinateSystemAxis csAxis = new DefaultCoordinateSystemAxis(
					new SimpleInternationalString("Light"),
					"\u03BB", // LAMBDA
					AxisDirection.OTHER, 	
					RangeUtilities.WAVELENGTH_UOM);
			final DefaultLinearCS lightCS = new DefaultLinearCS("Light",csAxis);
			final Map<String,Object> datumProperties = new HashMap<String,Object>();
			datumProperties.put("name", "light");
			
			final EngineeringDatum lightDatum = new DefaultEngineeringDatum( datumProperties );		
			CRS = new DefaultEngineeringCRS("Wave Length", lightDatum, lightCS );		
		}
		
		/**
		 * 
		 */
		public WavelengthAxis(final String name, final Unit<Length> uom) {
			super(new NameImpl(name),new SimpleInternationalString(name),uom);
		}
		/**
		 * These are units of length; as such the are
		 * not restricted to a coordinate reference system.
		 */
		public SingleCRS getCoordinateReferenceSystem() {		
			return CRS;
		}

		public InternationalString getDescription() {
			return new SimpleInternationalString("Spectral Information");
		}

		public Measure<MeasurementRange<Double>, Length> getKey(int keyIndex) {
			return this.keys.get(keyIndex);
		}

		public List<Measure<MeasurementRange<Double>, Length>> getKeys() {
			return Collections.unmodifiableList(keys);
		}

		public Name getName() {
			return name;
		}

		public int getNumKeys() {
			return keys.size();
		}

		public Unit<Length> getUnitOfMeasure() {
			return SI.MICRO(SI.METER);
		}
	}
	private SingleCRS crs;
	private InternationalString description;
	private Name name;
	private Unit<Q> unit;
	
	public Axis( final String name, final Unit<Q> unit){
		this( new NameImpl( name ), new SimpleInternationalString( name ),unit );
	}
	
	public Axis( final Name name, final InternationalString description, final Unit<Q> unit){
		this.name = name;
		this.unit = unit;
		this.description = description;
	}
	
	@SuppressWarnings("unchecked")
	public Axis( final Name name, final InternationalString description, SingleCRS crs ){
		this(name, description,  getUoM(crs));
		this.crs = crs;
	}
	
	public Axis( final String name, final String description, SingleCRS crs ){
		this(new NameImpl(name), new SimpleInternationalString(description),  crs);
	}
	
	public Axis( String name, SingleCRS crs ){
		this(new NameImpl(name), new SimpleInternationalString(name),  crs);
	}
	
	@SuppressWarnings("unchecked")
    private static Unit getUoM(SingleCRS crs) {
		return crs.getCoordinateSystem().getAxis(0).getUnit();
	}

	/**
     * Retrieves the coordinate reference system for this {@link Axis}.
     * 
     * <p>
     * In case the coordinate reference system is present the Unit of measure
     * for its single coordinate axis should conform to the global {@link Unit}
     * for this {@link Axis}.
     * 
     * @return the coordinate reference system for this {@link Axis} or
     *         <code>null</code>, if no coordinate reference system is know
     *         or applicable.
     */
	public SingleCRS getCoordinateReferenceSystem() {
		return crs;
	}

	/**
     * Retrieves the description of the {@link Axis}
     * 
     * @return description of the {@link Axis}
     */
	public InternationalString getDescription() {
		return description;
	}

    /**
     * Retrieves the {@link Axis} name
     * 
     * @return {@link org.opengis.feature.type.Name} of the {@link Axis}s
     */
	public Name getName() {
		return name;
	}

    /**
     * Retrieves the Unit of measure for the various keys of this axis.
     * 
     * In case this {@link Axis} is not made of measurable quantities
     * 
     * @return the Unit of measure for the various keys of this axis.
     */
	public Unit<Q> getUnitOfMeasure() {
		return unit;
	}
	
	public <V> boolean isBinCompatible(final AxisBin<V, Q> bin){
		return bin.getAxis().equals(this);
	}

	@Override
	public String toString() {
		final StringBuilder builder= new StringBuilder();
		builder.append("Axis description").append("\n");
		builder.append("Name:").append("\t\t\t\t\t").append(name.toString()).append("\n");
		builder.append("Description:").append("\t\t\t\t").append(description.toString()).append("\n");
		builder.append("Unit:").append("\t\t\t\t\t").append(unit!=null?unit.toString():"null uom").append("\n");
		builder.append("crs:").append("\t\t\t\t\t").append(crs!=null?crs.toString():"null crs").append("\n");
		return builder.toString();
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((crs == null) ? 0 : crs.hashCode());
		result = prime * result + ((description == null) ? 0 : description.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((unit == null) ? 0 : unit.hashCode());
		return result;
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		
		Axis that = (Axis) obj;
		
		if (crs == null) {
			if (that.crs != null)
				return false;
		} else if (!CRS.equalsIgnoreMetadata(crs,that.crs))
			return false;
		
		if (description == null) {
			if (that.description != null)
				return false;
		} else if (!description.toString().equalsIgnoreCase(that.description.toString()))
			return false;
		
		if (name == null) {
			if (that.name != null)
				return false;
		} else if (!name.toString().equalsIgnoreCase(that.name.toString()))
			return false;
		
		if (unit == null) {
			if (that.unit != null)
				return false;
		} else if (!unit.equals(that.unit))
			return false;
		return true;
	}
	@SuppressWarnings("unchecked")
	public boolean compatibleWith(final Axis that){
		// if the two axis are equals then we have compatibility
		if(equals(that))
			return true;

		// if the crs and the uom is the same thing, the axis are compatible
		if (crs == null) {
			if (that.crs != null)
				return false;
		} else if (!CRS.equalsIgnoreMetadata(crs,that.crs))
			return false;
		
		
		if (unit == null) {
			if (that.unit != null)
				return false;
		} else if (!unit.equals(that.unit))
			return false;
		
		return false;
	}
}
