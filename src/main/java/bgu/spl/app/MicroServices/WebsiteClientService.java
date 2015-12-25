package bgu.spl.app.MicroServices;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import bgu.spl.app.passiveObjects.NewDiscountBroadcast;
import bgu.spl.app.passiveObjects.PurchaseSchedule;
import bgu.spl.app.passiveObjects.TickBroadcast;
import bgu.spl.mics.MicroService;

public class WebsiteClientService extends MicroService{
	private List<PurchaseSchedule> myPurchaseSchedule;
	private Set<String> myWishList;
	private int tick;
	
	public WebsiteClientService(String name, List<PurchaseSchedule> purchaseSchedule, Set<String> wishList) {
		super(name);
		myPurchaseSchedule = new ArrayList<PurchaseSchedule>();
		myWishList= new HashSet<String>();
	}

	//TODO: implement map with key=tick & value=purchase
	// so in each tick we check if there's a purchase to invoke
	
	private void copyList(List<PurchaseSchedule> toCopy){
		for(PurchaseSchedule Ps : toCopy)
			myPurchaseSchedule.add(Ps);
	}
	
	private void copySet(Set<String> wishList){
		for(String wish : wishList)
			myWishList.add(wish);
	}
	
	@Override
	protected void initialize() {
		subscribeBroadcast(NewDiscountBroadcast.class, wish->{		
			sendBroadcast(wish);
			myWishList.remove(wish);
		});
		subscribeBroadcast(TickBroadcast.class, wish->{
			tick= wish.getCurrent();
			
			for(PurchaseSchedule it : myPurchaseSchedule)
			{
				int i = it.getTick();
				if(i== tick){	
					sendBroadcast(wish);
				}
			}
		});
	}

}
