/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation
 *               of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009, The University of Melbourne, Australia
 */

package cloudsim;

import java.util.Iterator;
import java.util.List;

import gridsim.ParameterException;
import gridsim.datagrid.File;
import gridsim.datagrid.storage.HarddriveStorage;

/**
 * SANStorage represents a storage area network composed of a set of
 * harddisks connected in a LAN. Capacity of individual disks are abstracted,
 * thus only the overall capacity of the SAN is considered.
 * 
 * WARNING: This class is not yet fully functional. Effects of network contention
 * are not considered in the simulation. So, time for file transfer is underestimated
 * in the presence of high network load.
 *
 * @author       Rodrigo N. Calheiros
 * @since        CloudSim Toolkit 1.0 Beta
 * @invariant $none
 */
public class SANStorage extends HarddriveStorage {
	
	double bandwidth;
	double networkLatency;

	/**
	 * Creates a new SAN with a given capacity, latency, and bandwidth of
	 * the network connection
	 * @param capacity Storage device capacity
	 * @param bandwidth Network bandwidth
	 * @param networkLatency Network latency
	 * @throws ParameterException when the name and the capacity are not valid
	 */
	public SANStorage(double capacity, double bandwidth, double networkLatency) throws ParameterException {
		super(capacity);
		this.bandwidth=bandwidth;
		this.networkLatency=networkLatency;
	}
	
	/**
	 * Creates a new SAN with a given capacity, latency, and bandwidth of
	 * the network connection
	 * @param name      the name of the new harddrive storage
	 * @param capacity Storage device capacity
	 * @param bandwidth Network bandwidth
	 * @param networkLatency Network latency
	 * @throws ParameterException when the name and the capacity are not valid
	 */
	public SANStorage(String name, double capacity, double bandwidth, double networkLatency) throws ParameterException {
		super(name,capacity);
		this.bandwidth=bandwidth;
		this.networkLatency=networkLatency;
	}
	
	/**
     * Adds a file for which the space has already been reserved.
     * @param file the file to be added
     * @return the time (in seconds) required to add the file
     */
	public double addReservedFile(File file) {
		double time = super.addReservedFile(file);
		time+=this.networkLatency;
		time+=file.getSize()*this.bandwidth;
	   
		return time;
	}

	/**
	 * Gets the maximum transfer rate of the storage in MB/sec.
	 * @return  the maximum transfer rate in MB/sec
	 */
	public double getMaxTransferRate() {
	    	
		double diskRate=super.getMaxTransferRate();
	    	
	    //the max transfer rate is the minimum between
	    //the network bandwidth and the disk rate
	    if(diskRate<this.bandwidth) return diskRate;
	    	return this.bandwidth;
    }
 
    /**
     * Adds a file to the storage.
     * @param file  the file to be added
     * @return the time taken (in seconds) for adding the specified file
     */
    public double addFile(File file) {
    	double time=super.addFile(file);
    	
		time+=this.networkLatency;
		time+=file.getSize()*this.bandwidth;
		   
		return time;
    }
	    
    /**
     * Adds a set of files to the storage.
     * Runs through the list of files and save all of them.
     * The time taken (in seconds) for adding each file can also be
     * found using {@link gridsim.datagrid.File#getTransactionTime()}.
     *
     * @param list the files to be added
     * @return the time taken (in seconds) for adding the specified files
     */
    public double addFile(List list) {
        double result = 0.0;
        if (list == null || list.size() == 0) {
            System.out.println(this.getName() + ".addFile(): Warning - list is empty.");
            return result;
        }

        Iterator it = list.iterator();
        File file = null;
        while (it.hasNext()) {
            file = (File) it.next();
            result += this.addFile(file);    // add each file in the list
        }
        return result;
    }

    /**
     * Removes a file from the storage.
     * The time taken (in seconds) for deleting the file can also be
     * found using {@link gridsim.datagrid.File#getTransactionTime()}.
     *
     * @param fileName  the name of the file to be removed
     * @param file      the file which is removed from the storage is returned
     *                  through this parameter
     * @return the time taken (in seconds) for deleting the specified file
     */
    public double deleteFile(String fileName, File file) {
        return this.deleteFile(file);
    }
    
    /**
     * Removes a file from the storage.
     * The time taken (in seconds) for deleting the file can also be
     * found using {@link gridsim.datagrid.File#getTransactionTime()}.
     *
     * @param file the file which is removed from the storage is returned
     *             through this parameter
     * @return the time taken (in seconds) for deleting the specified file
     */
    public double deleteFile(File file) {
    	double time=super.deleteFile(file);
    	
		time+=this.networkLatency;
		time+=file.getSize()*this.bandwidth;
		   
		return time;
    }
    	    
}
