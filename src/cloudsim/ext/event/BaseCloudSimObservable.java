package cloudsim.ext.event;

import java.util.ArrayList;
import java.util.List;

abstract public class BaseCloudSimObservable implements CloudsimObservable {

	private List<CloudSimEventListener> listeners;
	
	public BaseCloudSimObservable(){
		listeners = new ArrayList<CloudSimEventListener>();
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
