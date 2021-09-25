/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation
 *               of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009, The University of Melbourne, Australia
 */


package cloudsim;

import java.util.LinkedList;

import gridsim.Machine;
import gridsim.PEList;


/**
 * Host class extends a Machine to include other resources beside PEs
 * to support simulation of virtualized grids. It executes actions related
 * to management of virtual machines (e.g., creation and destruction). A host has
 * a defined policy for provisioning memory and bw, as well as an allocation policy
 * for PE's to virtual machines.
 * 
 * A host is associated to a datacenter. It can host virtual machines.
 *
 * @author       Rodrigo N. Calheiros
 * @since        CloudSim Toolkit 1.0 Beta
 * @invariant 	 $none
 */
public class Host extends Machine {
	
	protected int memory;
	protected long storage;
	protected long bw;
	protected MemoryProvisioner memoryProvisioner;
	protected BWProvisioner bwProvisioner;
	protected VMMAllocationPolicy allocationPolicy;
	protected LinkedList<VirtualMachine> vmList;

	/**
	 * Create a new Host
	 * @param id ID associated to the host
	 * @param memory amount of memory of this host
	 * @param storage amount of storage of this host
	 * @param bw amount of bw in this host
	 * @param list list of PEs oft his host
	 * @param memoryProvisioner memory provisioner associated to this host
	 * @param bwProvisioner bandwidth provisioner associated to this host
	 * @param allocationPolicy policy to assign processing power to virtual machines
	 * @pre id >= 0
	 * @pre memory > 0
	 * @pre storage > 0
	 * @pre memoryProvisioner != $null
	 * @pre bwProvisioner != $null
	 * @pre allocationPolicy != $null
	 * @pre bw > 0
	 * @post $none
	 * 
	 */
	public Host(int id, int memory, long storage, long bw, PEList list, MemoryProvisioner memoryProvisioner, BWProvisioner bwProvisioner, VMMAllocationPolicy allocationPolicy) {
		super(id, list);
		this.memory = memory;
		this.storage = storage;
		this.bw = bw;
		this.memoryProvisioner = memoryProvisioner;
		this.bwProvisioner = bwProvisioner;
		this.allocationPolicy = allocationPolicy;
		memoryProvisioner.init(this.memory);
		bwProvisioner.init(this.bw);
	
		this.vmList = new LinkedList<VirtualMachine>();
	}
	
	/**
     * Gets the machine bw
     * @return the machine bw
     * @pre $none
     * @post $result > 0
     */
	public long getBw() {
		return bw;
	}

	/**
     * Gets the machine memory
     * @return the machine memory
     * @pre $none
     * @post $result > 0
     */
	public int getMemory() {
		return memory;
	}

	/**
     * Gets the machine storage
     * @return the machine storage
     * @pre $none
     * @post $result > 0
     */
	public long getStorage() {
		return storage;
	}
	
	/**
	 * Allocates PEs and memory to a new VM in the Host
	 * @param vm VirtualMachine being started
	 * @return $true if the VM could be started in the host; $false otherwise
	 * @pre $none
	 * @post $none
	 */
	public synchronized boolean vmCreate(VMCharacteristics vm){
		
		boolean result = bwProvisioner.allocateBWforVM(vm);
		
		if(!result) {
			return false;
		}
		
		result = memoryProvisioner.allocateMemoryForVM(vm);
		if(!result) {
				bwProvisioner.deallocateBWForVM(vm.getVmId(),vm.getUserId());
				return false;
		}
		
		result = allocationPolicy.allocatePEsForVM(vm);
		if(!result){
			bwProvisioner.deallocateBWForVM(vm.getVmId(),vm.getUserId());
			memoryProvisioner.deallocateMemoryForVM(vm.getVmId(),vm.getUserId());
			return false;
		}
		
		VirtualMachine newVm = new VirtualMachine(vm);
		vmList.add(newVm);
								
		return true;
	}
	
