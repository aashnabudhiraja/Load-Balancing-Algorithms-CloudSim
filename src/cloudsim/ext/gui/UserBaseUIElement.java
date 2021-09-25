package cloudsim.ext.gui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.io.Serializable;

import cloudsim.ext.Simulation;
import cloudsim.ext.UserBase;

/**
 * UserBaseUIElement encapsulates the User Base specific data for the use of GUI classes. This 
 * information is used by {@link Simulation} to construct {@link UserBase}s when initialising 
 * the simulation.
 * 
 * @author Bhathiya Wickremasinghe
 *
 */
public class UserBaseUIElement extends SimulationUIElement implements Serializable {

	private double maxTrafficInterval;
	private int width = 5;
	private long reqSize;
	private int peakHoursStart;
	private int peakHoursEnd;
	private int peakUserCount;
	private int offPeakUserCount;
	private int reqPerHrPerUser;
	
	public UserBaseUIElement(){
		
	}
	
	public UserBaseUIElement(String name, 
							 int region, 
							 int reqPerHrPerUser,
							 long reqSize,
							 int[] peakHours,
							 int peakUserCount,
							 int offPeakUserCount){
		super(name, region);
		this.reqPerHrPerUser = reqPerHrPerUser;
		this.reqSize = reqSize;
		this.peakHoursStart = peakHours[0];
		this.peakHoursEnd = peakHours[1];
		this.peakUserCount = peakUserCount;
		this.offPeakUserCount = offPeakUserCount;
		
		this.color = Color.BLUE;
	}
	
	@Override
	public void paint(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;
		g2.setColor(color);
		
		g2.fill3DRect((int) (location.getX() - (width / 2)), 
					  (int) (location.getY() - (width / 2)),
					  width, width, true);
		
		g2.drawString(name, (int) (location.getX() + (width / 2)), (int) location.getY());
	}

	/**
	 * @return the maxTrafficInterval
	 */
	public double getMaxTrafficInterval() {
		return maxTrafficInterval;
	}

	/**
	 * @param maxTrafficInterval the maxTrafficInterval to set
	 */
	public void setMaxTrafficInterval(double maxTrafficInterval) {
		this.maxTrafficInterval = maxTrafficInterval;
	}

	
	/**
	 * @return the peakHoursStart
	 */
	public int getPeakHoursStart() {
		return peakHoursStart;
	}

	/**
	 * @param peakHoursStart the peakHoursStart to set
	 */
	public void setPeakHoursStart(int peakHoursStart) {
		this.peakHoursStart = peakHoursStart;
	}

	/**
	 * @return the peakHoursEnd
	 */
	public int getPeakHoursEnd() {
		return peakHoursEnd;
	}

	/**
	 * @param peakHoursEnd the peakHoursEnd to set
	 */
	public void setPeakHoursEnd(int peakHoursEnd) {
		this.peakHoursEnd = peakHoursEnd;
	}
	
	/**
	 * @return the dataSize
	 */
	public long getReqSize() {
		return reqSize;
	}

	/**
	 * @param dataSize the dataSize to set
	 */
	public void setReqSize(long dataSize) {
		this.reqSize = dataSize;
	}

	/**
	 * @return the peakUserCount
	 */
	public int getPeakUserCount() {
		return peakUserCount;
	}

	/**
	 * @param peakUserCount the peakUserCount to set
	 */
	public void setPeakUserCount(int peakUserCount) {
		this.peakUserCount = peakUserCount;
	}

	/**
	 * @return the offPeakUserCount
	 */
	public int getOffPeakUserCount() {
		return offPeakUserCount;
	}

	/**
	 * @param offPeakUserCount the offPeakUserCount to set
	 */
	public void setOffPeakUserCount(int offPeakUserCount) {
		this.offPeakUserCount = offPeakUserCount;
	}

	/**
	 * @return the reqPerHrPerUser
	 */
	public int getReqPerHrPerUser() {
		return reqPerHrPerUser;
	}

	/**
	 * @param reqPerHrPerUser the reqPerHrPerUser to set
	 */
	public void setReqPerHrPerUser(int reqPerHrPerUser) {
		this.reqPerHrPerUser = reqPerHrPerUser;
	}

	
}
