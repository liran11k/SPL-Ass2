package bgu.spl.app.MicroServices;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import bgu.spl.app.passiveObjects.PurchaseSchedule;
import bgu.spl.app.passiveObjects.Receipt;
import bgu.spl.mics.MicroService;

public class WebsiteClientService extends MicroService{
	private LinkedList<PurchaseSchedule> myPurchaseSchedule;
	private LinkedList<String> myWishList;
	
	public WebsiteClientService(String name, List<PurchaseSchedule> purchaseSchedule, Set<String> wishList) {
		super(name);
		myPurchaseSchedule = new LinkedList<PurchaseSchedule>();
		myWishList= new LinkedList<String>();
	}

	//TODO: implement map with key=tick & value=purchase
	// so in each tick we check if there's a purchase to invoke
	
	private void CopyList(List<PurchaseSchedule> toCopy){
		for(PurchaseSchedule Ps : toCopy)
			myPurchaseSchedule.add(Ps);
	}
	
	private void copySet(Set<String> wishList){
		for(String wish : wishList)
			myWishList.addLast(wish);
	}
	
	@Override
	protected void initialize() {
		// TODO Auto-generated method stub
		
	}

}
