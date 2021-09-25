package cloudsim.ext;

import gridsim.GridSim;
import gridsim.util.Poisson;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import cloudsim.ext.util.CommPath;
import cloudsim.ext.util.IOUtil;

/**
 * This class holds the data that define the behaviour of the Internet which includes latencies
 * and bandwidth, and also provides methods to access those information.
 *
 * This class is a singleton.
 *
 * @author Bhathiya Wickremasinghe
 *
 */
public class InternetCharacteristics {

	private static final int STANDARD_POISSON_MEAN = 100;

	private static InternetCharacteristics instance = null;
	private double[][] latencyMatrix;
	private double[][] bwMatrix;
	private Map<String, Integer> entityLocations = null;
	private List<GeoLocatable> allEntities = null;
	private final Map<Integer, List<Integer>> proximityCache;
	private final Map<CommPath, Long> trafficLevels;
	private final Map<String, Double[]> serviceLatencies;

	private final Poisson availableBwDistribution;
	private final Poisson pingDelayDistribution;

	/**
	 * No args constructor.
	 *
	 * @throws IOException - If loading of the latency matrix or bw matrix fails.
	 * @throws URISyntaxException
	 */
	private InternetCharacteristics() throws IOException, URISyntaxException{
		entityLocations = new HashMap<String, Integer>();
		allEntities = new ArrayList<GeoLocatable>();

		latencyMatrix = (double[][]) IOUtil.loadFromXml(getClass().getClassLoader().getResourceAsStream(Constants.DELAYMATRIX_FILE));
		bwMatrix = (double[][]) IOUtil.loadFromXml(getClass().getClassLoader().getResourceAsStream(Constants.BWMATRIX_FILE));

		proximityCache = new HashMap<Integer, List<Integer>>();
		trafficLevels = Collections.synchronizedMap(new HashMap<CommPath, Long>());
		serviceLatencies = Collections.synchronizedMap(new HashMap<String, Double[]>());

		pingDelayDistribution = new Poisson("PingDelayDistribution", STANDARD_POISSON_MEAN);
		availableBwDistribution = new Poisson("AvailableBwDistribution", STANDARD_POISSON_MEAN);
	}

	/**
	 * Returns the singleton instance
	 *
	 * @return
	 */
	public static InternetCharacteristics getInstance(){
		if (instance == null){
			try {
				instance = new InternetCharacteristics();
			} catch (IOException e) {
				throw new RuntimeException("Failed to load delay matrix. Please make sure config file is" +
						" available at config/delaymatrix.xml");
			} catch (URISyntaxException e) {
				throw new RuntimeException("Failed to load delay matrix. Please make sure config file is" +
						" available at config/delaymatrix.xml");
			}
		}

		return instance;
	}

	/**
	 * @return the delayMatrixs
	 */
	public double[][] getLatencyMatrix() {
		return latencyMatrix;
	}

	/**
	 * @param delayMatrix the delayMatrix to set
	 */
	public void setLatencyMatrix(double[][] delayMatrix) {
		this.latencyMatrix = delayMatrix;
	}

	/**
	 * Adds an entity ({@link GeoLocatable}) to the repository.
	 *
	 * @param entity
	 */
	public void addEntity(GeoLocatable entity){
		int region = entity.getRegion();
		String name = entity.get_name();
		entityLocations.put(name, region);
		allEntities.add(entity);
	}

	/**
	 * Updates the service latency staticistics for a data center.
	 */
	public void updateSerivceLatency(String serviceProvider, Double delay){
		serviceLatencies.put(serviceProvider, new Double[]{delay, GridSim.clock()});
	}

	/**
	 *
	 * @return
	 */
	public Map<String, Double[]> getServiceLatencies(){
		return serviceLatencies;
	}

	/**
	 * @return the allEntities
	 */
	public List<GeoLocatable> getAllEntities() {
		return allEntities;
	}

	/**
	 * Returns the data transfer delay, based on the current traffic level and bw matrix.
	 *
	 * @param src
	 * @param dest
	 * @param reqSize
	 * @return
	 */
	public double getDataTransferDelay(String src, String dest, double reqSize){
		int srcRegion = entityLocations.get(src);
		int destRegion = entityLocations.get(dest);

		return getDataTransferDelay(new CommPath(srcRegion, destRegion), reqSize);
	}

	/**
	 * Returns the data transfer delay, based on the current traffic level and bw matrix.
	 *
	 * @param commPath
	 * @param perUserReqSize
	 * @return
	 */
	public double getDataTransferDelay(CommPath commPath, double perUserReqSize){
		double availableBw = bwMatrix[commPath.getRegion1()][commPath.getRegion2()] * 1024 * 1024; //in bytes

		double avgPerUserBw;
		if (trafficLevels.containsKey(commPath)){
			long currentTraffic = trafficLevels.get(commPath);
			avgPerUserBw = availableBw / currentTraffic;
		} else {
			avgPerUserBw = availableBw;
		}

		double transmissionTime = (perUserReqSize /
								 (avgPerUserBw / STANDARD_POISSON_MEAN * availableBwDistribution.sample())) //in seconds
										* 1000;   			   //in ms

		return transmissionTime;
	}

