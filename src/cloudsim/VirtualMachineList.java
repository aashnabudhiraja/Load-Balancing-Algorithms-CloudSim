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


/**
 * VirtualMachineList is a list to store virtual machines
 *
 * @author       Rodrigo N. Calheiros
 * @since        CloudSim Toolkit 1.0 Beta
 * @invariant $none
 */
public class VirtualMachineList extends LinkedList {

	private static final long serialVersionUID = 1L;

	/**
	 * Allocates a new VirtualMachineList
	 * @pre $none
	 * @post $none
	 *
	 */
	public VirtualMachineList() {
		super();
	}

	/**
	 * Return a reference to a VirtualMachine object from its ID
	 * @param vmId ID of required VM
	 * @return VirtualMachine with the given ID, $null if not found
	 * @pre $none
	 * @post $none
	 */
	public VirtualMachine getVMbyID(int vmId){
		
		for(int i=0;i<this.size();i++){
			if(((VirtualMachine)this.get(i)).getVmId()==vmId) return (VirtualMachine)this.get(i);
		}
		return null;
	}

}
