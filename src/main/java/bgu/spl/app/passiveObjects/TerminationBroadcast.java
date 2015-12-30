package bgu.spl.app.passiveObjects;

import bgu.spl.mics.Broadcast;
/**
 * Termination message which the timer sends to all services
 * in order they will know when to shut down
 * @author Liran & Yahel
 *
 */
public class TerminationBroadcast implements Broadcast{
	private int current;
	
	public TerminationBroadcast(int tick){
		this.current=tick;
	}
	
	public int getCurrent(){
		return current;
	}
}
