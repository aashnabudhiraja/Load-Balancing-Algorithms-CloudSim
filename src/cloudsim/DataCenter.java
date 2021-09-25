/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation
 *               of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009, The University of Melbourne, Australia
 */


package cloudsim;

import eduni.simjava.Sim_event;
import eduni.simjava.Sim_system;
import gridsim.GridSim;
import gridsim.GridSimTags;
import gridsim.MachineList;
import gridsim.datagrid.DataGridTags;
import gridsim.datagrid.File;
import gridsim.datagrid.storage.Storage;
import gridsim.net.InfoPacket;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;


/**
 * Datacenter class is a GridResource whose resources
 * are virtualized. It deals with processing of VM queries (i.e., handling
 * of VMs) instead of processing Cloudlet-related queries. So, even though an
 * AllocPolicy will be instantiated (in the init() method of the superclass,
 * it will not be used, as processing of gridlets are handled by the VMScheduler
 * and processing of VirtualMachines are handled by the VMProvisioner.
 *
 * @author       Rodrigo N. Calheiros
 * @since        CloudSim Toolkit 1.0 Beta
 * @invariant $none
 */
public class DataCenter extends CloudSim {
	
	protected DatacenterCharacteristics resource_;
    protected String regionalGISName_;
    
	protected VMProvisioner vmprovisioner;
	protected double lastProcessTime;
	protected HashMap<Integer,Double> debts;
	protected LinkedList<Storage> storageList;

	/**
     * Allocates a new Datacenter object.
     *
     * @param name       the name to be associated with this entity (as
     *                   required by Sim_entity class from simjava package)
     * @param resource   an object of DatacenterCharacteristics
     * @param storageList a LinkedList of storage elements, for data simulation
     * @param vmprovisioner the VMProvisioner associated to this datacenter
     * @throws Exception This happens when one of the following scenarios occur:
     *      <ul>
     *          <li> creating this entity before initializing GridSim package
     *          <li> this entity name is <tt>null</tt> or empty
     *          <li> this entity has <tt>zero</tt> number of PEs (Processing
     *              Elements). <br>
     *              No PEs mean the Cloudlets can't be processed.
     *              A GridResource must contain one or more Machines.
     *              A Machine must contain one or more PEs.
     *      </ul>
     * @pre name != null
     * @pre resource != null
     * @post $none
     */
	public DataCenter(String name, DatacenterCharacteristics resource, VMProvisioner vmprovisioner, LinkedList<Storage> storageList) throws Exception {
		super(name);
		resource_ = resource;
		this.vmprovisioner = vmprovisioner;
		vmprovisioner.init(this.resource_.getMachineList());
		this.lastProcessTime=0.0;
		this.debts=new HashMap<Integer,Double>();
		this.storageList = storageList;
        init();
	}
	
   /**
     * Initializes the resource allocation policy
     * @throws Exception    If number of PEs is zero
     * @pre $none
     * @post $none
     */
    private void init() throws Exception {
        // If this resource doesn't have any PEs then no useful at all
        if (resource_.getNumPE() == 0) {
            throw new Exception(super.get_name() + " : Error - this entity has no PEs. Therefore, can't process any Cloudlets.");
        }

        // stores id of this class
        resource_.setResourceID( super.get_id() );

    }
	
	/**
     * Handles external events that are coming to this Datacenter entity.
     * This method also registers the identity of this entity to
     * <tt>GridInformationService</tt> class.
     * <p>
     * The services or tags available for this resource are:
     * <ul>
     *      <li> {@link cloudsim.DatacenterTags#VM_CREATE} </li>
     *      <li> {@link cloudsim.DatacenterTags#VM_CREATE_ACK} </li>
     *      <li> {@link cloudsim.DatacenterTags#VM_DESTROY} </li>
     *      <li> {@link cloudsim.DatacenterTags#VM_DESTROY_ACK} </li>
     *      <li> {@link cloudsim.DatacenterTags#VM_MIGRATE} </li>
     *      <li> {@link cloudsim.DatacenterTags#VM_MIGRATE_ACK} </li>
     * </ul>
     * <br>
     * @pre $none
     * @post $none
     */
    public void body() {

        // this resource should register to regional GIS.
        // However, if not specified, then register to system GIS (the
        // default GridInformationService) entity.
        int gisID = GridSim.getEntityId(regionalGISName_);
        if (gisID == -1) {
            gisID = GridSim.getGridInfoServiceEntityId();
        }
        // need to wait for few seconds before registering to a regional GIS.
        // This is because to allow all routers to fill in their routing tables
        else {
            super.sim_pause(10);
            System.out.println(super.get_name() + ".body(): wait for " +
                " 10 seconds before registering to " +
                regionalGISName_);
        }

        // send the registration to GIS
        super.send(gisID, GridSimTags.SCHEDULE_NOW, GridSimTags.REGISTER_RESOURCE,this.get_id());
        // Below method is for a child class to override
        registerOtherEntity();

        // Process events until END_OF_SIMULATION is received from the
        // GridSimShutdown Entity
        Sim_event ev = new Sim_event();
        while (Sim_system.running()){
            super.sim_get_next(ev);
            
            // if the simulation finishes then exit the loop
            if (ev.get_tag() == GridSimTags.END_OF_SIMULATION){
            	break;
            }

            // process the received event
            processEvent(ev);
        }

        // remove I/O entities created during construction of this entity
        super.terminateIOEntities();
    }
    
