package cloudsim.ext;

import java.awt.Rectangle;
import java.awt.Shape;
import java.util.HashMap;
import java.util.Map;

public class WorldGeometry {

	private Shape[] regionBoundaries;
	private Map<Integer, Integer> timeZones;
	
	private static WorldGeometry instance;
	
	
	private WorldGeometry(){
		regionBoundaries = new Rectangle[Constants.WORLD_REGIONS];
		
		regionBoundaries[0] = new Rectangle(90, 90, 120, 140);
		regionBoundaries[1] = new Rectangle(190, 280, 80, 150);
		regionBoundaries[2] = new Rectangle(380, 80, 90, 120);
		regionBoundaries[3] = new Rectangle(480, 70, 200, 200);
		regionBoundaries[4] = new Rectangle(350, 200, 125, 180);
		regionBoundaries[5] = new Rectangle(640, 320, 90, 90);
		
		timeZones = new HashMap<Integer, Integer>();
		timeZones.put(0, -7);
		timeZones.put(1, -4);
		timeZones.put(2, 1);
		timeZones.put(3, 8);
		timeZones.put(4, 2);
		timeZones.put(5, 10);
	}
	
	public static WorldGeometry getInstance(){
		if (instance == null){
			instance = new WorldGeometry();
		}
		
		return instance;
	}
	
	public Shape getBoundary(int region){
		if ((region >= 0) || (region < Constants.WORLD_REGIONS)){
			return regionBoundaries[region];
		} else {
			throw new RuntimeException("Invalid region: " + region);
		}
	}
	
	public int getTimeZone(int region){
		return timeZones.get(region);
	}
}
