package org.geotools.gml3;

import org.geotools.gml3.bindings.GMLBindingConfiguration;
import org.geotools.gml3.bindings.GMLSchemaLocationResolver;
import org.geotools.gml3.bindings.smil.SMIL20BindingConfiguration;
import org.geotools.gml3.bindings.smil.SMIL20SchemaLocationResolver;
import org.geotools.xlink.bindings.XLINKBindingConfiguration;
import org.geotools.xlink.bindings.XLINKSchemaLocationResolver;
import org.geotools.xml.Configuration;
import org.geotools.xml.test.XMLTestSupport;
import org.geotools.xs.bindings.XSBindingConfiguration;
import org.picocontainer.MutablePicoContainer;
import org.w3c.dom.Element;

import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.impl.CoordinateArraySequenceFactory;

public abstract class GML3TestSupport extends XMLTestSupport {

	protected void registerNamespaces( Element root ) {
		super.registerNamespaces( root );
		root.setAttribute( "xmlns", "http://www.opengis.net/gml" ); 
	}

	protected void registerSchemaLocation( Element root ) {
		root.setAttribute( 
			"xsi:schemaLocation", "http://www.opengis.net/gml gml.xsd " 
		);
	}

	protected Configuration createConfiguration() {
		return new Configuration() {

			public void configureBindings(MutablePicoContainer container) {
				new XSBindingConfiguration().configure( container );
				new XLINKBindingConfiguration().configure( container );
				new SMIL20BindingConfiguration().configure( container );
				new GMLBindingConfiguration().configure( container );
			}

			public void configureContext(MutablePicoContainer container) {
				container.registerComponentImplementation( XLINKSchemaLocationResolver.class );
				container.registerComponentImplementation( SMIL20SchemaLocationResolver.class );
				container.registerComponentImplementation( GMLSchemaLocationResolver.class );
				container.registerComponentInstance( CoordinateArraySequenceFactory.instance() );
				container.registerComponentImplementation( GeometryFactory.class );
			}
		};
	}

}
