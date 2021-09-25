/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation
 *               of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009, The University of Melbourne, Australia
 */


package cloudsim;

import gridsim.PEList;


/**
 * VMScheduler is an abstract class that represents the policy of
 * scheduling performed by a virtual machine. So, classes
 * extending this must execute Cloudlets. Also, the interface for cloudlet
 * management is also implemented in this class.
 * 
 * @author Rodrigo N. Calheiros
 * @since CloudSim Toolkit 1.0 Beta
 * @invariant $none
 */
public abstract class VMScheduler {
	
	protected PEList list;
	protected double previousTime;

	/**
	 * Creates a new VMScheduler object. This method must be invoked
	 * before starting the actual simulation.
	 * 
	 * @pre $none
	 * @post $none 
	 */
	public VMScheduler(){
		this.previousTime = 0.0;
	}
	
	/**
	 * Updates the processing of cloudlets running under management of this scheduler.
	 * @param currentTime current simulation time
	 * @param mipsShare array with MIPS share of each processor available to the scheduler
	 * @return time predicted completion time of the earliest finishing cloudlet, or 0
	 * 				if there is no next events
	 * @pre currentTime >= 0
	 * @post $none
	 */
	public abstract double updateVMProcessing(double currentTime, double[]mipsShare);
	
	/**
	 * Receives an cloudlet to be executed in the VM managed by this scheduler
	 * @param gl the submited cloudlet
	 * @param fileTransferTime time required to move the required files from the SAN to the VM
	 * @return expected finish time of this cloudlet, or 0 if it is in a waiting queue
	 * @pre gl != null
	 * @post $none
	 */
	public abstract double cloudletSubmit(Cloudlet gl, double fileTransferTime);
	
	/**
	 * Cancels execution of a cloudlet
	 * @param clId ID of the cloudlet being cancealed
	 * @return the canceled cloudlet, $null if not found
	 * @pre $none
	 * @post $none
	 */
	public abstract Cloudlet cloudletCancel(int clId);
	
	/**
	 * Pauses execution of a cloudlet
	 * @param clId ID of the cloudlet being paused
	 * @return $true if cloudlet paused, $false otherwise
	 * @pre $none
	 * @post $none
	 */
	public abstract boolean cloudletPause(int clId);
	
	/**
	 * Resumes execution of a paused cloudlet
	 * @param clId ID of the cloudlet being resumed
	 * @return expected finish time of the cloudlet, 0.0 if queued
	 * @pre $none
	 * @post $none
	 */
	public abstract double cloudletResume(int clId);
	
	/**
	 * Gets the status of a cloudlet
	 * @param clId ID of the cloudlet
	 * @return status of the cloudlet, -1 if cloudlet not found
	 * @pre $none
	 * @post $none
	 */
	public abstract int cloudletstatus(int clId);
	
	/**
	 * Informs about completion of some cloudlet in the VM managed
	 * by this scheduler
	 * @return $true if there is at least one finished cloudlet; $false otherwise
	 * @pre $none
	 * @post $none
	 */
	public abstract boolean isFinishedCloudlets();
	
	/**
	 * Returns the next cloudlet in the finished list, $null if this list is empty
	 * @return a finished cloudlet
	 * @pre $none
	 * @post $none
	 */
	public abstract Cloudlet getNextFinishedCloudlet();

	/**
	 * Returns the number of cloudlets runnning in the virtual machine
	 * @return number of cloudlets runnning
	 * @pre $none
	 * @post $none
	 */
	public abstract int runningCloudlets();
	
	/**
	 * Returns one cloudlet to migrate to another vm
	 * @return one running cloudlet
	 * @pre $none
	 * @post $none
	 */
	public abstract Cloudlet migrateCloudlet();
	
}
