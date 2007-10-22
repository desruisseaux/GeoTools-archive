package it.geosolutions.imageio.plugins.jhdf;


public class SubDatasetInfo {
		private String name;
		private int rank;
		private int [] dims;
		private int [] chunkSize;
		private int datatype;
		public SubDatasetInfo(final String name, final int rank, int[] subDatasetDims, int[] subDatasetChunkSize, final int datatype) {
			if (subDatasetDims.length!=rank)
				throw new RuntimeException("Wrong SubDatasetInfo initialization. subDatasetDims length != rank");
			this.name=name;
			this.dims = subDatasetDims;
			this.rank = rank; 
			this.datatype = datatype;
			this.chunkSize = subDatasetChunkSize;
		}
		public int[] getChunkSize() {
			return chunkSize;
		}
		public void setChunkSize(int[] chunkSize) {
			this.chunkSize = chunkSize;
		}
		public int[] getDims() {
			return dims;
		}
		public void setDims(int[] dims) {
			this.dims = dims;
		}
		public final String getName() {
			return name;
		}
		public void setName(final String name) {
			this.name = name;
		}
		public final int getRank() {
			return rank;
		}
		public void setRank(final int rank) {
			this.rank = rank;
		}
		public int getDatatype() {
			return datatype;
		}
		public void setDatatype(final int datatype) {
			this.datatype = datatype;
		}
		public int getWidth() {
			return (int)dims[rank-1];
		}
		public int getHeight() {
			return (int)dims[rank-2];
		}
}
