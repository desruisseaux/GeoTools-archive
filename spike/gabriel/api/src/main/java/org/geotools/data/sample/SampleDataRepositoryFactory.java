package org.geotools.data.sample;

import java.awt.RenderingHints.Key;
import java.io.IOException;
import java.util.Map;

import javax.swing.Icon;

import org.geotools.data.FeatureData;
import org.geotools.data.FeatureDataFactory;
import org.opengis.feature.Feature;
import org.opengis.feature.type.FeatureType;

public class SampleDataRepositoryFactory implements FeatureDataFactory<FeatureType, Feature> {

    public FeatureData<FeatureType, Feature> createNewFeatureData(Map params) throws IOException {
        return new SampleDataRepository();
    }

    public FeatureData<FeatureType, Feature> createFeatureData(Map params) throws IOException {
        return new SampleDataRepository();
    }

    public boolean canProcess(Map params) {
        return false;
    }
	public boolean canCreateNew(Map params) {
		return false;
	}
    public String getDescription() {
        return null;
    }

    public String getDisplayName() {
        return null;
    }
	public Icon getIcon() {
		return null;
	}
    public org.geotools.data.FeatureDataFactory.Param[] getParametersInfo() {
        return null;
    }

    public boolean isAvailable() {
        return false;
    }

    public Map<Key, ?> getImplementationHints() {
        return null;
    }
}
