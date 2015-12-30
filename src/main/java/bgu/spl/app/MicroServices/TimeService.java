package bgu.spl.app.MicroServices;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;

import javax.print.attribute.standard.Finishings;

import bgu.spl.app.passiveObjects.TerminationBroadcast;
import bgu.spl.app.passiveObjects.TickBroadcast;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.impl.MessageBusImpl;

public class TimeService extends MicroService{
	private int speed;
	private int duration;
	private int tick;
	private Timer time;
	private CountDownLatch _startLatch;
	private CountDownLatch _finishLatch;
	
	public TimeService(int speed, int duration, CountDownLatch startLatch, CountDownLatch finishLatch) {
		super("Timer");
		this.setDuration(duration);
		this.setSpeed(speed);
		time = new Timer();
		_startLatch = startLatch;
		_finishLatch = finishLatch;
		tick = 0;
	}

	@Override
	protected void initialize() {
		MessageBusImpl.LOGGER.info("Timer is waiting for services..");
		try {
			_startLatch.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
			return;
		}
		//TODO: remove print and activate logger
		System.out.println("Tick === " + tick);
		MessageBusImpl.LOGGER.info("All services done initializing --> Timer starts!");
		
		time.schedule(
				new TimerTask(){
					@Override
					public void run() {
						duration--;
						tick++;
						sendBroadcast(new TickBroadcast(tick));
						MessageBusImpl.getInstance().LOGGER.info("Time is == " + tick);
						if(duration == 0){
							sendBroadcast(new TerminationBroadcast(tick));
							time.cancel();
							terminate();
							_finishLatch.countDown();
						}
					}	
		}, speed, speed);
	}

	public int getDuration() {
		return duration;
	}

	public void setDuration(int duration) {
		this.duration = duration;
	}

	public int getSpeed() {
		return speed;
	}

	public void setSpeed(int speed) {
		this.speed = speed;
	}





	
}