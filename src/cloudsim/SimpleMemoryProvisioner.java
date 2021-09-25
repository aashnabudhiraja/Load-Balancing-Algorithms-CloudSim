/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation
 *               of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009, The University of Melbourne, Australia
 */

package cloudsim;


import java.util.HashMap;


/**
 * SimpleMemoryProvisioner is an extension of MemoryProvisioner
 * which uses a best-effort policy to allocate memory to a VM
 * 
 * @author Rodrigo N. Calheiros
 * @since CloudSim Toolkit 1.0 Beta
 * @invariant $none
 */
public class SimpleMemoryProvisioner extends MemoryProvisioner {
	
	protected HashMap<String, Integer> memoryTable;
	
	/**
	 * Creates the SimpleMemoryProvisioner object
	 *
	 */
	public SimpleMemoryProvisioner() {
		super();
		this.memoryTable=new HashMap<String, Integer>();
	}

	/**
	 * Allocates memory for a given VM. Creates a table to store the vm and
	 * its mount of memory.
	 * @param vm virtual machine for which the memory are being allocated
	 * @return $true if the memory could be allocated; $false otherwise
	 * @pre $none
	 * @post $none
	 */
	@Override
	public synchronized boolean allocateMemoryForVM(VMCharacteristics vm){
		
		if(this.availableMemory>=vm.getMemory()){
			availableMemory-=vm.getMemory();
			memoryTable.put(vm.getVmId()+"-"+vm.getUserId(), vm.getMemory());
			return true;
		}
		return false;
	}
	
	/**
	 * Releases memory used by a VM
	 * @param vmID ID form the vm that is releasing memory
	 * @pre $none
	 * @post none
	 */
	public void deallocateMemoryForVM(int vmID, int userID){
		int amountFreed = memoryTable.remove(vmID+"-"+userID);
		availableMemory+=amountFreed;
	}

}
