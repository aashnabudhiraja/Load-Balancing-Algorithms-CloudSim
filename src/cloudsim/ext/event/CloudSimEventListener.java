package cloudsim.ext.event;

import java.io.Serializable;

/**
 * CloudSimEventListener defines the contract for a listener object to the CloudSimEvent framework.
 * 
 * @author Bhathiya Wickremasinghe
 *
 */
public interface CloudSimEventListener extends Serializable {

	void cloudSimEventFired(CloudSimEvent e);
}
