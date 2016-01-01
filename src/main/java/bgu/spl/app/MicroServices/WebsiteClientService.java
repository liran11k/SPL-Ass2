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
	private LinkedList<PurchaseSchedule> _myPurchaseSchedule;
	private Map<PurchaseOrderRequest,Boolean> _myPurchaseWaiting;
	private LinkedList<PurchaseSchedule> _tmpPurchaseSchedule;
	private Set<String> _myWishList;
	private int _tick;
	private CountDownLatch _startLatch;
	private CountDownLatch _finishLatch;
	public static int _countSent;
	public static int _countCompleted;
	public static int _countFailed;
	
	public WebsiteClientService(String name, List<PurchaseSchedule> purchaseSchedule, Set<String> wishList, CountDownLatch startLatch, CountDownLatch finishLatch) {
		super(name);
		_myPurchaseSchedule = new LinkedList<PurchaseSchedule>();
		_myPurchaseWaiting = new HashMap<PurchaseOrderRequest,Boolean>();
		_tmpPurchaseSchedule = new LinkedList<PurchaseSchedule>();
		_myWishList= new HashSet<String>();
		copyAndSort(purchaseSchedule);
		copySet(wishList);
		_tick=0;
		_startLatch = startLatch;
		_finishLatch = finishLatch;
		_countSent=0;
		_countCompleted=0;
		_countFailed=0;
	}
	
	private void copyAndSort(List<PurchaseSchedule> toCopy) {
		for(PurchaseSchedule purchase : toCopy){
			_myPurchaseSchedule.addFirst(purchase);	
		}
		Comparator<PurchaseSchedule> c = new PurchaseScheduleComparator();
		_myPurchaseSchedule.sort(c);
	}
	
	private void copySet(Set<String> wishList){
		for(String wish : wishList)
			_myWishList.add(wish);
	}
	
	//TODO: what if customer receive null as receipt??
	@SuppressWarnings("unchecked")
	@Override
	protected void initialize() {
		
		subscribeBroadcast(TickBroadcast.class, currentTick ->{
			_tick = currentTick.getCurrent();
			while(!_myPurchaseSchedule.isEmpty() && _myPurchaseSchedule.getFirst().getTick() == _tick){
				//TODO: move logger to shoeStoreRunner
				MessageBusImpl.LOGGER.info(getName() + " sending PurchaseOrderRequest");
				PurchaseOrderRequest request = new PurchaseOrderRequest(_myPurchaseSchedule.getFirst().getType(), _tick, false, getName());
				_myPurchaseSchedule.removeFirst();
				Sent();
				boolean requestReceived = sendRequest(request, v -> {
					Completed();
					_myPurchaseWaiting.remove(request);
					if(_myWishList.contains(request.getShoeType()))
						_myWishList.remove(request.getShoeType());
					if(_myPurchaseSchedule.isEmpty() && _myWishList.isEmpty() && _myPurchaseWaiting.isEmpty()){
						terminate();
						_finishLatch.countDown();
					}
				});
				if(requestReceived)
					_myPurchaseWaiting.put(request, true);
				else
					Failed();
			}
		});
		
		subscribeBroadcast(NewDiscountBroadcast.class, discount->{		
			if(_myWishList.contains(discount.getShoeType())){
				MessageBusImpl.LOGGER.info(getName() + " received NewDiscountBroadcast" );
				PurchaseOrderRequest request = new PurchaseOrderRequest(discount.getShoeType(), _tick, true, getName());
				//TODO: remove counter from here and seller 
				Sent();
				boolean requestReceived = sendRequest(request, v -> {
					Completed();
					_myPurchaseWaiting.remove(request);
					if(_myPurchaseSchedule.isEmpty() && _myWishList.isEmpty() && _myPurchaseWaiting.isEmpty()){
						terminate();
						_finishLatch.countDown();
					}
					//TODO: Tick 20
				});
				MessageBusImpl.LOGGER.info(getName() + " sending PurchaseOrderRequest");
				if(requestReceived){
					_myWishList.remove(discount.getShoeType());
					_myPurchaseWaiting.put(request, true);
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
		return (HashSet<String>) _myWishList;
	}
	
	//TODO: delete this method for checking
	public LinkedList getPurchaseList(){
		return _myPurchaseSchedule;
	}
	
	private synchronized void Sent(){
		_countSent++;
	}
	private synchronized void Completed(){
		_countCompleted++;
	}
	private synchronized void Failed(){
		_countFailed++;
	}
}
