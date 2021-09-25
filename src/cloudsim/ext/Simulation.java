package cloudsim.ext;

import eduni.simjava.Sim_stat;
import eduni.simjava.Sim_system;
import gridsim.GridSim;
import gridsim.MachineList;
import gridsim.PE;
import gridsim.PEList;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.JOptionPane;

import cloudsim.Cloudlet;
import cloudsim.CloudletList;
import cloudsim.DataCenter;
import cloudsim.DatacenterCharacteristics;
import cloudsim.Host;
import cloudsim.SimpleBWProvisioner;
import cloudsim.SimpleMemoryProvisioner;
import cloudsim.SimpleVMProvisioner;
import cloudsim.SpaceSharedAllocationPolicy;
import cloudsim.TimeSharedAllocationPolicy;
import cloudsim.TimeSharedVMScheduler;
import cloudsim.TimeSharedWithPriorityAllocationPolicy;
import cloudsim.TimeSpaceSharedAllocationPolicy;
import cloudsim.VMCharacteristics;
import cloudsim.VMMAllocationPolicy;
import cloudsim.VirtualMachine;
import cloudsim.VirtualMachineList;
import cloudsim.ext.datacenter.DatacenterController;
import cloudsim.ext.event.BaseCloudSimObservable;
import cloudsim.ext.event.CloudSimEvent;
import cloudsim.ext.event.CloudSimEventListener;
import cloudsim.ext.event.CloudSimEvents;
import cloudsim.ext.gui.DataCenterUIElement;
import cloudsim.ext.gui.MachineUIElement;
import cloudsim.ext.gui.UserBaseUIElement;
import cloudsim.ext.gui.VmAllocationUIElement;
import cloudsim.ext.gui.utils.SimMeasure;
import cloudsim.ext.servicebroker.BestResponseTimeServiceBroker;
import cloudsim.ext.servicebroker.CloudAppServiceBroker;
import cloudsim.ext.servicebroker.DynamicServiceBroker;
import cloudsim.ext.servicebroker.ServiceProximityServiceBroker;
import cloudsim.ext.stat.HourlyEventCounter;
import cloudsim.ext.util.InternetEntitityRegistry;
import cloudsim.ext.util.ObservableList;

/**
 * Main controller class of the simulation.
 * 
 * @author Bhathiya Wickremasinghe
 *
 */
public class Simulation extends BaseCloudSimObservable implements Constants {

	//	private static CloudletList cloudletList;
	private static VirtualMachineList vmlist;

	private final ObservableList<DataCenterUIElement> dataCenters;
	private List<DatacenterController> dcbs;
	private List<DataCenter> dcs;
	private final ObservableList<UserBaseUIElement> userBases;
	private List<UserBase> ubs;
	private double simulationTime;
	private String serviceBrokerPolicy = Constants.BROKER_POLICY_PROXIMITY;
	private String loadBalancePolicy = Constants.LOAD_BALANCE_POLICY_RR;
	private int userGroupingFactor = 10;
	private int dcRequestGroupingFactor = 10;
	private int instructionLengthPerRequest = 100;
	private CloudSimEventListener progressListener;
	private Map<String, Object> results;
	private Internet internet;
	
	/** Constructor. */
	public Simulation(CloudSimEventListener gui) throws Exception {
		
		this.progressListener = gui;
		addCloudSimEventListener(gui);
		
		dataCenters = new ObservableList<DataCenterUIElement>();
		userBases = new ObservableList<UserBaseUIElement>();
				
		InternetEntitityRegistry.initialize(userBases, dataCenters);
		
		createDefaultSimulation();
	}
	
