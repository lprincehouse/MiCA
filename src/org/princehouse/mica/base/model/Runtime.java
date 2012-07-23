package org.princehouse.mica.base.model;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Type;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.locks.ReentrantLock;

import org.princehouse.mica.base.net.model.Address;
import org.princehouse.mica.base.simple.SelectException;
import org.princehouse.mica.util.ClassUtils;
import org.princehouse.mica.util.Distribution;
import org.princehouse.mica.util.Functional;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

/**	
 * The Runtime instance represents the local node in the gossip network. 
 * It runs the local protocol instance with the help of a RuntimeAgent,
 * generated by a Compiler instance.
 * 
 * To launch your own protocol, see the SimpleRuntime.launch method.
 * 
 * @author lonnie
 *
 * @param <P>
 */
public abstract class Runtime<P extends Protocol> {

	public Runtime() {
		initGsonBuilder();
	}
	
	/**
	 * Get a lock that can be used to suspend incoming or outgoing gossip
	 * WARNING: Failure to release this lock will effectively cause node failure.
	 * @return
	 */
	public abstract ReentrantLock getProtocolInstanceLock();

	// Enable new JSON logs
	public static boolean LOGGING_JSON = true;

	/**
	 * Universal debugging printstream.  System.err by default; adjust as necessary.
	 */
	public static PrintStream debug = System.out;
	
	private static int uidCounter = 0;
	private static final ReentrantLock uidlock = new ReentrantLock();

	private ReentrantLock runtimeLoglock = new ReentrantLock();

	/**
	 * Runtime includes a local unique id generator.  (IDs are only unique locally)
	 * @return
	 */
	public static int getNewUID() {
		uidlock.lock();
		int x = uidCounter++;
		uidlock.unlock();
		return x;
	}

	// json log file for this runtime instance
	private File logfile = null;

	// initial value; can be changed with command line options
	private File logDirectory = new File("mica_log");

	public File getLogFile() {
		if(logfile == null) {
			if(!logDirectory.exists()) {
				logDirectory.mkdirs();
			}
			String addr = getAddress().toString();
			addr = addr.replace("/", "_");
			logfile = new File(logDirectory, String.format("%s.log",addr));
		}
		return logfile;
	}

	public void setLogFile(File logfile) {
		this.logfile = logfile;
	}

	public void setLogDirectory(File logDirectory, boolean create) {
		if(create && !logDirectory.exists()) {
			logDirectory.mkdirs();
		}

		if(!logDirectory.exists()) {
			throw new RuntimeException(String.format("Log directory %s does not exist", logDirectory));
		}
		this.logDirectory = logDirectory;
	}

	//private long runtimeStartingTimestamp = 0;

	public long getRuntimeClockMS() {
	//	return (new Date().getTime()) - runtimeStartingTimestamp;
		return (new Date().getTime());
	}

	public long getRuntimeClock() {
		return getRuntimeClockMS();
	}


	public static class JsonLogEvent {
		public long timestamp;
		public String address;
		public String event_type;
		public Object data;
		public JsonLogEvent(long timestamp, String address, String type, Object event) {
			this.timestamp = timestamp;
			this.address = address;
			this.event_type = type;
			this.data = event;
		}
	}

	public void logJson(final String eventType) {
		logJson(eventType, null);
	}

	private GsonBuilder gsonBuilder = new GsonBuilder();
	
	public GsonBuilder getGsonBuilder() {
		return gsonBuilder;
	}

	public void setGsonBuilder(GsonBuilder gsonBuilder) {
		this.gsonBuilder = gsonBuilder;
	}

	public void initGsonBuilder() {
		JsonSerializer<Address> addressSerializer = new JsonSerializer<Address>() {
			@Override
			public JsonElement serialize(Address src, Type typeOfSrc, JsonSerializationContext context) {
				return new JsonPrimitive(src.toString());
			}
		};
		getGsonBuilder().registerTypeAdapter(Address.class, addressSerializer);	
	}
	
	public void logJson(final String eventType, final Object theEvent) {
		logJson(getAddress(), eventType, theEvent);
	}
		
	public void logJson(final Address origin, final String eventType, final Object theEvent) {

		if(!Runtime.LOGGING_JSON) return;

		runtimeLoglock.lock();

		File logfile = getLogFile();

		/**
		if(runtimeStartingTimestamp == 0) {
			runtimeStartingTimestamp = new Date().getTime();
			if(logfile.exists()) {
				logfile.delete();
			} 
		} **/   // old logfile now deleted at startup 

		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(logfile, logfile.exists());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			runtimeLoglock.unlock();
			return;
		}

