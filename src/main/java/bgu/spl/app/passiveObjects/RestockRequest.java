package bgu.spl.app.passiveObjects;

import bgu.spl.mics.Request;

public class RestockRequest implements Request{
	private String shoeType;
	private int amount;
	private boolean result;
	
	public RestockRequest(String shoeType, int amount){
		this.shoeType=shoeType;
		this.amount=amount;
	}

	public String getShoeType() {
		return shoeType;
	}

	public int getAmount() {
		return amount;
	}

	public boolean getResult() {
		return result;
	}

	public void setResult(boolean result) {
		this.result = result;
	}
}
