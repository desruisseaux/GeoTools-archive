package it.geosolutions.imageio.plugins.jhdf;

import it.geosolutions.hdf.object.h4.H4Attribute;
import it.geosolutions.hdf.object.h4.H4DatatypeUtilities;
import ncsa.hdf.hdflib.HDFConstants;
import ncsa.hdf.hdflib.HDFException;

public class HDFUtilities {

	/**
	 * Given a HDF Attribute, builds a String containing comma separated values
	 * related to the attribute. Some Attribute may have a int array as value.
	 * 
	 * @param att
	 *            a HDF <code>Attribute</code>.
	 * @return the built <code>String</code>
	 * @throws HDFException
	 */

	public static String buildAttributeString(H4Attribute att)
			throws HDFException {
		final int datatype = att.getDatatype();
		Object buf = att.getValues();
		final StringBuffer sb = new StringBuffer();
		int i = 0;
		String attributeValue = "";

		if (datatype == HDFConstants.DFNT_FLOAT32
				|| datatype == HDFConstants.DFNT_FLOAT) {
			float[] ff = (float[]) buf;
			final int size = ff.length;
			for (i = 0; i < size - 1; i++) {
				sb.append(ff[i]).append(",");
			}
			sb.append(ff[i]);
		} else if (datatype == HDFConstants.DFNT_DOUBLE
				|| datatype == HDFConstants.DFNT_FLOAT64) {
			double[] dd = (double[]) buf;
			final int size = dd.length;
			for (i = 0; i < size - 1; i++) {
				sb.append(dd[i]).append(",");
			}
			sb.append(dd[i]);
		} else if (datatype == HDFConstants.DFNT_INT8
				|| datatype == HDFConstants.DFNT_UINT8) {
			byte[] bb = (byte[]) buf;
			final int size = bb.length;
			for (i = 0; i < size - 1; i++) {
				sb.append(bb[i]).append(",");
			}
			sb.append(bb[i]);
		} else if (datatype == HDFConstants.DFNT_INT16
				|| datatype == HDFConstants.DFNT_UINT16) {
			short[] ss = (short[]) buf;
			final int size = ss.length;
			for (i = 0; i < size - 1; i++) {
				sb.append(ss[i]).append(",");
			}
			sb.append(ss[size]);
		} else if (datatype == HDFConstants.DFNT_INT32
				|| datatype == HDFConstants.DFNT_UINT32) {
			int[] ii = (int[]) buf;
			final int size = ii.length;
			for (i = 0; i < size - 1; i++) {
				sb.append(ii[i]).append(",");
			}
			sb.append(ii[i]);
		} else if (datatype == HDFConstants.DFNT_CHAR
				|| datatype == HDFConstants.DFNT_UCHAR8) {
			byte[] bb = (byte[]) buf;
			final int size = bb.length;
			for (i = 0; i < size && bb[i] != 0; i++) {
				sb.append(new String(bb, i, 1));
			}

		}
		attributeValue = sb.toString();
		return attributeValue;
	}
}
