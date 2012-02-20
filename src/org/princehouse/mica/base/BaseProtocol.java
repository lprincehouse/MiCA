package org.princehouse.mica.base;


import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;

import org.princehouse.mica.base.annotations.GossipRate;
import org.princehouse.mica.base.model.Protocol;
import org.princehouse.mica.base.model.Runtime;
import org.princehouse.mica.base.model.RuntimeState;
import org.princehouse.mica.base.net.model.Address;
import org.princehouse.mica.util.Distribution;
import org.princehouse.mica.util.MarkingObjectInputStream;

/**
 * Base for all MiCA protocols.  Extend this class to create your own protocol.
 * 
 * @author lonnie
 *
 */
public abstract class BaseProtocol implements Protocol, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7604139731035133018L;
	
	/**
	 * Default constructor
	 */
	public BaseProtocol() {}
	
	// clunky mechanism to register "foreign" objects when they are deserialized at a remote node
	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		in.defaultReadObject();
		if(in instanceof MarkingObjectInputStream) {
			((MarkingObjectInputStream) in).getForeignObjectSet().add(this);
		} 
	}

	@Override
	public final RuntimeState getRuntimeState() {
		return Runtime.getRuntime().getRuntimeState(this);
	}

	@Override
	public String toString() {
		try {
		return String.format("[%s@%s]", getName(), getRuntimeState().getAddress());  
		} catch (RuntimeException e) {
			return "[!]";
		}
	}
	
	@Override
	final public Distribution<Address> getSelectDistribution() {
		return Runtime.getRuntime().getSelectDistribution(this);
	}
	
	
	/**
	 * Get the current node's address. This is part of Runtime state.
	 * @return Current node's address
	 */
	public Address getAddress() {
		return getRuntimeState().getAddress();
	}
	
	@Override
	public void executeUpdate(Protocol other) {
		Runtime.getRuntime().executeUpdate(this,other);
	}
	
	
	@Override 
	public double getFrequency() {
		return Runtime.getRuntime().getRate(this);
	}

	// Debugging functionality
	private String name;
	/**
	 * Optional protocol name, used in the logs generated by the SimpleRuntime
	 * @return Protocol name (default is null)
	 */
	public String getName() {
		if(name == null) {
			setName(String.format("p%d",Runtime.getNewUID()));
		}
		return name;
	}
	/**
	 * Optional protocol name, used in the logs generated by the SimpleRuntime
	 */
	public Protocol setName(String name) {
		this.name = name;
		return this;
	}
	
	// TODO "local_timestamp":  Is that in ms or rounds?
	/**
	 * Write a message to the log.  Log messages are comma-separated fields of the format:
	 * 
	 * "local_timestamp,local_event_number,address,classname,name,MESSAGE"
	 * 
	 * Where MESSAGE is the result of String.format(formatStr,arguments).
	 * 
	 * @param formatStr
	 * @param arguments
	 */
	public void log(String formatStr, Object... arguments) {
		Runtime.log(String.format("%s,%s,%s,",getAddress(),getClass().getSimpleName(),getName()) + String.format(formatStr, arguments));
	}
	
	/**
	 * Write the representation of local state (from getStateString) to the log
	 */
	public void logstate() {
		log("state,"+getStateString());
	}
	
	/**
	 * String representation of local state
	 * @return
	 */
	public String getStateString() {
		return "-";
	}
	
	/**
	 * The default rate of all protocols is 1.0.
	 * Override this only if you specifically want to make this protocol gossip at a 
	 * non-uniform rate (i.e., merge operators do this)
	 * @return
	 */
	@GossipRate
	public double rate() {
		return 1.0;
	}
	
}
