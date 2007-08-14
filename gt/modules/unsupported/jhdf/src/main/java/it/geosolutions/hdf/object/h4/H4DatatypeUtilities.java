package it.geosolutions.hdf.object.h4;

import java.awt.image.DataBuffer;

import ncsa.hdf.hdflib.HDFConstants;

/**
 * Utility abstract class for retrieving datatype information and building
 * properly typed and properly sized data array where to load data values.
 * 
 * @author Daniele Romagnoli
 */
public class H4DatatypeUtilities {
	
	//TODO: Should I change the datatype from int to this class?
	private H4DatatypeUtilities(){
		
	}

	/**
	 * Builds a properly typed and properly sized array to store a specific
	 * amount of data, given the type of data and its size.
	 * 
	 * @param datatype
	 *            the datatype of the data which will be stored in the array.
	 * @param size
	 *            the size of the required array
	 * @return the allocated array
	 */
	public static Object allocateArray(final int datatype, final int size) {
		if (size <= 0)
			return null;

		// //
		// 
		// Allocating a buffer of the required type and size.
		// 
		// //
		Object data = null;

		switch (datatype) {

		// Byte array
		case HDFConstants.DFNT_CHAR:
		case HDFConstants.DFNT_UCHAR8:
		case HDFConstants.DFNT_UINT8:
		case HDFConstants.DFNT_INT8:
			data = new byte[size];
			break;

		// short array
		case HDFConstants.DFNT_INT16:
		case HDFConstants.DFNT_UINT16:
			data = new short[size];
			break;

		// int array
		case HDFConstants.DFNT_INT32:
		case HDFConstants.DFNT_UINT32:
			data = new int[size];
			break;

		// long array
		case HDFConstants.DFNT_INT64:
		case HDFConstants.DFNT_UINT64:
			data = new long[size];
			break;

		// float array
		case HDFConstants.DFNT_FLOAT32:
			data = new float[size];
			break;

		// double array
		case HDFConstants.DFNT_FLOAT64:
			data = new double[size];
			break;

		// unrecognized datatype!!
		default:
			data = null;
			break;
		}

		return data;
	}

	/**
	 * Returns the size (in bytes) of a given datatype
	 * 
	 * @param datatype
	 *            the input datatype
	 * @return the size (in bytes) of a given datatype
	 */
	public static int getDataTypeSize(final int datatype) {
		switch (datatype) {
		case HDFConstants.DFNT_CHAR:
		case HDFConstants.DFNT_UCHAR8:
		case HDFConstants.DFNT_INT8:
		case HDFConstants.DFNT_UINT8:
			return 1;
		case HDFConstants.DFNT_INT16:
		case HDFConstants.DFNT_UINT16:
			return 2;
		case HDFConstants.DFNT_INT32:
		case HDFConstants.DFNT_UINT32:
		case HDFConstants.DFNT_FLOAT32:
			return 4;
		case HDFConstants.DFNT_INT64:
		case HDFConstants.DFNT_UINT64:
		case HDFConstants.DFNT_FLOAT64:
			return 8;
		default:
			return 0;
		}
	}

	/**
	 * Returns <code>true</code> if the provided datatype is unsigned;
	 * <code>false</code> otherwise.
	 * 
	 * @param datatype
	 *            the given datatype
	 * @return <code>true</code> if the provided datatype is unsigned;
	 *         <code>false</code> otherwise.
	 */
	public static final boolean isUnsigned(final int datatype) {
		boolean unsigned = false;
		switch (datatype) {
		case HDFConstants.DFNT_UCHAR8:
		case HDFConstants.DFNT_UINT8:
		case HDFConstants.DFNT_UINT16:
		case HDFConstants.DFNT_UINT32:
		case HDFConstants.DFNT_UINT64:
			unsigned = true;
			break;
		}
		return unsigned;
	}

	/**
	 * Given a HDF datatype, returns a proper databuffer type depending on the
	 * datatype properties.
	 * 
	 * @param datatype
	 *            the input datatype
	 * @return the proper databuffer type
	 */
	public static int getBufferTypeFromDataType(final int datatype) {
		int bufferType = DataBuffer.TYPE_UNDEFINED;

		switch (datatype) {
		case HDFConstants.DFNT_INT8:
		case HDFConstants.DFNT_UINT8:
		case HDFConstants.DFNT_CHAR8:
		case HDFConstants.DFNT_UCHAR:
			bufferType = DataBuffer.TYPE_BYTE;
			break;
		case HDFConstants.DFNT_INT16:
			bufferType = DataBuffer.TYPE_SHORT;
			break;
		case HDFConstants.DFNT_UINT16:
			bufferType = DataBuffer.TYPE_USHORT;
			break;
		case HDFConstants.DFNT_INT32:
		case HDFConstants.DFNT_UINT32:
			bufferType = DataBuffer.TYPE_INT;
			break;
		case HDFConstants.DFNT_FLOAT32:
			bufferType = DataBuffer.TYPE_FLOAT;
			break;
		case HDFConstants.DFNT_FLOAT64:
			bufferType = DataBuffer.TYPE_DOUBLE;
			break;
		// TODO: Handle more cases??

		}
		return bufferType;
	}
}
