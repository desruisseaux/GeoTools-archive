package org.geotools.data.wms.request;

public interface GetCapabilitiesRequest extends Request {
    public static String SECTION_ALL = "/";
    public static String SECTION_SERVICE = "/OGC_CAPABILITIES/ServiceMetadata";
    public static String SECTION_OPERATIONS = "/OGC_CAPABILITIES/OperationSignatures";
    public static String SECTION_CONTENT = "/OGC_CAPABILITIES/ContentMetadata";
    public static String SECTION_COMMON = "/OGC_CAPABILITIES/Common";
}
