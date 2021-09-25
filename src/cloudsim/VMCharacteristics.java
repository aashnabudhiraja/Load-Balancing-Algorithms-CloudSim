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
 * VMCharacteristics stores the description of a VM
 *
 * @author       Rodrigo N. Calheiros
 * @since        CloudSim Toolkit 1.0 Beta
 * @invariant $none
 */
public class VMCharacteristics {
	
	int vmId;
	int userId;
	long size;
	int memory;
	int cpus;
	long bw;
	int priority;
	String vmm;
	VMScheduler scheduler;
	

	/**
	 * Creates a new VMCharacteristics object
	 * @param vmId unique ID of the VM
	 * @param userId ID of the VM's owner
	 * @param size amount of storage
	 * @param memory amount of memory
	 * @param bw amount of bandwidth
	 * @param cpus amount of CPUs
	 * @param vmm virtual machine monitor
	 * @param scheduler scheduler policy for gridlets
	 * @pre vmId >= 0
	 * @pre userId >= 0
	 * @pre size > 0
	 * @pre memory > 0
	 * @pre bw > 0
	 * @pre cpus > 0
	 * @pre priority >= 0
	 * @pre scheduler != null
	 * @post $none
	 */
	public VMCharacteristics(int vmId, int userId, long size, int memory, long bw, int cpus, int priority, String vmm, VMScheduler scheduler) {
		this.vmId = vmId;
		this.userId = userId;
		this.size = size;
		this.memory = memory;
		this.cpus=cpus;
		this.bw = bw;
		this.vmm = vmm;
		this.priority= priority;
		this.scheduler = scheduler;
	}

	/**
	 * Gets the amount of bandwidth
	 * @return amount of bandwidth
	 * @pre $none
	 * @post $none
	 */
	public long getBw() {
		return bw;
	}

	/**
	 * Sets the amount of bandwidth
	 * @param bw new amount of bandwidth
	 * @pre bw > 0
	 * @post $none
	 */
	public void setBw(long bw) {
		this.bw = bw;
	}
	
	/**
	 * Sets the ID of the VM 
	 * @return Vm ID
	 * @pre $none
	 * @post $none
	 */
	public int getVmId() {
		return vmId;
	}

	/**
	 * Gets the amount of memory
	 * @return amount of memory
	 * @pre $none
	 * @post $none
	 */
	public int getMemory() {
		return memory;
	}

	/**
	 * Sets the amount of memory
	 * @param memory new amount of memory
	 * @pre memory > 0
	 * @post $none
	 */
	public void setMemory(int memory) {
		this.memory = memory;
	}

	/**
	 * Gets the amount of storage
	 * @return amount of storage
	 * @pre $none
	 * @post $none
	 */
	public long getSize() {
		return size;
	}

	/**
	 * Sets the amount of storage
	 * @param size new amount of storage
	 * @pre size > 0
	 * @post $none
	 */
	public void setSize(long size) {
		this.size = size;
	}

	/**
	 * Gets the ID of the owner of the VM 
	 * @return VM's owner ID
	 * @pre $none
	 * @post $none
	 */
	public int getUserId() {
		return userId;
	}

	/**
	 * Gets the required number of CPUs
	 * @return number of CPUs
	 * @pre $none
	 * @post $none
	 */
	public int getCpus() {
		return cpus;
	}

	/**
	 * Sets the required number of CPUS
	 * @param cpus number of CPUs
	 * @pre cpus > 0
	 * @post $none
	 */
	public void setCpus(int cpus) {
		this.cpus = cpus;
	}
	
	/**
	 * Gets the VMM
	 * @return VMM
	 * @pre $none
	 * @post $none
	 */
	public String getVmm(){
		return vmm;
	}
	
	/**
	 * Returns a reference to the scheduler
	 * @return scheduler
	 * @pre $none
	 * @post $none
	 */
	public VMScheduler getVMScheduler(){
		return this.scheduler;
	}

	/**
	 * Returns the priority assigned to the VM.
	 * Effect of the priority in the scheduling
	 * is policy dependent
	 * @return VM priority
	 * @pre $none
	 * @post $none
	 */
	public int getPriority() {
		return this.priority;
	}

	/**
	 * Sets the priority assigned to the VM.
	 * Effect of the priority in the scheduling
	 * is policy dependent
	 * @param priority new priority assigned to the VM
	 * @pre priority >= 0
	 * @post $none
	 */
	public void setPriority(int priority) {
		this.priority = priority;		
	}
}
