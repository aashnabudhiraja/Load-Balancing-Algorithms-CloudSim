package cloudsim.network;

import java.util.Iterator;

import cloudsim.network.TopologicalGraph;
import cloudsim.network.TopologicalLink;

/**
 * this class represents an delay-topology
 * storing every distance between connected nodes
 * 
 * @author Thomas Hohnstein
 *
 */
public class DelayMatrix_Float {

	/**
	 * matrix holding delay information between any two nodes
	 */
	protected float[][] mDelayMatrix=null;
	
	/**
	 * number of nodes in the distance-aware-topology
	 */
	protected int mTotalNodeNum=0;
	

	
	/**
	 * private constructor to ensure that only an correct initialized delay-matrix could be created
	 */
	private DelayMatrix_Float(){};
	
	/**
	 * this constructor creates an correct initialized Float-Delay-Matrix
	 * @param graph the topological graph as source-information
	 * @param directed true if an directed matrix should be computed, false otherwise
	 */
	public DelayMatrix_Float(TopologicalGraph graph, boolean directed){
		
		//lets preinitialize the Delay-Matrix
		this.createDelayMatrix(graph, directed);
		
		// now its time to calculate all possible connection-delays
		this.calculateShortestPath();
	}
	
	/**
	 * @param srcID the id of the source-node
	 * @param destID the id of the destination-node
	 * @return the delay-count between the given two nodes
	 */
	public float getDelay(int srcID, int destID){
		//check the nodeIDs against internal array-boundarys
		if(srcID > this.mTotalNodeNum || destID > this.mTotalNodeNum){
			throw new ArrayIndexOutOfBoundsException("srcID or destID is higher than highest stored node-ID!");
		}
		
		return this.mDelayMatrix[srcID][destID];
	}
	
	/**
	 * creates all internal necessary network-distance structures from the given graph
	 * for similarity we assume all kommunikation-distances are symmetrical
	 * thus leads to an undirected network
	 *
	 * @param graph this graph contains all node and link information
	 * @param directed defines to preinitialize an directed or undirected Delay-Matrix!
	 */
	private void createDelayMatrix(TopologicalGraph graph, boolean directed){

		//number of nodes inside the network 
		mTotalNodeNum = graph.getNumberOfNodes();
		
		mDelayMatrix = new float[mTotalNodeNum][mTotalNodeNum];
		
		//cleanup the complete distance-matrix with "0"s
		for(int row=0; row < mTotalNodeNum; ++row){
			for(int col=0; col < mTotalNodeNum; ++col){
				this.mDelayMatrix[row][col] = Float.MAX_VALUE;
			}
		}
		
		Iterator<TopologicalLink> itr = graph.getLinkIterator();
		
		TopologicalLink edge;
		while(itr.hasNext()){
			edge = itr.next();
			
			this.mDelayMatrix[edge.getSrcNodeID()][edge.getDestNodeID()] = edge.getLinkDelay();
			
			if(!directed){
				//according to aproximity of symmetry to all kommunication-paths
				this.mDelayMatrix[edge.getDestNodeID()][edge.getSrcNodeID()] = edge.getLinkDelay();
			}
			
		}
	}
	

	/**
	 * just calculates all pairs shortest paths
	 */
	private void calculateShortestPath(){
		FloydWarshall_Float floyd = new FloydWarshall_Float();
		
		floyd.initialize(mTotalNodeNum);
		mDelayMatrix = floyd.allPairsShortestPaths(mDelayMatrix);
	}
	
	/**
	 * this method just creates an string-output from the internal structures...
	 * eg. printsout the delay-matrix...
	 * 
	 */
	public String toString(){
		StringBuffer buffer = new StringBuffer();
		
		buffer.append("just an simple printout of the distance-aware-topology-class\n");
		buffer.append("delay-matrix is:\n");
		
		for(int column = 0; column < mTotalNodeNum; ++column){
			buffer.append("\t"+column);
		}

		for(int row = 0; row < mTotalNodeNum; ++row){
			buffer.append("\n"+row);
			
			for(int col = 0; col < mTotalNodeNum; ++col){
				if(this.mDelayMatrix[row][col] == Float.MAX_VALUE){
					buffer.append("\t"+"-");
				}else{
					buffer.append("\t"+this.mDelayMatrix[row][col]);
				}
			}
		}
		
		return buffer.toString();
	}
}
