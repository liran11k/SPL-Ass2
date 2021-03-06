package bgu.spl.app.MicroServices;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

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
	private int _tick;
	private CountDownLatch _startLatch;
	private CountDownLatch _finishLatch;
	public static AtomicInteger _countSent;
	public static AtomicInteger _countCompleted;
	public static AtomicInteger _countFailed;
	
	public SellingService(String name, CountDownLatch startLatch, CountDownLatch finishLatch) {
		super(name);
		_tick=0;
		_startLatch = startLatch;
		_finishLatch = finishLatch;
		_countSent=new AtomicInteger(0);
		_countCompleted=new AtomicInteger(0);
		_countFailed=new AtomicInteger(0);
	}

	@Override
	protected void initialize() {
		
		subscribeRequest(PurchaseOrderRequest.class, req -> {
			MessageBusImpl.LOGGER.info(getName() + " received PurchaseOrderRequest");
			Buy(req);
		});

		
		subscribeBroadcast(TickBroadcast.class, v ->{
			_tick = v.getCurrent();
		});
		
		subscribeBroadcast(TerminationBroadcast.class, v ->{
			terminate();
			_finishLatch.countDown();
		});
		
		_startLatch.countDown();
		
	}
	@SuppressWarnings("unchecked")
	private void Buy(Request req){
		PurchaseOrderRequest purchaseRequest = (PurchaseOrderRequest) req;
		BuyResult requestStatus = Store.getInstance().take(purchaseRequest.getShoeType(), purchaseRequest.onlyOnDiscount());
		Receipt receipt;
		if(requestStatus == BuyResult.DISCOUNTED_PRICE){
			MessageBusImpl.LOGGER.info(getName() + ": I'm happy to tell you that the shoe is on sale !!!  😄");
			receipt = new Receipt(getName(), purchaseRequest.getCustomer() , purchaseRequest.getShoeType(), true, _tick, purchaseRequest.getTick(), 1);
			//MessageBusImpl.LOGGER.info("shoe " + purchaseRequest.getShoeType() +" amount is " + shoe.getAmountOnStorage());
			//MessageBusImpl.LOGGER.info("shoe " + purchaseRequest.getShoeType() +" discounted amount is " + shoe.getDiscountedAmount());
			Store.getInstance().file(receipt);
			complete(purchaseRequest,receipt);
		}
		else if(requestStatus == BuyResult.NOT_IN_STOCK){
			MessageBusImpl.LOGGER.info(getName() + ": I don't have this shoe in stock I'm ordering it for you. please wait in patience.... 😭");
			RestockRequest restockRequest = new RestockRequest(purchaseRequest.getShoeType(),1);
			Sent();
			boolean requestReceived = sendRequest(restockRequest, v ->{
				if((boolean) v){
					Completed();
					Receipt delayedReceipt = new Receipt(getName(), purchaseRequest.getCustomer(), purchaseRequest.getShoeType(), false, _tick, purchaseRequest.getTick(), 1);
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
			receipt = new Receipt(getName(), purchaseRequest.getCustomer() , purchaseRequest.getShoeType(), false, _tick, purchaseRequest.getTick(), 1);
			Store.getInstance().file(receipt);
			complete(purchaseRequest,receipt);
		}
		else if(requestStatus == BuyResult.NOT_ON_DISCOUNT){
			MessageBusImpl.LOGGER.info(getName() + ": unfortunatly the shoe is not on discount 😡");
			complete(purchaseRequest,null);
		}
	}
	private void Sent(){
		_countSent.incrementAndGet();
	}
	private void Completed(){
		_countCompleted.incrementAndGet();
	}
	private void Failed(){
		_countFailed.incrementAndGet();
	}
}
