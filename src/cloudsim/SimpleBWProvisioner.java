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
 * SimpleBWProvisioner is a class that implements a simple best
 * effort allocation policy: if there is bw available to request, it allocates;
 * otherwise, it fails.
 * 
 * @author Rodrigo N. Calheiros
 * @since CloudSim Toolkit 1.0 Beta
 * @invariant $none
 */
public class SimpleBWProvisioner extends BWProvisioner {
	
	protected HashMap<String, Long> bwTable;
	
	/**
	 * Creates the SimpleBWProvisioner object
	 * @pre $none
	 * @post $none
	 */
	public SimpleBWProvisioner() {
		super();
		this.bwTable=new HashMap<String, Long>();
	}
	

	/**
	 * Allocates bw for a given VM
	 * @param vm virtual machine for which the bw are being allocated
	 * @return $true if the bw could be allocated; $false otherwise
	 * @pre $none
	 * @post $none
	 */
	@Override
	public synchronized boolean allocateBWforVM(VMCharacteristics vm) {
		if(availableBw>=vm.getBw()){
			availableBw-=vm.getBw();
			bwTable.put(vm.getVmId()+"-"+vm.getUserId(), vm.getBw());
			return true;		
		}
		
		return false;
	}
	
	/**
	 * Releases bw used by a VM
	 * @param vmID ID form the vm that is releasing bw
	 * @pre $none
	 * @post none
	 */
	@Override
	public void deallocateBWForVM(int vmID, int userID) {
		long amountFreed = bwTable.remove(vmID+"-"+userID);
		availableBw+=amountFreed;
	}

}
