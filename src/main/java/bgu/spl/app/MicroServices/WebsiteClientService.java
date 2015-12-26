package bgu.spl.app.MicroServices;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import bgu.spl.app.passiveObjects.NewDiscountBroadcast;
import bgu.spl.app.passiveObjects.PurchaseOrderRequest;
import bgu.spl.app.passiveObjects.PurchaseSchedule;
import bgu.spl.app.passiveObjects.Store;
import bgu.spl.app.passiveObjects.TickBroadcast;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.Request;

public class WebsiteClientService extends MicroService{
	private List<PurchaseSchedule> myPurchaseSchedule;
	private Set<String> myWishList;
	private int tick;
	
	public WebsiteClientService(String name, List<PurchaseSchedule> purchaseSchedule, Set<String> wishList) {
		super(name);
		myPurchaseSchedule = new ArrayList<PurchaseSchedule>();
		myWishList= new HashSet<String>();
		copyList(myPurchaseSchedule);
		copySet(wishList);
		tick=0;
	}

	//TODO: implement map with key=tick & value=purchase
	// so in each tick we check if there's a purchase to invoke
	
	private void copyList(List<PurchaseSchedule> toCopy){
		for(PurchaseSchedule purchase : toCopy)
			myPurchaseSchedule.add(purchase);
	}
	
	private void copySet(Set<String> wishList){
		for(String wish : wishList)
			myWishList.add(wish);
	}
	
	@Override
	protected void initialize() {
		
		subscribeBroadcast(TickBroadcast.class, currentTick ->{
			tick = currentTick.getCurrent();
			for(PurchaseSchedule purchase : myPurchaseSchedule){
				if(purchase.getTick() == tick){	
					Request request = new PurchaseOrderRequest(purchase.getType(), tick, false, getName());
					sendRequest(request, v -> {});
					Store.getInstance().remove(purchase.getType());
				}
			}
			if(myPurchaseSchedule.isEmpty() && myWishList.isEmpty())
				terminate();
		});
		
		subscribeBroadcast(NewDiscountBroadcast.class, wish->{		
			Request request = new PurchaseOrderRequest(wish.getShoeType(), tick, true, getName());
			boolean success = sendRequest(request, v -> {});
			if(success)
				myWishList.remove(wish.getShoeType());
			if(myPurchaseSchedule.isEmpty() && myWishList.isEmpty())
				terminate();
		});
		
		
	}

}
