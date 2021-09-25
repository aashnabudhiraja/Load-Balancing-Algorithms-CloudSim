/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation
 *               of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009, The University of Melbourne, Australia
 */

package cloudsim;

import gridsim.PEList;

/**
 * VMMAllocationPolicy is an abstract class that represents the
 * policy used by a VMM to share processing power among VMs running
 * in a host.
 * 
 * @author Rodrigo N. Calheiros
 * @since CloudSim Toolkit 1.0 Beta
 * @invariant $none
 */
public abstract class VMMAllocationPolicy {
	
	PEList pelist;

	/**
	 * Creates a new HostAllocationPolicy
	 * @param pelist list of PEs managed by the AllocationPolicy
	 * @pre pelist != $null
	 * @post $none
	 */
	public VMMAllocationPolicy(PEList pelist) {
		this.pelist = pelist;
	}
	
	/**
	 * Returns the MIPS share of each PE that is available to a given VM
	 * @param vmId ID of the VM
	 * @param userId ID of VM's owner
	 * @return an array containing the amount of MIPS of each pe that is available to the VM
	 * @pre $none
	 * @post $none
	 */
	public abstract double[] getMIPSShare(int vmId, int userId);
	
	/**
	 * Allocates PEs for a VM
	 * @param vmcharacteristics VM description
	 * @return $true if this policy allows a new VM in the host, $false otherwise
	 * @pre $none
	 * @post $none
	 */
	public abstract boolean allocatePEsForVM(VMCharacteristics vmcharacteristics);
	
	/**
	 * Releases PEs allocated to a VM
	 * @param vmId ID of the VM
	 * @param userId ID of the VM's owner
	 * @pre $none
	 * @post $none
	 */
	public abstract void deallocatePEsForVM(int vmId, int userId);	

}
