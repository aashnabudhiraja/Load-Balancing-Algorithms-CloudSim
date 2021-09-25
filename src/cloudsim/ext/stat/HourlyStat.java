package cloudsim.ext.stat;

import cloudsim.ext.Constants;
import eduni.simjava.Sim_stat;

public class HourlyStat {
	
	private Sim_stat stat;
	private String measureName;
	
	public HourlyStat(Sim_stat stat, String measureName, int measureType){
		this.stat = stat;
		this.measureName = measureName;
		
		for (int i = 0; i < 24; i++){
			stat.add_measure(measureName + Constants.STANDARD_SEPARATOR + i, measureType);
		}
	}	
	
	public void update(double startTime, double endTime){
		int hour = (int) Math.floor( (startTime / (1000 * 60 * 60)) % 24);
		
		stat.update((measureName + Constants.STANDARD_SEPARATOR + hour), startTime, endTime);
	}
}
