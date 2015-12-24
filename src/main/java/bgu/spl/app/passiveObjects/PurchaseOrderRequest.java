package bgu.spl.app.passiveObjects;

import bgu.spl.mics.Request;

public class PurchaseOrderRequest implements Request{
	private String shoeType;
	private int amount;
	private Receipt result;
	
	public PurchaseOrderRequest(String shoeType, int amount){
		this.shoeType=shoeType;
		this.amount=amount;
	}

	public String getShoeType() {
		return shoeType;
	}

	public int getAmount() {
		return amount;
	}

	public Receipt getResult() {
		return result;
	}

	public void setResult(Receipt result) {
		this.result = result;
	}
}
