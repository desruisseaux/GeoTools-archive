
package org.geotools.gce.imageio.asciigrid;

import java.util.Arrays;

import javax.imageio.ImageTypeSpecifier;
import javax.imageio.metadata.IIOMetadataFormat;
import javax.imageio.metadata.IIOMetadataFormatImpl;

/**
 * @author Daniele Romagnoli
 * @author Simone Giannecchini (Simboss)
 */

public class AsciiGridsImageMetadataFormat extends IIOMetadataFormatImpl {
    private static IIOMetadataFormat instance = null;

    protected AsciiGridsImageMetadataFormat() {
        super(AsciiGridsImageMetadata.nativeMetadataFormatName,
            IIOMetadataFormatImpl.CHILD_POLICY_ALL);

        // root -> FormatDescriptor
        addElement("FormatDescriptor",
            AsciiGridsImageMetadata.nativeMetadataFormatName, CHILD_POLICY_EMPTY);
        addAttribute("FormatDescriptor", "GRASS", DATATYPE_BOOLEAN, true, null);

        // root -> GridDescriptor
        addElement("GridDescriptor",
            AsciiGridsImageMetadata.nativeMetadataFormatName, CHILD_POLICY_EMPTY);
        addAttribute("GridDescriptor", "nColumns", DATATYPE_INTEGER, true, null);
        addAttribute("GridDescriptor", "nRows", DATATYPE_INTEGER, true, null);

        addAttribute("GridDescriptor", "rasterSpaceType", DATATYPE_STRING,
            true, null, Arrays.asList(AsciiGridsImageMetadata.rasterSpaceTypes));
        addAttribute("GridDescriptor", "noDataValue", DATATYPE_DOUBLE, false,
            null);

        // root -> EnvelopeDescriptor
        addElement("EnvelopeDescriptor",
            AsciiGridsImageMetadata.nativeMetadataFormatName, CHILD_POLICY_EMPTY);
        addAttribute("EnvelopeDescriptor", "cellsizeX", DATATYPE_DOUBLE, true,
            null);
        addAttribute("EnvelopeDescriptor", "cellsizeY", DATATYPE_DOUBLE, true,
                null);
        addAttribute("EnvelopeDescriptor", "xll", DATATYPE_DOUBLE, true, null);
        addAttribute("EnvelopeDescriptor", "yll", DATATYPE_DOUBLE, true, null);

    }

    public static synchronized IIOMetadataFormat getInstance() {
        if (instance == null) {
            instance = new AsciiGridsImageMetadataFormat();
        }

        return instance;
    }

    /**
     * @see javax.imageio.metadata.IIOMetadataFormatImpl#canNodeAppear(java.lang.String,
     *      javax.imageio.ImageTypeSpecifier)
     */
    public boolean canNodeAppear(String elementName,
        ImageTypeSpecifier imageType) {
    	
    	//@todo @task TODO
        return true;
    }
}
