package bgu.spl.app.MicroServices;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Logger;
import bgu.spl.app.passiveObjects.NewDiscountBroadcast;
import bgu.spl.app.passiveObjects.PurchaseOrderRequest;
import bgu.spl.app.passiveObjects.PurchaseSchedule;
import bgu.spl.app.passiveObjects.PurchaseScheduleComparator;
import bgu.spl.app.passiveObjects.Store;
import bgu.spl.app.passiveObjects.TerminationBroadcast;
import bgu.spl.app.passiveObjects.TickBroadcast;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.Request;
import bgu.spl.mics.impl.MessageBusImpl;

public class WebsiteClientService extends MicroService{
	private LinkedList<PurchaseSchedule> myPurchaseSchedule;
	private Map<PurchaseOrderRequest,Boolean> myPurchaseWaiting;
	private LinkedList<PurchaseSchedule> tmpPurchaseSchedule;
	private Set<String> myWishList;
	private int tick;
	private CountDownLatch _startLatch;
	private CountDownLatch _finishLatch;
	public static int countSent;
	public static int countCompleted;
	public static int countFailed;
	
	public WebsiteClientService(String name, List<PurchaseSchedule> purchaseSchedule, Set<String> wishList, CountDownLatch startLatch, CountDownLatch finishLatch) {
		super(name);
		myPurchaseSchedule = new LinkedList<PurchaseSchedule>();
		myPurchaseWaiting = new HashMap<PurchaseOrderRequest,Boolean>();
		tmpPurchaseSchedule = new LinkedList<PurchaseSchedule>();
		myWishList= new HashSet<String>();
		copyAndSort(purchaseSchedule);
		copySet(wishList);
		tick=0;
		_startLatch = startLatch;
		_finishLatch = finishLatch;
		countSent=0;
		countCompleted=0;
		countFailed=0;
	}
	
	private void copyAndSort(List<PurchaseSchedule> toCopy) {
		for(PurchaseSchedule purchase : toCopy){
			myPurchaseSchedule.addFirst(purchase);	
		}
		Comparator<PurchaseSchedule> c = new PurchaseScheduleComparator();
		myPurchaseSchedule.sort(c);
	}
	
	private void copySet(Set<String> wishList){
		for(String wish : wishList)
			myWishList.add(wish);
	}
	
	//TODO: what if customer receive null as receipt??
	@SuppressWarnings("unchecked")
	@Override
	protected void initialize() {
		
		subscribeBroadcast(TickBroadcast.class, currentTick ->{
			tick = currentTick.getCurrent();
			while(!myPurchaseSchedule.isEmpty() && myPurchaseSchedule.getFirst().getTick() == tick){
				//TODO: move logger to shoeStoreRunner
				MessageBusImpl.LOGGER.info(getName() + " sending PurchaseOrderRequest");
				PurchaseOrderRequest request = new PurchaseOrderRequest(myPurchaseSchedule.getFirst().getType(), tick, false, getName());
				myPurchaseSchedule.removeFirst();
				Sent();
				boolean requestReceived = sendRequest(request, v -> {
					Completed();
					myPurchaseWaiting.remove(request);
					if(myWishList.contains(request.getShoeType()))
						myWishList.remove(request.getShoeType());
					if(myPurchaseSchedule.isEmpty() && myWishList.isEmpty() && myPurchaseWaiting.isEmpty()){
						terminate();
						_finishLatch.countDown();
					}
				});
				if(requestReceived)
					myPurchaseWaiting.put(request, true);
				else
					Failed();
			}
		});
		
		subscribeBroadcast(NewDiscountBroadcast.class, discount->{		
			if(myWishList.contains(discount.getShoeType())){
				MessageBusImpl.LOGGER.info(getName() + " received NewDiscountBroadcast" );
				PurchaseOrderRequest request = new PurchaseOrderRequest(discount.getShoeType(), tick, true, getName());
				//TODO: remove counter from here and seller 
				Sent();
				boolean requestReceived = sendRequest(request, v -> {
					Completed();
					myPurchaseWaiting.remove(request);
					if(myPurchaseSchedule.isEmpty() && myWishList.isEmpty() && myPurchaseWaiting.isEmpty()){
						terminate();
						_finishLatch.countDown();
					}
				});
				MessageBusImpl.LOGGER.info(getName() + " sending PurchaseOrderRequest");
				if(requestReceived){
					myWishList.remove(discount.getShoeType());
					myPurchaseWaiting.put(request, true);
				}
				else
					Failed();
				
			}
		});
		
		subscribeBroadcast(TerminationBroadcast.class, v ->{
			MessageBusImpl.LOGGER.info(getName() + ": Received TerminationBroadcast. currentTick is " + v.getCurrent() + ". Terminating...");
			terminate();
			_finishLatch.countDown();
		});
		
		_startLatch.countDown();
		MessageBusImpl.LOGGER.info(getName() + " finished initialization");
	}
	
	//TODO: delete this method for checking
	public HashSet<String> getWishList(){
		return (HashSet<String>) myWishList;
	}
	
	//TODO: delete this method for checking
	public LinkedList getPurchaseList(){
		return myPurchaseSchedule;
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