	/** 
	 * Populates the initial default simulation displayed at the start.
	 */
	private void createDefaultSimulation(){
		DataCenterUIElement dataCenter = new DataCenterUIElement(DEFAULT_DATA_CENTER_NAME,
																 DEFAULT_DC_REGION, 
																 DEFAULT_ARCHITECTURE,
																 DEFAULT_OS,
																 DEFAULT_VMM,
																 DEFAULT_COST_PER_PROC,
														 		 DEFAULT_COST_PER_MEM,
																 DEFAULT_COST_PER_STOR,
																 DEFAULT_COST_PER_BW);
		MachineUIElement machine1 = new MachineUIElement(DEFAULT_MC_MEMORY,
														 DEFAULT_MC_STORAGE,
														 DEFAULT_MC_BW,
														 DEFAULT_MC_PROCESSORS,
														 DEFAULT_MC_SPEED,
														 MachineUIElement.VmAllocationPolicy.TIME_SHARED);
		MachineUIElement machine2 = new MachineUIElement(DEFAULT_MC_MEMORY,
														 DEFAULT_MC_STORAGE,
														 DEFAULT_MC_BW,
														 DEFAULT_MC_PROCESSORS,
														 DEFAULT_MC_SPEED,
														 MachineUIElement.VmAllocationPolicy.TIME_SHARED );
		List<MachineUIElement> machineList = new ArrayList<MachineUIElement>();
		machineList.add(machine1);
		machineList.add(machine2);
		dataCenter.setMachineList(machineList);
		dataCenters.add(dataCenter);
		VmAllocationUIElement vmAllocation = new VmAllocationUIElement(dataCenter, 
																	   DEFAULT_VM_COUNT, 
																	   DEFAULT_VM_IMAGE_SIZE, 
																	   DEFAULT_VM_MEMORY,
																	   DEFAULT_VM_BW);
		dataCenter.setVmAllocation(vmAllocation);
				
		userBases.add(new UserBaseUIElement(DEFAULT_USER_BASE_NAME, 
											DEFAULT_UB_REGION, 
											DEFAULT_REQ_PER_USER_PER_HR,
											DEFAULT_REQ_SIZE,
											DEFAULT_PEAK_HOURS,
											DEFAULT_PEAK_USERS,
											DEFAULT_OFFPEAK_USERS));
		simulationTime = 3600000.0;
		
	}
	
