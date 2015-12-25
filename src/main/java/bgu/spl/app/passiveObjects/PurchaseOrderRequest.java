package bgu.spl.app.passiveObjects;

import bgu.spl.mics.Request;

public class PurchaseOrderRequest implements Request{
	private String shoeType;
	private String customer;
	private int amount;
	private Receipt result;
	private boolean onlyDiscount;
	private int tick;
	
	public PurchaseOrderRequest(String shoeType, int amount,int tick, boolean onlyDiscount, String customer){
		this.shoeType=shoeType;
		this.amount=amount;
		this.onlyDiscount=onlyDiscount;
		this.customer=customer;
		this.tick=tick;
	}

	public String getShoeType() {
		return shoeType;
	}

	public int getAmount() {
		return amount;
	}
	
	public String getCustomer(){
		return customer;
	}
	
	public Receipt getResult() {
		return result;
	}

	public boolean onlyOnDiscount() {
		return onlyDiscount;
	}
	
	public void setResult(Receipt result) {
		this.result = result;
	}
	public int getTick(){
		return tick;
	}
}
