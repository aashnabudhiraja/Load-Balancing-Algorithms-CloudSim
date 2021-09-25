package cloudsim.ext.datacenter;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import cloudsim.ext.Constants;
import cloudsim.ext.event.CloudSimEvent;
import cloudsim.ext.event.CloudSimEventListener;
import cloudsim.ext.event.CloudSimEvents;


public class WeightedRoundRobinVmLoadBalancer extends VmLoadBalancer implements CloudSimEventListener  {
	
	private Map<Integer, VirtualMachineState> vmStatesList;
	private Map<Integer, Integer> currentAllocationCounts;

	private int[] vmWeights = {21, 20, 21, 22, 21, 20, 22, 22, 22, 20, 21, 21, 20, 21, 22, 21, 20, 21, 22, 22, 21, 21, 21, 20, 22, 20, 20, 22, 21, 20, 22, 20, 21, 20, 21, 22, 22, 21, 22, 20, 21, 20, 21, 21, 20, 22, 21, 20, 21, 20}; 

	public WeightedRoundRobinVmLoadBalancer(Map<Integer, VirtualMachineState> vmStatesList, DatacenterController dcb){
		super();
		dcb.addCloudSimEventListener(this);
		this.vmStatesList = vmStatesList;
	}

	public int getNextAvailableVm(){
		int vm = weightedRoundRobin();
		allocatedVm(vm);
		return vm;
	}
	
	public int weightedRoundRobin() {
		int i = -1;
		int cw = 0;
		
		while (true) { 
			i = (i + 1) % 50; 
			if (i == 0) { 
				cw = cw - vmGCD(vmWeights); 
				if (cw <= 0) { 
					cw = vmMax(vmWeights); 
//					if (cw == 0)  return 0; 
				} 
			} 
			if (vmWeights[i] >= cw)  return i; 
		}
	}
	
	public int vmGCD(int[] vmWeights) {
		int currGcd = gcd(vmWeights[0], vmWeights[1]);
		for (int i = 2; i < 50; i++) {
			currGcd = gcd(currGcd, vmWeights[i]);
		}
		return currGcd;
	}
	
	public int vmMax(int[] vmWeights) {
		int max = -1;
		for(int wt : vmWeights) {
			if(wt > max) max = wt;
		}
		return max;
	}
	
	static int gcd(int a, int b) 
    { 
        // Everything divides 0  
        if (a == 0) 
          return b; 
        if (b == 0) 
          return a; 
       
        // base case 
        if (a == b) 
            return a; 
       
        // a is greater 
        if (a > b) 
            return gcd(a-b, b); 
        return gcd(a, b-a); 
    }
	
	public void cloudSimEventFired(CloudSimEvent e) {
		if (e.getId() == CloudSimEvents.EVENT_CLOUDLET_ALLOCATED_TO_VM){
			int vmId = (Integer) e.getParameter(Constants.PARAM_VM_ID);
			vmWeights[vmId]--;
			System.out.println(vmId+" allocated");
		} else if (e.getId() == CloudSimEvents.EVENT_VM_FINISHED_CLOUDLET){

			int vmId = (Integer) e.getParameter(Constants.PARAM_VM_ID);
			vmWeights[vmId]++;
			System.out.println(vmId+" deallocated");
				
			}
			
		}
	}
