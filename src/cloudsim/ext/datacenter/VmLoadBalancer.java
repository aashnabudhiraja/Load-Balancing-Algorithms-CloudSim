package cloudsim.ext.datacenter;

import java.util.HashMap;
import java.util.Map;

/**
 * This is the base class defining the behaviour of a Virtual Machine load balancer
 * used by a {@link DatacenterController}. The main method all load balancers should implement
 * is <code>public int getNextAvailableVm()</code>.
 * 
 * This class provides a basic load balancing statistic collection that can be used by 
 * implementing classes. The implementing classes should call the  <code>void allocatedVM(int currVm)</code>
 *  method to use the statisitics collection feature.
 * 
 * @author Bhathiya Wickremasinghe
 */
abstract public class VmLoadBalancer {
	/** Holds the count of allocations for each VM */
	protected Map<Integer, Integer> vmAllocationCounts;
	
	/** No args contructor */
	public VmLoadBalancer(){
		vmAllocationCounts = new HashMap<Integer, Integer>();
	}
	
	/**
	 * The main contract of {@link VmLoadBalancer}. All load balancers should implement
	 * this method according to their specific load balancing policy.
	 * 
	 * @return id of the next available Virtual Machine to which the next task should be
	 * 			allocated 
	 */
	abstract public int getNextAvailableVm();
	
	/**
	 * Used internally to update VM allocation statistics. Should be called by all impelementing
	 * classes to notify when a new VM is allocated.
	 * 
	 * @param currVm
	 */
	protected void allocatedVm(int currVm){
		
		Integer currCount = vmAllocationCounts.get(currVm);
		if (currCount == null){
			currCount = 0;
		}
		vmAllocationCounts.put(currVm, currCount + 1);		
	}
		
	/**
	 * Returns a {@link Map} indexed by VM id and having the number of allocations for each VM.
	 * @return
	 */
	public Map<Integer, Integer> getVmAllocationCounts(){
		return vmAllocationCounts;
	}
}