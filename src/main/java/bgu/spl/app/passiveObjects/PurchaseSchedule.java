package bgu.spl.app.passiveObjects;

public class PurchaseSchedule {
	private String shoeType;
	private int tick;

	public PurchaseSchedule(String shoeType, int tick){
		this.shoeType=shoeType;
		this.tick=tick;
	}
	//Copy constructor
	public PurchaseSchedule(PurchaseSchedule other){
		shoeType=other.getType();
		tick=other.getTick();
	}
	public int getTick() {
		return tick;
	}
	public String getType() {
		return shoeType;
	}
}
