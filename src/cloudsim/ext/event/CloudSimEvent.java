package cloudsim.ext.event;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class CloudSimEvent implements Serializable {
	private static final long serialVersionUID = 1710001127028361171L;
	
	private int id;
	private Map<String, Object> parameters;

	public CloudSimEvent(int id) {
		super();
		this.id = id;
	}
	
	
	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(int id) {
		this.id = id;
	}

	
	public void addParameter(String name, Object value){
		if (parameters == null){
			parameters = new HashMap<String, Object>();
		}
		
		parameters.put(name, value);
	}
	
	public Object getParameter(String name){
		if (parameters == null){
			return null;
		}
		
		return parameters.get(name);
	}

}
