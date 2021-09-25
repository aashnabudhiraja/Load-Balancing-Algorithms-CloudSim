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
 * TimeSharedWithPriorityAllocationPolicy is a VMM allocation policy that
 * allows sharing of PEs among virtual machines. CPU Share of each VM can be
 * set through the priority field. The smaller value accepted for priority is
 * 1. Values lesser than that are set to 1.  Priority means how many times
 * one machine runs faster than the other. E.g.: if a VM A has priority
 * 1 and a VM B has a priority 2, B will run twice as faster as A.
 * 
 * @author Rodrigo N. Calheiros
 * @since CloudSim Toolkit 1.0 Beta
 * @invariant $none
 */
public class TimeSharedWithPriorityAllocationPolicy extends VMMAllocationPolicy {

	protected HashMap<String,PEShare> peAllocationMap;
	protected int pesInUse;
	protected int shares;

	/**
	 * Creates a new TimeSharedAllocationPolicy
	 * @param pelist list of PEs managed by the AllocationPolicy
	 * @pre pelist != $null
	 * @post $none
	 */
	public TimeSharedWithPriorityAllocationPolicy(PEList pelist) {
		super(pelist);

		this.pesInUse=0;
		this.shares=0;
		this.peAllocationMap = new HashMap<String,PEShare>();
		
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
		int priority = vmcharacteristics.getPriority();
		if(priority<1) priority=1;
		peAllocationMap.put(vmcharacteristics.getUserId()+"-"+vmcharacteristics.getVmId(),new PEShare(vmcharacteristics.getCpus(),priority));
		pesInUse+=vmcharacteristics.getCpus();	
		shares+=priority;
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
		PEShare peShare = peAllocationMap.remove(userId+"-"+vmId);
		pesInUse-=peShare.getPes();
		shares-=peShare.getShare();
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
			
		capacity/=shares;

		PEShare peShare = this.peAllocationMap.get(userId+"-"+vmId);
		int pes = peShare.getPes();
		int share = peShare.getShare();
		
		for(int i=0;i<pes;i++){
			myShare[i]=capacity*share/pes;
		}

		return myShare;		
	}
	
	/**
	 * Internal class to store the PEs and the share of each VM.
	 *
	 */
	class PEShare{
		int pes;
		int share;
		
		PEShare(int pes, int share){
			this.pes=pes;
			this.share=share;
		}

		public int getPes() {
			return pes;
		}

		public int getShare() {
			return share;
		}
		
	}
	
}
