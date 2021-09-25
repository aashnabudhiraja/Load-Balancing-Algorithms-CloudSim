package cloudsim.ext.gui;

import java.io.Serializable;

/**
 * MachineUIElement holds the data specific to a machine used by the configuration screens.
 * 
 * @author Bhathiya Wickremasinghe
 *
 */
public class MachineUIElement implements Serializable {

	private static final long serialVersionUID = -5228849912997582314L;
	
	private int memory;
	private long storage;
	private int bw;
	private int processors;
	private int speed;
	private VmAllocationPolicy vmAllocationPolicy;
		

	public MachineUIElement(){
		
	}
	
	public MachineUIElement(int memory, long storage, int bw, int processors, int speed,
							VmAllocationPolicy vmAllocationPolicy) {
		super();
		this.memory = memory;
		this.storage = storage;
		this.bw = bw;
		this.processors = processors;
		this.speed = speed;
		this.vmAllocationPolicy = vmAllocationPolicy;
	}

	
	/**
	 * @return the memory
	 */
	public int getMemory() {
		return memory;
	}

	/**
	 * @param memory the memory to set
	 */
	public void setMemory(int memory) {
		this.memory = memory;
	}

	/**
	 * @return the storage
	 */
	public long getStorage() {
		return storage;
	}

	/**
	 * @param storage the storage to set
	 */
	public void setStorage(long storage) {
		this.storage = storage;
	}

	/**
	 * @return the bw
	 */
	public int getBw() {
		return bw;
	}

	/**
	 * @param bw the bw to set
	 */
	public void setBw(int bw) {
		this.bw = bw;
	}

	/**
	 * @return the processors
	 */
	public int getProcessors() {
		return processors;
	}

	/**
	 * @param processors the processors to set
	 */
	public void setProcessors(int processors) {
		this.processors = processors;
	}

	/**
	 * @return the speed
	 */
	public int getSpeed() {
		return speed;
	}

	/**
	 * @param speed the speed to set
	 */
	public void setSpeed(int speed) {
		this.speed = speed;
	}

	/**
	 * @return the vmAllocationPolicy
	 */
	public VmAllocationPolicy getVmAllocationPolicy() {
		return vmAllocationPolicy;
	}

	/**
	 * @param vmAllocationPolicy the vmAllocationPolicy to set
	 */
	public void setVmAllocationPolicy(VmAllocationPolicy vmAllocationPolicy) {
		this.vmAllocationPolicy = vmAllocationPolicy;
	}
	
	public enum VmAllocationPolicy implements Serializable {
		TIME_SHARED,
		SPACE_SHARED,
		TIME_SHARED_W_PRIORITY,
		TIMS_SPACE_SHARED
	}
	
	
}
