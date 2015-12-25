package bgu.spl.app.MicroServices;

import bgu.spl.app.passiveObjects.BuyResult;
import bgu.spl.app.passiveObjects.PurchaseOrderRequest;
import bgu.spl.app.passiveObjects.Receipt;
import bgu.spl.app.passiveObjects.RestockRequest;
import bgu.spl.app.passiveObjects.ShoeStorageInfo;
import bgu.spl.app.passiveObjects.Store;
import bgu.spl.mics.MicroService;

public class SellingService extends MicroService{
	private int tick;
	public SellingService(String name) {
		super(name);
	}

	@Override
	protected void initialize() {
		subscribeRequest(PurchaseOrderRequest.class, req -> {
			
			BuyResult status = Store.getInstance().take(req.getShoeType(),req.onlyOnDiscount());
			Store.getInstance().file(new Receipt(getName(), req.getCustomer() , req.getShoeType(), status.compareTo(BuyResult.DISCOUNTED_PRICE)==0,tick, req.getTick(), 1));
		});
		
	}
	public synchronized void Buy(String shoeType, boolean onlyDiscount){
		BuyResult result = Store.getInstance().take(shoeType,onlyDiscount);
		ShoeStorageInfo shoe = Store.getInstance().getShoe(shoeType);
		if(result == BuyResult.DISCOUNTED_PRICE){
			Store.getInstance().remove(shoeType);
		}
		else if(result == BuyResult.NOT_IN_STOCK){
			
			sendRequest(rstk, v ->{
				
			});
		}
	}
	
}
