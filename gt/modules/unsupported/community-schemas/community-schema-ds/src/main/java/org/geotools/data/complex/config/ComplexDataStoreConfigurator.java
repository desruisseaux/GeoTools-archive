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

import org.geotools.data.DataAccess;
import org.geotools.data.DataAccessFinder;
import org.geotools.data.DataSourceException;
import org.geotools.data.complex.AttributeMapping;
import org.geotools.data.complex.FeatureTypeMapping;
import org.geotools.data.feature.FeatureAccess;
import org.geotools.data.feature.FeatureSource2;
import org.geotools.text.filter.FilterBuilder;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.TypeName;
import org.opengis.filter.expression.Expression;

/**
 * Utility class to create a set of {@linkPlain
 * org.geotools.data.complex.FeatureTypeMapping} objects from a complex
 * datastore's configuration object ({@link
 * org.geotools.data.complex.config.ComplexDataStoreDTO}).
 * 
 * @author Gabriel Roldan, Axios Engineering
 * @version $Id: ComplexDataStoreConfigurator.java 24255 2007-02-07 14:12:24Z
 *          groldan $
 * 
 * @source $URL$
 * @since 2.3.x
 */
public class ComplexDataStoreConfigurator {
    /** DOCUMENT ME! */
    private static final Logger LOGGER = Logger.getLogger(ComplexDataStoreConfigurator.class
            .getPackage().getName());

    /** DOCUMENT ME! */
    private ComplexDataStoreDTO config;

    private Map registry;

    private Map sourceDataStores;

