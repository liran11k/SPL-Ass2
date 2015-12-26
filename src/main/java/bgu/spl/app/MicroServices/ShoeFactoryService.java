package bgu.spl.app.MicroServices;

import java.util.ArrayList;
import java.util.LinkedList;

import bgu.spl.app.passiveObjects.ManufacturingOrderRequest;
import bgu.spl.app.passiveObjects.Receipt;
import bgu.spl.app.passiveObjects.Store;
import bgu.spl.app.passiveObjects.TickBroadcast;
import bgu.spl.mics.MicroService;

public class ShoeFactoryService extends MicroService{

	private int tick;
	// list to keep track of products' manufacturing
	private LinkedList<ManufacturingOrderRequest> orderRequests; 
	
	public ShoeFactoryService(String name) {
		super(name);
		tick=0;
		orderRequests = new LinkedList<ManufacturingOrderRequest>();
	}

	@Override
	protected void initialize() {
		
		/**
		 * For each Tick broadcast --> manufacture 1 shoe:
		 * else:
		 * 1. create receipt
		 * 2. send complete to manager
		 * 3. remove from my products
		 */
		subscribeBroadcast(TickBroadcast.class, v-> {
			tick=v.getCurrent();
			if(!orderRequests.isEmpty()){
				ManufacturingOrderRequest orderRequest = orderRequests.getFirst();
				if(orderRequest.getTmpAmount()>0){
					orderRequest.setTmpAmount(orderRequest.getTmpAmount()-1);
				}
				else{
					Store.getInstance().add(orderRequest.getShoeType(), orderRequest.getAmount());
					Receipt receipt = new Receipt(getName(), "Store", orderRequest.getShoeType(), false, tick, orderRequest.getTick(), orderRequest.getAmount());
					Store.getInstance().file(receipt);
					complete(orderRequest, receipt);
					orderRequests.removeFirst();
				}
			}
		});
		
		subscribeRequest(ManufacturingOrderRequest.class, v-> {
			orderRequests.addLast(v);
		});
		
		/*
		subscribeBroadcast(TickBroadcast.class, v-> {
			tick=v.getCurrent();
		});
		
		subscribeRequest(ManufacturingOrderRequest.class, v-> {
			int toAdd = v.getAmount();
			int requestedTick = tick;
			while(toAdd > 0){
				if( (requestedTick +1 <= tick) && (requestedTick >= ) ){
					Store.getInstance().add(v.getShoeType(), 1);
					toAdd--;
				}
			}
		});
		*/
	}

}
