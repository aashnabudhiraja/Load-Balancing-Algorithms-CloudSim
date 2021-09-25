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
import java.util.Vector;

import gridsim.PE;
import gridsim.PEList;

/**
 * SpaceSharedAllocationPolicy is a VMM allocation policy that
 * allocates one or more PE to a VM, and doesn't allow sharing
 * of PEs. If there is no free PEs to the VM, allocation fails.
 * Free PEs are not allocated to VMs
 * 
 * @author Rodrigo N. Calheiros
 * @since CloudSim Toolkit 1.0 Beta
 * @invariant $none
 */
public class SpaceSharedAllocationPolicy extends VMMAllocationPolicy {
	
	protected HashMap<String,Vector<Integer>> peAllocationMap;

	/**
	 * Creates the SpaceSharedAllocationPolicy object
	 * @param pelist host PE list
	 * @pre pelist != null
	 * @post $none
	 */
	public SpaceSharedAllocationPolicy(PEList pelist) {
		super(pelist);

		this.peAllocationMap = new HashMap<String,Vector<Integer>>();
		
	}

	/**
	 * Allocates PEs for a VM, if there is enough free PEs.
	 * @param vmcharacteristics VM description
	 * @return $true if there were enough free PEs, $false otherwise
	 * @pre $none
	 * @post $none
	 */
	@Override
	public synchronized boolean allocatePEsForVM(VMCharacteristics vmcharacteristics) {
		
		//if there is no free PEs, fails
		if (pelist.getNumFreePE()<vmcharacteristics.getCpus()) return false;
		
		Vector<Integer> chosenPEs = new Vector<Integer>();
		
		for(int i=0;i<vmcharacteristics.getCpus();i++){
			int id = pelist.getFreePEID();
			pelist.setStatusPE(PE.BUSY,id);
			chosenPEs.add(id);
		}
		
		peAllocationMap.put(vmcharacteristics.getUserId()+"-"+vmcharacteristics.getVmId(),chosenPEs);
		
		return true;
	}

	/**
	 * Releases PEs allocated to a VM
	 * @param vmId ID of the VM
	 * @param userId ID of the VM's owner
	 * @pre $none
	 * @post $none
	 */
	@Override
	public void deallocatePEsForVM(int vmId, int userId) {
		
		Vector<Integer> peVector = peAllocationMap.remove(userId+"-"+vmId);
		while(!peVector.isEmpty()){
			Integer element = peVector.remove(0);
			pelist.setStatusPE(PE.FREE, element);
		}
	}

	/**
	 * Returns the MIPS share of each PE that is available to a given VM. For this policy,
	 * it will be all the capacity of each PE allocated to the VM.
	 * @param vmId ID of the VM
	 * @param userId ID of VM's owner
	 * @return an array containing the amount of MIPS of each pe that is available to the VM
	 * @pre $none
	 * @post $none
	 */
	@Override
	public double[] getMIPSShare(int vmId, int userId) {
			
			double[] myShare = new double[pelist.size()];
			for(int i=0;i<myShare.length;i++) myShare[i]=0;
			
			Vector<Integer> peVector = peAllocationMap.get(userId+"-"+vmId);
			int i=0;
			while(i<peVector.size()){//it receives only MIPS from the allocated PEs
				Integer element = peVector.get(i);
				myShare[element]=pelist.getMIPSRating(element);
				i++;
			}
			return myShare;		
	}
}
