package bgu.spl.mics.impl;

import static org.junit.Assert.*;

import java.util.concurrent.CountDownLatch;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import bgu.spl.app.MicroServices.ShoeFactoryService;
import bgu.spl.app.passiveObjects.ManufacturingOrderRequest;
import bgu.spl.app.passiveObjects.NewDiscountBroadcast;
import bgu.spl.app.passiveObjects.Receipt;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.RequestCompleted;

public class MessageBusImplTest {

	@Before
	public void setUp() throws Exception {
		MessageBusImpl.getInstance().initialize();
	}

	@After
	public void tearDown() throws Exception {
		MessageBusImpl.getInstance().initialize();
	}
	@Test
	public void testGetInstance(){
		assertNotNull(MessageBusImpl.getInstance());
	}
	
	@Test
	public void testRegister(){
		CountDownLatch start = new CountDownLatch(1);
		CountDownLatch finish = new CountDownLatch(1);
		MicroService m = new ShoeFactoryService("factory", start, finish);
		assertTrue(MessageBusImpl.getInstance().getMicroServices().length == 0);
		MessageBusImpl.getInstance().register(m);
		assertTrue(MessageBusImpl.getInstance().getMicroServices().length == 1);
		assertTrue(MessageBusImpl.getInstance().getMicroServices()[0] == m);
	}

	@Test
	public void testRegisteredQueue(){
		CountDownLatch start = new CountDownLatch(1);
		CountDownLatch finish = new CountDownLatch(1);
		MicroService m = new ShoeFactoryService("factory", start, finish);
		MessageBusImpl.getInstance().register(m);
		assertNotNull(MessageBusImpl.getInstance().getQueues());
	}
	
	@Test
	public void testUnregister(){
		CountDownLatch start = new CountDownLatch(1);
		CountDownLatch finish = new CountDownLatch(1);
		MicroService m = new ShoeFactoryService("factory", start, finish);
		MessageBusImpl.getInstance().register(m);
		assertTrue(MessageBusImpl.getInstance().getMicroServices().length == 1);
		MessageBusImpl.getInstance().unregister(m);
		assertTrue(MessageBusImpl.getInstance().getMicroServices().length == 0);
	}
	
	@Test
	public void testSubscribeBroadcast(){
		CountDownLatch start = new CountDownLatch(1);
		CountDownLatch finish = new CountDownLatch(1);
		MicroService m = new ShoeFactoryService("factory", start, finish);
		MessageBusImpl.getInstance().register(m);
		MessageBusImpl.getInstance().subscribeBroadcast(NewDiscountBroadcast.class, m);
		assertTrue(MessageBusImpl.getInstance().getBroadcastSubscription(m).length == 1);
		assertTrue(MessageBusImpl.getInstance().getBroadcastSubscription(m)[0] == NewDiscountBroadcast.class);
	}
	
	@Test
	public void testSubscribeRequest(){
		CountDownLatch start = new CountDownLatch(1);
		CountDownLatch finish = new CountDownLatch(1);
		MicroService m = new ShoeFactoryService("factory", start, finish);
		MessageBusImpl.getInstance().register(m);
		MessageBusImpl.getInstance().subscribeRequest(ManufacturingOrderRequest.class, m);
		assertTrue(MessageBusImpl.getInstance().getRequestSubscription(m).length == 1);
		assertTrue(MessageBusImpl.getInstance().getRequestSubscription(m)[0] == ManufacturingOrderRequest.class);
	}
	
	@Test
	public void testSendRequestFailed(){
		CountDownLatch start = new CountDownLatch(1);
		CountDownLatch finish = new CountDownLatch(1);
		MicroService m = new ShoeFactoryService("factory", start, finish);
		MessageBusImpl.getInstance().register(m);
		assertFalse(MessageBusImpl.getInstance().sendRequest(new ManufacturingOrderRequest("ShoeA", 1, 1), m));
	}
	
	@Test
	public void testSendRequestPassed(){
		CountDownLatch start = new CountDownLatch(1);
		CountDownLatch finish = new CountDownLatch(1);
		MicroService m1 = new ShoeFactoryService("factory1", start, finish);
		MicroService m2 = new ShoeFactoryService("factory2", start, finish);
		MessageBusImpl.getInstance().register(m1);
		MessageBusImpl.getInstance().register(m2);
		MessageBusImpl.getInstance().subscribeRequest(ManufacturingOrderRequest.class, m1);
		assertTrue(MessageBusImpl.getInstance().sendRequest(new ManufacturingOrderRequest("ShoeA", 1, 1), m2));
	}
	
	@Test
	public void testSendBroadcast(){
		CountDownLatch start = new CountDownLatch(1);
		CountDownLatch finish = new CountDownLatch(1);
		MicroService m = new ShoeFactoryService("factory", start, finish);
		MessageBusImpl.getInstance().register(m);
		MessageBusImpl.getInstance().subscribeBroadcast(NewDiscountBroadcast.class, m);
		NewDiscountBroadcast discount = new NewDiscountBroadcast("ShoeA", 1);
		MessageBusImpl.getInstance().sendBroadcast(discount);
		assertNotNull(MessageBusImpl.getInstance().getQueues()[0].peek());
		assertTrue(MessageBusImpl.getInstance().getQueues()[0].poll() == discount);
	}
	
	@Test
	public void testComplete(){
		CountDownLatch start = new CountDownLatch(1);
		CountDownLatch finish = new CountDownLatch(1);
		MicroService m1 = new ShoeFactoryService("factory", start, finish);
		MessageBusImpl.getInstance().register(m1);
		MessageBusImpl.getInstance().subscribeRequest(ManufacturingOrderRequest.class, m1);
		ManufacturingOrderRequest request = new ManufacturingOrderRequest("ShoeA", 1, 1);
		MessageBusImpl.getInstance().sendRequest(request, m1);
		Receipt receipt = new Receipt("factory", "factory2", "ShoeA", false, 1, 1, 1);
		try {
			MessageBusImpl.getInstance().awaitMessage(m1);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		MessageBusImpl.getInstance().complete(request, receipt);
		assertTrue(((RequestCompleted)MessageBusImpl.getInstance().getQueues()[0].poll()).getResult() == receipt);
	}
	
	@Test
	public void testAwaiteMessege(){
		CountDownLatch start = new CountDownLatch(1);
		CountDownLatch finish = new CountDownLatch(1);
		MicroService m = new ShoeFactoryService("factory", start, finish);
		MessageBusImpl.getInstance().register(m);
		MessageBusImpl.getInstance().subscribeBroadcast(NewDiscountBroadcast.class, m);
		NewDiscountBroadcast discount = new NewDiscountBroadcast("ShoeA", 1);
		MessageBusImpl.getInstance().sendBroadcast(discount);
		assertFalse(MessageBusImpl.getInstance().getQueues()[0].isEmpty());
		try {
			assertNotNull(MessageBusImpl.getInstance().awaitMessage(m));
			MessageBusImpl.getInstance().sendBroadcast(discount);
			assertTrue(MessageBusImpl.getInstance().awaitMessage(m) == discount);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
