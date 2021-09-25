package cloudsim.ext.stat;

/**
 * A utitlity class to keep a list of events occuring during the day grouped by the 
 * hour of the day.
 * 
 * @author Bhathiya Wickremasinghe
 *
 */
public class HourlyEventCounter {

	private String name;
	private long[] hourlyCount = new long[24];
	
	
	public HourlyEventCounter(String name){
		this.name = name;
		for (int i = 0; i < 24; i++){
			hourlyCount[i] = 0;
		}
	}
	
	
	public void addEvent(double timeInMS, int countAs){
		int timeInHrs = (int) Math.floor( (timeInMS / (1000 * 60 * 60)) % 24);
		
		if (timeInHrs < 24){
			hourlyCount[timeInHrs] += countAs;
		} 
	}
	
	public void printHourlyCounts(){
		System.out.println("*********** " + name + " *************");
		for (int i = 0; i < 24; i++){
			System.out.println((i+1) + "-" + hourlyCount[i]);
		}
	}
	
	public long getMax(){
		long max = -1;
		for (long i : hourlyCount){
			if (i > max){
				max = i;
			}
		}
		
		return max;
	}

	/**
	 * @return the hourlyCount
	 */
	public long[] getHourlyCount() {
		return hourlyCount;
	}

}
