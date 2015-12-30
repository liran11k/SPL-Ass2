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

public class ManagementService extends MicroService {
	private LinkedList<DiscountSchedule> myDiscounts;
	private int tick;
	private Map <String,Integer> myOrders;
	private Map <String, LinkedList<RestockRequest>> _myOrders;
	private CountDownLatch _startLatch;
	private CountDownLatch _finishLatch;
	
	public ManagementService(String name, List<DiscountSchedule> discounts, CountDownLatch startLatch, CountDownLatch finishLatch) {
		super(name);
		myDiscounts = new LinkedList<DiscountSchedule>();
		myOrders = new HashMap<String, Integer>();
		_myOrders = new HashMap<String, LinkedList<RestockRequest>>();
		copyAndSort(discounts);
		tick=0;
		_startLatch = startLatch;
		_finishLatch = finishLatch;
	}
	
	private void copyAndSort(List<DiscountSchedule> toCopy){
		for(DiscountSchedule discount : toCopy){
			myDiscounts.addLast(discount);	
		}
		Comparator<DiscountSchedule> c = new DiscountsComparator<DiscountSchedule>();
		myDiscounts.sort(c);
	}
													
	@SuppressWarnings("unchecked")
	@Override
	protected void initialize() {
		
		/**
		 * For each tick arrived
		 * --> send discount broadcast (if exist for current tick)
		 */
		subscribeBroadcast(TickBroadcast.class, v ->{
			tick=v.getCurrent();
			while(!myDiscounts.isEmpty() && myDiscounts.getFirst().getTick()==tick){
				NewDiscountBroadcast discount = new NewDiscountBroadcast(myDiscounts.getFirst().getType(), myDiscounts.getFirst().getDiscountedAmount());
				int newDiscountAmount=discount.getDiscountedAmount();
				synchronized (Store.getInstance()) {
					ShoeStorageInfo shoe = Store.getInstance().getShoe(discount.getShoeType());
					if(shoe != null){
						int amountOnStorage = shoe.getAmountOnStorage();
						if(amountOnStorage<newDiscountAmount){
							newDiscountAmount=amountOnStorage;
						}
						Store.getInstance().getShoe(myDiscounts.getFirst().getType()).setDiscountAmount(newDiscountAmount);
						sendBroadcast((Broadcast)discount);
					}
				}
				myDiscounts.removeFirst();
			}
		});
		
		subscribeBroadcast(TerminationBroadcast.class, v ->{
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

			RestockRequest request = (RestockRequest) req;
			
			if(myOrders.containsKey(request.getShoeType()) && myOrders.get(request.getShoeType()).intValue()>0){
				_myOrders.get(request.getShoeType()).addLast(request);
				int newAmount = myOrders.get(request.getShoeType()).intValue()-1;
				myOrders.put(request.getShoeType(), newAmount);
			}

			else if(!myOrders.containsKey(request.getShoeType()) || myOrders.get(request.getShoeType()).intValue() == 0){
				if(!myOrders.containsKey(request.getShoeType()))
					_myOrders.put(request.getShoeType(), new LinkedList<RestockRequest>());
				
				_myOrders.get(request.getShoeType()).addLast(request);
				ManufacturingOrderRequest order = new ManufacturingOrderRequest(request.getShoeType(), (tick%5) +1 , tick);
				myOrders.put(request.getShoeType(), (tick%5));
				boolean requestReceived = sendRequest(order, v -> {
						Receipt receipt = (Receipt) ((RequestCompleted) v).getResult();
						Store.getInstance().file(receipt);
						int itemsReserved = 0;
						while( !_myOrders.get(receipt.getShoeType()).isEmpty() && _myOrders.get(receipt.getShoeType()).size() < receipt.getAmount() ){
							complete(_myOrders.get(receipt.getShoeType()).getFirst(), true);
							_myOrders.get(receipt.getShoeType()).removeFirst();
							itemsReserved++;
						}
						Store.getInstance().add(receipt.getShoeType(), receipt.getAmount() - itemsReserved);	
					});
				if(!requestReceived){
					while( !_myOrders.get(request.getShoeType()).isEmpty() && _myOrders.get(request.getShoeType()).size() < request.getAmount() ){
						complete(_myOrders.get(request.getShoeType()).getFirst(), false);
						_myOrders.get(request.getShoeType()).removeFirst();
					}
				}
			}
		});
		
		_startLatch.countDown();
	}
	
	//TODO: delete this checking method
	public LinkedList getDiscounts(){
		return myDiscounts;
	}
}
