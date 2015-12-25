package bgu.spl.app.MicroServices;

import java.util.Timer;
import java.util.TimerTask;

import bgu.spl.app.passiveObjects.TickBroadcast;
import bgu.spl.mics.MicroService;

public class TimeService extends MicroService{
	private int speed;
	private int duration;
	private Timer time;
	private int tick;
	
	public TimeService(int speed, int duration) {
		super("Timer");
		this.setDuration(duration);
		this.setSpeed(speed);
		time = new Timer();
	}

	@Override
	protected void initialize() {
		time.schedule(
				new TimerTask(){
					@Override
					public void run() {
						sendBroadcast(new TickBroadcast(tick));
						duration--;
						if(duration == 0)
							terminate();
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