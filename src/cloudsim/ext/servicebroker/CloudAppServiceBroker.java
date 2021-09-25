package cloudsim.ext.servicebroker;

import cloudsim.ext.GeoLocatable;
import cloudsim.ext.datacenter.DatacenterController;

/**
 * Interface defining the contract for a service broker. Defines just the single method 
 * <code>String getDestination(GeoLocatable inquirer)</code> which should take in a {@link GeoLocatable}
 * as input and return the name of the {@link DatacenterController}, based on the brokerage policy
 * of the implementing class.
 * 
 * @author Bhathiya Wickremasinghe
 *
 */
public interface CloudAppServiceBroker {

	String getDestination(GeoLocatable inquirer);
}
