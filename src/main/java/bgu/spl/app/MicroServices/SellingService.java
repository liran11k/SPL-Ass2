package bgu.spl.app.MicroServices;

import bgu.spl.app.passiveObjects.BuyResult;
import bgu.spl.app.passiveObjects.PurchaseOrderRequest;
import bgu.spl.app.passiveObjects.Receipt;
import bgu.spl.app.passiveObjects.RestockRequest;
import bgu.spl.app.passiveObjects.ShoeStorageInfo;
import bgu.spl.app.passiveObjects.Store;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.Request;

public class SellingService extends MicroService{
	private int tick;
	
	public SellingService(String name) {
		super(name);
		tick=0;
	}

	@Override
	protected void initialize() {
		subscribeRequest(PurchaseOrderRequest.class, req -> {
			Buy(req);
		});
		
	}
	private void Buy(Request req){
		synchronized (Store.getInstance()) {
			PurchaseOrderRequest request = (PurchaseOrderRequest) req;
			ShoeStorageInfo shoe =  Store.getInstance().getShoe(((PurchaseOrderRequest) req).getShoeType());
			// while have not completed all customer's request
			BuyResult status = Store.getInstance().take(request.getShoeType(),request.onlyOnDiscount());
			if(status == BuyResult.DISCOUNTED_PRICE){
				Store.getInstance().remove(shoe.getType());
				Store.getInstance().file(new Receipt(getName(), request.getCustomer() , shoe.getType(), status.compareTo(BuyResult.DISCOUNTED_PRICE)==0,tick, request.getTick(), 1));
				//TODO: complete with receipt
			}
			else if(status == BuyResult.NOT_IN_STOCK){
				//this client receives his order from the callback
				//RestockRequest restock = new RestockRequest(shoe.getType(), request.getAmount());
//				sendRequest(restock, v ->{});
				//TODO: fix scenario when sent stock request
				//		now manager tries to add items
				//		but this function is locked on the store.
			}
			else if(status == BuyResult.REGULAR_PRICE){
				
			}
			
		}			
	}
	
}
