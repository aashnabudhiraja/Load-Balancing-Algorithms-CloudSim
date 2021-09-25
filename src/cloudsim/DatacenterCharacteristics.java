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
import gridsim.ResourceCharacteristics;


/**
 * DatacenterCharacteristics is an extension of ResourceCharacteristics
 * to add extra characteristics to a Datacenter
 * 
 * @author		Rodrigo N. Calheiros
 * @since 		CloudSim Toolkit 1.0 Beta
 * @invariant	$none
 */
public class DatacenterCharacteristics extends ResourceCharacteristics {

	protected String vmm;
	protected double costPerMem;
	protected double costPerStorage;
	protected double costPerBw;
	
	/**
     * Allocates a new DatacenterCharacteristics object.
     * If the time zone is invalid, then by default, it will be GMT+0.
     * @param architecture  the architecture of a resource
     * @param OS            the operating system used
     * @param VMM			the virtual machine monitor used
     * @param machineList   list of machines in a resource
     * @param timeZone   local time zone of a user that owns this reservation.
     *                   Time zone should be of range [GMT-12 ... GMT+13]
     * @param costPerSec    the cost per sec to use this resource
     * @param costPerMem	the cost to use memory in this resource
     * @param costPerStorage	the cost to use storage in this resource
     * @pre architecture != null
     * @pre OS != null
     * @pre VMM != null
     * @pre machineList != null
     * @pre timeZone >= -12 && timeZone <= 13
     * @pre costPerSec >= 0.0
     * @pre costPerMem >= 0
     * @pre costPerStorage >= 0
     * @post $none
     */
	public DatacenterCharacteristics(String architecture, String OS,String VMM, MachineList machineList, double timeZone,double costPerSec, double costPerMem, double costPerStorage, double costPerBw) {
		super(architecture, OS, machineList, 1, timeZone, costPerSec);
		this.vmm=VMM;
		this.costPerMem=costPerMem;
		this.costPerStorage=costPerStorage;
		this.costPerBw=costPerBw;
	}

	/**
	 * Get the cost to use memory in this resource
	 * @return the cost to use memory
	 */
	public double getCostPerMem() {
		return costPerMem;
	}
	
	/**
     * Sets cost to use memory
     * @param costPerMem  cost to use memory
     * @pre costPerMem >= 0
     * @post $none
     */
	public void setCostPerMem(double costPerMem) {
		this.costPerMem = costPerMem;
	}

	/**
	 * Get the cost to use storage in this resource
	 * @return the cost to use storage
	 */
	public double getCostPerStorage() {
		return costPerStorage;
	}

	/**
     * Sets cost to use storage
     * @param costPerStorage  cost to use storage
     * @pre costPerStorage >= 0
     * @post $none
     */
	public void setCostPerStorage(double costPerStorage) {
		this.costPerStorage = costPerStorage;
	}
	
	/**
	 * Get the cost to use bandwidth in this resource
	 * @return the cost to use bw
	 */
	public double getCostPerBw() {
		return costPerBw;
	}

	/**
	 *  Sets cost to use bw cost to use bw
	 * @param costPerBw
	 * @pre costPerBw >= 0
	 * @post $none
	 */
	public void setCostPerBw(double costPerBw) {
		this.costPerBw = costPerBw;
	}

	/**
	 * Gets the VMM in use in the datacenter
	 * @return the VMM name
	 */
	public String getVmm() {
		return vmm;
	}
}
