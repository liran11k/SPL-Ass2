package bgu.spl.app.MicroServices;

import java.util.concurrent.CountDownLatch;

import bgu.spl.app.passiveObjects.BuyResult;
import bgu.spl.app.passiveObjects.PurchaseOrderRequest;
import bgu.spl.app.passiveObjects.Receipt;
import bgu.spl.app.passiveObjects.RestockRequest;
import bgu.spl.app.passiveObjects.ShoeStorageInfo;
import bgu.spl.app.passiveObjects.Store;
import bgu.spl.app.passiveObjects.TerminationBroadcast;
import bgu.spl.app.passiveObjects.TickBroadcast;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.Request;
import bgu.spl.mics.RequestCompleted;
import bgu.spl.mics.impl.MessageBusImpl;

import java.util.logging.Logger;

public class SellingService extends MicroService{
	private int tick;
	private CountDownLatch _startLatch;
	private CountDownLatch _finishLatch;
	public static int countSent;
	public static int countCompleted;
	public static int countFailed;
	
	public SellingService(String name, CountDownLatch startLatch, CountDownLatch finishLatch) {
		super(name);
		tick=0;
		_startLatch = startLatch;
		_finishLatch = finishLatch;
		countSent=0;
		countCompleted=0;
		countFailed=0;
	}

	@Override
	protected void initialize() {
		
		subscribeRequest(PurchaseOrderRequest.class, req -> {
			MessageBusImpl.LOGGER.info(getName() + " received PurchaseOrderRequest");
			Buy(req);
		});

		
		subscribeBroadcast(TickBroadcast.class, v ->{
			tick = v.getCurrent();
		});
		
		subscribeBroadcast(TerminationBroadcast.class, v ->{
			terminate();
			_finishLatch.countDown();
		});
		
		_startLatch.countDown();
		
	}
	@SuppressWarnings("unchecked")
	private void Buy(Request req){
		synchronized (Store.getInstance()) {
			PurchaseOrderRequest purchaseRequest = (PurchaseOrderRequest) req;
			ShoeStorageInfo shoe = Store.getInstance().getShoe(purchaseRequest.getShoeType());
			if(shoe != null){
				Receipt receipt;
				BuyResult requestStatus = Store.getInstance().take(purchaseRequest.getShoeType(),purchaseRequest.onlyOnDiscount());
				if(requestStatus == BuyResult.DISCOUNTED_PRICE){
					MessageBusImpl.LOGGER.info(getName() + ": I'm happy to tell you that the shoe is on sale !!!  😄");
					receipt = new Receipt(getName(), purchaseRequest.getCustomer() , shoe.getType(), true, tick, purchaseRequest.getTick(), 1);
					Store.getInstance().remove(purchaseRequest.getShoeType());
					Store.getInstance().file(receipt);
					complete(purchaseRequest,receipt);
				}
				else if(requestStatus == BuyResult.NOT_IN_STOCK){
					MessageBusImpl.LOGGER.info(getName() + ": I don't have this shoe in stock I'm ordering it for you. please wait in patience.... 😭");
					RestockRequest restockRequest = new RestockRequest(shoe.getType(),1);
					Sent();
					boolean requestReceived = sendRequest(restockRequest, v ->{
						if((boolean) ((RequestCompleted) v).getResult()){
							Completed();
							Receipt delayedReceipt = new Receipt(getName(), purchaseRequest.getCustomer(), shoe.getType(), false, tick, purchaseRequest.getTick(), 1);
							Store.getInstance().file(delayedReceipt);
							complete(purchaseRequest,delayedReceipt);
						}
						else{
							complete(purchaseRequest,-1);
							Failed();
						}
					});
					if(!requestReceived){
						complete(purchaseRequest,-1);
						Failed();
					}
				}
				else if(requestStatus == BuyResult.REGULAR_PRICE){
					MessageBusImpl.LOGGER.info(getName() + ": regular price 🙂");
					receipt = new Receipt(getName(), purchaseRequest.getCustomer() , shoe.getType(), false, tick, purchaseRequest.getTick(), 1);
					Store.getInstance().remove(purchaseRequest.getShoeType());
					Store.getInstance().file(receipt);
					complete(purchaseRequest,receipt);
				}
				else if(requestStatus == BuyResult.NOT_ON_DISCOUNT){
					MessageBusImpl.LOGGER.info(getName() + ": unfortunatly the shoe is not on discount 😡");
					complete(purchaseRequest,null);
				}

			}
			
		}
				
	}
	private synchronized void Sent(){
		countSent++;
	}
	private synchronized void Completed(){
		countCompleted++;
	}
	private synchronized void Failed(){
		countFailed++;
	}
}
