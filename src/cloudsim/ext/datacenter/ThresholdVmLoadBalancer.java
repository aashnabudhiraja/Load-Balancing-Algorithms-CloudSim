package cloudsim.ext.datacenter;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;

import cloudsim.ext.Constants;
import cloudsim.ext.event.CloudSimEvent;
import cloudsim.ext.event.CloudSimEventListener;
import cloudsim.ext.event.CloudSimEvents;


public class ThresholdVmLoadBalancer extends VmLoadBalancer implements CloudSimEventListener {
	
	private Map<Integer, Integer> currentAllocationCounts;
	private Map<Integer, VirtualMachineState> vmStatesList;
	
	private final int tUnder = 50, tUpper = 150;
	
	private Deque<Integer> underLoadedNodes;
	private Deque<Integer> overLoadedNodes;
	Random rand;
	
	
	public ThresholdVmLoadBalancer(DatacenterController dcb){
		dcb.addCloudSimEventListener(this);
		this.vmStatesList = dcb.getVmStatesList();
		rand = new Random();
		initializeUnderLoaded();
		overLoadedNodes = new ArrayDeque<Integer>(vmStatesList.size());;
		this.currentAllocationCounts = Collections.synchronizedMap(new HashMap<Integer, Integer>());
	
	}
	
	private void initializeUnderLoaded(){
		underLoadedNodes = new ArrayDeque<Integer>(vmStatesList.size());
		for (int e : vmStatesList.keySet()){
			underLoadedNodes.push(e);
		}
	}


	@Override
	public int getNextAvailableVm(){
		int vmId = -1;
		
		if (vmStatesList.size() > 0){
			
			int rn = rand.nextInt(vmStatesList.size());
			
			Integer currCount = currentAllocationCounts.remove(rn); //get allocation counts for that node
			if (currCount == null){
				currCount = 1;
			}
			
			if (!(currCount > tUpper)){ //if(not overloaded) then rn
				vmId = rn;
			}else if(!underLoadedNodes.isEmpty()){ 
				vmId = findUnderLoadedNode();
			}else{
				vmId = rn;
				overLoadedNodes.addLast(vmId);
			}
			
			if(vmId == rn){
				if(currCount > 1) currCount++;
				currentAllocationCounts.put(vmId, currCount);
			}else{
				currentAllocationCounts.put(vmId, currCount);
			}
		}
		allocatedVm(vmId);
		System.out.println("allocated "+vmId);
		return vmId;
	}

	private int findUnderLoadedNode() {
		
		//If all available VMs are not allocated, allocated the new ones
		int vmId = underLoadedNodes.pop();
		Integer currCount = currentAllocationCounts.remove(vmId);
		if (currCount == null){
			currCount = 1;
		} else {
			currCount++;
		}
		if(currCount < tUnder){
			underLoadedNodes.addLast(vmId);
		}
		currentAllocationCounts.put(vmId, currCount);
		return vmId;
	}

	public void cloudSimEventFired(CloudSimEvent e) {
		if (e.getId() == CloudSimEvents.EVENT_CLOUDLET_ALLOCATED_TO_VM){
			int vmId = (Integer) e.getParameter(Constants.PARAM_VM_ID);
			Integer currCount = currentAllocationCounts.remove(vmId);
			if (currCount == null){
				currCount = 1;
			} else {
				currCount++;
			}
			currentAllocationCounts.put(vmId, currCount);
			
		} else if (e.getId() == CloudSimEvents.EVENT_VM_FINISHED_CLOUDLET){
			int vmId = (Integer) e.getParameter(Constants.PARAM_VM_ID);
			Integer currCount = currentAllocationCounts.remove(vmId);
			if (currCount != null){
				currCount--;
				currentAllocationCounts.put(vmId, currCount);
				if(currCount < tUnder){
					underLoadedNodes.addLast(vmId);//check VM for under and over load
				}
			}
			if (currCount == null){
				underLoadedNodes.push(vmId);
			}
		}
	}
}