	/**
	 * Creates and runs the simulation from the configuration obtained by the GUI.
	 */
	public void runSimulation() throws Exception {
		System.out.println("Starting Simulation...");
	
		//Set up stuff
		int num_user = 1; // number of grid users
		Calendar calendar = Calendar.getInstance();
		boolean trace_flag = false; // mean trace GridSim events
		String[] exclude_from_file = { "" };
		String[] exclude_from_processing = { "" };
		String report_name = null;

		//Initialize GridSim
		GridSim.init(num_user, calendar, trace_flag, exclude_from_file,
				exclude_from_processing, report_name);
		
		// Create Datacenters and Controllers
		dcbs  = new ArrayList<DatacenterController>();
		dcs =  new ArrayList<DataCenter>();
		for (DataCenterUIElement d : dataCenters) {
			if (d.isAllocated()){
				DataCenter dc = createDatacenter(d);
				DatacenterController controller = createBroker(d.getName(), 	
														       d.getRegion(), 
														       d.getCostPerProcessor(), 
														       d.getCostPerBw());
				dcbs.add(controller);
				dcs.add(dc);
				
				int brokerId = controller.get_id();
				vmlist = createVM(brokerId, d.getVmAllocation().getVmCount());
				controller.submitVMList(vmlist);
			}
		}
		
		//Create user bases
		ubs  = new ArrayList<UserBase>();
		for (UserBaseUIElement ub : userBases) {
			UserBase userBase = new UserBase(ub.getName(),
											 ub.getRegion(),
											 ub.getReqPerHrPerUser(),
											 new int[]{ub.getPeakHoursStart(), ub.getPeakHoursEnd()}, 
											 ub.getPeakUserCount(), 
											 ub.getOffPeakUserCount(),
											 ub.getReqSize(),
											 userGroupingFactor,
											 instructionLengthPerRequest);
			ubs.add(userBase);
		}

		//The Internet
		internet = new Internet(progressListener);
		
		CloudAppServiceBroker serviceBroker;
		if (serviceBrokerPolicy.equals(Constants.BROKER_POLICY_PROXIMITY)){
			serviceBroker = new ServiceProximityServiceBroker();
		} else if (serviceBrokerPolicy.equals(Constants.BROKER_POLICY_DYNAMIC)){
			serviceBroker = new DynamicServiceBroker(dcbs);
		} else {
			serviceBroker = new BestResponseTimeServiceBroker();
		}
		internet.addServiceBroker(DEFAULT_APP_ID, serviceBroker); 				
		
		//Set the simulation duration
		Sim_system.set_termination_condition(Sim_system.TIME_ELAPSED, simulationTime, false);
		
		//Start the simulation
		GridSim.startGridSimulation();
		

		// Comes here when the simulation has completed.
		// Gather the results and package them for the results screen
		results = new HashMap<String, Object>();
		results.put(Constants.SIMULATION_COMPLETED_TIME, new Date());
		
		Map<String, HourlyEventCounter> dcArrivalStats = new HashMap<String, HourlyEventCounter>();
		Map<String, HourlyEventCounter> dcLoadingStats = new HashMap<String, HourlyEventCounter>();
		Map<String, SimMeasure> dcProcTimes = new TreeMap<String, SimMeasure>();
		Map<String, Map<String, Double>> costs = new HashMap<String, Map<String,Double>>();
		HourlyEventCounter hrlyArrivalStat = null;
		double vmCost, dataCost, totalCost;
		
		for (DatacenterController dcb : dcbs) {
			hrlyArrivalStat = dcb.getHourlyArrival();				
			String dcName = dcb.get_name().substring(0, dcb.get_name().indexOf("-Broker"));
			String dcbName = dcName;
			dcArrivalStats.put(dcbName, hrlyArrivalStat);

			Map<String, Double> dcCosts = new HashMap<String, Double>();
			vmCost = dcb.getVmCost();
			dcCosts.put(Constants.VM_COST, vmCost);				
			dataCost = dcb.getDataTransferCost();
			dcCosts.put(Constants.DATA_COST, dataCost);
			totalCost = vmCost + dataCost;
			dcCosts.put(Constants.TOTAL_COST, totalCost);
			
			costs.put(dcName, dcCosts);
			
			Sim_stat stat = dcb.get_stat();
			List res = stat.get_measures();
			for (Object o : res) {
				Object[] oArray = (Object[]) o;
				String measure = (String) oArray[0];

				SimMeasure m = new SimMeasure();
				m.setName(measure);
				m.setEntityName(dcName);
				m.setType(MEASURE_TYPE_DC_PROCESSING_TIME);
				m.setAvg(stat.average(measure));
				m.setMin(stat.minimum(measure));
				m.setMax(stat.maximum(measure));
				m.setCount(dcb.getAllRequestsProcessed());

				dcProcTimes.put(dcName + "||" + measure, m);
			}
			
			printVmAllocations(dcName, dcb.getVmAllocationStats());
		}
		results.put(Constants.DC_PROCESSING_TIME_STATS, dcProcTimes);
		results.put(Constants.DC_ARRIVAL_STATS, dcArrivalStats);
		results.put(Constants.DC_OVER_LOADING_STATS, dcLoadingStats);
		results.put(Constants.COSTS, costs);
		
		for (DataCenter dc : dcs){
			dc.printDebts();
		}

		Map<String, SimMeasure> ubResults = new TreeMap<String, SimMeasure>();
		for (UserBase ub : ubs) {
			Sim_stat stat = ub.get_stat();
			String ubName = ub.get_name();
			
			List res = stat.get_measures();
			for (Object o : res) {
				Object[] oArray = (Object[]) o;
				String measure = (String) oArray[0];

				SimMeasure m = new SimMeasure();
				m.setName(measure);
				m.setEntityName(ubName);
				m.setType(MEASURE_TYPE_USER_BASE_RESPONSE);
				m.setAvg(stat.average(measure));
				m.setMin(stat.minimum(measure));
				m.setMax(stat.maximum(measure));
				m.setCount(ub.getResponsesReceived());

				ubResults.put(ubName + "||" + measure, m);
			}
		}
		results.put(Constants.UB_STATS, ubResults);
		
		//Finish off simulation
		System.out.println("Simulation finished at " + GridSim.clock());
		CloudSimEvent cloudSimEvent = new CloudSimEvent(CloudSimEvents.EVENT_SIMULATION_ENDED);
		fireCloudSimEvent(cloudSimEvent);

	}
	
	private void printVmAllocations(String dcName, Map<Integer, Integer> list){
		System.out.println("************ Vm allocations in " + dcName);
		for (Integer vm : list.keySet()){
			System.out.println(vm + "->" + list.get(vm));
		} 
	}

