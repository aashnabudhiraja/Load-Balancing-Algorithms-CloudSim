package cloudsim.network;

/**
 * just represents an topological network node
 * retrieves its information from an topological-generated file
 * (eg. topology-generator)
 * 
 * @author Thomas Hohnstein
 *
 */
public class TopologicalNode {
	
	/**
	 * its the nodes-ID inside this network
	 */
	private int nodeID=0;
	
	/**
	 * describes the nodes-name inside the network
	 */
	private String nodeName = null;
	
	/**
	 * representing the x an y world-coordinates
	 */
	private int worldX = 0;
	private int worldY = 0;
	
	/**
	 * constructs an new node
	 */
	public TopologicalNode(int nodeID){
		//lets initialize all private class attributes
		this.nodeID = nodeID;
		this.nodeName = new Integer(nodeID).toString();
	}

	/**
	 * constructs an new node including world-coordinates
	 */
	public TopologicalNode(int nodeID, int x, int y){
		//lets initialize all private class attributes
		this.nodeID = nodeID;
		this.nodeName = new Integer(nodeID).toString();
		this.worldX = x;
		this.worldY = y;
	}

	/**
	 * constructs an new node including world-coordinates and the nodeName
	 */
	public TopologicalNode(int nodeID, String nodeName, int x, int y){
		//lets initialize all private class attributes
		this.nodeID = nodeID;
		this.nodeName = nodeName;
		this.worldX = x;
		this.worldY = y;
	}

	/**
	 * delivers the nodes id
	 * @return just the nodeID
	 */
	public int getNodeID(){
		return nodeID;
	}
	
	/**
	 * delivers the name of the node
	 * @return name of the node
	 */
	public String getNodeLabel(){
		return this.nodeName;
	}
	
	/**
	 * returns the x coordinate of this network-node
	 * @return the x coordinate
	 */
	public int getCoordinateX(){
		return worldX;
	}
	
	/**
	 * returns the y coordinate of this network-node
	 * @return the y coordinate
	 */
	public int getCoordinateY(){
		return worldY;
	}

}
