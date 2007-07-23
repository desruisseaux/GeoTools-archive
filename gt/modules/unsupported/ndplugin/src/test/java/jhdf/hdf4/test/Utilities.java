package jhdf.hdf4.test;

import ncsa.hdf.hdflib.HDFConstants;

public abstract class Utilities {

	public static void printBuffer(Object buf, final int dataType) {
		if (dataType == HDFConstants.DFNT_FLOAT32
				|| dataType == HDFConstants.DFNT_FLOAT) {
			float[] ff = (float[]) buf;
			final int size = ff.length;
			for (int i = 0; i < size; i++) {
				System.out.print(ff[i]);
				System.out.print(" ");
			}
		} else if (dataType == HDFConstants.DFNT_DOUBLE
				|| dataType == HDFConstants.DFNT_FLOAT64) {
			double[] dd = (double[]) buf;
			final int size = dd.length;
			for (int i = 0; i < size; i++) {
				System.out.print(dd[i]);
				System.out.print(" ");
			}
		} else if (dataType == HDFConstants.DFNT_INT16) {
			short[] ss = (short[]) buf;
			final int size = ss.length;
			for (int i = 0; i < size; i++) {
				System.out.print(ss[i]);
				System.out.print(" ");
			}
		} else if (dataType == HDFConstants.DFNT_INT32) {
			int[] ii = (int[]) buf;
			final int size = ii.length;
			for (int i = 0; i < size; i++) {
				System.out.print(ii[i]);
				System.out.print(" ");
			}
		}
		System.out.println("");
	}
}
