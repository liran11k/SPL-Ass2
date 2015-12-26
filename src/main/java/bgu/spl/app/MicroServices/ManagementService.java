package bgu.spl.app.MicroServices;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import bgu.spl.app.passiveObjects.DiscountSchedule;
import bgu.spl.app.passiveObjects.ManufacturingOrderRequest;
import bgu.spl.app.passiveObjects.PurchaseSchedule;
import bgu.spl.app.passiveObjects.RestockRequest;
import bgu.spl.app.passiveObjects.TickBroadcast;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.Request;

public class ManagementService extends MicroService {
	private List<DiscountSchedule> myDiscounts;
	private int tick;
	Map <String,Integer> orders;
	
	public ManagementService(String name, List<DiscountSchedule> discounts) {
		super("manager");
		myDiscounts = new ArrayList<DiscountSchedule>();
		orders = new HashMap<String, Integer>();
		copyList(discounts);
		tick=0;
	}

	private void copyList(List<DiscountSchedule> toCopy){
		for(DiscountSchedule discount : toCopy)
			myDiscounts.add(discount);
	}
	
	@Override
	protected void initialize() {
		
		subscribeBroadcast(TickBroadcast.class, v ->{
			tick=v.getCurrent();
		});
		
		subscribeRequest(RestockRequest.class, req -> {
			RestockRequest request = (RestockRequest) req;
			if(orders.containsKey(request.getShoeType()) && orders.get(request.getShoeType()).intValue()>0){
				int newAmount = orders.get(request.getShoeType()).intValue();
				orders.put(request.getShoeType(), newAmount-1);
			}
			else if(!orders.containsKey(request.getShoeType()) || orders.get(request.getShoeType()).intValue() == 0){
				ManufacturingOrderRequest order = new ManufacturingOrderRequest(request.getShoeType(), (tick%5) +1 , tick);
				sendRequest(order, v->{/* Send completed to selling service*/ });
			}
		});
		
		//TODO: send discounts
		
	}

}
