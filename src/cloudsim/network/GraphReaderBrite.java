package cloudsim.network;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.StringTokenizer;

import cloudsim.network.TopologicalLink;
import cloudsim.network.TopologicalGraph;
import cloudsim.network.TopologicalNode;
import cloudsim.network.GraphReaderIF;

/**
 * this class is just an file-reader for the special brite-format!
 * 
 * the brite-file is structured as followed:
 * Node-section:
 *		NodeID, xpos, ypos, indegree, outdegree, ASid, type(router/AS)
 *
 * Edge-section:
 *		EdgeID, fromNode, toNode, euclideanLength, linkDelay, linkBandwith, AS_from, AS_to, type
 * 
 * @author Thomas Hohnstein
 *
 */
public class GraphReaderBrite implements GraphReaderIF {
	
	private static final int ParseNothing = 0; 

	private static final int ParseNodes = 1; 
	private static final int ParseEdges = 2; 
	
	private int state = ParseNothing;
	
	private TopologicalGraph graph = null;
	

	/**
	 * this method just reads the file and creates an TopologicalGraph object
	 * 
	 * @param filename name of the file to read
	 * @return created TopologicalGraph
	 * @throws IOException 
	 */
	public TopologicalGraph readGraphFile(String filename) throws IOException{
		
		graph = new TopologicalGraph();
		
		//lets read the file
		FileReader fr = new FileReader(filename);
		BufferedReader br  = new BufferedReader(fr);
		
		String lineSep = System.getProperty("line.separator");
		String nextLine = null;
		StringBuffer sb = new StringBuffer();

		while ((nextLine = br.readLine()) != null) {
			sb.append(nextLine);
			//
			// note:
			//   BufferedReader strips the EOL character.
			//
			sb.append(lineSep);
			
			//functionality to diferentiate between all the parsing-states
			//state that should just find the start of node-declaration
			if(state == ParseNothing){
				if(nextLine.contains("Nodes:")){
					//System.out.println("found start of Nodes... switch to parse nodes!");
					state = ParseNodes;
				}
			}
			
			//the state to retrieve all node-information
			else if(state == ParseNodes){
				//perform the parsing of this node-line
				parseNodeString(nextLine);
			}
			
			//the state to retrieve all edges-information
			else if(state == ParseEdges){
				parseEdgesString(nextLine);
			}
			
			
			
		}
		
		//System.out.println("read file successfully...");
		//System.out.println(sb.toString());

		
		return graph;
	}
	
	private void parseNodeString(String nodeLine){
		
		StringTokenizer tokenizer = new StringTokenizer(nodeLine);
		
		//number of node parameters to parse (counts at linestart)
		int parameters = 3;
		
		//first test to step to the next parsing-state (edges)
		if(nodeLine.contains("Edges:")){
			//System.out.println("found start of Edges... switch to parse edges!");
			state = ParseEdges;
			
			return;
		}
		
		//test against an empty line
		if(!tokenizer.hasMoreElements()){
			//System.out.println("this line contains no tokens...");
			return;
		}
				
		//parse this string-line to read all node-parameters
		//NodeID, xpos, ypos, indegree, outdegree, ASid, type(router/AS)
		
		int nodeID = 0;
		String nodeLabel = "";
		int xPos = 0;
		int yPos = 0;
		
		for(int actualParam = 0; tokenizer.hasMoreElements() && actualParam < parameters; actualParam++){
			String token = tokenizer.nextToken();
			switch(actualParam){
				case 0:	//System.out.println("nodeID: "+token);
						//System.out.println("nodeLabel: "+token);
						nodeID = new Integer(token).intValue();
						nodeLabel = Integer.toString(nodeID);
						break;

				case 1:	//System.out.println("x-Pos: "+token);
						xPos = new Integer(token).intValue();
						break;

				case 2:	//System.out.println("y-Pos: "+token);
						yPos = new Integer(token).intValue();
						break;
			}
		}

		//instanciate an new node-object with previous parsed parameters
		TopologicalNode topoNode = new TopologicalNode(nodeID, nodeLabel, xPos, yPos);
		graph.addNode(topoNode);
		

	}//parseNodeString-END
	
	
	private void parseEdgesString(String nodeLine){
		StringTokenizer tokenizer = new StringTokenizer(nodeLine);
		
		//number of node parameters to parse (counts at linestart)
		int parameters = 6;
		
		//test against an empty line
		if(!tokenizer.hasMoreElements()){
			//System.out.println("this line contains no tokens...");
			return;
		}
		
		//parse this string-line to read all node-parameters
		//EdgeID, fromNode, toNode, euclideanLength, linkDelay, linkBandwith, AS_from, AS_to, type
		
		//int edgeID = 0;
		int fromNode = 0;
		int toNode = 0;
		//float euclideanLength = 0;
		float linkDelay = 0;
		int linkBandwith = 0;
		
		for(int actualParam = 0; tokenizer.hasMoreElements() && actualParam < parameters; actualParam++){
			String token = tokenizer.nextToken();
			switch(actualParam){
				case 0:	//System.out.println("edgeID: "+token);
						//edgeID = new Integer(token).intValue();
						break;

				case 1:	//System.out.println("fromNode: "+token);
						fromNode = new Integer(token).intValue();
						break;

				case 2:	//System.out.println("toNode: "+token);
						toNode = new Integer(token).intValue();
						break;

				case 3:	//System.out.println("euclideanLength: "+token);
						//euclideanLength = new Float(token).floatValue();
						break;

				case 4:	//System.out.println("linkDelay: "+token);
						linkDelay = new Float(token).floatValue();
						break;

				case 5:	//System.out.println("linkBandwith: "+token);
						linkBandwith = new Float(token).intValue();
						break;
			}//switch-END
		}//for-END
		
		graph.addLink(new TopologicalLink(fromNode, toNode, linkDelay, linkBandwith));
		
	}
	

}
