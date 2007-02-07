/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002, Geotools Project Managment Committee (PMC)
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
 *
 */
package org.geotools.data.complex.config;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.geotools.data.DataSourceException;
import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.FeatureSource;
import org.geotools.data.complex.AttributeMapping;
import org.geotools.data.complex.FeatureTypeMapping;
import org.geotools.feature.type.TypeFactoryImpl;
import org.geotools.filter.Expression;
import org.geotools.filter.ExpressionBuilder;
import org.geotools.gml.schema.SchemaParser;
import org.geotools.util.AttributeName;
import org.opengis.feature.schema.AttributeDescriptor;
import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.TypeFactory;


/**
 * Utility class to create a set of {@linkPlain
 * org.geotools.data.complex.FeatureTypeMapping} objects from a complex
 * datastore's configuration object ({@link
 * org.geotools.data.complex.config.ComplexDataStoreDTO}).
 *
 * @author Gabriel Roldan, Axios Engineering
 * @version $Id$
 *
 * @source $URL$
 * @since 2.3.x
 */
public class ComplexDataStoreConfigurator {
    /** DOCUMENT ME!  */
    private static final Logger LOGGER = Logger.getLogger(ComplexDataStoreConfigurator.class.getPackage()
                                                                                            .getName());

    /** DOCUMENT ME!  */
    private ComplexDataStoreDTO config;

    /** DOCUMENT ME!  */
    private TypeFactory registry;

    private Map sourceDataStores;
    
    private SchemaParser schemaParser;
    
    /**
     * Creates a new ComplexDataStoreConfigurator object.
     *
     * @param config DOCUMENT ME!
     * @param registry DOCUMENT ME!
     */
    private ComplexDataStoreConfigurator(ComplexDataStoreDTO config,
        TypeFactory registry) {
        this.config = config;
        this.registry = registry;
    }

    /**
     * Takes a config object and creates a set of mappings.
     * 
     * <p>
     * In the process will parse xml schemas to geotools' Feature Model types
     * and descriptors, connect to source datastores and build the mapping
     * objects from source FeatureTypes to the target ones.
     * </p>
     *
     * @param config DOCUMENT ME!
     *
     * @return a Set of {@link org.geotools.data.complex.FeatureTypeMapping}
     *         source to target FeatureType mapping definitions
     *
     * @throws IOException if any error occurs while creating the mappings
     */
    public static Set buildMappings(ComplexDataStoreDTO config)
        throws IOException {
        TypeFactory registry = new TypeFactoryImpl();

        return buildMappings(config, registry);
    }

    /**
     * Takes a config object and creates a set of mappings.
     * 
     * <p>
     * In the process will parse xml schemas to geotools' Feature Model types
     * and descriptors, connect to source datastores and build the mapping
     * objects from source FeatureTypes to the target ones.
     * </p>
     *
     * @param config DOCUMENT ME!
     * @param registry TypeFactory to use as type registry, parsed types will
     *        be registered on it.
     *
     * @return a Set of {@link org.geotools.data.complex.FeatureTypeMapping}
     *         source to target FeatureType mapping definitions
     *
     * @throws IOException if any error occurs while creating the mappings
     * @throws NullPointerException DOCUMENT ME!
     */
    public static Set buildMappings(ComplexDataStoreDTO config,
        TypeFactory registry) throws IOException {
        if (config == null) {
            throw new NullPointerException("config");
        }

        if (registry == null) {
            throw new NullPointerException("TypeFactory");
        }

        ComplexDataStoreConfigurator mappingsBuilder;
        mappingsBuilder = new ComplexDataStoreConfigurator(config, registry);

        Set mappingObjects = mappingsBuilder.buildMappings();

        return mappingObjects;
    }

    /**
     * Actually builds the mappings from the config dto.
     * 
     * <p>
     * Build steps are: - parse xml schemas to FM types - connect to source
     * datastores - build mappings
     * </p>
     *
     * @return
     *
     * @throws IOException DOCUMENT ME!
     */
    private Set buildMappings() throws IOException {
        //-parse target xml schemas, let parsed types on <code>registry</code>
        parseGmlSchemas();
        
        //-create source datastores
        sourceDataStores = aquireSourceDatastores();

        //-create FeatureType mappings
        Set featureTypeMappings = createFeatureTypeMappings();
        
        return featureTypeMappings;
    }
    
    private Set createFeatureTypeMappings()throws IOException{
    	Set mappingsConfigs = config.getTypeMappings();
    	
    	Set featureTypeMappings = new HashSet();
    	
    	for(Iterator it = mappingsConfigs.iterator(); it.hasNext();){
    		TypeMapping dto = (TypeMapping)it.next();
    		
    		FeatureSource featureSoruce = getFeatureSource(dto);
    		AttributeDescriptor target = getTargetDescriptor(dto);
    		List attMappings = getAttributeMappings(dto);
    		Map fidMappings = getFidMappings(dto);
    		List groupByAtts = dto.getGroupbyAttributeNames();
    		
    		FeatureTypeMapping mapping;
    		
    		mapping = new FeatureTypeMapping(featureSoruce, target, attMappings, fidMappings);
    		
    		if(groupByAtts.size() > 0){
    			mapping.setGroupByAttNames(groupByAtts);
    		}
    		
    		featureTypeMappings.add(mapping);
    	}
    	return featureTypeMappings;
    }
    
