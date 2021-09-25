package cloudsim.ext.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import cloudsim.ext.Constants;
import cloudsim.ext.event.CloudSimEvent;
import cloudsim.ext.event.CloudSimEventListener;
import cloudsim.ext.event.CloudSimEvents;
import cloudsim.ext.event.CloudsimObservable;

/**
 * A {@link List} who's additions and removals can be monitored.
 * 
 * @author Bhathiya Wickremasinghe
 *
 * @param <DataType>
 */
public class ObservableList<DataType> extends ArrayList<DataType> 
									  implements CloudsimObservable, Serializable {

	private static final long serialVersionUID = -4005612299359693140L;
	
	//We don't want any copies to carry the same list of listeners. 
	transient private List<CloudSimEventListener> listeners;
	
	/** Constructor. */
	public ObservableList(){
		
	}
	
	public void addCloudSimEventListener(CloudSimEventListener l){
		if (listeners == null){
			listeners = new ArrayList<CloudSimEventListener>();
		}
		listeners.add(l);
	}
	
	public void removeCloudSimEventListener(CloudSimEventListener l){
		if (listeners != null){
			listeners.remove(l);
		}
	}
	
	public void fireCloudSimEvent(CloudSimEvent e){
		if (listeners != null){
			for (CloudSimEventListener l : listeners){		
				l.cloudSimEventFired(e);
			}
		}
	}
	
	@Override
	public boolean add(DataType o){
		boolean success = super.add(o);
		
		if (success){
			CloudSimEvent cloudSimEvent = new CloudSimEvent(CloudSimEvents.EVENT_LIST_CONTENT_ADDED);
			cloudSimEvent.addParameter(Constants.PARAM_DATA_ELEMENT, o);
			fireCloudSimEvent(cloudSimEvent);
		}
		
		return success;
	}
	
	@Override
	public boolean remove(Object o) {
		boolean success = super.remove(o);
		
		if (success){
			CloudSimEvent cloudSimEvent = new CloudSimEvent(CloudSimEvents.EVENT_LIST_CONTENT_REMOVED);
			cloudSimEvent.addParameter(Constants.PARAM_DATA_ELEMENT, o);
			fireCloudSimEvent(cloudSimEvent);
		}
		
		return success;
	}
	
	@Override
	public DataType remove(int index) {
		DataType removed = super.remove(index);
		
		if (removed != null){
			CloudSimEvent cloudSimEvent = new CloudSimEvent(CloudSimEvents.EVENT_LIST_CONTENT_REMOVED);
			cloudSimEvent.addParameter(Constants.PARAM_DATA_ELEMENT, removed);
			fireCloudSimEvent(cloudSimEvent);
		}
		
		return removed;
	}
	
	@Override
	public void clear() {
		super.clear();
		fireCloudSimEvent(new CloudSimEvent(CloudSimEvents.EVENT_LIST_CONTENT_CHANGED));
	}
	
	public boolean replaceContent(Collection<DataType> c){
		super.clear();
		boolean success = super.addAll(c);
		fireCloudSimEvent(new CloudSimEvent(CloudSimEvents.EVENT_LIST_CONTENT_CHANGED));
		
		return success;
	}
}
