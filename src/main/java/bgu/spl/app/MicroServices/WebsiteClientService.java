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

import bgu.spl.app.passiveObjects.NewDiscountBroadcast;
import bgu.spl.app.passiveObjects.PurchaseOrderRequest;
import bgu.spl.app.passiveObjects.PurchaseSchedule;
import bgu.spl.app.passiveObjects.PurchaseScheduleComparator;
import bgu.spl.app.passiveObjects.Store;
import bgu.spl.app.passiveObjects.TickBroadcast;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.Request;

public class WebsiteClientService extends MicroService{
	private LinkedList<PurchaseSchedule> myPurchaseSchedule;
	private Map<PurchaseOrderRequest,Boolean> myPurchaseWaiting;
	private LinkedList<PurchaseSchedule> tmpPurchaseSchedule;
	private Set<String> myWishList;
	private int tick;
	
	public WebsiteClientService(String name, List<PurchaseSchedule> purchaseSchedule, Set<String> wishList) {
		super(name);
		myPurchaseSchedule = new LinkedList<PurchaseSchedule>();
		myPurchaseWaiting = new HashMap<PurchaseOrderRequest,Boolean>();
		tmpPurchaseSchedule = new LinkedList<PurchaseSchedule>();
		myWishList= new HashSet<String>();
		copyAndSort(purchaseSchedule);
		copySet(wishList);
		tick=0;
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
			while(myPurchaseSchedule.getFirst().getTick() == tick){
				PurchaseOrderRequest request = new PurchaseOrderRequest(myPurchaseSchedule.getFirst().getType(), tick, false, getName());
				boolean requestReceived = sendRequest(request, v -> {
					myPurchaseWaiting.remove(request);
					if(myWishList.contains(request.getShoeType()))
						myWishList.remove(request.getShoeType());
					if(myPurchaseSchedule.isEmpty() && myWishList.isEmpty() && myPurchaseWaiting.isEmpty())
						terminate();
				});
				myPurchaseSchedule.removeFirst();
				myPurchaseWaiting.put(request, true);
			}
		});
		
		subscribeBroadcast(NewDiscountBroadcast.class, wish->{		
			PurchaseOrderRequest request = new PurchaseOrderRequest(wish.getShoeType(), tick, true, getName());
			boolean requestReceived = sendRequest(request, v -> {
				myPurchaseWaiting.remove(request);
				if(myPurchaseSchedule.isEmpty() && myWishList.isEmpty() && myPurchaseWaiting.isEmpty())
					terminate();
			});
			if(requestReceived){
				myWishList.remove(wish.getShoeType());
				myPurchaseWaiting.put(request, true);
			}
		});
	}
	
	//TODO: delete this method for checking
	public HashSet<String> getWishList(){
		return (HashSet<String>) myWishList;
	}
	
	//TODO: delete this method for checking
	public LinkedList getPurchaseList(){
		return myPurchaseSchedule;
	}
}
