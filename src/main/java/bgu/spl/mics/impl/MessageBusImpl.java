package bgu.spl.mics.impl;

import java.util.AbstractQueue;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Logger;

import bgu.spl.mics.Broadcast;
import bgu.spl.mics.Message;
import bgu.spl.mics.MessageBus;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.Request;
import bgu.spl.mics.RequestCompleted;

public class MessageBusImpl implements MessageBus{

	private Map<MicroService,ArrayList> _micro_services; //MicroService & Queue
	private Map<Class <? extends Request>,ArrayList> _request_subscribers_lists; //Request and it's subscribers
	private Map<Class <? extends Broadcast>,ArrayList> _broadcast_subscribers_lists; //Broadcast and it's subscribers
	private Map<MicroService, ArrayList> _micro_service_request_types;
	private Map<MicroService, ArrayList> _micro_service_broadcast_types;
	
    //Auxiliary map to get request & requester more efficiently
    private Map<Request<?>, MicroService> _request_and_requester;

    public final static Logger LOGGER = Logger.getLogger(MessageBusImpl.class.getName()); 
    /**
     * As taught in class, we need 3 methods
     * in order to create safe singleton implementation.
     * 1. Private Holder to create the instance from inside the class (by calling the constructor)
     * 2. Private Constructor to initialize the fields.
     * 3. Static instance getter to create the instance from outside the class.
     * @author Liran
     *
     */
    private static class MessageBusImplHolder{
        private static MessageBusImpl instance = new MessageBusImpl();
    }

    private MessageBusImpl(){
    	_micro_services = new ConcurrentHashMap<>();
    	_request_subscribers_lists = new ConcurrentHashMap<>();
    	_broadcast_subscribers_lists = new ConcurrentHashMap<>();
    	_micro_service_request_types = new ConcurrentHashMap<>();
    	_micro_service_broadcast_types = new ConcurrentHashMap<>();
    	_request_and_requester = new ConcurrentHashMap<>();
    }

    public static MessageBusImpl getInstance(){
        return MessageBusImplHolder.instance;
    }

    @Override
    public synchronized void subscribeRequest(Class<? extends Request> type, MicroService m) {
		if(_request_subscribers_lists.containsKey(type))
				_request_subscribers_lists.get(type).add(m);
        else{
        	_request_subscribers_lists.put(type, new ArrayList<>());
        	_request_subscribers_lists.get(type).add(m);
        }
		if(!_micro_service_request_types.get(m).contains(type))
			_micro_service_request_types.get(m).add(type);
    }

    /**
     * Connect micro service 'm' queue to the subscribed message type list
     */
    public synchronized void subscribeBroadcast(Class<? extends Broadcast> type, MicroService m) {
    	if(_broadcast_subscribers_lists.containsKey(type))
				_broadcast_subscribers_lists.get(type).add(m);
        else{
        	_broadcast_subscribers_lists.put(type, new ArrayList<>());
        	_broadcast_subscribers_lists.get(type).add(m);
        }
		if(!_micro_service_broadcast_types.get(m).contains(type))
			_micro_service_broadcast_types.get(m).add(type);
    }

    /**
     * Get the tuple of request and requester in order to notify the requester
     * that the request is completed by sending MessageComplete to it's queue.
     */
    public synchronized <T> void complete(Request<T> r, T result) {
    	RequestCompleted<T> completed = new RequestCompleted<>(r, result);
    	_micro_services.get(_request_and_requester.get(r)).add(completed);
		_request_and_requester.remove(r);
		notifyAll();
    }

    @Override
    public synchronized void sendBroadcast(Broadcast b) {
    	if(_broadcast_subscribers_lists.containsKey(b.getClass())){
    		Iterator<MicroService> iterator = _broadcast_subscribers_lists.get(b.getClass()).iterator();
    		while(iterator.hasNext())
				_micro_services.get(iterator.next()).add((Message)b);
    		notifyAll();
    	}
    }

    /**
     * Adding tuple of request & requester in order to operate m's (the requester) methods.
     * Adding pointer to map in order to reach the tuple more efficiently
     */
    public synchronized boolean sendRequest(Request<?> r, MicroService requester) {
        if(_request_subscribers_lists.containsKey(r.getClass())){
        	ArrayList <MicroService> subscribers = _request_subscribers_lists.get(r.getClass());
        	MicroService tmpMicroService = null;
        	if(!subscribers.isEmpty()){
            	tmpMicroService = subscribers.remove(0);
   				_micro_services.get(tmpMicroService).add((Message)r);
    			subscribers.add(tmpMicroService);
            	_request_and_requester.put(r, requester);
            	notifyAll();
            	return true;
        	}
        	return false;
        }
        else
        	return false;
    }

    @Override
    public synchronized void register(MicroService m) {
    	LOGGER.info(m.getName() + " registered to MessageBus");
    	_micro_services.put(m, new ArrayList<>());
        _micro_service_request_types.put(m, new ArrayList<>());
        _micro_service_broadcast_types.put(m, new ArrayList<>());
        
    }

    @Override
    public synchronized void unregister(MicroService m) {
        unsubscribe(m);
        _micro_services.remove(m);
        LOGGER.info(m.getName() + " unregistered from MessageBus");
    }

    @Override
    public synchronized Message awaitMessage(MicroService m) throws InterruptedException {
    	Message message = null;
    	while(_micro_services.get(m).isEmpty())
    		wait();
    	message = (Message) _micro_services.get(m).get(0);
		_micro_services.get(m).remove(0);
    	return message;
    }

    /**
     * Auxiliary method:
     * Unsubscribe m's queue from the message list given
     * @param m microservice's queue to remove
     * @param toRemoveFrom Message list to remove the queue from
     */
    public void unsubscribe(MicroService m){
    	ArrayList list = _micro_service_request_types.get(m);
    	while(!list.isEmpty())
    	{
    		Class<? extends Request> type;
			type = (Class<? extends Request>) list.get(0);
			list.remove(0);
			_request_subscribers_lists.get(type).remove(m);
    	}
    	_micro_service_request_types.remove(m);
    	list = _micro_service_broadcast_types.get(m);
    	while(!list.isEmpty())
    	{
    		Class<? extends Broadcast> type;
			type = (Class<? extends Broadcast>) list.get(0);
			list.remove(0);
			_broadcast_subscribers_lists.get(type).remove(m);
    	}
    	_micro_service_broadcast_types.remove(m);
    }

}