/*
 * Title: CloudSim Toolkit Description: CloudSim (Cloud Simulation) Toolkit for
 * Modeling and Simulation of Clouds Licence: GPL -
 * http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009, The University of Melbourne, Australia
 * 
 * 
 * 
 * package cloudsim;
 * 
 * import java.util.LinkedList;
 * 
 * import gridsim.Gridlet; import gridsim.datagrid.DataGridlet;
 * 
 *//**
	 * Cloudlet is an extension to the gridlet. It stores, despite all the
	 * information encapsulated in the Gridlet, the ID of the VM running it.
	 * 
	 * @author Rodrigo N. Calheiros
	 * @since CloudSim Toolkit 1.0 Beta
	 * @invariant $none
	 *
	 */
/*
 * public class Cloudlet extends DataGridlet {
 * 
 * //////////////////////////////////////////// // Below are CONSTANTS
 * attributes
 *//** The Cloudlet has been created and added to the GridletList object */
/*
 * public static final int CREATED = Gridlet.CREATED;
 * 
 *//** The Cloudlet has been assigned to a GridResource object as planned */
/*
 * public static final int READY = Gridlet.READY;
 * 
 *//** The Cloudlet has moved to a Grid node */
/*
 * public static final int QUEUED = Gridlet.QUEUED;
 * 
 *//** The Cloudlet is in execution in a Grid node */
/*
 * public static final int INEXEC = Gridlet.INEXEC;
 * 
 *//** The Cloudlet has been executed successfully */
/*
 * public static final int SUCCESS = Gridlet.SUCCESS;
 * 
 *//** The Cloudlet is failed */
/*
 * public static final int FAILED = Gridlet.FAILED;
 * 
 *//** The Cloudlet has been canceled. */
/*
 * public static final int CANCELED = Gridlet.CANCELED;
 * 
 *//**
	 * The Cloudlet has been paused. It can be resumed by changing the status into
	 * <tt>RESUMED</tt>.
	 */
/*
 * public static final int PAUSED = Gridlet.PAUSED;
 * 
 *//** The Cloudlet has been resumed from <tt>PAUSED</tt> state. */
/*
 * public static final int RESUMED = Gridlet.RESUMED;
 * 
 *//** The Cloudlet has failed due to a resource failure */
/*
 * public static final int FAILED_RESOURCE_UNAVAILABLE =
 * Gridlet.FAILED_RESOURCE_UNAVAILABLE;
 * 
 * protected int vmId; protected double costPerBw; protected double
 * accumulatedBwCost;
 * 
 *//**
	 * Allocates a new Cloudlet object. The Cloudlet length, input and output file
	 * sizes should be greater than or equal to 1.
	 *
	 * @param cloudletID         the unique ID of this cloudlet
	 * @param cloudletLength     the length or size (in MI) of this cloudlet to be
	 *                           executed in a Datacenter
	 * @param cloudletFileSize   the file size (in byte) of this cloudlet
	 *                           <tt>BEFORE</tt> submitting to a Datacenter
	 * @param cloudletOutputSize the file size (in byte) of this cloudlet
	 *                           <tt>AFTER</tt> finish executing by a Datacenter
	 * @param record             record the history of this object or not
	 * @param fileList           list of files required by this cloudlet
	 * @pre cloudletID >= 0
	 * @pre cloudletLength >= 0.0
	 * @pre cloudletFileSize >= 1
	 * @pre cloudletOutputSize >= 1
	 * @post $none
	 */
