package bgu.spl.mics.impl;

import java.util.AbstractQueue;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

import bgu.spl.mics.Broadcast;
import bgu.spl.mics.Message;
import bgu.spl.mics.MessageBus;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.Request;
import bgu.spl.mics.RequestCompleted;

public class MessageBusImpl implements MessageBus{

    //Lists which holds pointers to queues who's services subscribed to this type of message.
    private List<MicroService> request_subscribers;
    private List<MicroService> broadcast_subscribers;
    //Iterator to implement round robin
    private int index;
    //Map which holds all micro services and their messages queue.
    private Map<MicroService,ArrayList> micro_services;
    //Auxiliary map to get request & requester more efficiently
    private Map<Request<?>, Tuple<Request<?>, MicroService>> point_map_requests;

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
        request_subscribers = new ArrayList<MicroService>();
        broadcast_subscribers = new ArrayList<MicroService>();
        micro_services = new HashMap<MicroService,ArrayList>();
        point_map_requests = new HashMap<Request<?>, Tuple<Request<?>, MicroService>>();
    }

    public static MessageBusImpl getInstance(){
        return MessageBusImplHolder.instance;
    }

    @Override
    public synchronized void subscribeRequest(Class<? extends Request> type, MicroService m) {
        request_subscribers.add(m);
    }

    /**
     * Connect micro service 'm' queue to the subscribed message type list
     */
    public void subscribeBroadcast(Class<? extends Broadcast> type, MicroService m) {
        broadcast_subscribers.add(m);
    }

    /**
     * Get the tuple of request and requester in order to notify the requester
     * that the request is completed by sending MessageComplete to it's queue.
     */
    public synchronized <T> void complete(Request<T> r, T result) {
        Tuple<Request<?>, MicroService> reqNrequester = point_map_requests.get(r);
        RequestCompleted<T> completed = new RequestCompleted<>(r, result);
        micro_services.get(reqNrequester.getY()).add(completed);
        notifyAll();
    }

    @Override
    public void sendBroadcast(Broadcast b) {
        Iterator<MicroService> iter = broadcast_subscribers.iterator();
        while(iter.hasNext()){
            getInstance().micro_services.get(iter).add((Message)b); // get themicro service's queue
            iter.next();
        }
        notifyAll();
    }

    /**
     * Adding tuple of request & requester in order to operate m's (the requester) methods.
     * Adding pointer to map in order to reach the tuple more efficiently
     */
    public synchronized boolean sendRequest(Request<?> r, MicroService requester) {
    	if(!request_subscribers.isEmpty()){
    		getInstance().micro_services.get(request_subscribers.get(index)).add(r);
            notifyAll();
            Tuple<Request<?>, MicroService> reqNrequster = new Tuple<Request<?>, MicroService>(r,requester);
            point_map_requests.put(r,reqNrequster);
            if(index<request_subscribers.size()-1)
            	index++;
            else
            	index=0;
            return true;
    	}
    	return false;
    }

    @Override
    public void register(MicroService m) {
    	micro_services.put(m, new ArrayList());
    }

    @Override
    public void unregister(MicroService m) {
        unsubscribe(m, request_subscribers);
        unsubscribe(m, broadcast_subscribers);
        micro_services.remove(m);
    }

    @Override
    public synchronized Message awaitMessage(MicroService m) throws InterruptedException {
        while((micro_services.get(m).size()==0))
            wait();
        Message message = (Message) micro_services.get(m).get(0);
        micro_services.get(m).remove(0);
        return message;
    }

    /**
     * Auxiliary method:
     * Unsubscribe m's queue from the message list given
     * @param m microservice's queue to remove
     * @param toRemoveFrom Message list to remove the queue from
     */
    public synchronized void unsubscribe(MicroService m, List toRemoveFrom){
        Iterator iter = toRemoveFrom.iterator();
        while(iter.hasNext()){
            if(iter.equals(micro_services.get(m)))
                toRemoveFrom.remove(iter);
            iter.next();
        }
    }
}