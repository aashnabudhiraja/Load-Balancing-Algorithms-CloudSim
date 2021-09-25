package cloudsim.ext.servicebroker;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cloudsim.CloudSim;
import cloudsim.ext.InternetCharacteristics;
import cloudsim.ext.datacenter.DatacenterController;
import eduni.simjava.Sim_system;

/**
 * This class is WIP.
 * 
 * @author Bhathiya Wickremasinghe
 *
 */
public class DynamicServiceBroker extends ServiceProximityServiceBroker
											implements CloudAppServiceBroker {
	
	private Map<String, Double> bestResponseTimes;
	private Map<String, DatacenterController> dataCenters;
	private int maxVms = 100;
	
	public DynamicServiceBroker(List<DatacenterController> dcbs){
		super();
		
		bestResponseTimes = new HashMap<String, Double>();
		
		dataCenters = new HashMap<String, DatacenterController>();
		for (DatacenterController dcb : dcbs){
			dataCenters.put(dcb.get_name(), dcb);
		}
		
		try {
			StatusChecker statusChecker = new StatusChecker();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}	

	
	private void reconfigureApp(){
		checkLatencyStatus();
	}
	
	private void checkLatencyStatus(){
		Map<String, Double[]> serviceLatencies = InternetCharacteristics.getInstance().getServiceLatencies();
		
		
		Double bestSoFar;
		Double currLatency;
		
		for (String dc : serviceLatencies.keySet()){
			currLatency = serviceLatencies.get(dc)[0];
			bestSoFar = bestResponseTimes.get(dc);
			if (currLatency != null){
				if (bestSoFar != null){
					if (currLatency <= bestSoFar){
						bestResponseTimes.put(dc, currLatency);
					} else {
						DatacenterController dcb = dataCenters.get(dc);
						if (dcb.getVmStatesList().size() <= maxVms){ 
							dcb.createNewVm();
						}
					}
				} else {
					bestResponseTimes.put(dc, currLatency);
				}
			}
			
//			System.out.println("Best response time for " + dc + "=" + bestSoFar + " and curr=" + currLatency);
		}
	}
	
	
	
	private class StatusChecker extends CloudSim {

		private int count = 0;
		
		public StatusChecker() throws Exception {
			super("LatencyStatusChecker");
		}
		
		public void body(){
			System.out.println("Starting service latency status checker " + count++);
			
			while (Sim_system.running()){
				sim_pause(60000);
				reconfigureApp();
			}
		}
		
	}

}
