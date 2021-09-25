package cloudsim.ext.gui.utils;

import cloudsim.ext.gui.SimulationUIElement;

/** 
 * Util class used by the GUI to represent a communication path.
 * @author Bhathiya Wickremasinghe
 *
 */
public class CommunicationPath {

	private SimulationUIElement src;
	private SimulationUIElement dest;
	
	
	public CommunicationPath(SimulationUIElement src, SimulationUIElement dest){
		this.src = src;
		this.dest = dest;
	}
	
	public boolean equals(Object other){
		if (other == this){
			return true;
		}
		
		if (!(other instanceof CommunicationPath)){
			return false;
		}
		
		CommunicationPath otherCP = (CommunicationPath) other;
		if (otherCP.getSrc().getLocation().equals(this.src.getLocation())
				&& otherCP.getDest().getLocation().equals(this.dest.getLocation())){
			return true;
		} else if (otherCP.getSrc().getLocation().equals(this.dest.getLocation())
				&& otherCP.getDest().getLocation().equals(this.src.getLocation())){
			return true;
		}
		
		return false;
	}
	
	public int hashCode(){
		return (this.src.getLocation().hashCode() + this.dest.getLocation().hashCode());		
	}
	/**
	 * @return the src
	 */
	public SimulationUIElement getSrc() {
		return src;
	}

	/**
	 * @param src the src to set
	 */
	public void setSrc(SimulationUIElement src) {
		this.src = src;
	}

	/**
	 * @return the dest
	 */
	public SimulationUIElement getDest() {
		return dest;
	}

	/**
	 * @param dest the dest to set
	 */
	public void setDest(SimulationUIElement dest) {
		this.dest = dest;
	}

}
