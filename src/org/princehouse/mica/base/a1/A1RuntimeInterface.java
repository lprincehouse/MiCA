package org.princehouse.mica.base.a1;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.princehouse.mica.base.LogFlag;
import org.princehouse.mica.base.model.MiCA;
import org.princehouse.mica.base.model.Protocol;
import org.princehouse.mica.base.model.Runtime;
import org.princehouse.mica.base.model.RuntimeInterface;
import org.princehouse.mica.base.net.model.Address;
import org.princehouse.mica.base.simple.ThreadLocalRuntimeMechanism;
import org.princehouse.mica.util.Functional;

import fj.F;

public class A1RuntimeInterface implements RuntimeInterface {
	private List<Runtime> runtimes = Functional.list();

	private Map<Runtime,Integer> startTimes = Functional.map();
	
	@Override
	public void reset() {
		stop();
		runtimes.clear();
		startTimes.clear();
		running = false;
	}

	
	@Override
	public  Runtime addRuntime(Address address, Protocol protocol,
			long randomSeed, int roundLength, int startTime, int lockTimeout) {

		Runtime rt = new A1Runtime(address);

		setRuntime(rt); // tell the runtime mechanism that this is
								// the current runtime when the protocol is
								// started

		startTimes.put(rt, startTime);
		rt.setProtocolInstance(protocol);
		rt.setRandomSeed(randomSeed);
		rt.setRoundLength(roundLength);
		rt.setLockWaitTimeout(lockTimeout);
		setRuntime(null);
		runtimes.add(rt);
		return rt;
	}

	private boolean running = false;

	@Override
	public void run() {
		
		Runtime arbitraryRuntime = runtimes.get(0);
		arbitraryRuntime.logJson(LogFlag.init, "mica-options", MiCA.getOptions());

		// wait for interrupt
		running = true;
		// start runtimes
		
		// sort runtimes by start time, ascending
		Collections.sort(runtimes, new Comparator<Runtime>() {
			@Override
			public int compare(Runtime r1, Runtime r2) {
				return startTimes.get(r1).compareTo(startTimes.get(r2));
			} 
		});
		
		int t0 = 0;
		for(Runtime rt : runtimes) {
			int t1 = startTimes.get(rt);
			try {
				Thread.sleep(t1-t0); 
				System.out.printf("[sleep %s ms, start next runtime]\n", t1-t0);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			t0 = t1;
			setRuntime(rt);
			rt.start();
			setRuntime(null);
		}
		
		try {
			try {
				while (running) {
					Thread.sleep(1000);
				}
			} catch (InterruptedException e) {

			}
		} catch (RuntimeException e) {
			e.printStackTrace();
		}
		stop(); // clean up
	}

	@Override
	public void stop() {
		running = false;
		for (Runtime rt : runtimes) {
			rt.stop();
		}
	}

	@Override
	public void scheduleTask(long delay, TimerTask task) {
		Timer timer = new Timer(true);
		timer.schedule(task, delay);
	}
	
	@Override
	public  Runtime getRuntime(Protocol p) {
		return ThreadLocalRuntimeMechanism.getRuntime();
	}

	@Override
	public  void setRuntime(Runtime rt) {
		ThreadLocalRuntimeMechanism.setRuntime(rt);
	}

	@Override
	public F<Integer, Address> getAddressFunc() {
		return null; // use default
	}


}
