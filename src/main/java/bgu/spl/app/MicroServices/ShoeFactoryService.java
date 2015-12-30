package bgu.spl.app.MicroServices;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.concurrent.CountDownLatch;

import bgu.spl.app.passiveObjects.ManufacturingOrderRequest;
import bgu.spl.app.passiveObjects.Receipt;
import bgu.spl.app.passiveObjects.Store;
import bgu.spl.app.passiveObjects.TerminationBroadcast;
import bgu.spl.app.passiveObjects.TickBroadcast;
import bgu.spl.mics.MicroService;

public class ShoeFactoryService extends MicroService{

	private int tick;
	// list to keep track of products' manufacturing
	private LinkedList<ManufacturingOrderRequest> orderRequests;
	private CountDownLatch _startLatch;
	private CountDownLatch _finishLatch;
	
	public ShoeFactoryService(String name, CountDownLatch startLatch, CountDownLatch finishLatch) {
		super(name);
		tick=0;
		orderRequests = new LinkedList<ManufacturingOrderRequest>();
		_startLatch = startLatch;
		_finishLatch = finishLatch;
	}

	@Override
	protected void initialize() {
		
		/**
		 * For each Tick broadcast --> if there are orders in our queue
		 * --> Manufacture 1 shoe:
		 * if completed manufacturing an order:
		 * -->	1. create receipt
		 * 		2. send complete to manager
		 * 		3. remove from my products
		 */
		subscribeBroadcast(TickBroadcast.class, v-> {
			tick=v.getCurrent();
			if(!orderRequests.isEmpty()){
				ManufacturingOrderRequest orderRequest = orderRequests.getFirst();
				if(orderRequest.getTmpAmount()>0){
					orderRequest.setTmpAmount(orderRequest.getTmpAmount()-1);
				}
				else{
					Receipt receipt = new Receipt(getName(), "Store", orderRequest.getShoeType(), false, tick, orderRequest.getTick(), orderRequest.getAmount());
					complete(orderRequest, receipt);
					orderRequests.removeFirst();
				}
			}
		});
		
		subscribeBroadcast(TerminationBroadcast.class, v ->{
			terminate();
			_finishLatch.countDown();
		});
		
		/**
		 * For each request arrived
		 * --> add to requests' list
		 */
		subscribeRequest(ManufacturingOrderRequest.class, v-> {
			orderRequests.addLast(v);
		});
		
		_startLatch.countDown();
	}

}