	/**
	 * Returns the total delay. i.e. network latency + data transfer delay
	 * @param src
	 * @param dest
	 * @param reqSize
	 * @return
	 */
	public double getTotalDelay(String src, String dest, double reqSize){
		int srcRegion = entityLocations.get(src);
		int destRegion = entityLocations.get(dest);

		CommPath commPath = new CommPath(srcRegion, destRegion);
		double totalDelay = latencyMatrix[srcRegion][destRegion] * pingDelayDistribution.sample() / STANDARD_POISSON_MEAN
		                    + getDataTransferDelay(commPath, reqSize);

		return totalDelay;
	}

	/**
	 * Returns a list of region id's ordered in the ascending order of lowest latency
	 * @param requestorRegion
	 * @return
	 */
	public List<Integer> getProximityList(int requestorRegion){

		List<Integer> proximityList = proximityCache.get(requestorRegion);

		if (proximityList != null){
			return proximityList;
		} else {
			List<DelayEntry> delays = new ArrayList<DelayEntry>();

			for (int i = 0; i < latencyMatrix.length; i++){
				delays.add(new DelayEntry(requestorRegion, i, latencyMatrix[requestorRegion][i]));
			}

			Collections.sort(delays);

			proximityList = new LinkedList<Integer>();
			for (DelayEntry e : delays){
				proximityList.add(e.getDestRegion());
			}

			proximityCache.put(requestorRegion, proximityList);

			return proximityList;
		}
	}

	/**
	 * @return the bwMatrix
	 */
	public double[][] getBwMatrix() {
		return bwMatrix;
	}

	/**
	 * @param bwMatrix the bwMatrix to set
	 */
	public void setBwMatrix(double[][] bwMatrix) {
		this.bwMatrix = bwMatrix;
	}

	public CommPath addTraffic(String src, String dest, long requestCount){
		int srcRegion = entityLocations.get(src);
		int destRegion = entityLocations.get(dest);

		CommPath commPath = new CommPath(srcRegion, destRegion);
		addTraffic(commPath, requestCount);

		return commPath;
	}

	public void addTraffic(CommPath path, long requestCount){
		long currentLevel = 0;
		if (trafficLevels.containsKey(path)){
			currentLevel = trafficLevels.remove(path);
		}

		currentLevel += requestCount;

		trafficLevels.put(path, currentLevel);

//		System.out.println("Traffic level:" + path + ":" + currentLevel);
	}

	public void removeTraffic(String src, String dest, long requestCount){
		int srcRegion = entityLocations.get(src);
		int destRegion = entityLocations.get(dest);

		removeTraffic(new CommPath(srcRegion, destRegion), requestCount);
	}

	public void removeTraffic(CommPath path, long requestCount){
		long currentLevel = 0;
		if (trafficLevels.containsKey(path)){
			currentLevel = trafficLevels.remove(path);
			currentLevel -= requestCount;

			if (currentLevel < 0){
//				throw new RuntimeException("Traffic level is negative. Please check the logic.");
				currentLevel = 0;
			}

			trafficLevels.put(path, currentLevel);
		}

//		System.out.println("Traffic level:" + path + ":" + currentLevel);
	}

//	private void printMatrix(double[][] matrix){
//		System.out.println("Loaded delay matrix:");
//		for (int row = 0; row < matrix.length; row++){
//			for (int col = 0; col < matrix.length; col++){
//				System.out.print(matrix[row][col] + "\t");
//			}
//			System.out.println("");
//		}
//	}

	private class DelayEntry implements Comparable<DelayEntry> {
		private final int srcRegion;
		private final int destRegion;
		private final double delay;

		public DelayEntry(int srcRegion, int destRegion, double delay) {
			super();
			this.srcRegion = srcRegion;
			this.destRegion = destRegion;
			this.delay = delay;
		}

		/**
		 * @return the srcRegion
		 */
		public int getSrcRegion() {
			return srcRegion;
		}

		/**
		 * @return the destRegion
		 */
		public int getDestRegion() {
			return destRegion;
		}

		/**
		 * @return the delay
		 */
		public double getDelay() {
			return delay;
		}

		public int compareTo(DelayEntry other) {
			int LESS = -1;
			int EQUAL = 0;
			int GREATER = 1;

			if (other == this){
				return EQUAL;
			}

			if (this.delay < other.getDelay()){
				return LESS;
			} else if (this.delay > other.getDelay()) {
				return GREATER;
			} else {
				return EQUAL;
			}
		}

		@Override
		public int hashCode(){
			String s = "" + srcRegion + destRegion + delay;
			return s.hashCode();
		}

		@Override
		public boolean equals(Object other){
			if (other == this){
				return true;
			}

			if (!(other instanceof DelayEntry)){
				return false;
			}

			return (this.hashCode() == other.hashCode());
		}
	}



}