    /**
     * Overrides this method when making a new and different type of resource.
     * This method is called by {@link #body()} to register other type to
     * GIS entity. In doing so, you
     * need to create a new child class extending from
     * gridsim.GridInformationService.
     * <br>
     * <b>NOTE:</b> You do not need to override {@link #body()} method, if
     * you use this method.
     *
     * @pre $none
     * @post $none
     */
    protected void registerOtherEntity() {
        // empty. This should be override by a child class
    }
    
    /**
     * Processes events or services that are available for this Datacenter
     * @param ev    a Sim_event object
     * @pre ev != null
     * @post $none
     */
    protected void processEvent(Sim_event ev) {
        int src_id = -1;
        switch (ev.get_tag()) {
            // Resource characteristics inquiry
            case GridSimTags.RESOURCE_CHARACTERISTICS:
                src_id = ((Integer) ev.get_data() ).intValue();
                super.send(src_id, 0.0, ev.get_tag(),resource_);
                break;

                // Resource dynamic info inquiry
            case GridSimTags.RESOURCE_DYNAMICS:
                src_id = ((Integer) ev.get_data() ).intValue();
                super.send(src_id, 0.0, ev.get_tag(), 0);
                break;

            case GridSimTags.RESOURCE_NUM_PE:
                src_id = ((Integer) ev.get_data() ).intValue();
                int numPE = resource_.getNumPE();
                super.send(src_id, 0.0, ev.get_tag(), numPE);
                break;

            case GridSimTags.RESOURCE_NUM_FREE_PE:
                src_id = ( (Integer) ev.get_data() ).intValue();
                int numFreePE = resource_.getNumFreePE();
                super.send(src_id, 0.0, ev.get_tag(), numFreePE);
                break;

                // New Cloudlet arrives
            case GridSimTags.GRIDLET_SUBMIT:
                processCloudletSubmit(ev, false);
                break;

                // New Cloudlet arrives, but the sender asks for an ack
            case GridSimTags.GRIDLET_SUBMIT_ACK:
                processCloudletSubmit(ev, true);
                break;

                // Cancels a previously submitted Cloudlet
            case GridSimTags.GRIDLET_CANCEL:
                processCloudlet(ev, GridSimTags.GRIDLET_CANCEL);
                break;

                // Pauses a previously submitted Cloudlet
            case GridSimTags.GRIDLET_PAUSE:
                processCloudlet(ev, GridSimTags.GRIDLET_PAUSE);
                break;

                // Pauses a previously submitted Cloudlet, but the sender
                // asks for an acknowledgement
            case GridSimTags.GRIDLET_PAUSE_ACK:
                processCloudlet(ev, GridSimTags.GRIDLET_PAUSE_ACK);
                break;

                // Resumes a previously submitted Cloudlet
            case GridSimTags.GRIDLET_RESUME:
                processCloudlet(ev, GridSimTags.GRIDLET_RESUME);
                break;

                // Resumes a previously submitted Cloudlet, but the sender
                // asks for an acknowledgement
            case GridSimTags.GRIDLET_RESUME_ACK:
                processCloudlet(ev, GridSimTags.GRIDLET_RESUME_ACK);
                break;

                // Moves a previously submitted Cloudlet to a different resource
            case GridSimTags.GRIDLET_MOVE:
                processCloudletMove((int[])ev.get_data(), GridSimTags.GRIDLET_MOVE);
                break;

                // Moves a previously submitted Cloudlet to a different resource
            case GridSimTags.GRIDLET_MOVE_ACK:
                processCloudletMove((int[])ev.get_data(), GridSimTags.GRIDLET_MOVE_ACK);
                break;

                // Checks the status of a Cloudlet
            case GridSimTags.GRIDLET_STATUS:
                processCloudletStatus(ev);
                break;

                // Ping packet
            case GridSimTags.INFOPKT_SUBMIT:
                processPingRequest(ev);
                break;
                
           	case DatacenterTags.VM_CREATE:
        		processVMCreate(ev,false);        		
        		break;
        		
        	case DatacenterTags.VM_CREATE_ACK:
        		processVMCreate(ev,true);
        		break;
        		
           	case DatacenterTags.VM_DESTROY:
           		processVMDestroy(ev,false);
           		break;
           	
           	case DatacenterTags.VM_DESTROY_ACK:
        		processVMDestroy(ev,true);
        		break;
           	
           	case DatacenterTags.VM_MIGRATE:
           		processVMMigrate(ev,false);
           		break;
           	
           	case DatacenterTags.VM_MIGRATE_ACK:
           		processVMMigrate(ev,true);
           		break;
           		
           	case DatacenterTags.VM_DATA_ADD:
           		processDataAdd(ev,false);
           		break;
           		
           	case DatacenterTags.VM_DATA_ADD_ACK:
           		processDataAdd(ev,true);
           		break;
           		
           	case DatacenterTags.VM_DATA_DEL:
           		processDataDel(ev,false);
           		break;
           		
           	case DatacenterTags.VM_DATA_DEL_ACK:
           		processDataDel(ev,true);
           		break;
           	
           	case DatacenterTags.VM_DATACENTER_EVENT:
           		updateCloudletProcessing();
            	checkCloudletCompletion();
           		break;
            
           		// other unknown tags are processed by this method
            default:
                processOtherEvent(ev);
                break;
        }
    }
    
