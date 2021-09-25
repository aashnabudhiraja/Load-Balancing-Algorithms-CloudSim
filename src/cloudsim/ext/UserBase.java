package cloudsim.ext;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import cloudsim.CloudSim;
import cloudsim.ext.stat.HourlyStat;
import cloudsim.ext.util.CommPath;
import eduni.simjava.Sim_event;
import eduni.simjava.Sim_stat;
import eduni.simjava.Sim_system;
import gridsim.GridSim;
import gridsim.util.Poisson;

/**
 * A User Base models a group of users that is considered as a single unit in the simulation and its 
 * main responsibility is to generate traffic for the simulation. 
 * 
 * This class extends {@link CloudSim} and the body() method is used to periodically generate bursts
 * of traffic representing the number of users online at the time. The number of users online at a particular
 * time is calculated using the parameters peakHours, peakAvgUsers and offPeakAvgUsers and also by
 * using a Poisson distribution to randomly vary the number in a realistic manner. 
 * 
 * The private inner class {@link ResponseHandler} is responsible for accepting responses to the 
 * requests sent by UserBase. Therefore the traffic generation pattern is independent of receiving the 
 * responses to the requests. 
 * 
 * @author Bhathiya Wickremasinghe
 *
 */
public class UserBase extends CloudSim implements GeoLocatable {
	
	private static final int STANDARD_POISSON_DIST_MEAN = 100;
	private int region;
	private int instructionLengthPerRequest;
	private int messagesReceived = 0;	
	private int responsesReceived = 0;
	private boolean cancelled = false;
	private int requestsPerUserPerHour;
	private double[] peakHours;
	private int peakAvgUsers;
	private int offPeakAvgUsers;
	private long perRequestDataSize;
	private int userGroupingFactor;
	private Sim_stat stat;
	
	/** 
	 * Holds the id's of the requests that have been sent out, along with the request time, until
	 * the responses are received by the {@link ResponseHandler}
	 */
	private Map<Integer, Double> currentRequests;
	
	/** Records the resonse time statistics grouped by the hour of the day. */
	private HourlyStat hourlyResponseTimeStat;
	
	
	private Poisson userCountDistribution;
	private Poisson requestDelayDistribution;
	
	/** Constructor .*/
	public UserBase(String name, 
					int region,
					int requestsPerUserPerHour,
					int[] peakHours,
					int peakAvgUsers,
					int offPeakAvgUsers,
					long reqDataSize,
					int userGroupingFactor,
					int instructionLengthPerRequest) throws Exception {
		super(name);
		
		System.out.println(GridSim.clock() + " Creating new user base " + get_name());
		
		this.region = region;
		this.instructionLengthPerRequest = instructionLengthPerRequest;
		this.requestsPerUserPerHour = requestsPerUserPerHour;
		this.peakAvgUsers = peakAvgUsers;
		this.offPeakAvgUsers = offPeakAvgUsers;
		this.perRequestDataSize = reqDataSize;
		this.userGroupingFactor = userGroupingFactor;
		
		//Convert peak hrs to time in milliseconds from GMT
		this.peakHours = new double[2];
		this.peakHours[0] = peakHours[0] * Constants.MILLI_SECONDS_TO_HOURS;
		this.peakHours[1] = peakHours[1] * Constants.MILLI_SECONDS_TO_HOURS;
		
		this.currentRequests = Collections.synchronizedMap(new HashMap<Integer, Double>());
						
		InternetCharacteristics.getInstance().addEntity(this);
		
		stat = new Sim_stat();
		stat.add_measure(Constants.UB_RESPONSE_TIME, Sim_stat.INTERVAL_BASED);
		hourlyResponseTimeStat = new HourlyStat(stat, Constants.HOURLY_RESPONSE_TIME, Sim_stat.INTERVAL_BASED);
		set_stat(stat);		
		
		ResponseHandler responseHandler = new ResponseHandler(get_name() + "R");
		
		requestDelayDistribution = new Poisson("RequestDelayDistribution", STANDARD_POISSON_DIST_MEAN);
		userCountDistribution = new Poisson("UserCountDistribution", STANDARD_POISSON_DIST_MEAN);
		
	}
	
