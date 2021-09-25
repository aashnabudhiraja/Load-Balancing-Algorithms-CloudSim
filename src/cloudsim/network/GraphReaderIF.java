package cloudsim.network;

import java.io.IOException;

import cloudsim.network.TopologicalGraph;

/**
 * this interface abstracts an reader for different graph-file-formats
 * 
 * @author Thomas Hohnstein
 *
 */
public interface GraphReaderIF {
	
	/**
	 * this method just reads the file and creates an TopologicalGraph object
	 * 
	 * @param filename name of the file to read
	 * @return created TopologicalGraph
	 * @throws IOException 
	 */
	public TopologicalGraph readGraphFile(String filename) throws IOException;

}