    protected void processDataDel(Sim_event ev, boolean ack) {
		
        if (ev == null) {
            return;
        }
        Object[] data = (Object[]) ev.get_data();
        if (data == null) {
            return;
        }

        String filename = (String) data[0];
        int req_source = ((Integer) data[1]).intValue();
        int tag = -1;

        // check if this file can be deleted (do not delete is right now)
        int msg = deleteFileFromStorage(filename);
        if (msg == DataGridTags.FILE_DELETE_SUCCESSFUL)
           tag = DataGridTags.CTLG_DELETE_MASTER;
        else // if an error occured, notify user
           tag = DataGridTags.FILE_DELETE_MASTER_RESULT;
    
    	if(ack==true){
            // send back to sender
            Object pack[] = new Object[2];
            pack[0] = filename;
            pack[1] = new Integer(msg);

            super.send(req_source,GridSimTags.SCHEDULE_NOW,tag,pack);
    	}
		
	}

	protected void processDataAdd(Sim_event ev, boolean ack) {
		
	       if (ev == null) {
	            return;
	        }

	        Object[] pack = (Object[]) ev.get_data();
	        if (pack == null) {
	            return;
	        }

	        File file = (File) pack[0]; // get the file
	        file.setMasterCopy(true); // set the file into a master copy
	        int sentFrom = ((Integer) pack[1]).intValue(); // get sender ID

	        /******     // DEBUG
	         System.out.println(super.get_name() + ".addMasterFile(): " +
	         file.getName() + " from " + GridSim.getEntityName(sentFrom));
	         *******/

	        Object[] data = new Object[3];
	        data[0] = file.getName();

	        int msg = addFile(file); // add the file
	        
	        
	        double debit;
	        if(!this.debts.containsKey(sentFrom)) debit=0.0;
	        else debit=this.debts.get(sentFrom);

	        debit+=((DatacenterCharacteristics)resource_).getCostPerBw()*file.getSize();
	        
	        this.debts.put(sentFrom,debit);
	        
	        if(ack){
	        	data[1] = new Integer(-1); // no sender id
	        	data[2] = new Integer(msg); // the result of adding a master file
	        	super.send(sentFrom,GridSimTags.SCHEDULE_NOW,DataGridTags.FILE_ADD_MASTER_RESULT,data);
	        }
		
	}

	/**
     * Processes a ping request.
     * @param ev  a Sim_event object
     * @pre ev != null
     * @post $none
     */
    protected void processPingRequest(Sim_event ev) {
        InfoPacket pkt = (InfoPacket) ev.get_data();
        pkt.setTag(GridSimTags.INFOPKT_RETURN);
        pkt.setDestID( pkt.getSrcID() );

        // sends back to the sender
        super.send(pkt.getSrcID(),GridSimTags.SCHEDULE_NOW,GridSimTags.INFOPKT_RETURN,pkt);
    }
    