		PrintStream out = new PrintStream(fos);
		JsonLogEvent logobj = new JsonLogEvent(
					getRuntimeClock(),
					origin.toString(),
					eventType,
					theEvent);
		
		Gson gson = getGsonBuilder().create(); 
		
		try {
			String msg = gson.toJson(logobj);
			out.println(msg);
		} catch(StackOverflowError e) {
			// object probably has a reference cycle reference cycle
			out.println(gson.toJson("error:reference cycle"));
			// debugging:
			ClassUtils.findReferenceCycles(logobj);
		}


		try {
			fos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}	

		runtimeLoglock.unlock();
	}

	public abstract <T extends Protocol> RuntimeAgent<T> compile(T pinstance);

	/**
	 * Start the runtime.  If you override this, be sure to call the inherited run().
	 * 
	 * @param <T>
	 * 
	 * @param protocol Local top-level protocol instance
	 * @param address Local address
	 * @param intervalMS Round length, in milliseconds
	 * @param randomSeed Local random seed
	 * @throws InterruptedException
	 */
	public void run(P pinstance, Address address, int intervalMS, long randomSeed) throws InterruptedException {
		// clear old log
		File logfile = this.getLogFile();
		if(logfile.exists()) {
			logfile.delete();
		}
		
		logJson("runtime-init", Functional.<String,Object>mapFromPairs(
				"round_ms", intervalMS,
				"random_seed", randomSeed
				));					
		
		setRuntime(this);
	};

	/**
	 * Get the local top-level protocol instance
	 * @return Local top-level protocol instance
	 */
	public abstract P getProtocolInstance();

	/**
	 * Set local top-level protocol instance
	 * @param pinstance
	 */
	public abstract void setProtocolInstance(P pinstance);

	/** 
	 * Stop the local runtime
	 */
	public abstract void stop();

	/**
	 * 
	 * @return
	 */
	public abstract Address getAddress();

	public <T> T punt(Exception e) {
		throw new RuntimeException(e); 
	}

	public <T> T fatal(Exception e) {
		stop();
		System.err.printf("Fatal exception happened in runtime %s\n",this);
		e.printStackTrace();
		System.exit(1);
		return null;
	}

	public void tolerate(Exception e) {
		// ignore exception, but print diagnostic info
		debug.printf("[%s Suppressed exception: %s]\n", getAddress(), e);
		e.printStackTrace(debug);
	}

	public void handleUpdateException(Exception e) {
		debug.printf("[%s update execution exception: %s]\n", getAddress(), e);
		e.printStackTrace(debug);
	}

	public void handleSelectException(Exception e) {
		debug.printf("[%s select execution exception: %s]\n", getAddress(), e);
		e.printStackTrace(debug);
	}

	private Random random = new Random();

	public Random getRandom() {
		return random;
	}

	private static ThreadLocal<Runtime<?>> runtimeSingleton = new ThreadLocal<Runtime<?>>();

	public static void setRuntime(Runtime<?> rt) {
		//System.err.printf("[set %s for thread %d]\n", rt, Thread.currentThread().getId());
		if(runtimeSingleton.get() != null && rt != null) {
			throw new RuntimeException("attempt to set two runtimes in one thread");
		}
		runtimeSingleton.set(rt);
	}

	public static Runtime<?> getRuntime() {
		Runtime<?> rt = runtimeSingleton.get();
		if(rt == null)
			throw new RuntimeException(String.format("Failed attempt to get null runtime for thread %d", Thread.currentThread().getId()));
		return rt;
	}

	public static void clearRuntime(Runtime<?> rt) {
		Runtime<?> current = runtimeSingleton.get();
		if(current != null && !current.equals(rt)) { 
			throw new RuntimeException("attempt to replace active runtime");
		}
		setRuntime(null);
	}

	public abstract RuntimeState getRuntimeState(Protocol p);

	// Called by agents.  Protocols should not use directly
	public abstract RuntimeState getRuntimeState();

	public String toString() {
		return String.format("<Runtime %d>", hashCode());
	}

	/**
	 * Returns null if view is an empty distribution
	 * 
	 * Throws MalformedViewException if view has non-one, non-empty magnitude
	 * 
	 * @param p
	 * @return
	 * @throws SelectException 
	 */
	public abstract Distribution<Address> getView(Protocol p) throws SelectException;

	public abstract void executeUpdate(Protocol p1, Protocol p2);

	public abstract double getRate(Protocol protocol);

	public long getTime() {
		return new Date().getTime();
	}
}
