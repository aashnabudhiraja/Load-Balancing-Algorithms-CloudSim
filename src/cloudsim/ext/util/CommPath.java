package cloudsim.ext.util;

import cloudsim.ext.Constants;

public class CommPath {
	
//	private double currentTransmissionDelay;
	protected int region1;
	protected int region2;
	
	public CommPath(int r1, int r2){
		if ((r1 >= Constants.WORLD_REGIONS) || (r1 < 0 )
			|| (r2 >= Constants.WORLD_REGIONS) || (r2 < 0)){
			throw new RuntimeException("Regions have to be within 0 and " 
										+ (Constants.WORLD_REGIONS - 1) + " inclusive.");
		}
		
		this.region1 = r1;
		this.region2 = r2;
	}

	/**
	 * @return the region1
	 */
	public int getRegion1() {
		return region1;
	}

	/**
	 * @return the region2
	 */
	public int getRegion2() {
		return region2;
	}
	
	public boolean includesRegion(int r){
		return ((region1 == r) || (region2 == r));
	}
	
	public int getOtherRegion(int r){
		if (r == region1){
			return region2;
		} else if (r == region2) {
			return region1;
		} else {
			throw new RuntimeException("No such region " + r);
		}
	}
	
//	/**
//	 * @return the currentTransmissionDelay
//	 */
//	public double getCurrentTransmissionDelay() {
//		return currentTransmissionDelay;
//	}
//
//	/**
//	 * @param currentTransmissionDelay the currentTransmissionDelay to set
//	 */
//	public void setCurrentTransmissionDelay(double currentTransmissionDelay) {
//		this.currentTransmissionDelay = currentTransmissionDelay;
//	}

	public boolean equals(Object o){
		if (o == this){
			return true;
		}
		
		if (!(o instanceof CommPath)){
			return false;
		}
		
		CommPath other = (CommPath) o;
		
		return ((region1 == other.getRegion1()) && (region2 == other.getRegion2()))
				|| ((region1 == other.getRegion2()) && (region2 == other.getRegion1()));
	}
	
	public int hashCode(){
		return (region1 * region2 * 31 + region1 + region2); //Should be unique enough 
	}
	
//	public int compareTo(CommPath other) {
//		if (currentTransmissionDelay < other.getCurrentTransmissionDelay()){
//			return -1;
//		} else if (currentTransmissionDelay == other.getCurrentTransmissionDelay()){
//			return 0;
//		} else {
//			return 1;
//		}		
//	}

	public String toString(){
		return region1 + "<->" + region2;
	}
}