    /**
     * Creates a new ComplexDataStoreConfigurator object.
     * 
     * @param config
     *            DOCUMENT ME!
     * @param registry
     *            DOCUMENT ME!
     */
    private ComplexDataStoreConfigurator(ComplexDataStoreDTO config) {
        this.config = config;
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
     * @param config
     *            DOCUMENT ME!
     * 
     * @return a Set of {@link org.geotools.data.complex.FeatureTypeMapping}
     *         source to target FeatureType mapping definitions
     * 
     * @throws IOException
     *             if any error occurs while creating the mappings
     */
    public static Set buildMappings(ComplexDataStoreDTO config) throws IOException {
        ComplexDataStoreConfigurator mappingsBuilder;

        mappingsBuilder = new ComplexDataStoreConfigurator(config);
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
     * @throws IOException
     *             DOCUMENT ME!
     */
    private Set buildMappings() throws IOException {
        // -parse target xml schemas, let parsed types on <code>registry</code>
        parseGmlSchemas();

        // -create source datastores
        sourceDataStores = aquireSourceDatastores();

        // -create FeatureType mappings
        Set featureTypeMappings = createFeatureTypeMappings();

        return featureTypeMappings;
    }

    private Set createFeatureTypeMappings() throws IOException {
        Set mappingsConfigs = config.getTypeMappings();

        Set featureTypeMappings = new HashSet();

        for (Iterator it = mappingsConfigs.iterator(); it.hasNext();) {
            TypeMapping dto = (TypeMapping) it.next();

            FeatureSource2 featureSoruce = getFeatureSource(dto);
            AttributeDescriptor target = getTargetDescriptor(dto);
            List attMappings = getAttributeMappings(dto);
            List groupByAtts = dto.getGroupbyAttributeNames();

            FeatureTypeMapping mapping;

            mapping = new FeatureTypeMapping(featureSoruce, target, attMappings);

            if (groupByAtts.size() > 0) {
                mapping.setGroupByAttNames(groupByAtts);
            }

            featureTypeMappings.add(mapping);
        }
        return featureTypeMappings;
    }

    private AttributeDescriptor getTargetDescriptor(TypeMapping dto) throws IOException {
        if (registry == null) {
            throw new IllegalStateException("schemas not yet parsed");
        }

        String prefixedTargetName = dto.getTargetElementName();
        TypeName targetNodeName = deglose(prefixedTargetName);

        AttributeDescriptor targetDescriptor = (AttributeDescriptor) registry.get(targetNodeName);
        return targetDescriptor;
    }

    /**
     * Creates a list of {@link org.geotools.data.complex.AttributeMapping} from
     * the attribute mapping configurations in the provided list of
     * {@link AttributeMapping}
     * 
     * @param attDtos
     * @return
     */
    private List getAttributeMappings(List attDtos) throws IOException {
        List attMappings = new LinkedList();

        for (Iterator it = attDtos.iterator(); it.hasNext();) {

            org.geotools.data.complex.config.AttributeMapping attDto;
            attDto = (org.geotools.data.complex.config.AttributeMapping) it.next();

            String idExpr = attDto.getIdentifierExpression();
            String sourceExpr = attDto.getSourceExpression();
            String expectedInstanceTypeName = attDto.getTargetAttributeSchemaElement();

            final String targetXPath = attDto.getTargetAttributePath();

            final boolean isMultiValued = attDto.isMultiple();

            final Expression idExpression = parseOgcCqlExpression(idExpr);
            final Expression sourceExpression = parseOgcCqlExpression(sourceExpr);

            final AttributeType expectedInstanceOf;

            final Map clientProperties = getClientProperties(attDto);

            if (expectedInstanceTypeName != null) {
                TypeName expectedNodeTypeName = null;
                expectedNodeTypeName = deglose(expectedInstanceTypeName);
                expectedInstanceOf = (AttributeType) registry.get(expectedNodeTypeName);
                if (expectedInstanceOf == null) {
                    String msg = "mapping expects and instance of " + expectedNodeTypeName
                            + " for attribute " + targetXPath
                            + " but the attribute descriptor was not found";
                    throw new DataSourceException(msg);
                }
            } else {
                expectedInstanceOf = null;
            }

            AttributeMapping attMapping = new AttributeMapping(idExpression, sourceExpression,
                    targetXPath, expectedInstanceOf, isMultiValued, clientProperties);
            attMappings.add(attMapping);
        }
        return attMappings;
    }

    private Expression parseOgcCqlExpression(String sourceExpr) throws DataSourceException {
        Expression expression = Expression.NIL;
        if (sourceExpr != null && sourceExpr.trim().length() > 0) {
            try {
                expression = FilterBuilder.parseExpression(sourceExpr);
            } catch (Exception e) {
                String msg = "parsing expression " + sourceExpr;
                ComplexDataStoreConfigurator.LOGGER.log(Level.SEVERE, msg, e);
                throw new DataSourceException(msg + ": " + e.getMessage(), e);
            }
        }
        return expression;
    }

    private Map getClientProperties(org.geotools.data.complex.config.AttributeMapping dto)
            throws DataSourceException {

        if (dto.getClientProperties().size() == 0) {
            return Collections.EMPTY_MAP;
        }

        Map clientProperties = new HashMap();
        for (Iterator it = dto.getClientProperties().entrySet().iterator(); it.hasNext();) {
            Map.Entry entry = (Map.Entry) it.next();
            String name = (String) entry.getKey();
            TypeName qName = deglose(name);
            String cqlExpression = (String) entry.getValue();
            Expression expression = parseOgcCqlExpression(cqlExpression);
            clientProperties.put(qName, expression);
        }
        return clientProperties;
    }

    private List getAttributeMappings(TypeMapping dto) throws IOException {
        List attMappingDtos = dto.getAttributeMappings();
        List attMappings = getAttributeMappings(attMappingDtos);
        return attMappings;
    }

    private FeatureSource2 getFeatureSource(TypeMapping dto) throws IOException {
        String dsId = dto.getSourceDataStore();
        String typeName = dto.getSourceTypeName();

        DataAccess sourceDataStore = (DataAccess) sourceDataStores.get(dsId);
        if (sourceDataStore == null) {
            throw new DataSourceException("datastore " + dsId + " not found for type mapping "
                    + dto);
        }

        ComplexDataStoreConfigurator.LOGGER.fine("asking datastore " + sourceDataStore + " for source type " + typeName);
        TypeName name = deglose(typeName);
        FeatureSource2 fSource = (FeatureSource2) sourceDataStore.access(name);
        ComplexDataStoreConfigurator.LOGGER.fine("found feature source for " + typeName);
        return fSource;
    }

    /**
     * Parses the target xml schema files and stores the generated types and
     * AttributeDescriptors in {@link #registry}.
     * 
     * <p>
     * The list of file names to parse is obtained from
     * config.getTargetSchemasUris(). If a file name contained in that list is a
     * relative path (i.e., does not starts with file: or http:,
     * config.getBaseSchemasUrl() is used to resolve relative paths against.
     * </p>
     * 
     * @throws IOException
     */
    private void parseGmlSchemas() throws IOException {
        ComplexDataStoreConfigurator.LOGGER.finer("about to parse target schemas");

        final URL baseUrl;
        if (config.getBaseSchemasUrl() == null) {
            baseUrl = null;
        } else {
            baseUrl = new URL(config.getBaseSchemasUrl());
        }

        final List schemaFiles = config.getTargetSchemasUris();

        EmfAppSchemaReader schemaParser;
        schemaParser = EmfAppSchemaReader.newInstance();

        for (Iterator it = schemaFiles.iterator(); it.hasNext();) {
            String schemaLocation = (String) it.next();
            final URL schemaUrl = guessSchemaUrl(baseUrl, schemaLocation);
            ComplexDataStoreConfigurator.LOGGER.fine("parsing schema " + schemaUrl.toExternalForm());

            schemaParser.parse(schemaUrl);
        }

        registry = schemaParser.getTypeRegistry();
    }

    private URL guessSchemaUrl(final URL baseUrl, String schemaLocation)
            throws MalformedURLException {
        final URL schemaUrl;
        if (schemaLocation.startsWith("file:") || schemaLocation.startsWith("http:")) {
            ComplexDataStoreConfigurator.LOGGER.fine("using schema location as absolute path: " + schemaLocation);
            schemaUrl = new URL(schemaLocation);
        } else {
            if (baseUrl == null) {
                schemaUrl = new URL(schemaLocation);
                ComplexDataStoreConfigurator.LOGGER.warning("base url not provided, may be unable to locate" + schemaLocation
                        + ". Path resolved to: " + schemaUrl.toExternalForm());
            } else {
                ComplexDataStoreConfigurator.LOGGER.fine("using schema location " + schemaLocation + " as relative to "
                        + baseUrl);
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
     * @throws DataSourceException
     *             DOCUMENT ME!
     */
    private Map/* <String, FeatureAccess> */aquireSourceDatastores() throws IOException {
        ComplexDataStoreConfigurator.LOGGER.entering(getClass().getName(), "aquireSourceDatastores");

        final Map datastores = new HashMap();
        final List dsParams = config.getSourceDataStores();
        String id;

        for (Iterator it = dsParams.iterator(); it.hasNext();) {
            SourceDataStore dsconfig = (SourceDataStore) it.next();
            id = dsconfig.getId();

            Map datastoreParams = dsconfig.getParams();

            ComplexDataStoreConfigurator.LOGGER.fine("looking for datastore " + id);

            DataAccess dataStore = DataAccessFinder.createAccess((Object) datastoreParams);

            if (!(dataStore instanceof FeatureAccess)) {
                throw new DataSourceException("Cannot find a DataAccess for parameters "
                        + datastoreParams);
            }

            ComplexDataStoreConfigurator.LOGGER.fine("got datastore " + dataStore);
            datastores.put(id, dataStore);
        }

        return datastores;
    }

    /**
     * Takes a prefixed attribute name and returns an {@link AttributeName} by
     * looking which namespace belongs the prefix to in
     * {@link ComplexDataStoreDTO#getNamespaces()}.
     * 
     * @param prefixedName
     * @return
     * @throws IllegalArgumentException
     *             if <code>prefixedName</code> has no prefix.
     */
    private TypeName deglose(String prefixedName) throws IllegalArgumentException {
        TypeName name = null;

        if (prefixedName == null) {
            return null;
        }

        int prefixIdx = prefixedName.indexOf(':');
        if (prefixIdx == -1) {
            return new org.geotools.feature.type.TypeName(prefixedName);
            // throw new IllegalArgumentException(prefixedName + " is not
            // prefixed");
        }

        Map namespaces = config.getNamespaces();

        String nsPrefix = prefixedName.substring(0, prefixIdx);
        String localName = prefixedName.substring(prefixIdx + 1);
        String nsUri = (String) namespaces.get(nsPrefix);

        name = new org.geotools.feature.type.TypeName(nsUri, localName);

        return name;
    }

}
