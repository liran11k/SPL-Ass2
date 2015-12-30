package bgu.spl.mics.impl;

import static org.junit.Assert.*;

import java.util.concurrent.CountDownLatch;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import bgu.spl.app.MicroServices.ManagementService;
import bgu.spl.app.MicroServices.ShoeFactoryService;
import bgu.spl.mics.MicroService;

public class MessageBusImplTest {

	@Before
	public void setUp() throws Exception {
		MessageBusImpl.getInstance();
	}

	@After
	public void tearDown() throws Exception {
		
	}
	@Test
	public void testGetInstance(){
		assertNotNull(MessageBusImpl.getInstance());
	}
	
	@Test
	public void testRegister(){
		
		CountDownLatch startLatch = new CountDownLatch(1);
		CountDownLatch finishLatch = new CountDownLatch(1);;
		MicroService m = new ShoeFactoryService("factory", startLatch, finishLatch);
		MessageBusImpl.getInstance().register(m);
		//(0, (double)startLatch.getCount());
	}


	@Test
	public void test() {
		fail();
	}

}