/*
 * public Cloudlet(int cloudletID, double cloudletLength, long cloudletFileSize,
 * long cloudletOutputSize, boolean record, LinkedList fileList) {
 * super(cloudletID, cloudletLength, cloudletFileSize, cloudletOutputSize,
 * record, fileList); this.vmId=-1; this.accumulatedBwCost=0.0;
 * this.costPerBw=0.0; }
 * 
 *//**
	 * Allocates a new Cloudlet object. The Cloudlet length, input and output file
	 * sizes should be greater than or equal to 1. By default this constructor sets
	 * the history of this object.
	 *
	 * @param cloudletID         the unique ID of this Cloudlet
	 * @param cloudletLength     the length or size (in MI) of this cloudlet to be
	 *                           executed in a Datacenter
	 * @param cloudletFileSize   the file size (in byte) of this cloudlet
	 *                           <tt>BEFORE</tt> submitting to a Datacenter
	 * @param cloudletOutputSize the file size (in byte) of this cloudlet
	 *                           <tt>AFTER</tt> finish executing by a Datacenter
	 * @param fileList           list of files required by this cloudlet
	 * @pre cloudletID >= 0
	 * @pre cloudletLength >= 0.0
	 * @pre cloudletFileSize >= 1
	 * @pre cloudletOutputSize >= 1
	 * @post $none
	 */
/*
 * public Cloudlet(int cloudletID, double cloudletLength, long cloudletFileSize,
 * long cloudletOutputSize, LinkedList fileList) { super(cloudletID,
 * cloudletLength, cloudletFileSize, cloudletOutputSize, fileList);
 * this.vmId=-1; this.accumulatedBwCost=0.0; this.costPerBw=0.0; }
 * 
 *//**
	 * Allocates a new Cloudlet object. The Cloudlet length, input and output file
	 * sizes should be greater than or equal to 1.
	 *
	 * @param cloudletID         the unique ID of this cloudlet
	 * @param cloudletLength     the length or size (in MI) of this cloudlet to be
	 *                           executed in a Datacenter
	 * @param cloudletFileSize   the file size (in byte) of this cloudlet
	 *                           <tt>BEFORE</tt> submitting to a Datacenter
	 * @param cloudletOutputSize the file size (in byte) of this cloudlet
	 *                           <tt>AFTER</tt> finish executing by a Datacenter
	 * @param record             record the history of this object or not
	 * @pre cloudletID >= 0
	 * @pre cloudletLength >= 0.0
	 * @pre cloudletFileSize >= 1
	 * @pre cloudletOutputSize >= 1
	 * @post $none
	 */
/*
 * public Cloudlet(int cloudletID, double cloudletLength, long cloudletFileSize,
 * long cloudletOutputSize, boolean record) { super(cloudletID, cloudletLength,
 * cloudletFileSize, cloudletOutputSize, record, new LinkedList());
 * this.vmId=-1; this.accumulatedBwCost=0.0; this.costPerBw=0.0; }
 * 
 *//**
	 * Allocates a new Cloudlet object. The Cloudlet length, input and output file
	 * sizes should be greater than or equal to 1. By default this constructor sets
	 * the history of this object.
	 *
	 * @param cloudletID         the unique ID of this Cloudlet
	 * @param cloudletLength     the length or size (in MI) of this cloudlet to be
	 *                           executed in a Datacenter
	 * @param cloudletFileSize   the file size (in byte) of this cloudlet
	 *                           <tt>BEFORE</tt> submitting to a Datacenter
	 * @param cloudletOutputSize the file size (in byte) of this cloudlet
	 *                           <tt>AFTER</tt> finish executing by a Datacenter
	 * @pre cloudletID >= 0
	 * @pre cloudletLength >= 0.0
	 * @pre cloudletFileSize >= 1
	 * @pre cloudletOutputSize >= 1
	 * @post $none
	 */
/*
 * public Cloudlet(int cloudletID, double cloudletLength, long cloudletFileSize,
 * long cloudletOutputSize) { super(cloudletID, cloudletLength,
 * cloudletFileSize, cloudletOutputSize, new LinkedList()); this.vmId=-1;
 * this.accumulatedBwCost=0.0; this.costPerBw=0.0; }
 * 
 *//**
	 * Get the status of the Cloudlet.
	 * 
	 * @return status of the Cloudlet
	 * @pre $none
	 * @post $none
	 */
