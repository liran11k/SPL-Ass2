package bgu.spl.app.passiveObjects;

import bgu.spl.mics.Broadcast;

public class NewDiscountBroadcast implements Broadcast{
	private String shoeType;
	private int discountedAmount;
	//How much discount
	
	public NewDiscountBroadcast(String shoeType, int discountedAmount){
		this.shoeType=shoeType;
		this.discountedAmount=discountedAmount;
	}

	public String getShoeType() {
		return shoeType;
	}

	public void setShoeType(String shoeType) {
		this.shoeType = shoeType;
	}

	public int getDiscountedAmount() {
		return discountedAmount;
	}	
}
