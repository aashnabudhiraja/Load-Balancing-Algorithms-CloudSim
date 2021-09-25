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

import java.util.HashMap;


/**
 * SimpleVMProvisioner is an VMProvisioner that
 * chooses, as the host for a VM, the host with
 * less PEs in use.
 * 
 * @author Rodrigo N. Calheiros
 * @since CloudSim Toolkit 1.0 Beta
 * @invariant $none
 */
public class SimpleVMProvisioner extends VMProvisioner {
	
	protected HashMap<String, Host> vmTable;
	protected HashMap<String, Integer> usedPEs;
	int[] freePEs;

	/**
	 * Creates the new SimpleVMProvisioner object
	 * @pre $none
	 * @post $none
	 */
	public SimpleVMProvisioner() {
		vmTable = new HashMap<String, Host>();
		usedPEs = new HashMap<String, Integer>();
	}
	
	/**
	 * Initializes the values of the fields. This method must be invoked
	 * before starting the actual simulation.
	 * 
	 * @param list Machines available in this Datacentre
	 * @pre $none
	 * @post $none 
	 */
	@Override
	public void init(MachineList list){
		
		super.init(list);
		freePEs = new int[resources.size()];
		for (int i=0;i<freePEs.length;i++) freePEs[i]=((Host)resources.get(i)).getNumPE();

	}
	
	/**
	 * Allocates a host for a given VM.
	 * @param vm VM specification
	 * @return $true if the host could be allocated; $false otherwise
	 * @pre $none
	 * @post $none
	 */
	@Override
	public boolean allocateHostForVM(VMCharacteristics vm) {
	
		int requiredPEs = vm.getCpus();
		boolean result=false;
		int tries=0;
		int[] freePEsTemp = freePEs.clone();
		
		if(!this.vmTable.containsKey(vm.getVmId()+"-"+vm.getUserId())){//if this vm was not created
		
			do{//we still trying until we find a host or untill we try all of them
				int moreFree=Integer.MIN_VALUE;
				int idx=-1;
			
				//we want the host with less pes in use
				for(int i=0;i<freePEsTemp.length;i++){
					if(freePEsTemp[i]>moreFree){
						moreFree=freePEsTemp[i];
						idx=i;
					}
				}
			
				Host host = (Host)resources.get(idx);
				result = host.vmCreate(vm);
			
				if(result){//if vm were succesfully created in the host
					vmTable.put(vm.getVmId()+"-"+vm.getUserId(),host);
					usedPEs.put(vm.getVmId()+"-"+vm.getUserId(),requiredPEs);
					freePEs[idx]-=requiredPEs;
					result=true;
					break;
				} else {
					freePEsTemp[idx]=Integer.MIN_VALUE;
				}
			
				tries++;
			}while(!result && tries<freePEs.length);
			
		}//if
		
		return result;
	}
	
	/**
	 * Releases the host used by a VM
	 * @param vmID ID form the vm that is releasing the host
	 * @param userID ID of VM's owner
	 * @pre $none
	 * @post none
	 */
	@Override
	public void deallocateHostForVM(int vmID, int userID) {		
		Host host = vmTable.remove(vmID+"-"+userID);
		int idx = resources.indexOf(host);
		Integer pes = usedPEs.remove(vmID+"-"+userID);
		if(host!=null) {
			host.vmDestroy(vmID,userID);
			freePEs[idx]+=pes;
		}
	}
	
	/**
	 * Triggers a migration from a given virtual machine to a selected
	 * host
	 * @param vmID ID from the virtual machine that will migrate
	 * @param newHostID ID from the host that will receive the VM
	 * @return $true if the migration succeeds; $false otherwise
	 * @pre $none
	 * @post $none
	 */
	@Override
	public boolean migrateVM(int vmID, int userID, int newHostID) {
		
		//where is this VM running?
		Host source = getHost(vmID,userID);
		VirtualMachine vm = source.vmMigrate(vmID, userID);
		if(vm==null) return false;
		
		Host destination = (Host) resources.getMachine(newHostID);
		if(destination==null) return false;
		
		if(destination.getNumFreePE()>=vm.getCpus()){
			return destination.vmMigrate(vm);
		}
		
		return false;
	}
	
	/**
	 * Gets the host that is executing the given VM belonging to the
	 * given user 
	 * 
	 * @param vmID ID from the virtual machine that will migrate
	 * @param userID ID from the owner of the VM
	 * @return the Host with the given vmID and userID; $null if not found
	 * @pre $none
	 * @post $none
	 */
	@Override
	public Host getHost(int vmID, int userID) {
		return vmTable.get(vmID+"-"+userID);
	}
}
