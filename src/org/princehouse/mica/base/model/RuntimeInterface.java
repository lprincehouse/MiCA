package org.princehouse.mica.base.model;

import java.util.TimerTask;

import org.princehouse.mica.base.net.model.Address;

import fj.F;

public interface RuntimeInterface {

	
	/**
	 * Add a runtime but do not start it yet.  Call start function to start all runtimes.
	 * Returns the newly created runtime.
	 * @param address 
	 * @param protocol
	 * @param randomSeed
	 * @param roundLength 
	 * @param startTime   Delay of initial sleep (in the runtime's units, probably ms)
	 */
	public Runtime addRuntime(Address address, Protocol protocol, long randomSeed, int roundLength,int startTime, int lockTimeout);	
	
	/**
	 * Start all runtimes and block until they're finished
	 */
	public void run();

	public void scheduleTask(long delay, TimerTask task);

	public void stop();
	
	/**
	 * Reset everything. Prepare for new experiments to be run.
	 */
	public void reset();
	
	// get the runtime associated with a protocol instance
	public Runtime getRuntime(Protocol p);
	
	// set the current thread's runtime
	public void setRuntime(Runtime rt);

	/**
	 * Returns a functionalJava function:
	 *    input: the index number of a node
	 *    output: a unique address for that node
	 *    
	 * If null is returned, the default is to generate TCP/IP addresses on localhost starting at port 8000 
	 * @return
	 */
	public F<Integer, Address> getAddressFunc();
	
}
