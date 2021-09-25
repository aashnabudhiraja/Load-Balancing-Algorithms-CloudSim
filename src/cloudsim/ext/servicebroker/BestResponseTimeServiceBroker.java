package cloudsim.ext.servicebroker;

import gridsim.GridSim;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cloudsim.ext.GeoLocatable;
import cloudsim.ext.InternetCharacteristics;
import cloudsim.ext.UserBase;
import cloudsim.ext.datacenter.DatacenterController;

/**
 * Implements {@link CloudAppServiceBroker} in a way to optimise response time. 
 * 
 * Initially traffic is routed to the {@link DatacenterController} closest to the requests
 * originating {@link UserBase} in terms of network latency. Then if the response time acheived by
 * the closest Data Center starts deteriorating, this service broker searches for the service broker 
 * with the best resonse time at the time and shares the load between the closest and the fastest
 * data centers.
 * 
 * @author Bhathiya Wickremasinghe
 *
 */
public class BestResponseTimeServiceBroker extends ServiceProximityServiceBroker
											implements CloudAppServiceBroker {

	
	private static final int COOL_OFF_TIME = 10 * 60 * 1000;//10 min
	private Map<String, Integer> allDataCenters;
	
	/** No args constructor */
	public BestResponseTimeServiceBroker() throws Exception {
		super();
	}
	
	@Override
	protected void init(){
		super.init();
		
		this.allDataCenters = new HashMap<String, Integer>();
		List<GeoLocatable> allInternetEntities = InternetCharacteristics.getInstance().getAllEntities();
		int region;
		String name;
		
		for (GeoLocatable entity : allInternetEntities){
			if (entity instanceof DatacenterController){
				region = entity.getRegion();
				name = entity.get_name();
				allDataCenters.put(name, region);
			}
		}
	}
	
	@Override
	public String getDestination(GeoLocatable inquirer) {
		String src = inquirer.get_name();
		String dest;
		String closestDc;
		double coolOffTime = COOL_OFF_TIME;
		double currTime = GridSim.clock();
		InternetCharacteristics internetCharacteristics = InternetCharacteristics.getInstance();
		
		//Get the closest DC
		closestDc = super.getDestination(inquirer);
		
		//Check if there is another DC with an estimated response time less than 
		// the closes DC
		Map<String, Double[]> serviceLatencies = internetCharacteristics.getServiceLatencies();
		String quickestDc = null;
		double currEstimatedResponseTime;
		double leastEstimatedResponseTime = Double.MAX_VALUE;
		double nwDelay;
		for (String dc : allDataCenters.keySet()){
			nwDelay = internetCharacteristics.getTotalDelay(src, dc, 1);
			
			Double[] updateEntry = serviceLatencies.get(dc);
			if (updateEntry == null){
				//This DC may not be receiving any traffic at the moment
				// So estimate the current processing delay to be the network delay,
				// which usually will make sure it gets at least one request since that would be small.
				currEstimatedResponseTime = nwDelay;
			} else {
				double lastRecordedProcTime = updateEntry[0];
				double lastProcTimeUpdateAt = updateEntry[1]; 
								
				//Now check if the last service update is out of date.
				if ((currTime - lastProcTimeUpdateAt) > coolOffTime){
					//Adjust the last processing time for this DC to the best recorded for it so far
					// since it has been idle for a while assume starting processing time of 0
					lastRecordedProcTime = 0;//bestResponseTimes.get(dc);
					internetCharacteristics.updateSerivceLatency(dc, lastRecordedProcTime);
				}
								
				currEstimatedResponseTime = lastRecordedProcTime + nwDelay;								
			}
			
			//Now see if this currEstimatedResponseTime is the least for all DC's
			if (currEstimatedResponseTime < leastEstimatedResponseTime){
				leastEstimatedResponseTime = currEstimatedResponseTime;
				quickestDc = dc;
			}	
			
//			System.out.println(currTime + " : Esitmated response time " + dc + "->" + currEstimatedResponseTime + " inquirer=" + src);
		}//End for
		
		//Now if the quickest DC is the closest, have to select it. 
		// Otherwise don't send all the traffic over to the quickest
		// dc, but load balance between the quickest and the closest
		if (closestDc.equals(quickestDc)){
			dest = closestDc;
		} else {
			int test = (int) (Math.random() * 2);
			dest = (test == 1) ? closestDc : quickestDc;
			
//			System.out.println("Closest DC is not the quickest DC. closest=" + closestDc 
//					+ " quickest=" + quickestDc + " message sent to " + dest);			
		}
		
		return dest;
	}

}