    /**
     * Process the event for an User/Broker who wants to know the status of a Cloudlet.
     * This Datacenter will then send the status back to the User/Broker.
     * @param ev   a Sim_event object
     * @pre ev != null
     * @post $none
     */
    protected void processCloudletStatus(Sim_event ev) {
        int cloudletId = 0;
        int userId = 0;
        int vmId = 0;
        int status = -1;

        try{
            // if a sender using gridletXXX() methods
            int data[] = (int[]) ev.get_data();
            cloudletId = data[0];
            userId = data[1];
            vmId = data[2];

            status = vmprovisioner.getHost(vmId, userId).getVM(userId, vmId).getVMScheduler().cloudletstatus(cloudletId);
        }

        // if a sender using normal send() methods
        catch (ClassCastException c) {
            try {
                Cloudlet cl = (Cloudlet) ev.get_data();
                cloudletId = cl.getGridletID();
                userId = cl.getUserID();

                status = vmprovisioner.getHost(vmId, userId).getVM(userId, vmId).getVMScheduler().cloudletstatus(cloudletId);
            }
            catch (Exception e) {
                System.out.println(super.get_name() +
                        ": Error in processing GridSimTags.GRIDLET_STATUS");
                System.out.println( e.getMessage() );
                return;
            }
        }
        catch (Exception e) {
            System.out.println(super.get_name() +
                    ": Error in processing GridSimTags.GRIDLET_STATUS");
            System.out.println( e.getMessage() );
            return;
        }

        int[] array = new int[3];
        array[0] = this.get_id();
        array[1] = cloudletId;
        array[2] = status;

        int tag = GridSimTags.GRIDLET_STATUS;
        super.send(userId, GridSimTags.SCHEDULE_NOW, tag, array);
    }
	
    /**
     * Here all the method related to VM requests will be received and forwarded to the related method.
     * @param ev the received event
     * @pre $none
     * @post $none
     */
    protected void processOtherEvent(Sim_event ev){

        if (ev==null){
            System.out.println(super.get_name() + ".processOtherEvent(): Error - an event is null.");
        } 
        
    }
    
    /**
     * Process the event for an User/Broker who wants to create a VM
     * in this Datacenter. This Datacenter will then send the status back to
     * the User/Broker.
     * @param ev   a Sim_event object
     * @pre ev != null
     * @post $none
     */
	protected void processVMCreate(Sim_event ev, boolean ack){
    	
    	VMCharacteristics description = (VMCharacteristics)ev.get_data();
    	 	
 	    boolean result = vmprovisioner.allocateHostForVM(description);

 	    if(ack){
 	       int[] array = new int[3];
           array[0] = this.get_id();
 	       array[1] = description.getVmId();
           
           if(result) array[2] = GridSimTags.TRUE;
           else array[2] = GridSimTags.FALSE;       
		   super.send(description.getUserId(),GridSimTags.SCHEDULE_NOW,DatacenterTags.VM_CREATE_ACK,array);
 	    }
 	    
 	    if(result){
 	    	double amount=0.0;
 	    	if(debts.containsKey(description.getUserId())){
 	    		amount=debts.get(description.getUserId());
 	    	}
 	    	amount+=((DatacenterCharacteristics)resource_).getCostPerMem()*description.getMemory();
 	    	amount+=((DatacenterCharacteristics)resource_).getCostPerStorage()*description.getSize();
 	   
 	    	debts.put(description.getUserId(), amount);
 	    }
 	    
    }
    
	/**
     * Process the event for an User/Broker who wants to destroy a VM
     * previously created in this Datacenter. This Datacenter may send,
     * upon request, the status back to the User/Broker.
     * @param ev   a Sim_event object
     * @pre ev != null
     * @post $none
     */
    protected void processVMDestroy(Sim_event ev, boolean ack){
    	
    	int[] array = (int[]) ev.get_data();
    	int destId = array[0];
    	int vmID = array[1];
 	    vmprovisioner.deallocateHostForVM(vmID,destId);
 	    if(ack){
           array = new int[3];
           array[0] = this.get_id();
           array[1] = vmID;
           array[2] = GridSimTags.TRUE;
           
		   super.send(destId,GridSimTags.SCHEDULE_NOW,DatacenterTags.VM_DESTROY_ACK,array);
 	    }
    }
    
    /**
     * Process the event for an User/Broker who wants to migrate a VM.
     * This Datacenter will then send the status back to the User/Broker.
     * @param ev   a Sim_event object
     * @pre ev != null
     * @post $none
     */
    protected void processVMMigrate(Sim_event ev, boolean ack){
    	
    	int[] array = (int[]) ev.get_data();
    	int userId = array[0];
    	int vmId = array[1];
    	int destId = array[2];
    	
    	boolean result = vmprovisioner.migrateVM(vmId,userId,destId);
 	    if(ack){
 	       array[0] = this.get_id();
           array[1] = vmId;
           if(result) array[2] = GridSimTags.TRUE;
           else array[2] = GridSimTags.FALSE;
           
		   super.send(destId,GridSimTags.SCHEDULE_NOW,DatacenterTags.VM_MIGRATE_ACK,array);
 	    }
    }
    
