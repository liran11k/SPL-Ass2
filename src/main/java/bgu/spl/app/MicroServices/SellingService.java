package bgu.spl.app.MicroServices;

import java.util.concurrent.CountDownLatch;

import bgu.spl.app.passiveObjects.BuyResult;
import bgu.spl.app.passiveObjects.PurchaseOrderRequest;
import bgu.spl.app.passiveObjects.Receipt;
import bgu.spl.app.passiveObjects.RestockRequest;
import bgu.spl.app.passiveObjects.ShoeStorageInfo;
import bgu.spl.app.passiveObjects.Store;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.Request;
import bgu.spl.mics.RequestCompleted;

public class SellingService extends MicroService{
	private int tick;
	private CountDownLatch _countDownLatch;
	
	public SellingService(String name, CountDownLatch countDownLatch) {
		super(name);
		tick=0;
		_countDownLatch = countDownLatch;
	}

	@Override
	protected void initialize() {
		
		subscribeRequest(PurchaseOrderRequest.class, req -> {
			Buy(req);
		});
		
		_countDownLatch.countDown();
		
	}
	@SuppressWarnings("unchecked")
	private void Buy(Request req){
		synchronized (Store.getInstance()) {
			
			PurchaseOrderRequest purchaseRequest = (PurchaseOrderRequest) req;
			int amountOn = Store.getInstance().getShoe(purchaseRequest.getShoeType()).getAmountOnStorage();
			ShoeStorageInfo shoe =  Store.getInstance().getShoe(purchaseRequest.getShoeType());
			Receipt receipt;
			BuyResult requestStatus = Store.getInstance().take(purchaseRequest.getShoeType(),purchaseRequest.onlyOnDiscount());

			if(requestStatus == BuyResult.DISCOUNTED_PRICE){
				receipt = new Receipt(getName(), purchaseRequest.getCustomer() , shoe.getType(), true, tick, purchaseRequest.getTick(), 1);
				Store.getInstance().getShoe(purchaseRequest.getShoeType()).setAmount(amountOn-1);
				Store.getInstance().file(receipt);
				complete(purchaseRequest,receipt);
			}
			else if(requestStatus == BuyResult.NOT_IN_STOCK){
				RestockRequest restockRequest = new RestockRequest(shoe.getType(),1);
				boolean requestReceived = sendRequest(restockRequest, v ->{
					if((boolean) ((RequestCompleted) v).getResult()){
						Receipt delayedReceipt = new Receipt(getName(), purchaseRequest.getCustomer(), shoe.getType(), false, tick, purchaseRequest.getTick(), 1);
						complete(purchaseRequest,delayedReceipt);
					}
				});
				if(!requestReceived)
					complete(purchaseRequest,null);
			}
			else if(requestStatus == BuyResult.REGULAR_PRICE){
				receipt = new Receipt(getName(), purchaseRequest.getCustomer() , shoe.getType(), false, tick, purchaseRequest.getTick(), 1);
				Store.getInstance().getShoe(purchaseRequest.getShoeType()).setAmount(amountOn-1);
				Store.getInstance().file(receipt);
				complete(purchaseRequest,receipt);
			}
			else if(requestStatus == BuyResult.NOT_ON_DISCOUNT){
				complete(purchaseRequest,null);
			}
			
		}			
	}
	
}
