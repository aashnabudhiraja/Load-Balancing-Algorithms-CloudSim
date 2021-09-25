/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation
 *               of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009, The University of Melbourne, Australia
 */

package cloudsim;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;

import cloudsim.network.DelayMatrix_Float;
import cloudsim.network.GraphReaderBrite;
import cloudsim.network.TopologicalGraph;
import cloudsim.network.TopologicalLink;


/**
 * NetworkTopology is a class that implements network layer
 * in CloudSim. It reads a BRITE file and generates a
 * topological network from it. Information of this network is
 * used to simulate latency in network traffic of CloudSim.
 * <p>
 * The topology file may contain more nodes the the number of
 * entities in the simulation. It allows for users to increase
 * the scale of the simulation without changing the topology
 * file. Nevertheless, each CloudSim entity must be mapped to
 * one (and only one) BRITE node to allow proper work of the
 * network simulation. Each BRITE node can be mapped to only
 * one entity at a time.
 * 
 * @author Rodrigo N. Calheiros
 * @since CloudSim Toolkit 1.0
 * @invariant $none
 */
public class NetworkTopology {
	
	private static boolean networkEnabled = false;
	protected static DelayMatrix_Float delayMatrix = null;
	protected static double[][] bwMatrix = null;
	protected static HashMap<Integer,Integer> conversingMap = null;

	/**
	 * Creates the network topology if file exists and
	 * if file can be succesfully parsed. File is written
	 * in the BRITE format and contains topologycal
	 * information on simulation entities.
	 * @param fileName name of the BRITE file
	 * @pre fileName != null
	 * @post $none
	 */
	public static void buildNetworkTopology(String fileName) {
		
    	System.out.println("Topology file: "+fileName);
		
		//try to find the file
		GraphReaderBrite reader = new GraphReaderBrite();
		
		try{
			TopologicalGraph graph =  reader.readGraphFile(fileName);
			conversingMap = new HashMap<Integer,Integer>();
			
			//creates the delay matrix
			delayMatrix = new DelayMatrix_Float(graph, false);
			
			//creates the bw matrix
			bwMatrix = createBwMatrix(graph,false);
			
			networkEnabled=true;
		} catch(IOException e){
			//problem with the file. Does not simulate network
			System.out.println("Problem in processing BRITE file. Network simulation is disabled. Error: "+e.getMessage());
		}
			
	}
	
	/**
	 * Creates the matrix containiing the available bandiwdth beteen two nodes
	 * @param graph topological graph describing the topology
	 * @param directed true if the graph is directed; false otherwise
	 * @return the bandwidth graph
	 */
	private static double[][] createBwMatrix(TopologicalGraph graph, boolean directed) {
		
		int nodes = graph.getNumberOfNodes();
		
		double[][] mtx = new double[nodes][nodes];
			
		//cleanup matrix
		for(int i=0;i<nodes;i++){
			for(int j=0;j<nodes;j++){
				mtx[i][j] = 0.0;
			}
		}
		
		Iterator<TopologicalLink> iter = graph.getLinkIterator();
		while(iter.hasNext()){
			TopologicalLink edge = iter.next();
			
			mtx[edge.getSrcNodeID()][edge.getDestNodeID()] = edge.getLinkBw();
			
			if(!directed){
				mtx[edge.getDestNodeID()][edge.getSrcNodeID()] = edge.getLinkBw();
			}
		}
		
		return mtx;
	}

	/**
	 * Maps a CloudSim entity to a node in the network topology
	 * @param cloudSimEntityID ID of the entity being mapped
	 * @param briteID ID of the BRITE node that corresponds to the CloudSim entity
	 * @pre cloudSimEntityID >= 0
	 * @pre briteID >= 0
	 * @post $none
	 */
	public static void mapNode(int cloudSimEntityID, int briteID){
		
		if(networkEnabled){
			try{
				if(!conversingMap.containsKey(cloudSimEntityID)){ //this CloudSim entity was already mapped?
					if(!conversingMap.containsValue(briteID)){ //this BRITE node was already mapped?
						conversingMap.put(cloudSimEntityID,briteID);
					} else {
						System.out.println("Error in network mapping. BRITE node "+briteID+" already in use.");
					}
				} else {
					System.out.println("Error in network mapping. CloudSim entity "+cloudSimEntityID+" already mapped.");
				}
			} catch (Exception e){
				System.out.println("Error in network mapping. CloudSim node "+cloudSimEntityID+" not mapped to BRITE node "+briteID+".");
			}
		}
	}
	
	/**
	 * Unmaps a previously mapped CloudSim entity to a node in the network topology
	 * @param cloudSimEntityID ID of the entity being unmapped
	 * @pre cloudSimEntityID >= 0
	 * @post $none
	 */
	public static void unmapNode(int cloudSimEntityID){
		
		if(networkEnabled){
			try{
				conversingMap.remove(cloudSimEntityID);
			}  catch (Exception e){
				System.out.println("Error in network unmapping. CloudSim node: "+cloudSimEntityID);
			}
		}
	}
	
	/**
	 * Calculates the delay between two nodes
	 * @param srcID ID of the source node
	 * @param destID ID of the destination node
	 * @return  communication delay between the two nodes
	 * @pre srcID >= 0
	 * @pre destID >= 0
	 * @post $none
	 */
	public static double getDelay(int srcID, int destID){
		
		if(networkEnabled){
			try{
				//add the network latency
				double delay = delayMatrix.getDelay(conversingMap.get(srcID),conversingMap.get(destID));
								
				return delay;
			} catch (Exception e){
				//in case of error, just keep running and return 0.0
			}
		}		
		return 0.0;
	}
	
	/**
	 * This method returns true if network simulation is working. If there were some problem
	 * during creation of network (e.g., during parsing of BRITE file) that does not allow
	 * a proper simulation of the network, this method returns false.
	 * @return $true if network simulation is ok. $false otherwise
	 * @pre $none
	 * @post $none
	 */
	public static boolean isNetworkEnabled(){
		return networkEnabled;
	}

}