/*
 * public int getStatus(){ return super.getGridletStatus(); }
 * 
 *//**
	 * Gets the ID of this Cloudlet
	 * 
	 * @return Cloudlet Id
	 * @pre $none
	 * @post $none
	 */
/*
 * public int getCloudletId() { return this.getGridletID(); }
 * 
 *//**
	 * Gets the ID of the VM that will run this Cloudlet
	 * 
	 * @return VM Id, -1 if the Cloudlet was not assigned to a VM
	 * @pre $none
	 * @post $none
	 */
/*
 * public int getVmId() { return vmId; }
 * 
 *//**
	 * Sets the ID of the VM that will run this Cloudlet
	 * 
	 * @param vmId the ID of the VM
	 * @pre id >= 0
	 * @post $none
	 */
/*
 * public void setVmId(int vmId) { this.vmId = vmId; }
 * 
 *//**
	 * Sets the resource parameters for which this Cloudlet is going to be executed.
	 * <br>
	 * NOTE: This method <tt>should</tt> be called only by a resource entity, not
	 * the user or owner of this Cloudlet.
	 * 
	 * @param resourceID the GridResource ID
	 * @param costPerCPU the cost running this Cloudlet per second
	 * @param costPerBw  the cost of data transfer to this Datacenter
	 * @pre resourceID >= 0
	 * @pre cost > 0.0
	 * @post $none
	 */
/*
 * public void setResourceParameter(int resourceID, double costPerCPU, double
 * costPerBw) {
 * 
 * super.setResourceParameter(resourceID, costPerCPU);
 * 
 * this.costPerBw=costPerBw; this.accumulatedBwCost =
 * costPerBw*super.getGridletFileSize();
 * 
 * }
 * 
 *//**
	 * Gets the total cost of processing or executing this Cloudlet
	 * <tt>Processing Cost = input data transfer + processing cost + output transfer cost</tt>
	 * 
	 * @return the total cost of processing Cloudlet
	 * @pre $none
	 * @post $result >= 0.0
	 *//*
		 * public double getProcessingCost() {
		 * 
		 * //cloudlet cost: execution cost... double cost = super.getProcessingCost();
		 * 
		 * //...plus input data transfer cost... cost+=this.accumulatedBwCost;
		 * 
		 * //...plus output cost cost+=this.costPerBw*super.getGridletOutputSize();
		 * 
		 * return cost; }
		 * 
		 * }
		 * 
		 */


/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation
 *               of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009, The University of Melbourne, Australia
 */


package cloudsim;

import java.util.LinkedList;

import gridsim.Gridlet;
import gridsim.datagrid.DataGridlet;

/**
 * Cloudlet is an extension to the gridlet. It stores, despite all the information
 * encapsulated in the Gridlet, the ID of the VM running it.
 * 
 * @author 		Rodrigo N. Calheiros
 * @since       CloudSim Toolkit 1.0 Beta
 * @invariant 	$none
 *
 */
public class Cloudlet extends DataGridlet {
	
    ////////////////////////////////////////////
    // Below are CONSTANTS attributes
    /** The Cloudlet has been created and added to the GridletList object */
    public static final int CREATED = Gridlet.CREATED;

    /** The Cloudlet has been assigned to a GridResource object as planned */
    public static final int READY = Gridlet.READY;

    /** The Cloudlet has moved to a Grid node */
    public static final int QUEUED = Gridlet.QUEUED;

    /** The Cloudlet is in execution in a Grid node */
    public static final int INEXEC = Gridlet.INEXEC;

    /** The Cloudlet has been executed successfully */
    public static final int SUCCESS = Gridlet.SUCCESS;

    /** The Cloudlet is failed */
    public static final int FAILED = Gridlet.FAILED;

    /** The Cloudlet has been canceled.  */
    public static final int CANCELED = Gridlet.CANCELED;

    /** The Cloudlet has been paused. It can be resumed by changing the status
     * into <tt>RESUMED</tt>.
     */
    public static final int PAUSED = Gridlet.PAUSED;

