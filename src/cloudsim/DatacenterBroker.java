/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation
 *               of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009, The University of Melbourne, Australia
 */


package cloudsim;

import java.util.Iterator;
import java.util.LinkedList;

import eduni.simjava.Sim_event;
import eduni.simjava.Sim_system;

import gridsim.GridSim;
import gridsim.GridSimTags;

/**
 * DatacentreBroker represents a broker
 * acting on behalf of a user. It hides VM management,
 * as vm creation, sumbission of cloudlets to this VMs
 * and destruction of VMs.
 *
 * @author       Rodrigo N. Calheiros
 * @since        CloudSim Toolkit 1.0 Beta
 * @invariant	 $none
 */
public class DatacenterBroker extends CloudSim {
	
	public VirtualMachineList vmlist;
	protected CloudletList cllist;
	protected CloudletList receiveList;
	protected int datacenters;
	protected int contactedDatacenters;
	protected int vmsCreated;
	protected int vmsRequested;
	protected int vmsAcks;
	protected int vmsDestroyed;
	protected int cloudletsSubmitted;
	protected int cloudletsFinished;
	protected int[] datacenterID;
	protected int[] vmMapping;
	protected DatacenterCharacteristics[] datacenterChar;
	protected boolean[] clSubmitted;
	
	
	/**
	 * Created a new DatacenterBroker object.
	 * @param name		name to be associated with this entity (as
     *                  required by Sim_entity class from simjava package)
	 * @throws Exception
	 * @pre name != null
     * @post $none
	 */
	public DatacenterBroker(String name) throws Exception {
		super(name);
		this.vmlist=new VirtualMachineList();
		this.cllist=new CloudletList();
		this.receiveList=new CloudletList();
		this.datacenters=0;
		this.contactedDatacenters=0;
		this.vmsCreated=0;
		this.vmsDestroyed=0;
		this.cloudletsSubmitted=0;
		this.cloudletsFinished=0;
	}
	
	/**
	 * This method is used to send to the broker the list with
	 * virtual machines that must be created.
	 * @param list
	 * @pre list !=null
	 * @post $none
	 */
	@SuppressWarnings("unchecked")
	public void submitVMList(VirtualMachineList list){
		
		Iterator iter = list.iterator();
		while(iter.hasNext()){
			
			VirtualMachine vm = (VirtualMachine) iter.next();
			vmlist.add(vm);
		}
		
		this.vmMapping = new int[list.size()];
		for(int i=0;i<vmMapping.length;i++) vmMapping[i]=-1;
		
	}
	
	/**
	 * This method is used to send to the broker the list of
	 * cloudlets.
	 * @param list
	 * @pre list !=null
	 * @post $none
	 */
	@SuppressWarnings("unchecked")
	public void submitCloudletList(CloudletList list){
		
		Iterator iter = list.iterator();
		while(iter.hasNext()){
			
			Cloudlet cl = (Cloudlet) iter.next();
			this.cllist.add(cl);
		}
		this.clSubmitted = new boolean[this.cllist.size()];
		for(int i=0;i<this.clSubmitted.length;i++) clSubmitted[i]=false;
	}
	
	/**
	 * Specifies that a given cloudlet must run in a specific virtual machine
	 * @param cloudletId ID of the cloudlet being bount to a vm
	 * @param vmId ID of the virtual machine
	 * @pre cloudletId > 0
	 * @pre vmId > 0
	 * @post $none
	 */
	public void bindCloudletToVM(int cloudletId, int vmId){
		
		Cloudlet cl = findCloudletbyId(cloudletId);
		cl.setVmId(vmId);
	}
	
	/**
	 * Returns the cloudlet list of this broker
	 * @return the cloudlet list
	 */
	public CloudletList getCloudletList(){
		return this.receiveList;
	}
	
	/**
     * Handles external events that are coming to this DatacenterBroker entity.
     * This method also queries <tt>GridInformationService</tt> about available
     * datacenters.
     * @pre $none
     * @post $none
     */
	public void body(){

		//queries GIS about available datacenters
		sim_process(5.0);
		LinkedList datacenterList = GridSim.getGridResourceList();
		System.out.println(GridSim.clock()+": "+this.get_name()+ ": Cloud Resource List received with "+datacenterList.size()+" resource(s)");
		
		//initilize fields
		this.datacenters= datacenterList.size();
		this.datacenterID=new int[this.datacenters];
		this.datacenterChar=new DatacenterCharacteristics[this.datacenters];
		for(int i=0;i<this.datacenterChar.length;i++) datacenterChar[i]=null;
		
		//queries datacenters about their characteristics
		for(int i=0;i<this.datacenters;i++) {
			Integer num = (Integer) datacenterList.get(i);
			datacenterID[i] = num.intValue();
			super.send(datacenterID[i], GridSimTags.SCHEDULE_NOW, GridSimTags.RESOURCE_CHARACTERISTICS, this.get_id());
		}
		
		//receives events and process them
		Sim_event ev = new Sim_event();
        while(Sim_system.running()){
            super.sim_get_next(ev);
        	        
            // if the simulation finishes then exit the loop
            if (ev.get_tag() == GridSimTags.END_OF_SIMULATION) break;

            // process the received event
            processEvent(ev);
        }//while(Sim_system.running())
	}//body
	
