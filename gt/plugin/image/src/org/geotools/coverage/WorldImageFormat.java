package src.org.geotools.coverage;

import java.io.File;
import java.net.URL;

import org.geotools.data.coverage.grid.AbstractGridFormat;
import org.geotools.data.coverage.grid.GridCoverageReader;
import org.geotools.data.coverage.grid.GridCoverageWriter;

/**
 * @author rgould
 *
 * A Format to allow discovery of Readers/Writers for raster images
 * that support world files containing information about the image.
 * 
 * Supports .gif+.gfw, .jpg/.jpeg+.jgw, .tif/.tiff+.tfw and .png+.pgw.
 * .wld may be used in place of the format specific extension (.jpg+.wld, etc)
 * 
 * Designed to be used with GridCoverageExchange. 
 */
public class WorldImageFormat extends AbstractGridFormat {

	/* (non-Javadoc)
	 * @see org.geotools.data.coverage.grid.Format#getReader(java.lang.Object)
	 */
	/**
	 * Call the accepts() method before asking for a reader to determine 
	 * if the current object is supported.
	 * 
	 * @param source The source object to read a WorldImage from
	 * @return a new WorldImageReader for the source
	 */
	public GridCoverageReader getReader(Object source) {
		return new WorldImageReader(source);
	}

	/* (non-Javadoc)
	 * @see org.geotools.data.coverage.grid.Format#getWriter(java.lang.Object)
	 */
	/**
	 * Call the accepts() method before asking for a writer to determine
	 * if the current object is supported.
	 * 
	 * @param destination the destination object to write a WorldImage to
	 * @return a new WorldImageWriter for the destination
	 */
	public GridCoverageWriter getWriter(Object destination) {
		return new WorldImageWriter(destination);
	}

	/* (non-Javadoc)
	 * @see org.geotools.data.coverage.grid.Format#accepts(java.lang.Object)
	 */
	/**
	 * Takes the input and determines if it is a class that we can understand
	 * and then futher checks the format of the class to make sure we can 
	 * read/write to it.
	 * 
	 * @param input The object to check for acceptance.
	 * @return true if the input is acceptable, false otherwise
	 */
	public boolean accepts(Object input) {
		String pathname = "";
		
		if (input instanceof URL) {
			URL url = (URL) input;
			pathname = url.getFile();
		}
		if (input instanceof File) {
			File file = (File) input;
			pathname = file.getName();
		}
		
		if (pathname.endsWith(".gif")  ||
			pathname.endsWith(".jpg")  ||
			pathname.endsWith(".jpeg") ||
			pathname.endsWith(".tif")  ||
			pathname.endsWith(".tiff") ||
			pathname.endsWith(".png")  ) {
			
			return true;
		}
		return false;
	}

	/**
	 * Takes an image file extension (such as .gif, including the '.') and
	 * returns it's corresponding world file extension (such as .gfw).
	 * 
	 * @param fileExtension an image file extension, including the '.'
	 * @return a corresponding world file extension, including the '.'
	 */
	public static String getWorldExtension(String fileExtension) {
		if (fileExtension == null)
			return null;
		if (fileExtension.equals(".png"))
			return ".pgw";
		if (fileExtension.equals(".gif"))
			return ".gfw";
		if (fileExtension.equals(".jpg") || fileExtension.equals(".jpeg")) 
			return ".jgw";
		if (fileExtension.equals(".tif") || fileExtension.equals(".tiff")) 
			return ".tfw";
		return null;
	}

}
