package cloudsim.ext.gui.utils;

/**
 * Data object used to transfer statistical measures from simulation to the screens.
 * @author Bhathiya Wickremasinghe
 *
 */
public class SimMeasure implements Comparable<SimMeasure> {
	
	private String name;	
	private String entityName;
	private String type;
	private double min;
	private double max;
	private double avg;
	private int count;
	
	
	/**
	 * @return the count
	 */
	public int getCount() {
		return count;
	}
	/**
	 * @param count the count to set
	 */
	public void setCount(int count) {
		this.count = count;
	}
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
	/**
	 * @return the name
	 */
	public String getEntityName() {
		return entityName;
	}
	/**
	 * @param name the name to set
	 */
	public void setEntityName(String name) {
		this.entityName = name;
	}
	/**
	 * @return the min
	 */
	public double getMin() {
		return min;
	}
	/**
	 * @param min the min to set
	 */
	public void setMin(double min) {
		this.min = min;
	}
	/**
	 * @return the max
	 */
	public double getMax() {
		return max;
	}
	/**
	 * @param max the max to set
	 */
	public void setMax(double max) {
		this.max = max;
	}
	/**
	 * @return the avg
	 */
	public double getAvg() {
		return avg;
	}
	/**
	 * @param avg the avg to set
	 */
	public void setAvg(double avg) {
		this.avg = avg;
	}
	/**
	 * @return the type
	 */
	public String getType() {
		return type;
	}
	/**
	 * @param type the type to set
	 */
	public void setType(String type) {
		this.type = type;
	}
	
	
	public int compareTo(SimMeasure other) {
		return name.compareTo(other.getName());
	}

}
