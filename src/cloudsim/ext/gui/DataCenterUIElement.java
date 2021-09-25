package cloudsim.ext.gui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class DataCenterUIElement extends SimulationUIElement implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -8423189065764085670L;
	
	private String architecture;
	private String os;
	private String vmm;
	private int width = 5;
	private double costPerProcessor;
	private double costPerMem;
	private double costPerStorage;
	private double costPerBw;
	private List<MachineUIElement> machineList;		
	private VmAllocationUIElement vmAllocation;

	/** Constructor */
	public DataCenterUIElement(){
		
	}
	
	public DataCenterUIElement(String name, 
							   int region,
							   String architecture, 
							   String os, 
							   String vmm,
							   double costPerProc,
							   double costPerMem,
							   double costPerStor,
							   double costPerBw){
		super(name, region);
		this.architecture = architecture;
		this.os = os;
		this.vmm = vmm;
		this.setColor(Color.RED);
		this.costPerProcessor = costPerProc;
		this.costPerMem = costPerMem;
		this.costPerStorage = costPerStor;
		this.costPerBw = costPerBw;
		
		this.machineList = new ArrayList<MachineUIElement>();
	}

	@Override
	public void paint(Graphics g) {
		if (isAllocated()){
			Graphics2D g2 = (Graphics2D) g;
			g2.setColor(color);
			
			g2.fill3DRect((int) (location.getX() - (width / 2)), 
						  (int) (location.getY() - (width / 2)),
						  width, width, true);
			
			g2.drawString(name, (int) (location.getX() + (width / 2)), (int) location.getY());
		}
	}

	/**
	 * @return the costPerProcessor
	 */
	public double getCostPerProcessor() {
		return costPerProcessor;
	}


	/**
	 * @param costPerProcessor the costPerProcessor to set
	 */
	public void setCostPerProcessor(double costPerProcessor) {
		this.costPerProcessor = costPerProcessor;
	}


	/**
	 * @return the costPerMem
	 */
	public double getCostPerMem() {
		return costPerMem;
	}


	/**
	 * @param costPerMem the costPerMem to set
	 */
	public void setCostPerMem(double costPerMem) {
		this.costPerMem = costPerMem;
	}


	/**
	 * @return the costPerStorage
	 */
	public double getCostPerStorage() {
		return costPerStorage;
	}


	/**
	 * @param costPerStorage the costPerStorage to set
	 */
	public void setCostPerStorage(double costPerStorage) {
		this.costPerStorage = costPerStorage;
	}


	/**
	 * @return the costPerBw
	 */
	public double getCostPerBw() {
		return costPerBw;
	}


	/**
	 * @param costPerBw the costPerBw to set
	 */
	public void setCostPerBw(double costPerBw) {
		this.costPerBw = costPerBw;
	}


	/**
	 * @return the machineList
	 */
	public List<MachineUIElement> getMachineList() {
		return machineList;
	}


	/**
	 * @param machineList the machineList to set
	 */
	public void setMachineList(List<MachineUIElement> machineList) {
		this.machineList = machineList;
	}


	/**
	 * @return the width
	 */
	public int getWidth() {
		return width;
	}


	/**
	 * @param width the width to set
	 */
	public void setWidth(int width) {
		this.width = width;
	}
	
	/**
	 * @return the architecture
	 */
	public String getArchitecture() {
		return architecture;
	}


	/**
	 * @param architecture the architecture to set
	 */
	public void setArchitecture(String architecture) {
		this.architecture = architecture;
	}


	/**
	 * @return the os
	 */
	public String getOs() {
		return os;
	}


	/**
	 * @param os the os to set
	 */
	public void setOs(String os) {
		this.os = os;
	}


	/**
	 * @return the vmm
	 */
	public String getVmm() {
		return vmm;
	}


	/**
	 * @param vmm the vmm to set
	 */
	public void setVmm(String vmm) {
		this.vmm = vmm;
	}

	/**
	 * @return the vmAllocation
	 */
	public VmAllocationUIElement getVmAllocation() {
		return vmAllocation;
	}

	/**
	 * @param vmAllocation the vmAllocation to set
	 */
	public void setVmAllocation(VmAllocationUIElement vmAllocation) {
		this.vmAllocation = vmAllocation;
	}

	public String toString(){
		return name;
	}
	
	public boolean isAllocated(){
		return (vmAllocation != null);
	}
	
	public int hashCode(){
		return name.hashCode();
	}
	
	public boolean equals(Object other){
		if (other == this){
			return true;
		}
		
		if (!(other instanceof DataCenterUIElement)){
			return false;
		}
		
		DataCenterUIElement otherDc = (DataCenterUIElement) other;
		return (otherDc.getName().equals(this.getName()));
	}
	
}
