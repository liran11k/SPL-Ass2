package bgu.spl.app.passiveObjects;

import java.util.concurrent.atomic.AtomicInteger;

public enum BuyResult{
	NOT_IN_STOCK,
	NOT_ON_DISCOUNT,
	REGULAR_PRICE,
	DISCOUNTED_PRICE;

	public static AtomicInteger notInStock = new AtomicInteger(0);
	public static AtomicInteger regular = new AtomicInteger(0);
	public static AtomicInteger notOnDiscount = new AtomicInteger(0);
	public static AtomicInteger Discounted = new AtomicInteger(0);
	
	
	public static BuyResult getStatus(ShoeStorageInfo shoe, boolean onlyDiscount){
		BuyResult ans = null;
		if(shoe == null || shoe.getAmountOnStorage() == 0){
			if(onlyDiscount){
				ans=NOT_ON_DISCOUNT;
				notOnDiscount.incrementAndGet();
			}
			else{
				ans=NOT_IN_STOCK;
				notInStock.incrementAndGet();
			}
		}
		else if(shoe.getAmountOnStorage() > 0){ 
			if(shoe.getDiscountedAmount() == 0){
				if(onlyDiscount){
					ans=NOT_ON_DISCOUNT;
					notOnDiscount.incrementAndGet();
				}
				else{
					ans=REGULAR_PRICE;
					regular.incrementAndGet();
				}
			}
			else if(shoe.getDiscountedAmount() > 0){
				ans=DISCOUNTED_PRICE;
				Discounted.incrementAndGet();
			}
		}
		return ans;
	}
}
