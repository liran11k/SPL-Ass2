package bgu.spl.app.passiveObjects;

import bgu.spl.mics.Broadcast;

public class DiscountSchedule {
	private String shoeType;
	private int tick;
	private int amount;
	
	public DiscountSchedule(String shoeType, int tick, int amount){
		this.shoeType=shoeType;
		this.tick=tick;
		this.amount=amount;
	}
	
	public String getType(){
		return shoeType;
	}
	
	public int getTick(){
		return tick;
	}
	
	public int getDiscountedAmount(){
		return amount;
	}
}
