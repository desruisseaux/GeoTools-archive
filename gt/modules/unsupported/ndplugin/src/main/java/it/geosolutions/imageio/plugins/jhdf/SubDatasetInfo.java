package it.geosolutions.imageio.plugins.jhdf;

public class SubDatasetInfo {
		private String name;
		private int rank;
		private long [] dims;
		private long [] chunkSize;
		public SubDatasetInfo(String name, int rank, long[] subDatasetDims, long[] subDatasetChunkSize) {
			this.name=name;
			this.rank=rank;
			this.dims = subDatasetDims;
			this.chunkSize = subDatasetChunkSize;
		}
		public long[] getChunkSize() {
			return chunkSize;
		}
		public void setChunkSize(long[] chunkSize) {
			this.chunkSize = chunkSize;
		}
		public long[] getDims() {
			return dims;
		}
		public void setDims(long[] dims) {
			this.dims = dims;
		}
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public int getRank() {
			return rank;
		}
		public void setRank(int rank) {
			this.rank = rank;
		}
}