    /**
     * Processes a Cloudlet based on the event type
     * @param ev   a Sim_event object
     * @param type event type
     * @pre ev != null
     * @pre type > 0
     * @post $none
     */
    protected void processCloudlet(Sim_event ev, int type) {
        int cloudletId = 0;
        int userId = 0;
        int vmId = 0;
        
        try {
            // if a sender using cloudletXXX() methods
            int data[] = (int[]) ev.get_data();
            cloudletId = data[0];
            userId = data[1];
            vmId = data[2];
        }
        // if a sender using normal send() methods
        catch (ClassCastException c) {
            try {
                Cloudlet cl = (Cloudlet) ev.get_data();
                cloudletId = cl.getGridletID();
                userId = cl.getUserID();
                vmId = cl.getVmId();
            }
            catch (Exception e) {
                System.out.println(super.get_name() + ": Error in processing Cloudlet");
                System.out.println(e.getMessage());
                return;
            }
        } catch (Exception e) {
            System.out.println(super.get_name() + ": Error in processing a Cloudlet.");
            System.out.println( e.getMessage() );
            return;
        }

        // begins executing ....
        switch (type) {
            case GridSimTags.GRIDLET_CANCEL:
                processCloudletCancel(cloudletId,userId,vmId);
                break;

            case GridSimTags.GRIDLET_PAUSE:
            	processCloudletPause(cloudletId,userId,vmId,false);
                break;

            case GridSimTags.GRIDLET_PAUSE_ACK:
            	processCloudletPause(cloudletId,userId,vmId,true);
                break;

            case GridSimTags.GRIDLET_RESUME:
            	processCloudletResume(cloudletId,userId,vmId,false);
                break;

            case GridSimTags.GRIDLET_RESUME_ACK:
            	processCloudletResume(cloudletId,userId,vmId,true);
                break;
            default:
                break;
        }

    }
    
    /**
     * Process the event for an User/Broker who wants to move a Cloudlet.
     * @param receivedData   information about the migration
     * @param type  event tag
     * @pre receivedData != null
     * @pre type > 0
     * @post $none
     */
    protected void processCloudletMove(int[] receivedData, int type) {

    	updateCloudletProcessing();
    	
    	int[] array = receivedData;
    	int cloudletId = array[0];
    	int userId = array[1];
    	int vmId = array[2];
    	int vmDestId = array[3];
    	int destId = array[4];
    	
    	//get the cloudlet
    	Cloudlet cl = (Cloudlet) vmprovisioner.getHost(vmId, userId).getVM(userId, vmId).getVMScheduler().cloudletCancel(cloudletId);
    	
    	
    	boolean failed=false;
    	if (cl == null) {//cloudlet doesn't exist
    		failed=true;
    	}else {
    		//has the cloudlet already finished?
    		if (cl.getGridletStatus() == Cloudlet.SUCCESS){//if yes, send it back to user
    			int[] data = new int[3];
    			data[0]=this.get_id();
        		data[1]=cloudletId;
        		data[2]=0;
        		super.send(cl.getUserID(),GridSimTags.SCHEDULE_NOW,GridSimTags.GRIDLET_SUBMIT_ACK, data);
		        super.send(cl.getUserID(),GridSimTags.SCHEDULE_NOW,GridSimTags.GRIDLET_RETURN,cl);
    		}
    		
    		//prepare cloudlet for migration
    		cl.setVmId(vmDestId);
 
    		if (destId==this.get_id()){ //the cloudlet will migrate from one vm to another
    			//does the destination VM exist?
    			VirtualMachine vm = vmprovisioner.getHost(vmDestId, userId).getVM(userId, vmDestId);
    			if(vm==null){
    				failed=true;
    			}else{
    				double fileTransferTime = predictFileTransferTime(cl.getRequiredFiles());//time to transfer the files
    				vm.getVMScheduler().cloudletSubmit(cl,fileTransferTime);
    			}
    		} else {//the cloudlet will migrate from one resource to another

    			int tag=((type==GridSimTags.GRIDLET_MOVE_ACK)?GridSimTags.GRIDLET_SUBMIT_ACK:GridSimTags.GRIDLET_SUBMIT);
		        super.send(destId,GridSimTags.SCHEDULE_NOW,tag,cl);
    		}
    	}

    	if(type==GridSimTags.GRIDLET_MOVE_ACK){//send ACK if requested
    		int[] data = new int[3];
    		data[0]=this.get_id();
    		data[1]=cloudletId;
    		if (failed) data[2]=0; else data[2]=1;
    		super.send(cl.getUserID(),GridSimTags.SCHEDULE_NOW,GridSimTags.GRIDLET_SUBMIT_ACK,data);
    	}
    	
    }
    
