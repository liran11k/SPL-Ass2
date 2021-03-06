package bgu.spl.app.MicroServices;

import java.util.LinkedList;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Logger;

import bgu.spl.app.passiveObjects.ManufacturingOrderRequest;
import bgu.spl.app.passiveObjects.Receipt;
import bgu.spl.app.passiveObjects.TerminationBroadcast;
import bgu.spl.app.passiveObjects.TickBroadcast;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.impl.MessageBusImpl;

public class ShoeFactoryService extends MicroService{

	private int _tick;
	// list to keep track of products' manufacturing
	private LinkedList<ManufacturingOrderRequest> _orderRequests;
	private CountDownLatch _startLatch;
	private CountDownLatch _finishLatch;

	public ShoeFactoryService(String name, CountDownLatch startLatch, CountDownLatch finishLatch) {
		super(name);
		_tick=0;
		_orderRequests = new LinkedList<ManufacturingOrderRequest>();
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
			_tick=v.getCurrent();
			if(!_orderRequests.isEmpty()){
				ManufacturingOrderRequest orderRequest = _orderRequests.getFirst();
				if(orderRequest.getTmpAmount()>0){
					orderRequest.setTmpAmount(orderRequest.getTmpAmount()-1);
				}
				else{
					MessageBusImpl.LOGGER.info(getName() + ": Finished ManufacturingOrderRequest in currentTick " + _tick);
					Receipt receipt = new Receipt(getName(), "Store", orderRequest.getShoeType(), false, _tick, orderRequest.getTick(), orderRequest.getAmount());
					complete(orderRequest, receipt);
					_orderRequests.removeFirst();
				}
			}
		});
		
		subscribeBroadcast(TerminationBroadcast.class, v ->{
			MessageBusImpl.LOGGER.info(getName() + ": Received TerminationBroadcast. currentTick is " + v.getCurrent() + ". Terminating...");
			terminate();
			_finishLatch.countDown();
		});
		
		/**
		 * For each request arrived
		 * --> add to requests' list
		 */
		subscribeRequest(ManufacturingOrderRequest.class, v-> {
			MessageBusImpl.LOGGER.info(getName() + ": Received ManufacturingOrderRequest for " + v.getAmount() + " "+ v.getShoeType());
			_orderRequests.addLast(v);
		});
		
		_startLatch.countDown();
		MessageBusImpl.LOGGER.info(getName() + " finished initialization ShoeFactoryService");
	}
}