    /**
     * Processes events available for this Broker
     * @param ev    a Sim_event object
     * @pre ev != null
     * @post $none
     */
	protected void processEvent(Sim_event ev) {

		switch (ev.get_tag()){
			// Resource characteristics answer
	        case GridSimTags.RESOURCE_CHARACTERISTICS:
	        	processResourceCharacteristics(ev);
	            break;
	        // VM Creation answer
	        case DatacenterTags.VM_CREATE_ACK:
	           	processVMCreate(ev);
	           	break;
	        //A finished cloudlet returned
	        case GridSimTags.GRIDLET_RETURN:
	        	processCloudletReturn(ev);
	            break;
            // other unknown tags are processed by this method
	        default:
	            processOtherEvent(ev);
	            break;
		}
		
	}
	
	/**
	 * Process the return of a request for the characteristics of a Datacenter
	 * @param ev a Sim_event object
	 * @pre ev != $null
	 * @post $none
	 */
	protected void processResourceCharacteristics(Sim_event ev) {
		
		DatacenterCharacteristics characteristics = (DatacenterCharacteristics) ev.get_data();
		
		//which datacenter is this?
		int thisDatacenterID = characteristics.getResourceID();
	
		//this ID corresponds to which vector position?
		for(int i=0;i<this.datacenterID.length;i++){
			if(datacenterID[i]==thisDatacenterID){
				this.datacenterChar[i]=characteristics;
				break;
			}
		}
				
		this.contactedDatacenters++;
		if(this.contactedDatacenters==this.datacenters){
			createVMsinDatacenter(0);
		}
		
	}
	
	/**
	 * Process the ack received due to a request for VM creation
	 * @param ev a Sim_event object
	 * @pre ev != null
	 * @post $none
	 */
	protected void processVMCreate(Sim_event ev) {
		
		int[] array = (int[]) ev.get_data();
		int senderId=array[0];
		int vmId=array[1];

		int id=-1;
		for(int i=0;i<this.datacenterID.length;i++){
			if(this.datacenterID[i]==senderId){
					id=i;
					break;
			}
		}
		
		if(array[2]==GridSimTags.TRUE){
			this.vmMapping[vmId]=this.datacenterID[id];
			this.vmsCreated++;
		} else {
        	System.out.println(GridSim.clock()+": "+this.get_name()+ ": Creation of VM #"+((VirtualMachine)this.vmlist.get(vmId)).getVmId()+" failed in "+this.datacenterChar[id].getResourceName());
        }
		
		this.vmsAcks++;
		
		if(this.vmsCreated==this.vmlist.size()-this.vmsDestroyed){//all the requested VMs have been created
			submitCloudlets();
		} else {
			if(this.vmsRequested==this.vmsAcks){//all the acks received, but some vms were not created
				
				if(id<(this.datacenterID.length-1)){//if there is a datacenter that were not tried
					//request to the next datacenter (if it exists)
					id++;
					if(id<datacenterChar.length)
						createVMsinDatacenter(id);
				} else {//all datacenters already queried
					if(this.vmsCreated>0){//if some vm were created
						submitCloudlets();
					} else {//no vms created. abort
						System.out.println(GridSim.clock()+": "+this.get_name()+ ": not all required VMs could be created. Aborting");
						finishExecution();
					}
				}
			}
		}
	}
	
	/**
	 * Process a cloudlet return event
	 * @param ev a Sim_event object
	 * @pre ev != $null
	 * @post $none
	 */
	@SuppressWarnings("unchecked")
	protected void processCloudletReturn(Sim_event ev) {
		
		Cloudlet cloudlet = (Cloudlet) ev.get_data();
		this.receiveList.add(cloudlet);
		System.out.println(GridSim.clock()+": "+this.get_name()+ ": Cloudlet "+cloudlet.getGridletID()+" received");
		this.cloudletsFinished++;
		if(this.cloudletsFinished==this.cllist.size()){//all cloudlets executed.
			System.out.println(GridSim.clock()+": "+this.get_name()+ ": All Cloudlets executed. Finishing...");
			clearDatacenters();
			finishExecution();
		} else {//some cloudlets haven't finished yet
			if (this.cloudletsFinished==this.cloudletsSubmitted){
				//all the cloudlets sent finished. It means that some bount
				//cloudlet is waiting its VM be created
				clearDatacenters();
				createVMsinDatacenter(0);
			}			
		}
	
	}
	
