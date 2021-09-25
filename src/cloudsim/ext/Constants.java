package cloudsim.ext;

public interface Constants {

	final String STANDARD_SEPARATOR = "-";

	final int WORLD_REGIONS = 6;
	final String INTERNET = "Internet";

	final int REQUEST_INTERNET_CLOUDLET_TAG = 2001;

	final int RESPONSE_INTERNET_CLOUDLET_TAG = 2002;

	final String MEASURE_TYPE_OVERALL_USER_BASE_RESPONSE = "Overall userbase response time";
	final String MEASURE_TYPE_USER_BASE_RESPONSE = "Userbase Response Time";
	final String MEASURE_TYPE_DC_PROCESSING_TIME = "DC Processing Time";
	final String UB_RESPONSE_TIME = "UB Response time";
	final String HOURLY_RESPONSE_TIME = "Hourly Response Time";
	final String DC_SERVICE_TIME = "Service time";

	final String INTERNET_ENTITIES = "internet_entities";

	final String DELAYMATRIX_FILE = "config/delaymatrix.xml";
	final String BWMATRIX_FILE = "config/bwmatrix.xml";

	final String PARAM_DATA_ELEMENT = "data_element";
	final String PARAM_VM_ID = "vm_id";
	final String PARAM_TIME = "time";
	final String PARAM_PROCESSING_TIME = "processing_time";
	final String PARAM_COMM_PATH = "commPath";

	final double MILLI_SECONDS_TO_MINS = 1000 * 60;
	final double MILLI_SECONDS_TO_HOURS = 1000 * 60 * 60;
	final double MILLI_SECONDS_TO_DAYS = 1000 * 60 * 60 * 24;

	final int DEFAULT_APP_ID = 1;
	final String SIMULATION_COMPLETED_TIME = "sim_completed_at";
	final String PDF_EXTENSION = ".pdf";

	//Default Data Center characteristics
	final String DEFAULT_DATA_CENTER_NAME = "DC1";
	final String DEFAULT_ARCHITECTURE = "x86";
	final String DEFAULT_OS = "Linux";
	final String DEFAULT_VMM = "Xen";
	final int DEFAULT_DC_REGION = 0;
	final int DEFAULT_VM_COUNT = 5;
	final double DEFAULT_COST_PER_PROC = 0.1; // the cost of using processing in this resource
	final double DEFAULT_COST_PER_MEM = 0.05; // the cost of using memory in this resource
	final double DEFAULT_COST_PER_STOR = 0.1; // the cost of using storage in this resource
	final double DEFAULT_COST_PER_BW = 0.1;

	final int DEFAULT_MC_MEMORY = 204800;
	final long DEFAULT_MC_STORAGE = 100000000;
	final int DEFAULT_MC_BW = 1000000;
	final int DEFAULT_MC_PROCESSORS =  4;
	final int DEFAULT_MC_SPEED = 10000;

	final long DEFAULT_VM_IMAGE_SIZE = 10000;//MB
	final int DEFAULT_VM_MEMORY = 512;//MB
	final long DEFAULT_VM_BW = 1000;

	//Default User base characteristics
	final String DEFAULT_USER_BASE_NAME = "UB1";
	final int DEFAULT_UB_REGION = 2;
	final int DEFAULT_REQ_PER_USER_PER_HR = 60;
	final long DEFAULT_REQ_SIZE = 100;
	final int[] DEFAULT_PEAK_HOURS = new int[]{3, 9};
	final int DEFAULT_PEAK_USERS = 1000;
	final int DEFAULT_OFFPEAK_USERS = 100;


	final String UB_STATS = "UB stats";
	final String DC_ARRIVAL_STATS = "DC stats";
	final String DC_PROCESSING_TIME_STATS = "DC processing time stats";
	final String DC_OVER_LOADING_STATS = "DC overloading stats";
	final String COSTS = "Costs";
	final String VM_COST = "VM Cost";
	final String DATA_COST = "Data Cost";
	final String TOTAL_COST = "Total Cost";

	final String BROKER_POLICY_PROXIMITY = "Closest Data Center";
	final String BROKER_POLICY_OPTIMAL_RESPONSE = "Optimise Response Time";
	final String BROKER_POLICY_DYNAMIC = "Reconfigure Dynamically with Load";
	final String LOAD_BALANCE_ACTIVE = "Equally Spread Current Execution Load";
	final String LOAD_BALANCE_THROTTLED = "Location Aware";

	final String LOAD_BALANCE_POLICY_RR = "Round Robin";
	final String LOAD_BALANCE_WRR = "Weighted Round Robin";
	final String LOAD_BALANCE_SHORTEST_JOB_FIRST = "SJF loadbalancer";
	final String LOAD_BALANCE_ANT_COLONY = "Ant Colony LB";
	final String LOAD_BALANCE_HONEY_COLONY = "Honey Bee Foraging loadbalancer";
	final String LOAD_BALANCE_PSO = "PSO LoadBalancer";
	final String LOAD_BALANCE_THRESHOLD = "Threshold based LB";
	
}