	@Override
	public void body(){
		System.out.println("Starting user base " + get_id() + " " + get_name());
		
		//Wait for Data Centers to initialize
		sim_pause(100);
		
		int id = 0;
    	long output_size = 300; //Nominal output size
    	int requestGroups;
    	int userCountForRequest;
    	int remainingUsers;    	
    	int messagesSent = 0;
    	int requestsSent = 0;
    	InternetCloudlet cloudlet;    	
		double currTime;		
		
		while (Sim_system.running() && !cancelled){
			currTime = GridSim.clock();
			
			userCountForRequest = getOnlineUsers(currTime);
			requestGroups = getCurrUserCountInGroups(userCountForRequest);
			
			for (int i = 0; i < requestGroups; i++){
				cloudlet = new InternetCloudlet(get_id() * 100000 + ++id, //Id need not be unique, just used for debugging
												 instructionLengthPerRequest, 
												 perRequestDataSize, 
												 output_size, 
												 this, 
												 Constants.DEFAULT_APP_ID,
												 userGroupingFactor);
				
				send(Constants.INTERNET, 0.0, Constants.REQUEST_INTERNET_CLOUDLET_TAG, cloudlet);
				messagesSent++;
			
				requestsSent += userGroupingFactor;
				
				currentRequests.put(cloudlet.getCloudletId(), currTime);	
				
				//System.out.println(currTime + ": userbase " + get_name() + " sent message " 
				//		+ cloudlet.getCloudletId() + " to internet with " + userGroupingFactor);
			}
			
			remainingUsers = userCountForRequest - (userGroupingFactor * requestGroups);
			if (remainingUsers > 0){
				cloudlet = new InternetCloudlet(get_id() * 100000 + ++id, //Id need not be unique, just used for debugging
										 instructionLengthPerRequest, 
										 perRequestDataSize, 
										 output_size, 
										 this, 
										 Constants.DEFAULT_APP_ID,
										 remainingUsers);
				
				send(Constants.INTERNET, 0.0, Constants.REQUEST_INTERNET_CLOUDLET_TAG, cloudlet);
				messagesSent++;
				
				requestsSent += remainingUsers;
				
				currentRequests.put(cloudlet.getCloudletId(), currTime);
				
				//System.out.println(currTime + ": userbase " + get_name() + " sent message " 
				//				+ cloudlet.getCloudletId() + " to internet with " + remainingUsers);
			}			

			
			sim_pause(getInterRequestDelay());

		}
				
		System.out.println(get_name() + " finalizing. Messages sent:" + messagesSent + ", Received:" + messagesReceived);
		System.out.println(get_name() + " requests sent=" + requestsSent + " , received=" + responsesReceived);
	}

	private long getInterRequestDelay(){
		long avgReqDelay = 3600000 / requestsPerUserPerHour; //ms
		return (avgReqDelay * requestDelayDistribution.sample() / STANDARD_POISSON_DIST_MEAN );
	}
	
	private int getCurrUserCountInGroups(int userCount){
		return userCount / userGroupingFactor;
	}

	private int getOnlineUsers(double time) {
		int avgUsers;
		if ((time > peakHours[0]) && (time < peakHours[1])){
			avgUsers = peakAvgUsers;
		} else {
			avgUsers = offPeakAvgUsers;
		}
		
		return (int) (avgUsers * userCountDistribution.sample() / STANDARD_POISSON_DIST_MEAN);
	}
	
	public int getRegion() {
		return region;
	}
	
	public synchronized void cancelRun(){
		cancelled = true;
	}


	/**
	 * @return the responsesReceived
	 */
	public int getResponsesReceived() {
		return responsesReceived;
	}
	

	
	/** 
	 * ResponseHandler receives the responses to requests sent by {@link UserBase}, in a separate
	 * thread, so the traffic generation is independent of responses.
	 * 
	 */
	private class ResponseHandler extends CloudSim {
		
		public ResponseHandler(String name) throws Exception {
			super(name);
		}

		public void body(){
			while (Sim_system.running()){
				Sim_event e = new Sim_event();
				sim_get_next(e);
				if (e.get_data() instanceof InternetCloudlet){
					InternetCloudlet cl = (InternetCloudlet) e.get_data();
					int requestCount = cl.getRequestCount();
					
					//Reflect completion of request transmission in traffic levels
					InternetCharacteristics.getInstance().removeTraffic((CommPath) cl.getData(Constants.PARAM_COMM_PATH), 
																		 requestCount);
					
					messagesReceived++;
					responsesReceived += requestCount;
					
					//Reflect completion of request transmission in traffic levels
					InternetCharacteristics.getInstance().removeTraffic((CommPath) cl.getData(Constants.PARAM_COMM_PATH), 
																		 requestCount);
								
					Double startTime = currentRequests.remove(cl.getCloudletId());
					if (startTime != null){
						double endTime = Sim_system.sim_clock();
						
						stat.update(Constants.UB_RESPONSE_TIME, startTime, endTime);
						hourlyResponseTimeStat.update(startTime, endTime);
						
						//System.out.println(GridSim.clock() + ": Userbase " + UserBase.this.get_name() + " got response " + e.get_data() + " delay=" + (endTime - startTime));
						
						Double procTime = (Double) cl.getData("procTime");
						if (procTime > (endTime - startTime)){
							throw new RuntimeException("OOOPS");
						}
					}
				}
			}			
		}
		
	}


}
