/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation
 *               of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009, The University of Melbourne, Australia
 */

package cloudsim;

import eduni.simjava.Sim_port;

import gridsim.GridSim;
import gridsim.GridSimCore;
import gridsim.IO_data;

/**
 * This class extends the GridSimCore to enable network simulation in
 * CloudSim. Also, it disables all the network models from GridSim, to
 * provide a simpler simulation of networking. In the network model
 * used by CloudSim, a topology file written in BRITE format is used to
 * describe the network. Later, nodes in such file are mapped to CloudSim
 * entities. Delay calculated from the BRITE model are added to the messages
 * send through CloudSim. Messages using the old model are converted to the
 * apropriate methods with the correct parameters. 
 * 
 * @author Rodrigo N. Calheiros
 * @since CloudSim Toolkit 1.0
 * @invariant $none
 */
public class CloudSim extends GridSimCore {
	
	/**
	 * Allocates a new CloudSim object
	 * @param name the name to be associated with this entity (as
     *                   required by Sim_entity class from simjava package)
	 * @throws Exception This happens when creating this entity before
     *                   initializing GridSim package or this entity name is
     *                   <tt>null</tt> or empty
	 */
	public CloudSim(String name) throws Exception {
		super(name);
	}
		  
	/**
	 * Sends a message without data to a given entity. If network simulation is enabled,
	 * the delay for message transfer is considered before message delivery.
	 * @param destID ID of the entity receiving the message
	 * @param delay time, from the current simulation time, to deliver the message
	 * 			Delay due to the latency is added to this time.
	 * @param gridSimTag tag of the message
	 * @pre destID >= 0
	 * @post $none
	 */
    protected void send(int destID, double delay, int gridSimTag) {
        
    	if (destID < 0) return;

        // if delay is -ve, then it doesn't make sense. So resets to 0.0
        if (delay < 0.0) delay = 0.0;
        
        int srcID = this.get_id();
        if(destID!=srcID){//does not delay self messages
        	delay+=getNetworkDelay(srcID,destID);
        }
        
        super.sim_schedule(destID, delay, gridSimTag);
    }
    
	/**
	 * Sends a message to a given entity. If network simulation is enabled,
	 * the delay for message transfer is considered before message delivery.
	 * @param destID ID of the entity receiving the message
	 * @param delay time, from the current simulation time, to deliver the message
	 * 			Delay due to the latency is added to this time.
	 * @param gridSimTag tag of the message
	 * @param data message data
	 * @pre destID >= 0
	 * @post $none
	 */
	protected void send(int destID, double delay, int gridSimTag, Object data) {
        
    	if (destID < 0) return;
        
        // if delay is -ve, then it doesn't make sense. So resets to 0.0
        if (delay < 0.0) delay = 0.0;
        
        int srcID = this.get_id();
        if(destID!=srcID){//does not delay self messages
        	delay+=getNetworkDelay(srcID,destID);
        }
        
        super.sim_schedule(destID, delay, gridSimTag, data);
    }
	
	/**
	 * Gets the network delay associated to the sent of a message from
	 * a given source to a given destination.
	 * @param src source of the message
	 * @param dst destination of the message
	 * @return delay to send a message from src to dst
	 * @pre src >= 0
	 * @pre dst >= 0
	 */
	private double getNetworkDelay(int src,int dst){
		
		if(NetworkTopology.isNetworkEnabled()) return NetworkTopology.getDelay(src,dst);
		return 0.0;
		
	}
	
	///////////////////////////////////////////////////////////////////////////////
	//deprecated methods, fixes here to increase compatibility with GridSim
	///////////////////////////////////////////////////////////////////////////////
    /*
     * @deprecated As in CloudSim entity ID should be used instead of entity name
     */ 
    protected void send(String entityName, double delay, int gridSimTag) {
        
    	if (entityName == null)  return;
        
        int destID = GridSim.getEntityId(entityName);
        if (destID < 0) {
            System.out.println(super.get_name() + ".send(): Error - invalid entity name \"" + entityName + "\".");
            return;
        }

        this.send(destID, delay, gridSimTag);
    }
    
    /*
     * @deprecated As in CloudSim entity ID should be used instead of entity name
     */ 
    protected void send(String entityName, double delay, int gridSimTag, Object data) {
        
    	if (entityName == null)  return;
        
        int destID = GridSim.getEntityId(entityName);
        if (destID < 0) {
            System.out.println(super.get_name() + ".send(): Error - invalid entity name \"" + entityName + "\".");
            return;
        }

        this.send(destID, delay, gridSimTag, data);
    }
    
    /*
     * @deprecated As in CloudSim Sim_ports are not used by CloudSim entities
     */ 
    protected void send(Sim_port destPort, double delay, int gridSimTag) {
    	    	
    	this.send(this.get_id(),delay,gridSimTag);
    }        
    
    /*
     * @deprecated As in CloudSim Sim_ports are not used by CloudSim entities
     */ 
    protected void send(Sim_port destPort, double delay, int gridSimTag, Object data) {
    	
    	int destID = ((IO_data) data).getDestID();
    	Object message = ((IO_data)data).getData();
    	
    	this.send(destID,delay,gridSimTag,message);
    }
}
