package cloudsim.ext.util;

import java.awt.Shape;
import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import cloudsim.CloudSim;
import cloudsim.ext.Constants;
import cloudsim.ext.WorldGeometry;
import cloudsim.ext.event.BaseCloudSimObservable;
import cloudsim.ext.event.CloudSimEvent;
import cloudsim.ext.event.CloudSimEventListener;
import cloudsim.ext.event.CloudSimEvents;
import cloudsim.ext.gui.DataCenterUIElement;
import cloudsim.ext.gui.SimulationUIElement;
import cloudsim.ext.gui.UserBaseUIElement;
import cloudsim.ext.gui.utils.CommunicationPath;

/**
 * InternetEntitityRegistry maintains a listing of all Simulation elements in several indexed
 * forms for fast look up.
 * 
 * @author Bhathiya Wickremasinghe
 *
 */
public class InternetEntitityRegistry extends BaseCloudSimObservable 
									  implements CloudSimEventListener {
	
	private Map<String, SimulationUIElement> internetEntities ;
	private Map<String, SimulationUIElement>[] regionWiseList;
	private Set<CommunicationPath> communicationPaths;
	private ObservableList<UserBaseUIElement> userBases;
	private ObservableList<DataCenterUIElement> dataCenters;
	
	private static InternetEntitityRegistry instance = null;
		
	@SuppressWarnings("unchecked")
	private InternetEntitityRegistry(){
		internetEntities = new HashMap<String, SimulationUIElement>();
		
		regionWiseList = new HashMap[Constants.WORLD_REGIONS];
		for (int i = 0; i < Constants.WORLD_REGIONS; i++){
			regionWiseList[i] = new HashMap<String, SimulationUIElement>();
		}
		
		communicationPaths = new HashSet<CommunicationPath>();
	}
	
	public static InternetEntitityRegistry getInstance(){
		if (instance == null){
			throw new RuntimeException("InternetEntityRegistry has not been initialized.");
		}
		
		return instance;
	}
	
	public static void initialize(ObservableList<UserBaseUIElement> userBases, 
								  ObservableList<DataCenterUIElement> dataCenters){
		if (instance == null){
			instance = new InternetEntitityRegistry();
		}
		instance.userBases = userBases;
		instance.dataCenters = dataCenters;
		
		userBases.addCloudSimEventListener(instance);
		dataCenters.addCloudSimEventListener(instance);
	}
	
	private void reset(){
		internetEntities.clear();
		
		for (int i = 0; i < Constants.WORLD_REGIONS; i++){
			regionWiseList[i].clear();
		}		
		communicationPaths.clear();
		
		for (SimulationUIElement e : userBases){
			addEntity(e);
		}
		
		for (SimulationUIElement e : dataCenters){
			addEntity(e);
		}
	}
	
	public void addEntity(SimulationUIElement elem){
		assignPhysicalLocation(elem);
		
		String name = elem.getName();
		internetEntities.put(name, elem);
		
		Map<String, SimulationUIElement> regionalList = regionWiseList[elem.getRegion()];
		regionalList.put(name, elem);
	}
	
	public void removeEntry(SimulationUIElement elem){
		String name = elem.getName();
		internetEntities.remove(name);
		
		Map<String, SimulationUIElement> regionalList = regionWiseList[elem.getRegion()];
		regionalList.remove(name);
	}
	
	private void assignPhysicalLocation(SimulationUIElement elem){
		int region = elem.getRegion();
		Map<String, SimulationUIElement> regionalList = regionWiseList[region];
		int existingElems = regionalList.size();
		Shape regionBoundary = WorldGeometry.getInstance().getBoundary(region);
		Point2D center = new Point2D.Double(regionBoundary.getBounds().getCenterX(),
											regionBoundary.getBounds().getCenterY());
		Point2D pos = getPosition(existingElems, center);
		elem.setLocation(pos);
	}
	
	private Point2D getPosition(int num, Point2D center){
		if (num == 0){
			return center;
		}
		
		int gap = 30;
		
		int distance = getDistance(num);
		int numCols = 2 * distance + 1;
		int numRows = numCols;
		int absNum = (num - (distance - 1) * 8) % 8;
		
		int row = - distance; 
		int col = - distance;
		
		if (absNum == 0){
			row += 1;
		} else if (absNum < numCols){
			col +=  (absNum - 1); 
		} else if (absNum <= (numCols * 2 - 1)) {
			col += (numCols - 1);
			row += (absNum - numCols);
		} else if (absNum <= (numCols * 3 - 2)) {
			row += (numRows - 1);
			col += ((numCols - (absNum - numRows - (numCols - 1))) - 1) ;
		} else {
			row = (numRows - (absNum - numRows - (numCols - 1) - (numRows - 2))) - 1;
		}
		
//		System.out.println("Position is : " + col + "," + row);
		
		return new Point2D.Double(center.getX() + (col * gap), center.getY() + (row * gap));
	}
	
	private int getDistance(int num){
		//Series goes like distance 1, count 8
		//				   distance 2, count 16
		//				   distance 3, count 24
		
		if (num == 0){
			return 1;
		} else {
			return ((num - 1) / 8) + 1;
		}
	}	
	
	/**
	 * @return the communicationPaths
	 */
	public Set<CommunicationPath> getCommunicationPaths() {
		return communicationPaths;
	}

	/**
	 * @param communicationPaths the communicationPaths to set
	 */
	public void setCommunicationPaths(Set<CommunicationPath> communicationPaths) {
		this.communicationPaths = communicationPaths;
	}

	/**
	 * @return the internetEntities
	 */
	public Map<String, SimulationUIElement> getInternetEntities() {
		return internetEntities;
	}

	/**
	 * @param internetEntities the internetEntities to set
	 */
	public void setInternetEntities(
			Map<String, SimulationUIElement> internetEntities) {
		this.internetEntities = internetEntities;
	}
	
	public void addCommunicationPath(String srcName, String destName){
		SimulationUIElement src = internetEntities.get(srcName);
		SimulationUIElement dest = internetEntities.get(destName);
		
		if ((src != null) && (dest != null)){
			communicationPaths.add(new CommunicationPath(src, dest));
		}
		
		fireCloudSimEvent(new CloudSimEvent(CloudSimEvents.EVENT_NEW_COMM_PATH));
	}

	public void cloudSimEventFired(CloudSimEvent e) {
		reset();
	}

}
