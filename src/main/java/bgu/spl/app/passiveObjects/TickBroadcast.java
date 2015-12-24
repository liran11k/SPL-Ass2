package bgu.spl.app.passiveObjects;

import bgu.spl.mics.Broadcast;

public class TickBroadcast implements Broadcast{
	private int current;
	
	public TickBroadcast(int tick){
		this.current=tick;
	}
	
	public int getCurrent(){
		return current;
	}
}
