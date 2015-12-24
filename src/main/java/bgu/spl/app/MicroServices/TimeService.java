package bgu.spl.app.MicroServices;

import bgu.spl.app.passiveObjects.TickBroadcast;
import bgu.spl.mics.MicroService;

public class TimeService extends MicroService{
	//OurFields
	private int speed;
	private int duration;
	private TickBroadcast currentTick;
	
	
	public TimeService(int speed, int duration){
		super("timer");
		this.speed=speed;
		this.duration=duration;
		currentTick = new TickBroadcast(1);
		
	}

	@Override
	protected void initialize() {
		// TODO Auto-generated method stub
		
	}

}