    private AttributeDescriptor getTargetDescriptor(TypeMapping dto)throws IOException{
    	if(schemaParser == null)throw new IllegalStateException("schemas not yet parsed");
    	TypeFactory tf = schemaParser.getTypeFactory();
    	
    	AttributeDescriptor target = null;
    	String prefixedTargetName = dto.getTargetElementName();
    	AttributeName targetNodeName = deglose(prefixedTargetName);
    	
    	target = tf.getDescriptor(targetNodeName);
    	if(target == null){
    		throw new DataSourceException("Descriptor name not found: " + targetNodeName);
    	}
    	return target;
    }

    /**
     * Creates a list of {@link org.geotools.data.complex.AttributeMapping}
     * from the attribute mapping configurations in the provided list
     * of {@link AttributeMapping}
     * 
     * @param attDtos
     * @return
     */
    private List getAttributeMappings(List attDtos)throws IOException{
		final TypeFactory registry = schemaParser.getTypeFactory();
    	
		List attMappings = new LinkedList();
		
    	for(Iterator it = attDtos.iterator(); it.hasNext();){
    	
    		org.geotools.data.complex.config.AttributeMapping attDto;
    		attDto = (org.geotools.data.complex.config.AttributeMapping)it.next();
    		
    		String sourceExpr = attDto.getSourceExpression();
    		String expectedInstanceTypeName = attDto.getTargetAttributeSchemaElement();

    		final String targetAttPath = attDto.getTargetAttributePath();
    		
    		final boolean isMultiValued = attDto.isMultiple();
    		
    		final Expression expression = parseOgcCqlExpression(sourceExpr);

    		final AttributeType expectedInstanceOf;
    		
    		final Map clientProperties = getClientProperties(attDto);
    		
    		if(expectedInstanceTypeName != null){
        		AttributeName expectedNodeTypeName = null;
    			expectedNodeTypeName = deglose(expectedInstanceTypeName);
    			expectedInstanceOf = registry.getType(expectedNodeTypeName);
    			if(expectedInstanceOf == null){
    				String msg = "mapping expects and instance of " +
    				expectedNodeTypeName + " for attribute " + targetAttPath + 
    				" but the attribute descriptor was not found";
    				throw new DataSourceException(msg);
    			}
    		}else{
    			expectedInstanceOf = null;
    		}
    		
    		AttributeMapping attMapping = new AttributeMapping(expression, targetAttPath, 
    															expectedInstanceOf, isMultiValued, 
    															clientProperties);
    		attMappings.add(attMapping);
    	}
    	return attMappings;
    }

	private Expression parseOgcCqlExpression(String sourceExpr) throws DataSourceException {
		Expression expression = FeatureTypeMapping.NULL_EXPRESSION;
		if(sourceExpr != null){
			try {
				expression = (Expression)ExpressionBuilder.parse(sourceExpr);
			} catch (Exception e) {
				String msg = "parsing expression " + sourceExpr;
				LOGGER.log(Level.SEVERE, msg, e);
				throw new DataSourceException(msg + ": " + e.getMessage(), e);
			}
		}
		return expression;
	}

	private Map getClientProperties(org.geotools.data.complex.config.AttributeMapping dto)
	throws DataSourceException{
	
		if(dto.getClientProperties().size() == 0){
			return Collections.EMPTY_MAP;
		}
		
		Map clientProperties = new HashMap();
		for(Iterator it = dto.getClientProperties().entrySet().iterator(); it.hasNext();){
			Map.Entry entry = (Map.Entry)it.next();
			String name = (String)entry.getKey();
			AttributeName qName = deglose(name);
			String cqlExpression = (String)entry.getValue();
			Expression expression = parseOgcCqlExpression(cqlExpression);
			clientProperties.put(qName, expression);
		}
		return clientProperties;
	}
	
    private List getAttributeMappings(TypeMapping dto)throws IOException{
    	List attMappingDtos = dto.getAttributeMappings();
    	List attMappings = getAttributeMappings(attMappingDtos);
    	return attMappings;
    }    
    
    private Map getFidMappings(TypeMapping dto)throws IOException{
    	
    	List fidMappingDtos = dto.getFidMappings();
    	List fidMappings = getAttributeMappings(fidMappingDtos);
    	
    	Map fidMappingsMap = new HashMap();
    	for(Iterator it = fidMappings.iterator(); it.hasNext();){
    		AttributeMapping attMapping = (AttributeMapping) it.next();
    		String xpath = attMapping.getTargetXPath();
    		Expression expression = attMapping.getSourceExpression();
    		
    		fidMappingsMap.put(xpath, expression);
    	}
    	return fidMappingsMap;
    }
    
