/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation
 *               of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009, The University of Melbourne, Australia
 */


package cloudsim;

import gridsim.MachineList;


/**
 * VMProvisioner is an abstract class that represents the provisioning
 * policy of hosts to virtual machines in a Datacentre. It supports two-stage
 * commit of reservation of hosts: first, we reserve the host and, once
 * commited by the user, it is effectivelly allocated to he/she
 * 
 * @author Rodrigo N. Calheiros
 * @since CloudSim Toolkit 1.0 Beta
 * @invariant $none
 */
public abstract class VMProvisioner {
	
	protected MachineList resources;
	
	/**
	 * Allocates a new VMProvisioner object
	 * @pre $none
	 * @post $none
	 *
	 */
	public VMProvisioner() {
		
	}
	
	/**
	 * Initializes the values of the fields. This method must be invoked
	 * before starting the actual simulation.
	 * 
	 * @param list Machines available in this Datacentre
	 * @pre bw>=0
	 * @post $none 
	 */
	public void init(MachineList list){
		this.resources = list;
	}
	
	/**
	 * Returns the resources associated to this VMProvisioner
	 * @return list of machines managed by the VMProvisioner
	 * @pre $none
	 * @post $none
	 */
	protected MachineList getResources(){
		return this.resources;
	}
	
	/**
	 * Allocates a host for a given VM. The host to be allocated is the one
	 * that was already reserved.
	 * @param vm virtual machine which the host is reserved to
	 * @return $true if the host could be allocated; $false otherwise
	 * @pre $none
	 * @post $none
	 */
	public abstract boolean allocateHostForVM(VMCharacteristics vm);
	
	/**
	 * Releases the host used by a VM
	 * @param vmID ID form the vm that is releasing the host
	 * @pre $none
	 * @post $none
	 */
	public abstract void deallocateHostForVM(int vmID, int userID);
		
	/**
	 * Trigger a migration from a given virtual machine to a selected
	 * host
	 * @param vmID ID from the virtual machine that will migrate
	 * @param newHostID ID from the host that will receive the VM
	 * @return $true if the migration succeeds; $false otherwise
	 * @pre $none
	 * @post $none
	 */
	public abstract boolean migrateVM(int vmID, int userID, int newHostID);
	
	/**
	 * Get the host that is executing the given VM belonging to the
	 * given user 
	 * 
	 * @param vmID ID from the virtual machine that will migrate
	 * @param userID ID from the owner of the VM
	 * @return the Host with the given vmID and userID; $null if not found
	 * @pre $none
	 * @post $none
	 */
	public abstract Host getHost(int vmID, int userID);

}
