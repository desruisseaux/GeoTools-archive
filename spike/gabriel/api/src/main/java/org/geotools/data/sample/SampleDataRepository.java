package org.geotools.data.sample;

import java.io.IOException;
import java.util.List;

import org.geotools.data.FeatureData;
import org.geotools.data.Query;
import org.geotools.data.Reader;
import org.geotools.data.Source;
import org.geotools.data.Transaction;
import org.geotools.data.Writer;
import org.geotools.feature.SchemaException;
import org.opengis.feature.Feature;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.Name;
import org.opengis.filter.Filter;

public class SampleDataRepository implements FeatureData<FeatureType, Feature> {

    public void createSchema(FeatureType featureType) throws IOException {
    }

    public Reader<FeatureType, Feature> getFeatureReader(Query query, Transaction transaction)
            throws IOException {
        return null;
    }

    public Source<FeatureType, Feature> getFeatureSource(String typeName) throws IOException {
        return null;
    }

    public Writer<FeatureType, Feature> getFeatureWriter(String typeName, Filter filter,
            Transaction transaction) throws IOException {
        return null;
    }

    public Writer<FeatureType, Feature> getFeatureWriter(String typeName, Transaction transaction)
            throws IOException {
        return null;
    }

    public Writer<FeatureType, Feature> getFeatureWriterAppend(String typeName,
            Transaction transaction) throws IOException {
        return null;
    }

    public List<Name> getNames() throws IOException {
        return null;
    }

    public FeatureType getSchema(String typeName) throws IOException {
        return null;
    }

    public FeatureType getSchema(Name name) throws IOException {
        return null;
    }

    public Source<FeatureType, Feature> getView(Query query) throws IOException, SchemaException {
        return null;
    }

    public void updateSchema(String typeName, FeatureType featureType) throws IOException {
    }

}
