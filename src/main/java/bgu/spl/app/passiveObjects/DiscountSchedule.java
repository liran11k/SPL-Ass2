package bgu.spl.app.passiveObjects;

public class DiscountSchedule {
	private String shoeType;
	private int tick;
	private int amount;
	
	public DiscountSchedule(String shoeType, int tick, int amount){
		this.shoeType=shoeType;
		this.tick=tick;
		this.amount=amount;
	}
	
}
