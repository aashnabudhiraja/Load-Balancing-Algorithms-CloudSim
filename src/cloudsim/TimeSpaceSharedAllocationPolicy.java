/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation
 *               of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009, The University of Melbourne, Australia
 */


package cloudsim;

import gridsim.PE;
import gridsim.PEList;

import java.util.HashMap;


/**
 * TimeSpaceSharedAllocationPolicy is a VMM allocation policy that
 * allocates one or more PE to a VM, and doesn't allow sharing
 * of PEs. If there is no free PEs to the VM, allocation fails
 * However, if there is free PEs, they are scheduled to the VMs
 * 
 * @author Rodrigo N. Calheiros
 * @since CloudSim Toolkit 1.0 Beta
 * @invariant $none
 */
public class TimeSpaceSharedAllocationPolicy extends VMMAllocationPolicy {

	protected HashMap<String,Integer> peAllocationMap;
	protected int pesInUse;

	/**
	 * Creates a new TimeSpaceSharedAllocationPolicy
	 * @param pelist list of PEs managed by the AllocationPolicy
	 * @pre pelist != $null
	 * @post $none
	 */
	public TimeSpaceSharedAllocationPolicy(PEList pelist) {
		super(pelist);

		this.pesInUse=0;
		this.peAllocationMap = new HashMap<String,Integer>();
		
	}

	/**
	 * Allocates PEs for a VM
	 * @param vmcharacteristics VM description
	 * @return $true if this policy allows a new VM in the host, $false otherwise
	 * @pre $none
	 * @post $none
	 */
	@Override
	public synchronized boolean allocatePEsForVM(VMCharacteristics vmcharacteristics){
		
		//if there is no freePEs, fails
		if(vmcharacteristics.getCpus()>pelist.size()) return false;
		peAllocationMap.put(vmcharacteristics.getUserId()+"-"+vmcharacteristics.getVmId(),vmcharacteristics.getCpus());
		pesInUse+=vmcharacteristics.getCpus();
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
	public void deallocatePEsForVM(int vmId, int userId){
		int pes = peAllocationMap.remove(userId+"-"+vmId);
		pesInUse-=pes;
	}

	/**
	 * Returns the MIPS share of each PE that is available to a given VM
	 * @param vmId ID of the VM
	 * @param userId ID of VM's owner
	 * @return an array containing the amount of MIPS of each pe that is available to the VM
	 * @pre $none
	 * @post $none
	 */
	@Override
	public double[] getMIPSShare(int vmId, int userId) {
			
			double[] myShare = new double[pelist.size()];
			for(int i=0;i<myShare.length;i++) myShare[i]=0.0;
			
			double capacity=0.0;
			for (int i=0;i<pelist.size();i++){
				capacity+=((PE)pelist.get(i)).getMIPSRating();
			}
			
			//it receives the capacity of the allocated VMs and the capacity of the free PEs.
			if(pesInUse>pelist.size()){
				capacity/=pesInUse;
			} else {
				capacity/=pelist.size();
			}

			int pes = peAllocationMap.get(userId+"-"+vmId);

			for(int i=0;i<pes;i++){
				myShare[i]=capacity;
			}
			
			return myShare;		
	}
}