    /** The Cloudlet has been resumed from <tt>PAUSED</tt> state. */
    public static final int RESUMED = Gridlet.RESUMED;

    /** The Cloudlet has failed due to a resource failure */
    public static final int FAILED_RESOURCE_UNAVAILABLE = Gridlet.FAILED_RESOURCE_UNAVAILABLE;
	
	protected int vmId;
	protected double costPerBw;
	protected double accumulatedBwCost;
	protected double cloudletLength;
	
    /**
     * Allocates a new Cloudlet object. The Cloudlet length, input and output
     * file sizes should be greater than or equal to 1.
     *
     * @param cloudletID            the unique ID of this cloudlet
     * @param cloudletLength        the length or size (in MI) of this cloudlet
     *                             to be executed in a Datacenter
     * @param cloudletFileSize      the file size (in byte) of this cloudlet
     *                             <tt>BEFORE</tt> submitting to a Datacenter
     * @param cloudletOutputSize    the file size (in byte) of this cloudlet
     *                             <tt>AFTER</tt> finish executing by
     *                             a Datacenter
     * @param record               record the history of this object or not
     * @param fileList				   list of files required by this cloudlet
     * @pre cloudletID >= 0
     * @pre cloudletLength >= 0.0
     * @pre cloudletFileSize >= 1
     * @pre cloudletOutputSize >= 1
     * @post $none
     */
	public Cloudlet(int cloudletID, double cloudletLength, long cloudletFileSize, long cloudletOutputSize, boolean record, LinkedList fileList) {
		super(cloudletID, cloudletLength, cloudletFileSize, cloudletOutputSize, record, fileList);
		this.vmId=-1;
		this.accumulatedBwCost=0.0;
		this.costPerBw=0.0;
		this.cloudletLength = cloudletLength;
	}

    /**
     * Allocates a new Cloudlet object. The Cloudlet length, input and output
     * file sizes should be greater than or equal to 1.
     * By default this constructor sets the history of this object.
     *
     * @param cloudletID            the unique ID of this Cloudlet
     * @param cloudletLength        the length or size (in MI) of this cloudlet
     *                             to be executed in a Datacenter
     * @param cloudletFileSize      the file size (in byte) of this cloudlet
     *                             <tt>BEFORE</tt> submitting to a Datacenter
     * @param cloudletOutputSize    the file size (in byte) of this cloudlet
     *                             <tt>AFTER</tt> finish executing by
     *                             a Datacenter
     * @param fileList				   list of files required by this cloudlet
     * @pre cloudletID >= 0
     * @pre cloudletLength >= 0.0
     * @pre cloudletFileSize >= 1
     * @pre cloudletOutputSize >= 1
     * @post $none
     */
	public Cloudlet(int cloudletID, double cloudletLength, long cloudletFileSize, long cloudletOutputSize, LinkedList fileList) {
		super(cloudletID, cloudletLength, cloudletFileSize, cloudletOutputSize, fileList);
		this.vmId=-1;
		this.accumulatedBwCost=0.0;
		this.costPerBw=0.0;
		this.cloudletLength = cloudletLength;
	}
	
    /**
     * Allocates a new Cloudlet object. The Cloudlet length, input and output
     * file sizes should be greater than or equal to 1.
     *
     * @param cloudletID            the unique ID of this cloudlet
     * @param cloudletLength        the length or size (in MI) of this cloudlet
     *                             to be executed in a Datacenter
     * @param cloudletFileSize      the file size (in byte) of this cloudlet
     *                             <tt>BEFORE</tt> submitting to a Datacenter
     * @param cloudletOutputSize    the file size (in byte) of this cloudlet
     *                             <tt>AFTER</tt> finish executing by
     *                             a Datacenter
     * @param record               record the history of this object or not
     * @pre cloudletID >= 0
     * @pre cloudletLength >= 0.0
     * @pre cloudletFileSize >= 1
     * @pre cloudletOutputSize >= 1
     * @post $none
     */
	public Cloudlet(int cloudletID, double cloudletLength, long cloudletFileSize, long cloudletOutputSize, boolean record) {
		super(cloudletID, cloudletLength, cloudletFileSize, cloudletOutputSize, record, new LinkedList());
		this.vmId=-1;
		this.accumulatedBwCost=0.0;
		this.costPerBw=0.0;
		this.cloudletLength = cloudletLength;
	}

