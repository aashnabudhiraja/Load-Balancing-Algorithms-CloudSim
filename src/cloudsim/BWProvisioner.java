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
 * BWProvisioner is an abstract class that represents the provisioning
 * policy of bandwidth to virtual machines inside a Host. When extending
 * this class, care must be taken to guarantee that the field availableBw
 * will always contain the amount of free bandwidth available for future
 * allocations.
 * 
 * @author Rodrigo N. Calheiros
 * @since CloudSim Toolkit 1.0 Beta
 * @invariant $none
 */
public abstract class BWProvisioner {
	
	protected long bw;
	protected long availableBw;
	
	/**
	 * Creates the new BWProvisioner
	 * @pre $none
	 * @post $none
	 *
	 */
	public BWProvisioner(){
		this.bw=0;
		this.availableBw=0;
	}
	
	/**
	 * Initializes the values of the fields. This method must be invoked
	 * before starting the actual simulation.
	 * 
	 * @param bw overall amount of bandwidth available in the host.
	 * @pre bw>=0
	 * @post $none 
	 */
	public void init(long bw){
		
		this.bw = bw;
		this.availableBw = bw;
		
	}
	
	/**
	 * Gets the available bw in the host
	 * @return available bw
	 * @pre $none
	 * @post $none
	 */
	public long getAvailableBw(){
		return this.availableBw;
	}
		
	/**
	 * Allocates bw for a given VM
	 * @param vm virtual machine for which the bw are being allocated
	 * @return $true if the bw could be allocated; $false otherwise
	 * @pre $none
	 * @post $none
	 */
	public abstract boolean allocateBWforVM(VMCharacteristics vm);
	
	/**
	 * Releases bw used by a VM
	 * @param vmID ID form the vm that is releasing bw
	 * @pre $none
	 * @post none
	 */
	public abstract void deallocateBWForVM(int vmID, int userID);
}
