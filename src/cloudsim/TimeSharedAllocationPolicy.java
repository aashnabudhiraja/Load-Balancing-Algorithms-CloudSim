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
 * TimeSharedAllocationPolicy is a VMM allocation policy that
 * allows sharing of PEs among virtual machines. It does not limit
 * the amount of VMs in the host
 * 
 * @author Rodrigo N. Calheiros
 * @since CloudSim Toolkit 1.0 Beta
 * @invariant $none
 */
public class TimeSharedAllocationPolicy extends VMMAllocationPolicy {

	protected HashMap<String,Integer> peAllocationMap;
	protected int pesInUse;

	/**
	 * Creates a new TimeSharedAllocationPolicy
	 * @param pelist list of PEs managed by the AllocationPolicy
	 * @pre pelist != $null
	 * @post $none
	 */
	public TimeSharedAllocationPolicy(PEList pelist) {
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
			
		//divides available MIPS among all VMs
			double[] myShare = new double[pelist.size()];
			for(int i=0;i<myShare.length;i++) myShare[i]=0;

			double capacity=0.0;
			for (int i=0;i<pelist.size();i++){
				capacity+=((PE)pelist.get(i)).getMIPSRating();
				if(i+1==pesInUse) break;
			}
			
			capacity/=pesInUse;

			int pes = this.peAllocationMap.get(userId+"-"+vmId);
			for(int i=0;i<pes;i++){
				myShare[i]=capacity;
			}

			return myShare;		
	}

}
