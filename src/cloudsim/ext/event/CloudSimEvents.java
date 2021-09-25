package cloudsim.ext.event;

public interface CloudSimEvents {

	public static final int EVENT_SIMULATION_ENDED = 1000;
	
	public static final int EVENT_LIST_CONTENT_CHANGED = 2001;
	public static final int EVENT_LIST_CONTENT_ADDED = 2002;
	public static final int EVENT_LIST_CONTENT_REMOVED = 2003;
	
	public static final int EVENT_NEW_COMM_PATH = 3001;
	public static final int EVENT_CLOUDLET_ALLOCATED_TO_VM = 3002;
	public static final int EVENT_VM_FINISHED_CLOUDLET = 3003;
	
	final int EVENT_PROGRESS_UPDATE = 4000;
}