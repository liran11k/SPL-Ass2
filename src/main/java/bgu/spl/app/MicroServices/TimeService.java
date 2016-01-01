package bgu.spl.app.MicroServices;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Logger;

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
	private final Logger LOGGER = Logger.getLogger(TimeService.class.getName());
	
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
							LOGGER.info("Timer tick duration reached. Sending termination broadcast");
							time.cancel();
							MessageBusImpl.LOGGER.info("Timer is waiting a safety time before terminating itself and other services..");
							try {
								Thread.sleep(1000);
							} catch (InterruptedException e) {
								MessageBusImpl.LOGGER.warning("Timer safety sleep (in the end) interrupted.");
								e.printStackTrace();
							}
							sendBroadcast(new TerminationBroadcast(tick));
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