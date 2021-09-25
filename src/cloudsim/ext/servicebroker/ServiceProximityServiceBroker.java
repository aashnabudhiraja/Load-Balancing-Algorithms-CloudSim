package cloudsim.ext.servicebroker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cloudsim.ext.GeoLocatable;
import cloudsim.ext.InternetCharacteristics;
import cloudsim.ext.datacenter.DatacenterController;

public class ServiceProximityServiceBroker implements CloudAppServiceBroker {

	protected Map<Integer, List<String>> regionalDataCenterIndex = null;
	
	public ServiceProximityServiceBroker(){
		regionalDataCenterIndex = new HashMap<Integer, List<String>>();
		
		init();
	}
	
	protected void init(){
		List<GeoLocatable> allInternetEntities = InternetCharacteristics.getInstance().getAllEntities();
		int region;
		
		for (GeoLocatable entity : allInternetEntities){
			if (entity instanceof DatacenterController){
				region = entity.getRegion();
				List<String> l = regionalDataCenterIndex.get(region);
				if (l == null){
					l = new ArrayList<String>();
					regionalDataCenterIndex.put(region, l);
				}
				l.add(entity.get_name());
			}
		}
	}
	
	public String getDestination(GeoLocatable inquirer) {
		List<Integer> proximityList = InternetCharacteristics.getInstance().getProximityList(inquirer.getRegion());
		
		int region;
		String dcName;
		for (int i = 0; i < proximityList.size(); i++){
			region = proximityList.get(i);
			dcName = getAnyDataCenter(region);
			if (dcName != null){
				return dcName;
			}
		}
		
		//If it comes here, that means there are no DC's anywhere
		throw new RuntimeException("Looks like you have not configured any Data Centers. Please check the configuration");
	}

	protected String getAnyDataCenter(int region) {
		List<String> regionalList = regionalDataCenterIndex.get(region);		
		String dcName = null;
		
		if (regionalList != null){
			int listSize = regionalList.size();
			if (listSize == 1){
				dcName = regionalList.get(0);
			} else {
				//More than one candidate
				// Load balance between them
				int rand = (int) (Math.random() * listSize);
				dcName = regionalList.get(rand);
			}
		}
		
		return dcName;
	}

}