    /**
     * Processes a Cloudlet submission
     * @param ev  a Sim_event object
     * @param ack  an acknowledgement
     * @pre ev != null
     * @post $none
     */
    protected void processCloudletSubmit(Sim_event ev, boolean ack) {
    	
    	updateCloudletProcessing();
    	    	
        try {
            // gets the Cloudlet object
            Cloudlet cl = (Cloudlet) ev.get_data();

            // checks whether this Cloudlet has finished or not
            if (cl.isFinished() == true){
                String name = GridSim.getEntityName(cl.getUserID());
                System.out.println(super.get_name()+": Warning - Gridlet #"+cl.getGridletID()+" owned by "+name+" is already completed/finished.");
                System.out.println("Therefore, it is not being executed again");
                System.out.println();

                // NOTE: If a Cloudlet has finished, then it won't be processed.
                // So, if ack is required, this method sends back a result.
                // If ack is not required, this method don't send back a result.
                // Hence, this might cause GridSim to be hanged since waiting
                // for this Cloudlet back.
                if (ack == true) {
                    int[] array = new int[3];
                    array[0] = this.get_id();
                    array[1] = cl.getGridletID();
                    array[2] = GridSimTags.FALSE;

                    // unique tag = operation tag
                    int tag = GridSimTags.GRIDLET_SUBMIT_ACK;
                    super.send(cl.getUserID(), GridSimTags.SCHEDULE_NOW, tag, array);
                }

                super.send(cl.getUserID(), GridSimTags.SCHEDULE_NOW, GridSimTags.GRIDLET_RETURN, cl);

                return;
            }
            
            // process this Cloudlet to this GridResource
            cl.setResourceParameter(super.get_id(), resource_.getCostPerSec(), ((DatacenterCharacteristics)resource_).costPerBw);
            
            int userId = cl.getUserID();
            int vmId = cl.getVmId();
             
            double fileTransferTime = predictFileTransferTime(cl.getRequiredFiles());//time to transfer the files
            
            Host host = vmprovisioner.getHost(vmId,userId);
            VirtualMachine vm = host.getVM(userId, vmId);
            VMScheduler scheduler = vm.getVMScheduler();
            double capacity = scheduler.cloudletSubmit(cl,fileTransferTime);
            
            if(capacity>0.0){//if this gridlet is in the exec queue
            	double estimatedFinishTime = (cl.getGridletLength()/(capacity*cl.getNumPE())); //time to process the gridlet
            	//System.out.println(estimatedFinishTime+"="+gl.getGridletLength()+"/("+capacity+"*"+gl.getNumPE()+")");
            	estimatedFinishTime+=fileTransferTime;
            	//System.out.println(GridSim.clock()+": Next event set to "+estimatedFinishTime);
            	super.send(this.get_id(),estimatedFinishTime,DatacenterTags.VM_DATACENTER_EVENT);
            }
 
        }
        catch (ClassCastException c) {
            System.out.println(super.get_name() + ".processCloudletSubmit(): " + "ClassCastException error.");
            c.printStackTrace();
        }
        catch (Exception e) {
            System.out.println(super.get_name() + ".processCloudletSubmit(): " + "Exception error.");
            e.printStackTrace();
        }
        
    	checkCloudletCompletion();

    }

    private double predictFileTransferTime(LinkedList requiredFiles) {
		
    	double time=0.0;
 
       	Iterator iter = requiredFiles.iterator();
    	while(iter.hasNext()){
    		String fileName = (String) iter.next();
	        for (int i = 0; i < storageList.size(); i++) {
	            Storage tempStorage = (Storage) storageList.get(i);
	            File tempFile = tempStorage.getFile(fileName);
	            if (tempFile != null) {
	            	time+=tempFile.getSize()/tempStorage.getMaxTransferRate();
	            	break;
	            }
	        }
    	} 	
		return time;
	}