    private FeatureSource getFeatureSource(TypeMapping dto)throws IOException{
    	String dsId = dto.getSourceDataStore();
    	String typeName = dto.getSourceTypeName();

    	DataStore sourceDataStore = (DataStore)sourceDataStores.get(dsId);
    	if(sourceDataStore == null){
    		throw new DataSourceException("datastore " + dsId 
    				+ " not found for type mapping " + dto);
    	}
    	
    	LOGGER.fine("asking datastore " + sourceDataStore + " for source type " + typeName);
    	FeatureSource fSource = sourceDataStore.getFeatureSource(typeName);
    	LOGGER.fine("found feature source for " + typeName);
    	return fSource;
    }

    /**
     * Parses the target xml schema files and stores the generated types and
     * AttributeDescriptors in {@link #registry}.
     * 
     * <p>
     * The list of file names to parse is obtained from
     * config.getTargetSchemasUris(). If a file name contained in that list is
     * a relative path (i.e., does not starts with file: or http:,
     * config.getBaseSchemasUrl() is used to resolve relative paths against.
     * </p>
     *
     * @throws IOException
     */
    private void parseGmlSchemas() throws IOException {
        LOGGER.finer("about to parse target schemas");

        final URL baseUrl;
        if(config.getBaseSchemasUrl() == null){
        	baseUrl = null;
        }else{
        	baseUrl = new URL(config.getBaseSchemasUrl());
        }
        
        final List schemaFiles = config.getTargetSchemasUris();

        schemaParser = SchemaParser.newInstance();
        
        for (Iterator it = schemaFiles.iterator(); it.hasNext();) {
            String schemaLocation = (String) it.next();
            final URL schemaUrl = guessSchemaUrl(baseUrl, schemaLocation);
            LOGGER.fine("parsing schema " + schemaUrl.toExternalForm());

            schemaParser.parse(schemaUrl);
        }
    }

	private URL guessSchemaUrl(final URL baseUrl, String schemaLocation) throws MalformedURLException {
		final URL schemaUrl;
		if(schemaLocation.startsWith("file:") || schemaLocation.startsWith("http:")){
			LOGGER.fine("using schema location as absolute path: " + schemaLocation);
			schemaUrl = new URL(schemaLocation);
		}else{
			if(baseUrl == null){
			 	schemaUrl = new URL(schemaLocation);
				LOGGER.warning("base url not provided, may be unable to locate" + schemaLocation + 
						". Path resolved to: " + schemaUrl.toExternalForm());
			}else{
				LOGGER.fine("using schema location " + schemaLocation + 
						" as relative to " + baseUrl);
			 	schemaUrl = new URL(baseUrl, schemaLocation);
			}
		}
		return schemaUrl;
	}

    /**
     * DOCUMENT ME!
     *
     * @return a Map&lt;String,DataStore&gt; where the key is the id given to
     *         the datastore in the configuration.
     *
     * @throws IOException
     * @throws DataSourceException DOCUMENT ME!
     */
    private Map aquireSourceDatastores() throws IOException {
        LOGGER.entering(getClass().getName(), "aquireSourceDatastores");

        final Map datastores = new HashMap();
        final List dsParams = config.getSourceDataStores();
        String id;

        for (Iterator it = dsParams.iterator(); it.hasNext();) {
            SourceDataStore dsconfig = (SourceDataStore) it.next();
            id = dsconfig.getId();

            Map datastoreParams = dsconfig.getParams();

            LOGGER.fine("looking for datastore " + id);

            DataStore dataStore = DataStoreFinder.getDataStore(datastoreParams);

            if (dataStore == null) {
                throw new DataSourceException(
                    "Cannot find a datastore for parameters " + datastoreParams);
            }

            LOGGER.fine("got datastore " + dataStore);
            datastores.put(id, dataStore);
        }

        return datastores;
    }

	
    /**
     * Takes a prefixed attribute name and returns an {@link AttributeName} by
     * looking which namespace belongs the prefix to in {@link ComplexDataStoreDTO#getNamespaces()}.
     * 
     * @param prefixedName
     * @return
     * @throws IllegalArgumentException if <code>prefixedName</code> has no prefix.
     */
	private AttributeName deglose(String prefixedName)throws IllegalArgumentException{
		AttributeName name = null;
		
		if(prefixedName == null)return null;
		
		int prefixIdx = prefixedName.indexOf(':');
		if(prefixIdx == -1){
			return new AttributeName(prefixedName);
			//throw new IllegalArgumentException(prefixedName + " is not prefixed");
		}

		Map namespaces = config.getNamespaces();
		
		String nsPrefix = prefixedName.substring(0, prefixIdx);
		String localName = prefixedName.substring(prefixIdx + 1);
		String nsUri = (String)namespaces.get(nsPrefix);
		
		name = new AttributeName(nsUri, localName);
		
		return name;
	}

}
