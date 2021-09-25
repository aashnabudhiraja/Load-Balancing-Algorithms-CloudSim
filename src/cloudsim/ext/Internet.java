package cloudsim.ext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cloudsim.CloudSim;
import cloudsim.ext.event.CloudSimEvent;
import cloudsim.ext.event.CloudSimEventListener;
import cloudsim.ext.event.CloudSimEvents;
import cloudsim.ext.event.CloudsimObservable;
import cloudsim.ext.servicebroker.CloudAppServiceBroker;
import cloudsim.ext.util.CommPath;
import eduni.simjava.Sim_event;
import eduni.simjava.Sim_system;
import gridsim.GridSim;

/**
 * The class Internet emulates the function of the real world Internet. The {@link UserBase}s
 * send their requests to the Internet and the Internet routes those requests to the correct
 * destination DataCenterBroker while inserting the data transfer delay to the simulated requests.
 * 
 * The class Internet serves a secondary purpose as the progress updater of the simulation
 * to the GUI. This is not perfect design, but done so to minimize the number of CloudSim entities
 * in the simulation run.
 * 
 * @author Bhathiya Wickremasinghe
 */
public class Internet extends CloudSim implements CloudsimObservable {

	private static final long PROGRESS_UPDATE_INTERVAL = 60000;
	
	private List<CloudSimEventListener> listeners;

	private Map<Integer, CloudAppServiceBroker> serviceBrokers = new HashMap<Integer, CloudAppServiceBroker>();
	private boolean running = false;
	private double lastProgressUpdate = 0;
	
	public Internet(CloudSimEventListener progressListener) throws Exception {
		super("Internet");
		
		listeners = new ArrayList<CloudSimEventListener>();
		addCloudSimEventListener(progressListener);
	}
	
	public void addServiceBroker(int appId, CloudAppServiceBroker broker){
		serviceBrokers.put(appId, broker);
	}

	public void body(){
		running = true;
		System.out.println("Starting internet " + get_id());
		
		Sim_event ev = new Sim_event();
        while(Sim_system.running()){
            super.sim_get_next(ev);
            
//            System.out.println(GridSim.clock() + ": Internet got:" + ev.get_tag() + " from " + ev.get_src() + " with data=" + ev.get_data());
            processEvent(ev);
            updateProgress();
            
        }
        
        running = false;
	}
	
	private void processEvent(Sim_event ev){
		InternetCloudlet cloudlet;
		if (ev.get_data() instanceof InternetCloudlet){
			cloudlet = (InternetCloudlet) ev.get_data();
		} else {
			System.out.println("Internet got message with non-cloudlet.");
			return;
		}
		
		String srcName = null;
		String destName = null;
		double delay = 0.0;
		InternetCharacteristics internetCharacteristics = InternetCharacteristics.getInstance();		
    	
		GeoLocatable originator = cloudlet.getOriginator();
		
		//TODO refactor below switch statement to take out more common code
		switch (ev.get_tag()) {
        case Constants.REQUEST_INTERNET_CLOUDLET_TAG:
        	srcName = originator.get_name();
        	int appId = cloudlet.getAppId();
        	CloudAppServiceBroker serviceBroker = serviceBrokers.get(appId);
        	destName = serviceBroker.getDestination(originator);
        	
        	CommPath commPath1 = internetCharacteristics.addTraffic(srcName, destName, cloudlet.getRequestCount());
        	cloudlet.addData(Constants.PARAM_COMM_PATH, commPath1);
        	
        	double singleRequestSize = cloudlet.getDataSize();
        	delay = internetCharacteristics.getTotalDelay(srcName, destName, singleRequestSize);
        	
//        	System.out.println("Internet sending cloudlet to broker " + destName + " with a delay" + delay);
        	send(destName, delay, Constants.REQUEST_INTERNET_CLOUDLET_TAG, ev.get_data());
        	
        	break;
        	
        case Constants.RESPONSE_INTERNET_CLOUDLET_TAG:      
        	srcName = GridSim.getEntityName(ev.get_src());
			destName = originator.get_name();
			
			CommPath commPath2 = internetCharacteristics.addTraffic(srcName, destName, cloudlet.getRequestCount());
        	cloudlet.addData(Constants.PARAM_COMM_PATH, commPath2);
        	
        	singleRequestSize = cloudlet.getDataSize();
        	delay = internetCharacteristics.getTotalDelay(srcName, destName, singleRequestSize);
        	
//        	System.out.println(GridSim.clock() + ": Internet returning cloudlet "+ cloudlet.getCloudletId() + "to userbase, transfer delay=" + delay);
        	
        	send(destName + "R", delay, Constants.RESPONSE_INTERNET_CLOUDLET_TAG, ev.get_data());
        	break;
		}		
	}
	
	public boolean isRunning(){
		return running;
	}
	
//	public double getTime(){
//		return GridSim.clock();
//	}
	
//	public void shutDown(){
//		send(0, 0.0, GridSimTags.END_OF_SIMULATION);
//	}
	
	private void updateProgress(){
		double currSimTime = GridSim.clock();
		
		if ((currSimTime - lastProgressUpdate) > PROGRESS_UPDATE_INTERVAL){
			CloudSimEvent e = new CloudSimEvent(CloudSimEvents.EVENT_PROGRESS_UPDATE);
			e.addParameter(Constants.PARAM_TIME, currSimTime);
			
			fireCloudSimEvent(e);
		}
	}
	
	
	public void addCloudSimEventListener(CloudSimEventListener l){
		listeners.add(l);
	}
	
	public void removeCloudSimEventListener(CloudSimEventListener l){
		listeners.remove(l);
	}
	
	public void fireCloudSimEvent(CloudSimEvent e){
		for (CloudSimEventListener l : listeners){
			l.cloudSimEventFired(e);
		}
	}
}
