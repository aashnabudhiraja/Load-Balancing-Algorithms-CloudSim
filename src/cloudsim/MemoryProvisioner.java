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
 * MemoryProvisioner is an abstract class that represents the provisioning
 * policy of memory to virtual machines inside a Host. When extending
 * this class, care must be taken to guarantee that the field availableMemory
 * will always contain the amount of free memory available for future
 * allocations.
 * 
 * @author Rodrigo N. Calheiros
 * @since CloudSim Toolkit 1.0 Beta
 * @invariant $none
 */
public abstract class MemoryProvisioner {
	
	protected int memory;
	protected int availableMemory;
	
	/**
	 * Creates the new MemoryProvisioner
	 * @pre $none
	 * @post $none
	 *
	 */
	public MemoryProvisioner(){
		this.memory=0;
		this.availableMemory=0;
	}

	/**
	 * Initializes the values of the fields. This method must be invoked
	 * before starting the actual simulation.
	 * 
	 * @param memory overall amount of memory available in the host.
	 * @pre memory>=0
	 * @post $none 
	 */
	public void init(int memory) {
		
		this.memory = memory;
		this.availableMemory = memory;
		
	}
	
	/**
	 * Gets the available memory in the host
	 * @return available memory
	 * @pre $none
	 * @post $none
	 */
	public int getAvailableMemory(){
		return this.availableMemory;
	}
	
	/**
	 * Allocates memory for a given VM
	 * @param vm virtual machine for which the memory are being allocated
	 * @return $true if the memory could be allocated; $false otherwise
	 * @pre $none
	 * @post $none
	 */
	public abstract boolean allocateMemoryForVM(VMCharacteristics vm);
	
	/**
	 * Releases memory used by a VM
	 * @param vmID ID form the vm that is releasing memory
	 * @pre $none
	 * @post none
	 */
	public abstract void deallocateMemoryForVM(int vmID, int userID);

}
