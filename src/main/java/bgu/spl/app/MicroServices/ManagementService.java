package bgu.spl.app.MicroServices;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import bgu.spl.app.passiveObjects.DiscountSchedule;
import bgu.spl.app.passiveObjects.DiscountsComparator;
import bgu.spl.app.passiveObjects.ManufacturingOrderRequest;
import bgu.spl.app.passiveObjects.NewDiscountBroadcast;
import bgu.spl.app.passiveObjects.PurchaseSchedule;
import bgu.spl.app.passiveObjects.Receipt;
import bgu.spl.app.passiveObjects.RestockRequest;
import bgu.spl.app.passiveObjects.ShoeStorageInfo;
import bgu.spl.app.passiveObjects.Store;
import bgu.spl.app.passiveObjects.TerminationBroadcast;
import bgu.spl.app.passiveObjects.TickBroadcast;
import bgu.spl.mics.Broadcast;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.Request;
import bgu.spl.mics.RequestCompleted;
import bgu.spl.mics.impl.MessageBusImpl;

import java.util.logging.Logger;

import org.junit.internal.runners.statements.Fail;

public class ManagementService extends MicroService {
	private LinkedList<DiscountSchedule> _myDiscounts;
	private int _tick;
	private Map <String,Integer> myOrdersCount;
	private Map <String, LinkedList<RestockRequest>> _myOrders;
	private CountDownLatch _startLatch;
	private CountDownLatch _finishLatch;
	public static int _countSent;
	public static int _countCompleted;
	public static int _countFailed;
	
	public ManagementService(String name, List<DiscountSchedule> discounts, CountDownLatch startLatch, CountDownLatch finishLatch) {
		super(name);
		_myDiscounts = new LinkedList<DiscountSchedule>();
		myOrdersCount = new HashMap<String, Integer>();
		_myOrders = new HashMap<String, LinkedList<RestockRequest>>();
		copyAndSort(discounts);
		_tick=0;
		_startLatch = startLatch;
		_finishLatch = finishLatch;
		_countSent=0;
		_countCompleted=0;
		_countFailed=0;
	}
	
	private void copyAndSort(List<DiscountSchedule> toCopy){
		for(DiscountSchedule discount : toCopy){
			_myDiscounts.addLast(discount);	
		}
		Comparator<DiscountSchedule> c = new DiscountsComparator<DiscountSchedule>();
		_myDiscounts.sort(c);
	}
													
	@SuppressWarnings("unchecked")
	@Override
	protected void initialize() {
		
		/**
		 * For each tick arrived
		 * --> send discount broadcast (if exist for current tick)
		 */
		subscribeBroadcast(TickBroadcast.class, v ->{
			_tick=v.getCurrent();
			while(!_myDiscounts.isEmpty() && _myDiscounts.getFirst().getTick()==_tick){
				NewDiscountBroadcast discount = new NewDiscountBroadcast(_myDiscounts.getFirst().getType(), _myDiscounts.getFirst().getDiscountedAmount());
				int newDiscountAmount = discount.getDiscountedAmount();
				synchronized (Store.getInstance()) {
					ShoeStorageInfo shoe = Store.getInstance().getShoe(discount.getShoeType());
					if(shoe != null){
						int amountOnStorage = shoe.getAmountOnStorage();
						if(amountOnStorage < newDiscountAmount){
							newDiscountAmount=amountOnStorage;
						}
						Store.getInstance().addDiscount(shoe.getType(), newDiscountAmount);
						MessageBusImpl.LOGGER.info(getName() + ": Sending NewDiscountBroadcast on " + newDiscountAmount + " "  + shoe.getType());
					}
					
					sendBroadcast(discount);
				}
				_myDiscounts.removeFirst();
			}
		});
		
		subscribeBroadcast(TerminationBroadcast.class, v ->{
			MessageBusImpl.LOGGER.info(getName() + ": Received TerminationBroadcast. currentTick is " + v.getCurrent() + ". Terminating...");
			terminate();
			_finishLatch.countDown();
		});
		
		/**
		 * Handling Restock Request:
		 * 1. Check if this shoe is already on order
		 * 		if so --> reduce amount by 1 and reserve a shoe for current seller request
		 * 		else  --> order from factory according to formula
		 */
		subscribeRequest(RestockRequest.class, req -> {
			MessageBusImpl.LOGGER.info(getName() + ": RestockRequest received for " + req.getShoeType());
			RestockRequest request = (RestockRequest) req;
			
			if(myOrdersCount.containsKey(request.getShoeType()) && myOrdersCount.get(request.getShoeType()).intValue() > 0){
				_myOrders.get(request.getShoeType()).addLast(request);
				int newAmount = myOrdersCount.get(request.getShoeType()).intValue()-1;
				myOrdersCount.put(request.getShoeType(), newAmount);
			}
			else if(!myOrdersCount.containsKey(request.getShoeType()) || myOrdersCount.get(request.getShoeType()).intValue() == 0){
				if(!myOrdersCount.containsKey(request.getShoeType()))
					_myOrders.put(request.getShoeType(), new LinkedList<RestockRequest>());
				
				_myOrders.get(request.getShoeType()).addLast(request);
				ManufacturingOrderRequest order = new ManufacturingOrderRequest(request.getShoeType(), (_tick%5) +1 , _tick);
				myOrdersCount.put(request.getShoeType(), new Integer(_tick%5));
				Sent();
				boolean requestReceived = sendRequest(order, v -> {
						Completed();
						Receipt receipt = (Receipt) ((RequestCompleted) v).getResult();
						Store.getInstance().file(receipt);
						int itemsReserved = 0;
						int shoesCreated = receipt.getAmount();
						while( !_myOrders.get(receipt.getShoeType()).isEmpty() && shoesCreated > 0){
							complete(_myOrders.get(request.getShoeType()).getFirst(), true);
							_myOrders.get(receipt.getShoeType()).removeFirst();
							itemsReserved++;
							shoesCreated--;
						}
						Store.getInstance().add(receipt.getShoeType(), receipt.getAmount() - itemsReserved);	
					});
				if(!requestReceived){
					Failed();
					while( !_myOrders.get(request.getShoeType()).isEmpty() && _myOrders.get(request.getShoeType()).size() < request.getAmount() ){
						complete(_myOrders.get(request.getShoeType()).getFirst(), false);
						_myOrders.get(request.getShoeType()).removeFirst();
					}
					MessageBusImpl.LOGGER.info("No factories available, couldn't complete the restock request");
				}
			}
		});
		
		_startLatch.countDown();
	}
	
	/**
	 * Auxiliary method to check json read file
	 * @return discount list
	 */
	public LinkedList getDiscounts(){
		return _myDiscounts;
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