    /**
     * Allocates a new Cloudlet object. The Cloudlet length, input and output
     * file sizes should be greater than or equal to 1.
     * By default this constructor sets the history of this object.
     *
     * @param cloudletID            the unique ID of this Cloudlet
     * @param cloudletLength        the length or size (in MI) of this cloudlet
     *                             to be executed in a Datacenter
     * @param cloudletFileSize      the file size (in byte) of this cloudlet
     *                             <tt>BEFORE</tt> submitting to a Datacenter
     * @param cloudletOutputSize    the file size (in byte) of this cloudlet
     *                             <tt>AFTER</tt> finish executing by
     *                             a Datacenter
     * @pre cloudletID >= 0
     * @pre cloudletLength >= 0.0
     * @pre cloudletFileSize >= 1
     * @pre cloudletOutputSize >= 1
     * @post $none
     */
	public Cloudlet(int cloudletID, double cloudletLength, long cloudletFileSize, long cloudletOutputSize) {
		super(cloudletID, cloudletLength, cloudletFileSize, cloudletOutputSize, new LinkedList());
		this.vmId=-1;
		this.accumulatedBwCost=0.0;
		this.costPerBw=0.0;
		this.cloudletLength = cloudletLength;
	}
	
	/**
	 * Get the status of the Cloudlet.
	 * @return status of the Cloudlet
	 * @pre $none
	 * @post $none
	 */
	public int getStatus(){
		return super.getGridletStatus();
	}
	
	/**
	 * Gets the ID of this Cloudlet
	 * @return Cloudlet Id
	 * @pre $none
	 * @post $none
	 */
	public int getCloudletId() {
		return this.getGridletID();
	}
	
	/**
	 * Gets the ID of the VM that will run this Cloudlet
	 * @return VM Id, -1 if the Cloudlet was not assigned to a VM
	 * @pre $none
	 * @post $none
	 */
	public int getVmId() {
		return vmId;
	}

	/**
	 * Sets the ID of the VM that will run this Cloudlet
	 * @param vmId the ID of the VM
	 * @pre id >= 0
	 * @post $none
	 */
	public void setVmId(int vmId) {
		this.vmId = vmId;
	}
		
	   /**
     * Sets the resource parameters for which this Cloudlet is going to be
     * executed. <br>
     * NOTE: This method <tt>should</tt> be called only by a resource entity,
     * not the user or owner of this Cloudlet.
     * @param resourceID   the GridResource ID
     * @param costPerCPU   the cost running this Cloudlet per second
     * @param costPerBw	   the cost of data transfer to this Datacenter
     * @pre resourceID >= 0
     * @pre cost > 0.0
     * @post $none
     */
	public void setResourceParameter(int resourceID, double costPerCPU, double costPerBw) {
    
    	super.setResourceParameter(resourceID, costPerCPU);
    	
    	this.costPerBw=costPerBw;
   		this.accumulatedBwCost = costPerBw*super.getGridletFileSize();
    
    }
	
    /**
     * Gets the total cost of processing or executing this Cloudlet
     * <tt>Processing Cost = input data transfer + processing cost + output transfer cost</tt>
     * @return the total cost of processing Cloudlet
     * @pre $none
     * @post $result >= 0.0
     */
    public double getProcessingCost() {
    	
    	//cloudlet cost: execution cost...
    	double cost = super.getProcessingCost();
 
    	//...plus input data transfer cost...
    	cost+=this.accumulatedBwCost;
    	
    	//...plus output cost
    	cost+=this.costPerBw*super.getGridletOutputSize();
        
        return cost;
    }
    public double getCloudletLength(){
    	return this.cloudletLength;
    }
    
}
   
