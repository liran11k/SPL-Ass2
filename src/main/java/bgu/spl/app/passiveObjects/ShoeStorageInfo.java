package bgu.spl.app.passiveObjects;

public class ShoeStorageInfo {
	private String shoeType;
	private int amountOnStorage;
	private int discountedAmount;
	
	public ShoeStorageInfo(String shoeType, int amountOnStorage){
		this.shoeType=shoeType;
		this.amountOnStorage=amountOnStorage;
		this.discountedAmount=0;
	}
	
	//Copy constructor
	public ShoeStorageInfo(ShoeStorageInfo other){
		shoeType=other.getType();
		amountOnStorage=other.getAmountOnStorage();
		discountedAmount=other.getDiscountedAmount();
	}
	
	public String getType(){
		return shoeType;
	}
	
	public int getAmountOnStorage(){
		return amountOnStorage;
	}
	
	public int getDiscountedAmount(){
		return discountedAmount;
	}
	
	public void setAmount(int newAmount){
		this.amountOnStorage=newAmount;
	}
	
	public void setDiscountAmount(int newDiscoundAmount){
		this.discountedAmount = newDiscoundAmount;
	}
	
	public void print(){
		System.out.println("Shoe:");
		System.out.println("Type: " + shoeType);
		System.out.println("Amount on Storage: "+ amountOnStorage);
		System.out.println("Amount on Discount: "+ discountedAmount);
	}
}
