package bgu.spl.app.passiveObjects;

public class Receipt {

	private String seller;
	private String costumer;
	private String shoeType;
	private boolean discount;
	private int issuedTick;
	private int requestTick;
	private int amountSold;
	
	public Receipt(String seller,String costumer,String shoeType,boolean discount,
			int issuedTick,int requestTick,int amountSold)
	{
		this.seller=seller;
		this.costumer=costumer;
		this.shoeType=shoeType;
		this.discount=discount;
		this.issuedTick=issuedTick;
		this.requestTick=requestTick;
		this.amountSold=amountSold;
	}
	
	public void print(){
		System.out.println("Receipt details:");
		System.out.println("seller: " + seller);
		System.out.println("Costumer: " + costumer);
		System.out.println("ShoeType: " + shoeType);
		System.out.println("Has discount: " + discount);
		System.out.println("Amount sold: " + amountSold);
		System.out.println("issued: " + issuedTick + " || requested: " + requestTick);
	}
	
	
}
