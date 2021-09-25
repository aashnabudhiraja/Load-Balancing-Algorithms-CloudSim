package cloudsim.ext.datacenter;

import java.util.Map;

public class RoundRobinVmLoadBalancer extends VmLoadBalancer {
	
	private Map<Integer, VirtualMachineState> vmStatesList;
	private int currVm = -1;

	public RoundRobinVmLoadBalancer(Map<Integer, VirtualMachineState> vmStatesList){
		super();
		
		this.vmStatesList = vmStatesList;
	}

	public int getNextAvailableVm(){
		currVm++;
		
		if (currVm >= vmStatesList.size()){
			currVm = 0;
		}
		
		allocatedVm(currVm);
		System.out.println("allocated "+currVm);
		return currVm;
		
	}
}