	@SuppressWarnings("unchecked")
	private VirtualMachineList createVM(int userID, int vms) {

		// Creates a container to store VMs. This list is passed to the broker
		// later
		VirtualMachineList list = new VirtualMachineList();

		//VM Parameters
		long size = 10000; //image size (MB)
		int memory = 512; //vm memory (MB)
		long bw = 1000;
		int vcpus = 1; //number of cpus
		int priority = 1;
		String vmm = "Xen"; //VMM name

		//create VMs
		VirtualMachine[] vm = new VirtualMachine[vms];

		for (int i = 0; i < vms; i++) {
			vm[i] = new VirtualMachine(new VMCharacteristics(i, userID, size,
					memory, bw, vcpus, priority, vmm,
					new TimeSharedVMScheduler()));
			//for creating a VM with a space shared scheduling policy for cloudlets:
			//vm[i] = new VirtualMachine(new VMCharacteristics(i,userID,size,memory,bw,vcpus,vmm,new SpaceSharedVMScheduler()));

			list.add(vm[i]);
		}

		return list;
	}


	@SuppressWarnings("unchecked")
	private DataCenter createDatacenter(DataCenterUIElement dc) {

		MachineList mList = new MachineList();
		
		for (int mcNo = 0; mcNo < dc.getMachineList().size(); mcNo++){
			MachineUIElement mc = dc.getMachineList().get(mcNo);
			
			PEList peList1 = new PEList();
			for (int i = 0; i < mc.getProcessors(); i++){
				peList1.add(new PE(i, mc.getSpeed()));
			}
			
			
			VMMAllocationPolicy vmPolicy;
			if (mc.getVmAllocationPolicy().equals(MachineUIElement.VmAllocationPolicy.TIME_SHARED)){
				vmPolicy = new TimeSharedAllocationPolicy(peList1); 
			} else if (mc.getVmAllocationPolicy().equals(MachineUIElement.VmAllocationPolicy.SPACE_SHARED)){
				vmPolicy = new SpaceSharedAllocationPolicy(peList1);
			} else if (mc.getVmAllocationPolicy().equals(MachineUIElement.VmAllocationPolicy.TIME_SHARED_W_PRIORITY)){
				vmPolicy = new TimeSharedWithPriorityAllocationPolicy(peList1);
			} else {
				vmPolicy = new TimeSpaceSharedAllocationPolicy(peList1);
			}
			
			Host h = new Host(mcNo, 
							  mc.getMemory(), 
							  mc.getStorage(), 
							  mc.getBw(), 
							  peList1,
							  new SimpleMemoryProvisioner(), 
							  new SimpleBWProvisioner(),
							  vmPolicy);
			mList.add(h);
		}
		
		double time_zone = WorldGeometry.getInstance().getTimeZone(dc.getRegion());
		LinkedList storageList = new LinkedList(); //we are not adding SAN devices by now

		DatacenterCharacteristics resConfig = new DatacenterCharacteristics(dc.getArchitecture(), 
																			dc.getOs(), 
																			dc.getVmm(), 
																			mList, 
																			time_zone, 
																			dc.getCostPerProcessor(),
																			dc.getCostPerMem(),
																			dc.getCostPerStorage(),
																			dc.getCostPerBw());
		DataCenter datacenter = null;
		try {
			datacenter = new DataCenter(dc.getName(), resConfig, new SimpleVMProvisioner(), storageList);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return datacenter;
	}

	//We strongly encourage users to develop their own broker policies, to submit vms and cloudlets according
	//to the specific rules of the simulated scenario
	private DatacenterController createBroker(String namePrefix, 
														 int region, 
														 double costPerVmHour,
														 double costPerDataGB) {

		DatacenterController broker = null;
		try {
			broker = new DatacenterController(namePrefix, 
												  region,
												  costPerVmHour,
												  costPerDataGB,
												  dcRequestGroupingFactor,
												  loadBalancePolicy);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return broker;
	}

	/**
	 * Prints the Cloudlet objects
	 * @param list  list of Cloudlets
	 */
	private static void printCloudletList(CloudletList list) {
		int size = list.size();
		Cloudlet cloudlet;

		String indent = "    ";
		System.out.println();
		System.out.println("========== OUTPUT ==========");
		System.out.println("Cloudlet ID" + indent + "STATUS" + indent
				+ "Resource ID" + indent + "VM ID" + indent + indent + indent
				+ "Time" + indent + "Start Time" + indent + "Finish Time");

		for (int i = 0; i < size; i++) {
			cloudlet = (Cloudlet) list.get(i);
			System.out
					.print(indent + cloudlet.getGridletID() + indent + indent);

			if (cloudlet.getGridletStatus() == Cloudlet.SUCCESS) {
				System.out.print("SUCCESS");

				DecimalFormat dft = new DecimalFormat("###.##");
				System.out.println(indent + indent + cloudlet.getResourceID()
						+ indent + indent + indent + cloudlet.getVmId()
						+ indent + indent + indent
						+ dft.format(cloudlet.getActualCPUTime()) + indent
						+ indent + dft.format(cloudlet.getExecStartTime())
						+ indent + dft.format(cloudlet.getFinishTime()));
			}
		}

	}
	

	/**
	 * @return the simulationTime
	 */
	public double getSimulationTime() {
		return simulationTime;
	}

	/**
	 * @param simulationTime the simulationTime to set
	 */
	public void setSimulationTime(double simulationTime) {
		System.out.println("simulation time =" + simulationTime + "ms");
		this.simulationTime = simulationTime;
	}
	
	/**
	 * @return the dataCenters
	 */
	public ObservableList<DataCenterUIElement> getDataCenters() {
		return dataCenters;
	}
	
	/**
	 * @return the userBases
	 */
	public ObservableList<UserBaseUIElement> getUserBases() {
		return userBases;
	}

	/**
	 * @return the results
	 */
	public Map<String, Object> getResults() {
		return results;
	}	
		
	public boolean isRunning(){
		return internet.isRunning();
	}

	/**
	 * @return the userGroupingFactor
	 */
	public int getUserGroupingFactor() {
		return userGroupingFactor;
	}

	/**
	 * @param userGroupingFactor the userGroupingFactor to set
	 */
	public void setUserGroupingFactor(int userGroupingFactor) {
		this.userGroupingFactor = userGroupingFactor;
	}

	/**
	 * @return the dcCloudletGroupingFactor
	 */
	public int getDcRequestGroupingFactor() {
		return dcRequestGroupingFactor;
	}

	/**
	 * @param dcCloudletGroupingFactor the dcCloudletGroupingFactor to set
	 */
	public void setDcRequestGroupingFactor(int dcCloudletGroupingFactor) {
		this.dcRequestGroupingFactor = dcCloudletGroupingFactor;
	}

	/**
	 * @return the instructionLengthPerRequest
	 */
	public int getInstructionLengthPerRequest() {
		return instructionLengthPerRequest;
	}

	/**
	 * @param instructionLengthPerRequest the instructionLengthPerRequest to set
	 */
	public void setInstructionLengthPerRequest(int instructionLengthPerRequest) {
		this.instructionLengthPerRequest = instructionLengthPerRequest;
	}
	
	public void cancelSimulation(){
		for (UserBase ub : ubs){
			ub.cancelRun();
		}
	}

	/**
	 * @param serviceBrokerPolicy the serviceBrokerPolicy to set
	 */
	public void setServiceBrokerPolicy(String serviceBrokerPolicy) {
		this.serviceBrokerPolicy = serviceBrokerPolicy;
	}

	/**
	 * @return the serviceBrokerPolicy
	 */
	public String getServiceBrokerPolicy() {
		return serviceBrokerPolicy;
	}

	/**
	 * @return the loadBalancePolicy
	 */
	public String getLoadBalancePolicy() {
		return loadBalancePolicy;
	}

	/**
	 * @param loadBalancePolicy the loadBalancePolicy to set
	 */
	public void setLoadBalancePolicy(String loadBalancePolicy) {
		this.loadBalancePolicy = loadBalancePolicy;
	}
	
	
	
//	public static void main(String[] args) throws Exception{
//		Simulation sim = new Simulation(null);
//		sim.testClassLoading();
//	}
}
