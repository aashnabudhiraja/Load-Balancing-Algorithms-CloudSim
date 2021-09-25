/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation
 *               of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009, The University of Melbourne, Australia
 */

package cloudsim;

import gridsim.GridletList;


/**
 * CloudletList is a link to store Cloudlets
 * 
 * @author 		Rodrigo N. Calheiros
 * @since       CloudSim Toolkit 1.0 Beta
 * @invariant 	$none
 *
 */
public class CloudletList extends GridletList {

	private static final long serialVersionUID = -1311064410360616431L;

    /**
     * Allocates a new CloudletList object.
     * @pre $none
     * @post $none
     */
	public CloudletList() {
		super();
	}
}
