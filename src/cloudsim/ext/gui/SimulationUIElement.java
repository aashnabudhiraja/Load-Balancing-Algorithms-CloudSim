package cloudsim.ext.gui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.geom.Point2D;
import java.io.Serializable;

abstract public class SimulationUIElement implements Serializable {

	protected Point2D location;
	protected Color color;
	protected String name;
	protected int region;	
	
	public SimulationUIElement(){
		
	}
	
	public SimulationUIElement(String name, int region){
		this.name = name;
		this.region = region;
	}
	
	/**
	 * @return the color
	 */
	public Color getColor() {
		return color;
	}
	/**
	 * @param color the color to set
	 */
	public void setColor(Color color) {
		this.color = color;
	}
	
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * @return the region
	 */
	public int getRegion() {
		return region;
	}
	
	public void setLocation(Point2D loc){
		this.location = loc;
	}
	
	public Point2D getLocation(){
		return location;
	}
		
	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
	/**
	 * @param region the region to set
	 */
	public void setRegion(int region) {
		this.region = region;
	}
	
	abstract public void paint(Graphics g);
}
