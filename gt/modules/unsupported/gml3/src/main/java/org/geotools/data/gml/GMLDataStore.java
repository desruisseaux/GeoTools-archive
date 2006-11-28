package org.geotools.data.gml;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.xsd.XSDElementDeclaration;
import org.eclipse.xsd.XSDSchema;
import org.eclipse.xsd.XSDTypeDefinition;
import org.eclipse.xsd.util.XSDSchemaLocationResolver;
import org.geotools.data.DataSourceException;
import org.geotools.data.store.AbstractDataStore2;
import org.geotools.feature.FeatureType;
import org.geotools.gml3.GMLConfiguration;
import org.geotools.gml3.bindings.GML;
import org.geotools.gml3.bindings.GML3ParsingUtils;
import org.geotools.xml.BindingWalkerFactory;
import org.geotools.xml.Configuration;
import org.geotools.xml.Schemas;
import org.geotools.xml.impl.BindingLoader;
import org.geotools.xml.impl.BindingWalkerFactoryImpl;
import org.geotools.xml.impl.TypeWalker;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.defaults.DefaultPicoContainer;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

public class GMLDataStore extends AbstractDataStore2 {

	/**
	 * Application schema namespace
	 */
	String namespace;
	/**
	 * Document location
	 */
	String location;
	/**
	 * Schema location
	 */
	String schemaLocation;
	/**
	 * The application schema
	 */
	XSDSchema schema;
	
	/**
	 * Creates a new datastore from an instance document.
	 * 
	 * @param namespace The application schema namespace.
	 * @param location The location ( as a uri ) of the instance document.
	 */
	public GMLDataStore ( String namespace, String location ) {
		this( namespace, location , null );
	}
	
	/**
	 * Creates a new datastore from an instance document and an application schema location.
	 * 
	 * @param namespace The application schema namespace.
	 * @param location The location ( as a uri ) of the instance document.
	 * @param schemaLocation The location ( as a uri ) of the application schema.
	 */
	public GMLDataStore ( String namespace, String location, String schemaLocation ) {
		this.namespace = namespace;
		this.location = location;
		this.schemaLocation = schemaLocation;
	}
	
	/**
	 * @return The location of the instance document to parse.
	 */
	String getLocation() {
		return location;
	}
	
	/**
	 * @return The location of the application schema.
	 */
	String getSchemaLocation() {
		return schemaLocation;
	}
	
	protected List createContents() {
		//TODO: this method should create content lazily.
		try {
			List contents = new ArrayList();
			XSDSchema schema = schema();
			
			//look for elements in the schema which are of type AbstractFeatureType
			for ( Iterator e = schema.getElementDeclarations().iterator(); e.hasNext(); ) {
				XSDElementDeclaration element = (XSDElementDeclaration) e.next();
				if ( !namespace.equals( element.getTargetNamespace() ) ) 
					continue;
				
				final ArrayList isFeatureType = new ArrayList();
				TypeWalker.Visitor visitor = new TypeWalker.Visitor() {

					public boolean visit(XSDTypeDefinition type) {
						if ( GML.NAMESPACE.equals( type.getTargetNamespace() ) && 
								GML.AbstractFeatureCollectionType.getLocalPart().equals( type.getName() ) ) {
							return false;
						}
						
						if ( GML.NAMESPACE.equals( type.getTargetNamespace() ) && 
								GML.AbstractFeatureType.getLocalPart().equals( type.getName() ) ) {
							isFeatureType.add( Boolean.TRUE );
							return false;
						}
						
						return true;
					}
					
				};
				
				XSDTypeDefinition type = element.getType().getBaseType();
				new TypeWalker( type ).walk( visitor );
				
				if ( !isFeatureType.isEmpty() ) {
					FeatureType featureType = featureType( element );
					contents.add( new GMLTypeEntry( this, featureType, null ) );
				}
			}
			
			return contents;
		} 
		catch (IOException e) {
			throw new RuntimeException( e );
		}
	}
	
	/**
	 * Helper method for transforming an xml feautre type to a geotools feature type.
	 * @param element
	 * @return
	 * @throws IOException
	 */
	private FeatureType featureType( XSDElementDeclaration element )
		throws IOException {
		
		//load up the bindings for type conversion
		GMLConfiguration configuration = new GMLConfiguration();
		
		BindingLoader bindingLoader = new BindingLoader();
		bindingLoader.setContainer( configuration.setupBindings( bindingLoader.getContainer() ) );
		
		MutablePicoContainer context = new DefaultPicoContainer();
		context = configuration.setupContext( context );
		
		BindingWalkerFactory bwFactory = new BindingWalkerFactoryImpl( bindingLoader, context );
		try {
			return GML3ParsingUtils.featureType( element, bwFactory );
		} 
		catch (Exception e) {
			throw (IOException) new IOException().initCause( e );
		}
	}
	
	/**
	 * @return An input stream for hte document.
	 */
	InputStream document() throws IOException {
		File location;
		try {
			location = new File( new URI( getLocation() ) );
		} 
		catch (URISyntaxException e) {
			throw (IOException) new IOException().initCause( e );
		}
		return new BufferedInputStream( new FileInputStream( location ) );
	}
	
	/**
	 * Helper method which returns a parser configuration.
	 * @return
	 * @throws IOException
	 */
	Configuration configuration() throws IOException {
		if ( schemaLocation != null ) {
			//create a custom congfiguraiton
			return new ApplicationSchemaConfiguration( namespace, schemaLocation );
		}
		
		//just use gml3
		return new GMLConfiguration();
	}
	
	/**
	 * Helper method which lazily parses the application schema.
	 * 
	 * @throws IOException
	 */
	XSDSchema schema() throws IOException {
		if ( schema == null ) {
			synchronized ( this ) {
				if ( schema == null ) {
					GMLConfiguration configuration = new GMLConfiguration();
					
					//get all the necessary schema locations
					List dependencies = configuration.allDependencies();
					List resolvers = new ArrayList();
					for ( Iterator d = dependencies.iterator(); d.hasNext(); ) {
						Configuration dependency = (Configuration) d.next();
						XSDSchemaLocationResolver resolver = dependency.getSchemaLocationResolver();
						if ( resolver != null ) {
							resolvers.add( resolver );
						}
					}
					
					//if a schema location was specified, add one for it
					if ( schemaLocation == null ) {				
						//parse some of the instance document to find out the schema location
						InputStream input = document();
						
						//create stream parser
						XmlPullParser parser = null;
						
						try {
							XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
							factory.setNamespaceAware(true);
							factory.setValidating(false);
								
							//parse root element
							parser = factory.newPullParser();
							parser.setInput( input, "UTF-8" );
							parser.nextTag();
							
							//look for schema location
							for ( int i = 0; i < parser.getAttributeCount(); i++ ) {
								if ( "schemaLocation".equals( parser.getAttributeName( i ) ) ) {
									String xsiSchemaLocation = parser.getAttributeValue( i );
									String[] split = xsiSchemaLocation.split( " " );
									for ( int j = 0; j < split.length; j += 2 ) {
										if ( namespace.equals( split[ j ] ) ) {
											schemaLocation = split[ j + 1 ];
											break;
										}
									}
									
									break;
								}
							}
							
							//reset input stream
							parser.setInput( null );
							input.close();
						} 
						catch (XmlPullParserException e) {
							throw (IOException) new IOException().initCause( e );
						}
					}
				
					if ( schemaLocation == null ) {
						throw new DataSourceException( "Unable to determine application schema location ");
					}
					
					schema = Schemas.parse( schemaLocation, null, resolvers );
				}
			}
		}
		
		return schema;
	}
}