	/**
	 * Destroys a VM running in the host
	 * @param vmID ID from the VM being destroyed
	 * @pre $none
	 * @post $none
	 */
	public synchronized void vmDestroy(int vmID, int userID){
		
		bwProvisioner.deallocateBWForVM(vmID, userID);
		memoryProvisioner.deallocateMemoryForVM(vmID, userID);
		allocationPolicy.deallocatePEsForVM(vmID, userID);
		int i=0;
		do{
			VirtualMachine vm = vmList.get(i);
			if(vm.getUserId()==userID&&vm.getVmId()==vmID){
				vmList.remove(i);
				break;
			}
			i++;
		}while(i<vmList.size());

	}
	
	/**
	 * Process the migration of a virtual machine, acting as the sender of the VM
	 * (i.e., the host that is starting the migration process).
	 * @param vmID ID of the VM to be migrated
	 * @param userID ID of the VM owner
	 * @return the VirtualMachine object that represents the migrating VM. $null if
	 * 			no VM with the given ID belonging to the specified user is found
	 * @pre $none
	 * @post $none
	 * 
	 */
	public VirtualMachine vmMigrate(int vmID, int userID){
		
		memoryProvisioner.deallocateMemoryForVM(vmID, userID);
		bwProvisioner.deallocateBWForVM(vmID, userID);
		allocationPolicy.deallocatePEsForVM(vmID, userID);
		
		VirtualMachine vm=null;
		for (int i=0;i<vmList.size();i++){
			VirtualMachine element = vmList.get(i);
			if(element.getUserId()==userID&&element.getVmId()==vmID){
				vm=vmList.remove(i);
				break;
			}
		}
		return vm;
	}
	
	/**
	 * Processes the migration of a virtual machine, acting as the receiver of the VM
	 * @param vm VM to be migrated
	 * @return $true, if the migration succeeds; $false otherwise
	 * @pre vm != $null
	 * @post $none
	 */
	public boolean vmMigrate(VirtualMachine vm){
		
		//first, allocatee memory
		boolean result = memoryProvisioner.allocateMemoryForVM(vm.getCharacteristics());
		if (!result) return false;
		
		//now, tries to allocate bw
		result = bwProvisioner.allocateBWforVM(vm.getCharacteristics());
		if (!result) {
			memoryProvisioner.deallocateMemoryForVM(vm.getVmId(),vm.getUserId());
			return false;
		}
		
		//finally, tries to allocate PEs
		result = allocationPolicy.allocatePEsForVM(vm.getCharacteristics());
		if (!result) {
			memoryProvisioner.deallocateMemoryForVM(vm.getVmId(),vm.getUserId());
			bwProvisioner.deallocateBWForVM(vm.getVmId(),vm.getUserId());
			return false;
		}
		
		vmList.add(vm);
		return true;
	}
	
	/**
	 * Requests updating of processing of gridlets in the VMs running in this host.
	 * @param 		currentTime
	 * @return 		expected time of completion of the next gridlet in all VMs in this host. Double.MAX_VALUE
	 * 				if there is no future events expected in this host
	 * @pre 		currentTime >= 0.0
	 * @post 		$none
	 */
	public double updateVMsProcessing(double currentTime){
		
		double smallerTime = Double.MAX_VALUE;
		for(int i=0;i<vmList.size();i++){
			VirtualMachine vm = vmList.get(i);
			double time = vm.getVMScheduler().updateVMProcessing(currentTime,allocationPolicy.getMIPSShare(vm.getVmId(),vm.getUserId()));
			if(time>0.0&&time<smallerTime) smallerTime=time;
		}
		return smallerTime;
	}
	
	/**
	 * Returns a VM object
	 * @param userId ID of VM's owner
	 * @param vmId ID from the required VM
	 * @return the virtual machine object, $null if not found
	 * @pre $none
	 * @post $none
	 */
	public VirtualMachine getVM(int userId, int vmId){

		for(int i=0;i<vmList.size();i++){
			VirtualMachine vm = vmList.get(i);
			if(vm.getUserId()==userId&&vm.getVmId()==vmId) return vm;
		}		
		return null;
	}
	
	/**
	 * Returns a list with all the VMs in this host
	 * @return list of VMs
	 * @pre $none
	 * @post $none
	 */
	public LinkedList getVMs(){
		return vmList;
	}
	
}
