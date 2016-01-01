package bgu.spl.app.passiveObjects;

public enum BuyResult{
	NOT_IN_STOCK,
	NOT_ON_DISCOUNT,
	REGULAR_PRICE,
	DISCOUNTED_PRICE;
	
	public static BuyResult getStatus(ShoeStorageInfo shoe, boolean onlyDiscount){
		BuyResult ans = null;
		if(shoe == null || shoe.getAmountOnStorage() == 0){
			if(onlyDiscount)
				ans=NOT_ON_DISCOUNT;
			else
				ans=NOT_IN_STOCK;
		}
		else if(shoe.getAmountOnStorage() > 0){ 
				if(shoe.getDiscountedAmount() == 0){
					if(onlyDiscount)
						ans=NOT_ON_DISCOUNT;
					else
						ans=REGULAR_PRICE;
				}
				else if(shoe.getDiscountedAmount() > 0)
					ans=DISCOUNTED_PRICE;
		}
		return ans;
	}
}