	/**
     * Processes a Cloudlet resume request
     * @param cloudletId  resuming cloudlet ID
     * @param userId  ID of the cloudlet's owner
     * @param vmId ID of the virtual machine running the cloudlet
     * @param ack $true if an ack is requested after operation
     * @pre $none
     * @post $none
     */
	protected void processCloudletResume(int cloudletId, int userId, int vmId, boolean ack) {
		
		double eventTime = vmprovisioner.getHost(vmId,userId).getVM(userId, vmId).getVMScheduler().cloudletResume(cloudletId);
        
		boolean status = false;
		if(eventTime>0.0){//if this gridlet is in the exec queue
			status=true;
			if(eventTime>GridSim.clock()) super.sim_schedule(this.get_id(),eventTime,DatacenterTags.VM_DATACENTER_EVENT);
        }
		
		if(ack){
			int[] array = new int[3];
			array[0] = this.get_id();
            array[1] = cloudletId;
            if (status == true) {
                array[2] = GridSimTags.TRUE;
            }
            else {
                array[2] = GridSimTags.FALSE;
            }
			super.send(userId,GridSimTags.SCHEDULE_NOW,GridSimTags.GRIDLET_RESUME_ACK, array);
		}
		
	}

	/**
     * Processes a Cloudlet pause request
     * @param cloudletId  resuming cloudlet ID
     * @param userId  ID of the cloudlet's owner
     * @param vmId ID of the virtual machine running the cloudlet
     * @param ack $true if an ack is requested after operation
     * @pre $none
     * @post $none
     */
	protected void processCloudletPause(int cloudletId, int userId, int vmId, boolean ack) {
		
		boolean status = vmprovisioner.getHost(vmId,userId).getVM(userId, vmId).getVMScheduler().cloudletPause(cloudletId);
		
		if(ack){
			int[] array = new int[3];
			array[0] = this.get_id();
            array[1] = cloudletId;
            if (status == true) {
                array[2] = GridSimTags.TRUE;
            }
            else {
                array[2] = GridSimTags.FALSE;
            }
			super.send(userId,GridSimTags.SCHEDULE_NOW,GridSimTags.GRIDLET_PAUSE_ACK,array);
		}
		
	}

	/**
     * Processes a Cloudlet cancel request
     * @param cloudletId  resuming cloudlet ID
     * @param userId  ID of the cloudlet's owner
     * @param vmId ID of the virtual machine running the cloudlet
     * @pre $none
     * @post $none
     */
	protected void processCloudletCancel(int cloudletId, int userId, int vmId) {
		
		Cloudlet cl = (Cloudlet) vmprovisioner.getHost(vmId,userId).getVM(userId, vmId).getVMScheduler().cloudletCancel(cloudletId);
		
        long gridletSize = 0;
        if (cl != null) {
            gridletSize = cl.getGridletOutputSize();
        } else {
            try {
                gridletSize = 100;
                cl = new Cloudlet(cloudletId, 0, gridletSize, gridletSize);
                cl.setVmId(vmId);
                cl.setGridletStatus(Cloudlet.FAILED);
                cl.setResourceParameter(this.get_id(), resource_.getCostPerSec(),((DatacenterCharacteristics)resource_).getCostPerBw());
            } catch(Exception e) {
                // empty ...
            }
        }

        super.send(userId,GridSimTags.SCHEDULE_NOW, GridSimTags.GRIDLET_CANCEL, cl);	
	}
	
	/**
     * Updates processing of each gridlet running in this Datacenter. It is necessary because
     * Hosts and VirtualMachines are simple objects, not entities. So, they don't receive events
     * and updating cloudlets inside them must be called from the outside.
     * @pre $none
     * @post $none
     */
	protected void updateCloudletProcessing(){

		//if some time passed since last processing
		if(GridSim.clock()>this.lastProcessTime){
			MachineList list = vmprovisioner.getResources();
			double smallerTime = Double.MAX_VALUE;
			//for each host...
			for(int i=0;i<list.size();i++){
				Host host = (Host) list.get(i);
				double time = host.updateVMsProcessing(GridSim.clock());//inform VMs to update processing
				
				//what time do we expect that the next cloudlet will finish?
				if(time<smallerTime)
					smallerTime=time;
			}
			
			//schedules an event to the next time, if valid
			if (smallerTime>GridSim.clock()+0.01 && smallerTime!=Double.MAX_VALUE) {
				super.sim_schedule(this.get_id(),smallerTime-GridSim.clock(),DatacenterTags.VM_DATACENTER_EVENT);
			}
			this.lastProcessTime=GridSim.clock();
		}
	}
	
