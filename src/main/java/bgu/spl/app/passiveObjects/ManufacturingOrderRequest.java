package bgu.spl.app.passiveObjects;

import bgu.spl.mics.Request;

public class ManufacturingOrderRequest implements Request {
	private String shoeType;
	private int amount;
	private int tmpAmount;
	private int requestedTick;
	private Receipt result;
	
	public ManufacturingOrderRequest(String shoeType, int amount, int requestedTick){
		this.shoeType=shoeType;
		this.amount=amount;
		this.tmpAmount=amount;
		this.requestedTick=requestedTick;
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
	
	public int getTick(){
		return requestedTick;
	}

	public int getTmpAmount() {
		return tmpAmount;
	}
	
	public void setTmpAmount(int newTmpAmount){
		this.tmpAmount = newTmpAmount;
	}
	public void setResult(Receipt result) {
		this.result = result;
	}
}