	/**
     * Overrides this method when making a new and different type of Broker.
     * This method is called by {@link #body()} for incoming unknown tags.
     * @param ev   a Sim_event object
     * @pre ev != null
     * @post $none
     */
    protected void processOtherEvent(Sim_event ev){
        if (ev == null){
            System.out.println(super.get_name() + ".processOtherEvent(): " + "Error - an event is null.");
            return;
        }
        
        System.out.println(super.get_name() + ".processOtherEvent(): " + "Error - event unknown by this DatacenterBroker.");

    }
    
    /**
     * Create the virtual machines in a datacenter
     * @param chosenDatacenter Id of the chosen Datacenter
     * @pre $none
     * @post $none
     */
    protected void createVMsinDatacenter(int chosenDatacenter) {

 		//send as much vms as possible for this datacenter before trying the next one
		int amountOfVMs=0;
		for(int i=0;i<this.vmlist.size();i++){
			if(this.vmMapping[i]==-1){//if this vm were not mapped yet
				amountOfVMs++;
				VMCharacteristics characteristics = ((VirtualMachine)vmlist.get(i)).getCharacteristics();
				System.out.println(GridSim.clock()+": "+this.get_name()+ ": Trying to Create VM #"+((VirtualMachine)vmlist.get(i)).getVmId());
				super.send(datacenterID[chosenDatacenter], GridSimTags.SCHEDULE_NOW, DatacenterTags.VM_CREATE_ACK, characteristics);
			}
		}
		this.vmsRequested=amountOfVMs;
		this.vmsAcks=0;
	}

    /**
     * Submit cloudlets to the created VMs
     * @pre $none
     * @post $none
     * 
     */
	protected void submitCloudlets() {

		int cont=0;
		for(int i=0;i<this.cllist.size();i++){
			if(!this.clSubmitted[i]){
				Cloudlet cl = (Cloudlet) cllist.get(i);
				if(cl.getVmId()==-1){//if user didn't bind this cloudlet and it has not been executed yet
					//submit to the next machine
					cl.setVmId(cont);
					System.out.println(GridSim.clock()+": "+this.get_name()+ ": Sending cloudlet "+cl.getGridletID()+" to VM #"+((VirtualMachine)vmlist.get(cont)).getVmId());
					super.send(vmMapping[cont],GridSimTags.SCHEDULE_NOW, GridSimTags.GRIDLET_SUBMIT, cl);
					cont=(cont+1)%this.vmsCreated;
					cloudletsSubmitted++;
					clSubmitted[i]=true;
				} else { //submit to the specific vm, if created
					//which vm is this cloudlet waiting for?
					VirtualMachine vm = vmlist.getVMbyID(cl.getVmId());
					int index = vmlist.indexOf(vm);
					if(vmMapping[index]>=0){//if this vm is running
						System.out.println(GridSim.clock()+": "+this.get_name()+ ": Sending cloudlet "+cl.getGridletID()+" to VM #"+((VirtualMachine)vmlist.get(index)).getVmId());
						super.send(vmMapping[index],GridSimTags.SCHEDULE_NOW,GridSimTags.GRIDLET_SUBMIT,cl);
						cloudletsSubmitted++;
						clSubmitted[i]=true;
					} else {
						System.out.println(GridSim.clock()+": "+this.get_name()+ ": Postponing execution of cloudlet "+cl.getGridletID()+": bount VM not available");
					}
				}
			}
		}	
	}
	
	/**
	 * Destroy the virtual machines running in datacenters
	 * @pre $none
	 * @post $none
	 */
	protected void clearDatacenters() {
		for(int i=0;i<this.vmlist.size();i++){
		if(this.vmMapping[i]>=0){
			VMCharacteristics characteristics = ((VirtualMachine)vmlist.get(i)).getCharacteristics();
			int[] array = new int[2];
			array[0]=characteristics.getUserId();
			array[1]=characteristics.getVmId();
			System.out.println(GridSim.clock()+": "+this.get_name()+ ": Destroying VM #"+((VirtualMachine)vmlist.get(i)).getVmId());
			super.send(this.vmMapping[i], GridSimTags.SCHEDULE_NOW, DatacenterTags.VM_DESTROY, array);
			this.vmsDestroyed++;
			this.vmMapping[i]=-10;
		}
	}
		
	}
	
	/**
	 * Send an internal event communicating the end of the simulation
	 * @pre $none
	 * @post $none
	 *
	 */
	private void finishExecution() {
		super.send(this.get_id(),GridSimTags.SCHEDULE_NOW,GridSimTags.END_OF_SIMULATION);
	}
	
	/**
	 * Return a cloudlet whose ID is known
	 * @param cloudletId ID of the cloudlet
	 * @return a Cloudlet object
	 * @post $none
	 */
	private Cloudlet findCloudletbyId(int cloudletId) {

		for(int i=0;i<cllist.size();i++){
			if(((Cloudlet)cllist.get(i)).getGridletID()==cloudletId) return (Cloudlet) cllist.get(i);
		}
		return null;	
	}
}
