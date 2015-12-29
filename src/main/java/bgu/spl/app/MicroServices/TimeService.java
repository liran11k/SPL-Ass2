package bgu.spl.app.MicroServices;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;

import bgu.spl.app.passiveObjects.TickBroadcast;
import bgu.spl.mics.MicroService;

public class TimeService extends MicroService{
	private int speed;
	private int duration;
	private int tick;
	private Timer time;
	private CountDownLatch _countDownLatch;
	
	public TimeService(int speed, int duration, CountDownLatch countDownLatch) {
		super("Timer");
		this.setDuration(duration);
		this.setSpeed(speed);
		time = new Timer();
		_countDownLatch = countDownLatch;
	}

	@Override
	protected void initialize() {
		System.out.println("Timer waits for services..");
		try {
			_countDownLatch.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
			return;
		}
		System.out.println("All services done initializing --> Timer starts!");
		
		time.schedule(
				new TimerTask(){
					@Override
					public void run() {
						sendBroadcast(new TickBroadcast(tick));
						duration--;
						tick++;
						System.out.println("Tick === " + tick);
						if(duration == 0){
							terminate();
							time.cancel();
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