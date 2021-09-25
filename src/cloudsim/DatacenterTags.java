/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation
 *               of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009, The University of Melbourne, Australia
 */


package cloudsim;


/**
 * Contains constants used by other classes in this package.
 * @author Rodrigo N. Calheiros
 * @since  CloudSim Toolkit 1.0 Beta
 * @invariant $none
 *
 */
public class DatacenterTags {
	
	 //starting constant values for VM-related tags
    private static final int VMBASE = 1000;

    /**
     * Denotes a request to create a new VM in a Datacentre
     * With acknowledgement information sent by the Datacentre
     */
    public static final int VM_CREATE = VMBASE + 1;
    
    /**
     * Denotes a request to create a new VM in a Datacentre
     * With acknowledgement information sent by the Datacentre
     */
    public static final int VM_CREATE_ACK = VMBASE + 2;
    
    /**
     * Denotes a request to destroy a new VM in a Datacentre
     */
    public static final int VM_DESTROY = VMBASE + 3;

    /**
     * Denotes a request to destroy a new VM in a Datacentre
     */
    public static final int VM_DESTROY_ACK = VMBASE + 4;
    
    /**
     * Denotes a request to migrate a new VM in a Datacentre
     */
    public static final int VM_MIGRATE = VMBASE + 5;
    
    /**
     * Denotes a request to migrate a new VM in a Datacentre
     * With acknowledgement information sent by the Datacentre
     */
    public static final int VM_MIGRATE_ACK = VMBASE + 6;
    
 
    /**
     * Denotes an event to send a file from a user to a datacenter
     */
    public static final int VM_DATA_ADD = VMBASE + 7;
    
    /**
     * Denotes an event to send a file from a user to a datacenter
     */
    public static final int VM_DATA_ADD_ACK = VMBASE + 8;
    
    /**
     * Denotes an event to remove a file from a datacenter
     */
    public static final int VM_DATA_DEL = VMBASE + 9;
    
    /**
     * Denotes an event to remove a file from a datacenter
     */
    public static final int VM_DATA_DEL_ACK = VMBASE + 10;
    
    /**
     * Denotes an internal event generated in a Datacenter
     */
    public static final int VM_DATACENTER_EVENT = VMBASE + 21;
    
    /**
     * Denotes an internal event generated in a Broker
     */
    public static final int VM_BROKER_EVENT = VMBASE + 22;
        
	protected DatacenterTags(){
		
	}
	
}