	/**
     * Verifies if some cloudlet inside this Datacenter already finished.
     * If yes, send it to the User/Broker
     * @pre $none
     * @post $none
     */
	protected void checkCloudletCompletion(){
		
		MachineList list = vmprovisioner.getResources();
		for(int i=0;i<list.size();i++){
			Host host = (Host) list.get(i);
			LinkedList vms = host.getVMs();
			for(int j=0;j<vms.size();j++){
				VirtualMachine vm = (VirtualMachine) vms.get(j);
				while (vm.getVMScheduler().isFinishedCloudlets()){
					Cloudlet cl = vm.getVMScheduler().getNextFinishedCloudlet();
					if(cl!=null){
						super.send(cl.getUserID(),GridSimTags.SCHEDULE_NOW,GridSimTags.GRIDLET_RETURN,cl);
					}
				}
			}
		}
	}
	
	public VirtualMachineList getVMList(){
		return null;
	}
	
	   /**
     * Adds a file into the resource's storage before the experiment starts.
     * If the file is a master file, then it will be registered to the RC when
     * the experiment begins.
     * @param file  a DataGrid file
     * @return a tag number denoting whether this operation is a success or not
     * @see gridsim.datagrid.DataGridTags#FILE_ADD_SUCCESSFUL
     * @see gridsim.datagrid.DataGridTags#FILE_ADD_ERROR_EMPTY
     */
    public int addFile(File file) {
        if (file == null) {
            return DataGridTags.FILE_ADD_ERROR_EMPTY;
        }

        if (contains(file.getName()) == true) {
            return DataGridTags.FILE_ADD_ERROR_EXIST_READ_ONLY;
        }

        // check storage space first
        if (storageList.size() <= 0) {
            return DataGridTags.FILE_ADD_ERROR_STORAGE_FULL;
        }

        Storage tempStorage = null;
        int msg = DataGridTags.FILE_ADD_ERROR_STORAGE_FULL;

        for (int i = 0; i < storageList.size(); i++) {
            tempStorage = (Storage) storageList.get(i);
            if (tempStorage.getAvailableSpace() >= file.getSize()) {
                tempStorage.addFile(file);
                msg = DataGridTags.FILE_ADD_SUCCESSFUL;
                break;
            }
        }

        return msg;
    }
    
    /**
     * Checks whether the resource has the given file
     * @param file  a file to be searched
     * @return <tt>true</tt> if successful, <tt>false</tt> otherwise
     */
    protected boolean contains(File file)
    {
        if (file == null) {
            return false;
        }
        return contains( file.getName() );
    }

    /**
     * Checks whether the resource has the given file
     * @param fileName  a file name to be searched
     * @return <tt>true</tt> if successful, <tt>false</tt> otherwise
     */
    protected boolean contains(String fileName)
    {
        if (fileName == null || fileName.length() == 0) {
            return false;
        }

        Iterator it = storageList.iterator();
        Storage storage = null;
        boolean result = false;

        while (it.hasNext()) {
            storage = (Storage) it.next();
            if (storage.contains(fileName) == true) {
                result = true;
                break;
            }
        }

        return result;
    }
    
    /**
     * Deletes the file from the storage. Also, check whether it is
     * possible to delete the file from the storage.
     *
     * @param fileName      the name of the file to be deleted
     * @param deleteMaster  do we want to delete the master file or not
     * @param justTest      <tt>true</tt> if you just want to test the file, or
     *                      <tt>false</tt> if you want to actually delete it
     * @return the error message as defined in
     *         {@link gridsim.datagrid.DataGridTags}
     * @see gridsim.datagrid.DataGridTags#FILE_DELETE_SUCCESSFUL
     * @see gridsim.datagrid.DataGridTags#FILE_DELETE_ERROR_ACCESS_DENIED
     * @see gridsim.datagrid.DataGridTags#FILE_DELETE_ERROR
     */
    private int deleteFileFromStorage(String fileName) {
        Storage tempStorage = null;
        File tempFile = null;
        int msg = DataGridTags.FILE_DELETE_ERROR;

        for (int i = 0; i < storageList.size(); i++) {
            tempStorage = (Storage) storageList.get(i);
            tempFile = tempStorage.getFile(fileName);
            tempStorage.deleteFile(fileName, tempFile);
            msg = DataGridTags.FILE_DELETE_SUCCESSFUL;
        } // end for

        return msg;
    }
	
	public void printDebts(){
		
		System.out.println("*****Datacenter: "+this.get_name()+"*****");
		System.out.println("User id\t\tDebt");
		
		Set keys = debts.keySet();
		Iterator iter = keys.iterator();
		while(iter.hasNext()){
			int key = (Integer) iter.next();
			double value = debts.get(key);
			DecimalFormat df = new DecimalFormat("#.##");
			System.out.println(key+"\t\t"+df.format(value));
		}
		System.out.println("**********************************");
		
	}
    
}
